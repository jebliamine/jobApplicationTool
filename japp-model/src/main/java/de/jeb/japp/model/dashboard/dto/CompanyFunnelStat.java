package de.jeb.japp.model.dashboard.dto;

/** Per-company breakdown within {@link FunnelMetricsResponse}, sorted by application count descending. */
public class CompanyFunnelStat {
    private String companyName;
    private long applications;
    private double responseRate;
    private double offerRate;

    public CompanyFunnelStat() {
    }

    public CompanyFunnelStat(String companyName, long applications, double responseRate, double offerRate) {
        this.companyName = companyName;
        this.applications = applications;
        this.responseRate = responseRate;
        this.offerRate = offerRate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public long getApplications() {
        return applications;
    }

    public void setApplications(long applications) {
        this.applications = applications;
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
}
