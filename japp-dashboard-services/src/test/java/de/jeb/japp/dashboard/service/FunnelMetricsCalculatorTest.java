package de.jeb.japp.dashboard.service;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.company.Company;
import de.jeb.japp.model.dashboard.dto.CompanyFunnelStat;
import de.jeb.japp.model.dashboard.dto.FunnelMetricsResponse;
import de.jeb.japp.model.job.Job;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FunnelMetricsCalculatorTest {

    private Application applicationFor(ApplicationStatus status, String companyName, LocalDateTime updatedAt) {
        Company company = new Company();
        company.setName(companyName);
        Job job = new Job();
        job.setCompany(company);

        Application application = new Application();
        application.setStatus(status);
        application.setJob(job);
        application.setUpdatedAt(updatedAt);
        return application;
    }

    @Test
    void emptyListProducesAllZeroMetrics() {
        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(List.of());

        assertThat(response.getTotalApplications()).isZero();
        assertThat(response.getResponseRate()).isZero();
        assertThat(response.getOfferRate()).isZero();
        assertThat(response.getAverageDaysInCurrentStatus()).isEmpty();
        assertThat(response.getByCompany()).isEmpty();
    }

    @Test
    void nullListIsTreatedAsEmpty() {
        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(null);

        assertThat(response.getTotalApplications()).isZero();
    }

    @Test
    void appliedAndWithdrawnDoNotCountAsResponded() {
        List<Application> applications = List.of(
                applicationFor(ApplicationStatus.APPLIED, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.WITHDRAWN, "Acme", LocalDateTime.now()));

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(applications);

        assertThat(response.getResponseRate()).isZero();
    }

    @Test
    void phoneScreenInterviewingOfferRejectedAndAcceptedAllCountAsResponded() {
        List<Application> applications = List.of(
                applicationFor(ApplicationStatus.PHONE_SCREEN, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.INTERVIEWING, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.OFFER, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.REJECTED, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.ACCEPTED, "Acme", LocalDateTime.now()));

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(applications);

        assertThat(response.getResponseRate()).isEqualTo(1.0);
    }

    @Test
    void offerRateOnlyCountsOfferAndAccepted() {
        List<Application> applications = List.of(
                applicationFor(ApplicationStatus.OFFER, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.ACCEPTED, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.REJECTED, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.APPLIED, "Acme", LocalDateTime.now()));

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(applications);

        assertThat(response.getOfferRate()).isEqualTo(0.5);
    }

    @Test
    void averageDaysInCurrentStatusIsComputedPerStatusFromUpdatedAt() {
        List<Application> applications = List.of(
                applicationFor(ApplicationStatus.INTERVIEWING, "Acme", LocalDateTime.now().minusDays(4)),
                applicationFor(ApplicationStatus.INTERVIEWING, "Acme", LocalDateTime.now().minusDays(6)),
                applicationFor(ApplicationStatus.APPLIED, "Acme", LocalDateTime.now().minusDays(1)));

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(applications);

        assertThat(response.getAverageDaysInCurrentStatus().get(ApplicationStatus.INTERVIEWING)).isCloseTo(5.0, within(0.1));
        assertThat(response.getAverageDaysInCurrentStatus().get(ApplicationStatus.APPLIED)).isCloseTo(1.0, within(0.1));
    }

    @Test
    void applicationsWithNoUpdatedAtAreExcludedFromTheAverageRatherThanCrashing() {
        Application noUpdatedAt = applicationFor(ApplicationStatus.APPLIED, "Acme", null);

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(List.of(noUpdatedAt));

        assertThat(response.getAverageDaysInCurrentStatus().get(ApplicationStatus.APPLIED)).isZero();
    }

    @Test
    void byCompanyBreaksDownApplicationsResponseRateAndOfferRatePerCompany() {
        List<Application> applications = List.of(
                applicationFor(ApplicationStatus.OFFER, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.APPLIED, "Acme", LocalDateTime.now()),
                applicationFor(ApplicationStatus.REJECTED, "Globex", LocalDateTime.now()));

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(applications);

        CompanyFunnelStat acme = response.getByCompany().stream()
                .filter(stat -> stat.getCompanyName().equals("Acme"))
                .findFirst().orElseThrow();
        assertThat(acme.getApplications()).isEqualTo(2);
        assertThat(acme.getResponseRate()).isEqualTo(0.5);
        assertThat(acme.getOfferRate()).isEqualTo(0.5);

        CompanyFunnelStat globex = response.getByCompany().stream()
                .filter(stat -> stat.getCompanyName().equals("Globex"))
                .findFirst().orElseThrow();
        assertThat(globex.getApplications()).isEqualTo(1);
        assertThat(globex.getResponseRate()).isEqualTo(1.0);
    }

    @Test
    void byCompanyIsSortedByApplicationCountDescending() {
        List<Application> applications = List.of(
                applicationFor(ApplicationStatus.APPLIED, "OneApp", LocalDateTime.now()),
                applicationFor(ApplicationStatus.APPLIED, "TwoApps", LocalDateTime.now()),
                applicationFor(ApplicationStatus.APPLIED, "TwoApps", LocalDateTime.now()));

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(applications);

        assertThat(response.getByCompany()).extracting(CompanyFunnelStat::getCompanyName)
                .containsExactly("TwoApps", "OneApp");
    }

    @Test
    void applicationsWithNoJobOrCompanyAreGroupedAsUnknown() {
        Application noJob = new Application();
        noJob.setStatus(ApplicationStatus.APPLIED);
        noJob.setUpdatedAt(LocalDateTime.now());

        FunnelMetricsResponse response = FunnelMetricsCalculator.calculate(List.of(noJob));

        assertThat(response.getByCompany()).extracting(CompanyFunnelStat::getCompanyName).containsExactly("Unknown");
    }
}
