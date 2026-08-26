package de.jeb.japp.dashboard.service;

import de.jeb.japp.model.application.Application;
import de.jeb.japp.model.application.ApplicationStatus;
import de.jeb.japp.model.dashboard.dto.CompanyFunnelStat;
import de.jeb.japp.model.dashboard.dto.FunnelMetricsResponse;
import de.jeb.japp.model.job.Job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure, stateless computation of pipeline funnel metrics from a list of Applications — no
 * persistence, no authorization (that's already applied by whatever produced the list passed in,
 * same as DashboardService's other aggregations).
 * <p>
 * The Application model has no per-status change history — only the current
 * {@link Application#getStatus()} and {@link Application#getUpdatedAt()} — so every metric here is
 * necessarily derived from current-state snapshots rather than true historical transitions:
 * <ul>
 *   <li>"Responded" means the current status is anything other than APPLIED or WITHDRAWN
 *       (WITHDRAWN is excluded because we cannot tell whether the employer had already responded
 *       before the user withdrew).</li>
 *   <li>{@code averageDaysInCurrentStatus} uses {@code updatedAt} as an approximation of "when the
 *       current status was entered" — accurate only when nothing else on the application changed
 *       after its last status update.</li>
 * </ul>
 */
final class FunnelMetricsCalculator {

    private static final Set<ApplicationStatus> RESPONDED_STATUSES = EnumSet.of(
            ApplicationStatus.PHONE_SCREEN,
            ApplicationStatus.INTERVIEWING,
            ApplicationStatus.OFFER,
            ApplicationStatus.REJECTED,
            ApplicationStatus.ACCEPTED);

    private static final Set<ApplicationStatus> OFFER_STATUSES = EnumSet.of(
            ApplicationStatus.OFFER,
            ApplicationStatus.ACCEPTED);

    private FunnelMetricsCalculator() {
    }

    static FunnelMetricsResponse calculate(List<Application> applications) {
        FunnelMetricsResponse response = new FunnelMetricsResponse();
        List<Application> safeApplications = applications != null ? applications : List.of();

        response.setTotalApplications(safeApplications.size());
        response.setResponseRate(rate(safeApplications, RESPONDED_STATUSES));
        response.setOfferRate(rate(safeApplications, OFFER_STATUSES));
        response.setAverageDaysInCurrentStatus(averageDaysInCurrentStatus(safeApplications));
        response.setByCompany(byCompany(safeApplications));

        return response;
    }

    private static double rate(List<Application> applications, Set<ApplicationStatus> matching) {
        if (applications.isEmpty()) {
            return 0.0;
        }
        long count = applications.stream().filter(app -> matching.contains(app.getStatus())).count();
        return (double) count / applications.size();
    }

    private static Map<ApplicationStatus, Double> averageDaysInCurrentStatus(List<Application> applications) {
        Map<ApplicationStatus, List<Application>> byStatus = new EnumMap<>(ApplicationStatus.class);
        for (Application application : applications) {
            byStatus.computeIfAbsent(application.getStatus(), status -> new java.util.ArrayList<>()).add(application);
        }

        Map<ApplicationStatus, Double> result = new EnumMap<>(ApplicationStatus.class);
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<ApplicationStatus, List<Application>> entry : byStatus.entrySet()) {
            double average = entry.getValue().stream()
                    .filter(app -> app.getUpdatedAt() != null)
                    .mapToLong(app -> ChronoUnit.DAYS.between(app.getUpdatedAt(), now))
                    .average()
                    .orElse(0.0);
            result.put(entry.getKey(), average);
        }
        return result;
    }

    private static List<CompanyFunnelStat> byCompany(List<Application> applications) {
        Map<String, List<Application>> byCompanyName = new LinkedHashMap<>();
        for (Application application : applications) {
            Job job = application.getJob();
            String companyName = job != null && job.getCompany() != null ? job.getCompany().getName() : "Unknown";
            byCompanyName.computeIfAbsent(companyName, name -> new java.util.ArrayList<>()).add(application);
        }

        return byCompanyName.entrySet().stream()
                .map(entry -> new CompanyFunnelStat(
                        entry.getKey(),
                        entry.getValue().size(),
                        rate(entry.getValue(), RESPONDED_STATUSES),
                        rate(entry.getValue(), OFFER_STATUSES)))
                .sorted(Comparator.comparingLong(CompanyFunnelStat::getApplications).reversed())
                .toList();
    }
}
