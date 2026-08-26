package de.jeb.japp.jobsearch.service;

import de.jeb.japp.jobsearch.service.provider.ExternalJobSearchAdapter;
import de.jeb.japp.model.jobsearch.ExternalJobSource;
import de.jeb.japp.model.jobsearch.dto.ExternalJobListing;
import de.jeb.japp.model.jobsearch.dto.ExternalJobSearchResponse;
import de.jeb.japp.model.jobsearch.dto.JobSearchSourceSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSearchServiceTest {

    @Mock
    private ExternalJobSearchAdapter adzuna;
    @Mock
    private ExternalJobSearchAdapter jooble;
    @Mock
    private ExternalJobSearchAdapter jsearch;

    private JobSearchService jobSearchService;

    @BeforeEach
    void setUp() {
        lenient().when(adzuna.source()).thenReturn(ExternalJobSource.ADZUNA);
        lenient().when(jooble.source()).thenReturn(ExternalJobSource.JOOBLE);
        lenient().when(jsearch.source()).thenReturn(ExternalJobSource.JSEARCH);
        jobSearchService = new JobSearchService(List.of(adzuna, jooble, jsearch));
    }

    @Test
    void combinesResultsFromEveryConfiguredSource() {
        when(adzuna.isConfigured()).thenReturn(true);
        when(adzuna.search(anyString(), anyString(), anyInt())).thenReturn(List.of(listing("Backend Engineer", "Acme", "Berlin")));
        when(jooble.isConfigured()).thenReturn(true);
        when(jooble.search(anyString(), anyString(), anyInt())).thenReturn(List.of(listing("Frontend Engineer", "Globex", "Munich")));
        when(jsearch.isConfigured()).thenReturn(false);
        when(jsearch.search(anyString(), anyString(), anyInt())).thenReturn(List.of());

        ExternalJobSearchResponse response = jobSearchService.search("engineer", "Germany", 1);

        assertThat(response.getResults()).extracting(ExternalJobListing::getTitle)
                .containsExactlyInAnyOrder("Backend Engineer", "Frontend Engineer");
        assertThat(response.getSources()).extracting(JobSearchSourceSummary::getSource, JobSearchSourceSummary::isConfigured)
                .containsExactlyInAnyOrder(
                        tuple(ExternalJobSource.ADZUNA, true),
                        tuple(ExternalJobSource.JOOBLE, true),
                        tuple(ExternalJobSource.JSEARCH, false));
    }

    @Test
    void dedupesListingsThatMatchOnTitleCompanyAndLocation() {
        ExternalJobListing fromAdzuna = listing("Backend Engineer", "Acme", "Berlin");
        ExternalJobListing fromJooble = listing("Backend Engineer", "Acme", "Berlin");

        when(adzuna.isConfigured()).thenReturn(true);
        when(adzuna.search(anyString(), anyString(), anyInt())).thenReturn(List.of(fromAdzuna));
        when(jooble.isConfigured()).thenReturn(true);
        when(jooble.search(anyString(), anyString(), anyInt())).thenReturn(List.of(fromJooble));
        when(jsearch.isConfigured()).thenReturn(false);
        when(jsearch.search(anyString(), anyString(), anyInt())).thenReturn(List.of());

        ExternalJobSearchResponse response = jobSearchService.search("engineer", "Berlin", 1);

        assertThat(response.getResults()).hasSize(1);
    }

    @Test
    void anAdapterThrowingUnexpectedlyDoesNotFailTheWholeSearch() {
        when(adzuna.isConfigured()).thenReturn(true);
        when(adzuna.search(anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("boom"));
        when(jooble.isConfigured()).thenReturn(true);
        when(jooble.search(anyString(), anyString(), anyInt())).thenReturn(List.of(listing("Frontend Engineer", "Globex", "Munich")));
        when(jsearch.isConfigured()).thenReturn(false);
        when(jsearch.search(anyString(), anyString(), anyInt())).thenReturn(List.of());

        ExternalJobSearchResponse response = jobSearchService.search("engineer", "Germany", 1);

        assertThat(response.getResults()).extracting(ExternalJobListing::getTitle).containsExactly("Frontend Engineer");
        JobSearchSourceSummary adzunaSummary = response.getSources().stream()
                .filter(s -> s.getSource() == ExternalJobSource.ADZUNA)
                .findFirst()
                .orElseThrow();
        assertThat(adzunaSummary.isSucceeded()).isFalse();
    }

    private ExternalJobListing listing(String title, String company, String location) {
        ExternalJobListing listing = new ExternalJobListing();
        listing.setTitle(title);
        listing.setCompanyName(company);
        listing.setLocation(location);
        return listing;
    }
}
