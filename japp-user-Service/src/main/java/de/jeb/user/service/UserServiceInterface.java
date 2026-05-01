package de.jeb.user.service;

import de.jeb.japp.model.user.Credentials;
import de.jeb.japp.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserServiceInterface {

    public User registerUser(User user);

    public User updateUserCredentials(UUID id, Credentials credentials);

    public Boolean DeleteUser(UUID id);

    public User getUserById(UUID id);

    public List<User> getAllUsers();
}
