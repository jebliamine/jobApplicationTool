package de.jeb.user.service;

import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.Credentials;
import de.jeb.japp.model.user.User;
import de.jeb.user.service.validator.UserValidator;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserServiceInterface {
    private final UserValidator userValidator;
    private final UserDao userDao;


    public UserServiceImpl(UserValidator userValidator, UserDao userDao) {
        this.userValidator = userValidator;
        this.userDao = userDao;
    }

    @Override
    public User registerUser(User user) {
        return userDao.registerUser(userValidator.ValidateRegisterUser(user));
    }

    @Override
    public User updateUserCredentials(UUID id, Credentials credentials) {
//        userValidator.validateUpdateCredentials(id, credentials);
        return null;
    }

    @Override
    public Boolean DeleteUser(UUID id) {
        return null;
    }

    @Override
    public User getUserById(UUID id) {
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return List.of();
    }
}
