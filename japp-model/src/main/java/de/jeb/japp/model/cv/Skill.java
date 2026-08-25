package de.jeb.japp.model.cv;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Skill {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

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

    public CVProfile getCvProfile() {
        return cvProfile;
    }

    public void setCvProfile(CVProfile cvProfile) {
        this.cvProfile = cvProfile;
    }
}
