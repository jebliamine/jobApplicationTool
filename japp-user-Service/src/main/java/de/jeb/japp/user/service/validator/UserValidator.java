package de.jeb.japp.user.service.validator;

import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private final UserDao userDao;

    public UserValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    public User ValidateRegisterUser(User user) {
        if (user.getEmail().isEmpty()) {

        }

        if (user.getEmail().isEmpty()) {

        }

        if (user.getPasswordHash().isEmpty() || user.getPasswordHash().length() < 5) {

        }

        return user;
    }


}
