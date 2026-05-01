package de.jeb.japp.model.cv;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Experience {

    @Id
    @GeneratedValue
    private UUID id;

    private String company;

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 3000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CVProfile cvProfile;

    public UUID getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CVProfile getCvProfile() {
        return cvProfile;
    }

    public void setCvProfile(CVProfile cvProfile) {
        this.cvProfile = cvProfile;
    }
}