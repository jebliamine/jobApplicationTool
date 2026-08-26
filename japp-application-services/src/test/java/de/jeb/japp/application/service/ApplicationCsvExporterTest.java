package de.jeb.japp.application.service;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.job.Job;
import de.jeb.japp.model.tag.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationCsvExporterTest {

    private Application application() {
        Company company = new Company();
        company.setName("Acme");
        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setCompany(company);

        Application application = new Application();
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDate.of(2026, 1, 15));
        application.setContactPerson("Jane Recruiter");
        application.setNotes("Great chat.");
        return application;
    }

    @Test
    void includesTheHeaderRow() {
        String csv = ApplicationCsvExporter.toCsv(List.of());

        assertThat(csv).isEqualTo(
                "\"Company\",\"Job Title\",\"Status\",\"Applied Date\",\"Deadline\",\"Follow-up Date\","
                        + "\"Contact Person\",\"Tags\",\"Notes\"\r\n");
    }

    @Test
    void rendersACompleteRow() {
        String csv = ApplicationCsvExporter.toCsv(List.of(application()));

        assertThat(csv).contains(
                "\"Acme\",\"Backend Engineer\",\"APPLIED\",\"2026-01-15\",\"\",\"\",\"Jane Recruiter\",\"\",\"Great chat.\"");
    }

    @Test
    void quotesFieldsContainingCommasOrQuotes() {
        Application application = application();
        application.setNotes("Discussed salary, and \"remote\" work.");

        String csv = ApplicationCsvExporter.toCsv(List.of(application));

        assertThat(csv).contains("\"Discussed salary, and \"\"remote\"\" work.\"");
    }

    @Test
    void joinsMultipleTagsWithASemicolon() {
        Application application = application();
        Tag remote = new Tag();
        remote.setName("Remote");
        Tag referral = new Tag();
        referral.setName("Referral");
        application.setTags(new LinkedHashSet<>(List.of(referral, remote)));

        String csv = ApplicationCsvExporter.toCsv(List.of(application));

        assertThat(csv).contains("\"Referral; Remote\"");
    }

    @Test
    void handlesAnApplicationWithNoJobOrCompanyWithoutCrashing() {
        Application application = new Application();
        application.setStatus(ApplicationStatus.APPLIED);

        String csv = ApplicationCsvExporter.toCsv(List.of(application));

        assertThat(csv).contains("\"\",\"\",\"APPLIED\"");
    }
}
