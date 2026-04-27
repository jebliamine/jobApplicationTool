package de.jeb.japp.dao.user;

import de.jeb.japp.repositories.UserRepository;

public class UserDao {
    
    private final UserRepository userRepository;

    public UserDao(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
