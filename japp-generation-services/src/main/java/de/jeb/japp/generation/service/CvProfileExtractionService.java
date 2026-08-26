package de.jeb.japp.generation.service;

import de.jeb.japp.ai.service.ProviderSettingsResolver;
import de.jeb.japp.ai.service.ResolvedProviderConfig;
import de.jeb.japp.commons.exceptions.ai.AiProviderNotFoundException;
import de.jeb.japp.commons.exceptions.cv.CVAccessDeniedException;
import de.jeb.japp.commons.exceptions.cv.CVNotFoundException;
import de.jeb.japp.commons.exceptions.generation.CvProfileGenerationException;
import de.jeb.japp.dao.ai.AiProviderConfigurationDao;
import de.jeb.japp.dao.cv.CVDao;
import de.jeb.japp.dao.cv.CVProfileDao;
import de.jeb.japp.generation.service.provider.LanguageData;
import de.jeb.japp.generation.service.provider.cv.*;
import de.jeb.japp.model.ai.AdapterType;
import de.jeb.japp.model.ai.AiProviderConfiguration;
import de.jeb.japp.model.cv.*;
import de.jeb.japp.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Runs the CV-profile-generation workflow: validates ownership of the CV,
 * resolves the requested (or default Placeholder) provider, calls the
 * matching {@link CvProfileExtractionAdapter}, and persists the result onto
 * that CV's {@link CVProfile} — name, summary, experience, skills, and
 * languages — (NOT_ATTEMPTED → IN_PROGRESS → COMPLETED/FAILED — mirrors
 * GenerationRequestService's status lifecycle, but simpler since this is a
 * single entity rather than a separate request-log table: there's only ever
 * one profile per CV, and regenerating overwrites it).
 */
@Service
public class CvProfileExtractionService {

    private static final Logger log = LoggerFactory.getLogger(CvProfileExtractionService.class);

    private final CVProfileDao cvProfileDao;
    private final CVDao cvDao;
    private final AiProviderConfigurationDao providerDao;
    private final ProviderSettingsResolver providerSettingsResolver;
    private final CvProfileExtractionAdapterRegistry adapterRegistry;

    public CvProfileExtractionService(
            CVProfileDao cvProfileDao,
            CVDao cvDao,
            AiProviderConfigurationDao providerDao,
            ProviderSettingsResolver providerSettingsResolver,
            CvProfileExtractionAdapterRegistry adapterRegistry
    ) {
        this.cvProfileDao = cvProfileDao;
        this.cvDao = cvDao;
        this.providerDao = providerDao;
        this.providerSettingsResolver = providerSettingsResolver;
        this.adapterRegistry = adapterRegistry;
    }

    public CVProfile generate(UUID cvDocumentId, UUID providerId, User owner) {
        CVDocument cv = getOwnedCv(cvDocumentId, owner);
        CVProfile profile = cvProfileDao.getByCvDocumentId(cvDocumentId).orElseGet(() -> newProfileFor(cv));

        AiProviderConfiguration providerInstance = resolveProviderInstance(providerId);
        CvProfileExtractionAdapter adapter = adapterRegistry.resolve(resolveAdapterType(providerInstance));
        ResolvedProviderConfig resolvedConfig = providerSettingsResolver.resolve(providerInstance.getId());

        profile.setStatus(ProfileGenerationStatus.IN_PROGRESS);
        profile = cvProfileDao.save(profile);

        try {
            CvProfileExtractionResult result = adapter.extract(resolvedConfig, new CvProfileExtractionInput(cv.getExtractedText()));
            applyResult(profile, result);
            profile.setStatus(ProfileGenerationStatus.COMPLETED);
            profile.setErrorMessage(null);
            profile.setGeneratedAt(LocalDateTime.now());
        } catch (CvProfileGenerationException e) {
            profile.setStatus(ProfileGenerationStatus.FAILED);
            profile.setErrorMessage(e.getMessage());
        }

        return cvProfileDao.save(profile);
    }

    /**
     * Empty if no generation has ever been attempted for this CV.
     */
    public Optional<CVProfile> get(UUID cvDocumentId, User owner) {
        getOwnedCv(cvDocumentId, owner);
        return cvProfileDao.getByCvDocumentId(cvDocumentId);
    }

    private CVProfile newProfileFor(CVDocument cv) {
        CVProfile profile = new CVProfile();
        profile.setCvDocument(cv);
        profile.setExperiences(new ArrayList<>());
        profile.setSkills(new ArrayList<>());
        profile.setLanguages(new ArrayList<>());
        return profile;
    }

    private void applyResult(CVProfile profile, CvProfileExtractionResult result) {
        profile.setFullName(result.fullName());
        profile.setSummary(result.summary());
        applyExperiences(profile, result.experiences());
        applySkills(profile, result.skills());
        applyLanguages(profile, result.languages());
    }

    // Each of these mutates the existing managed collection in place (rather than assigning a
    // new List) so Hibernate's orphanRemoval correctly deletes rows from a previous generation
    // instead of just detaching them from the FK.

    private void applyExperiences(CVProfile profile, List<ExperienceData> data) {
        List<Experience> experiences = profile.getExperiences();
        if (experiences == null) {
            experiences = new ArrayList<>();
            profile.setExperiences(experiences);
        }
        experiences.clear();
        for (ExperienceData item : data != null ? data : List.<ExperienceData>of()) {
            Experience experience = new Experience();
            experience.setCvProfile(profile);
            experience.setCompany(item.company());
            experience.setTitle(item.title());
            experience.setStartDate(parseDateOrNull(item.startDate()));
            experience.setEndDate(parseDateOrNull(item.endDate()));
            experience.setDescription(item.description());
            experiences.add(experience);
        }
    }

    private void applySkills(CVProfile profile, List<String> data) {
        List<Skill> skills = profile.getSkills();
        if (skills == null) {
            skills = new ArrayList<>();
            profile.setSkills(skills);
        }
        skills.clear();
        for (String name : data != null ? data : List.<String>of()) {
            if (name == null || name.isBlank()) {
                continue;
            }
            Skill skill = new Skill();
            skill.setCvProfile(profile);
            skill.setName(name);
            skills.add(skill);
        }
    }

    private void applyLanguages(CVProfile profile, List<LanguageData> data) {
        List<Language> languages = profile.getLanguages();
        if (languages == null) {
            languages = new ArrayList<>();
            profile.setLanguages(languages);
        }
        languages.clear();
        for (LanguageData item : data != null ? data : List.<LanguageData>of()) {
            if (item.name() == null || item.name().isBlank()) {
                continue;
            }
            Language language = new Language();
            language.setCvProfile(profile);
            language.setName(item.name());
            language.setLevel(item.level());
            languages.add(language);
        }
    }

    private LocalDate parseDateOrNull(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(iso);
        } catch (DateTimeParseException e) {
            log.warn("CV-profile extraction returned an unparsable date, ignoring it: {}", iso);
            return null;
        }
    }

    /**
     * No providerId means "use the built-in Placeholder instance" — same convention as GenerationRequestService.
     */
    private AiProviderConfiguration resolveProviderInstance(UUID providerId) {
        if (providerId == null) {
            return providerDao.getFirstByAdapterType(AdapterType.PLACEHOLDER.name())
                    .orElseThrow(() -> new AiProviderNotFoundException("The built-in Placeholder provider is not available."));
        }
        return providerDao.getById(providerId)
                .orElseThrow(() -> new AiProviderNotFoundException("Unknown AI provider instance: " + providerId));
    }

    private AdapterType resolveAdapterType(AiProviderConfiguration providerInstance) {
        try {
            return AdapterType.valueOf(providerInstance.getAdapterType());
        } catch (IllegalArgumentException e) {
            throw new CvProfileGenerationException("This provider instance has an unknown adapter type.");
        }
    }

    private CVDocument getOwnedCv(UUID cvId, User owner) {
        CVDocument cv = cvDao.getCVById(cvId).orElseThrow(() -> new CVNotFoundException("CV not found."));
        if (cv.getOwner() == null || !cv.getOwner().getId().equals(owner.getId())) {
            throw new CVAccessDeniedException("You do not have access to this CV.");
        }
        return cv;
    }
}
