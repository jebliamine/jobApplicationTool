package de.jeb.japp.model.cv;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Language {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    /** Free-text proficiency as the CV states it (e.g. "native", "C1", "fluent") — no fixed scale is enforced. */
    private String level;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CVProfile cvProfile;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public CVProfile getCvProfile() {
        return cvProfile;
    }

    public void setCvProfile(CVProfile cvProfile) {
        this.cvProfile = cvProfile;
    }
}
