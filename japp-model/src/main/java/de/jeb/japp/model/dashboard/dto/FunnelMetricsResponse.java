package de.jeb.japp.model.dashboard.dto;

import de.jeb.japp.model.application.ApplicationStatus;

import java.util.List;
import java.util.Map;

/**
 * Pipeline funnel metrics, nested inside {@link DashboardResponse}. See
 * {@code FunnelMetricsCalculator} (japp-dashboard-services) for exactly how each field is
 * computed and the assumptions behind it — notably, responseRate/offerRate/averageDaysInStatus are
 * derived from each Application's current status only, since there is no per-status change
 * history table; averageDaysInCurrentStatus uses updatedAt as an approximation of "when the
 * current status was entered."
 */
public class FunnelMetricsResponse {
    private long totalApplications;
    private double responseRate;
    private double offerRate;
    private Map<ApplicationStatus, Double> averageDaysInCurrentStatus;
    private List<CompanyFunnelStat> byCompany;

    public FunnelMetricsResponse() {
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public double getResponseRate() {
        return responseRate;
    }

    public void setResponseRate(double responseRate) {
        this.responseRate = responseRate;
    }

    public double getOfferRate() {
        return offerRate;
    }

    public void setOfferRate(double offerRate) {
        this.offerRate = offerRate;
    }

    public Map<ApplicationStatus, Double> getAverageDaysInCurrentStatus() {
        return averageDaysInCurrentStatus;
    }

    public void setAverageDaysInCurrentStatus(Map<ApplicationStatus, Double> averageDaysInCurrentStatus) {
        this.averageDaysInCurrentStatus = averageDaysInCurrentStatus;
    }

    public List<CompanyFunnelStat> getByCompany() {
        return byCompany;
    }

    public void setByCompany(List<CompanyFunnelStat> byCompany) {
        this.byCompany = byCompany;
    }
}
