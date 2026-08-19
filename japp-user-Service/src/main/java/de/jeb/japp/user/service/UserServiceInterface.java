package de.jeb.japp.user.service;

import de.jeb.japp.model.user.Credentials;
import de.jeb.japp.model.user.User;

import java.util.List;
import java.util.UUID;

public interface UserServiceInterface {

    public User updateUserCredentials(UUID id, Credentials credentials);

    public Boolean DeleteUser(UUID id);

    public User getUserById(UUID id);

    public List<User> getAllUsers();

    /** Global count of every registered user. ADMIN-only use is enforced by the caller (DashboardService). */
    public long countAllUsers();
}
