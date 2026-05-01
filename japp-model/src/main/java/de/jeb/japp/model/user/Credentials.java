package de.jeb.japp.model.user;

import jakarta.persistence.Column;

public class Credentials {

    String userName;

    @Column(nullable = false)
    String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
