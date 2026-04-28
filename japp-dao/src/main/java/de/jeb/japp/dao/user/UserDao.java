package de.jeb.japp.dao.user;

import de.jeb.japp.model.user.User;
import de.jeb.japp.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDao {

    private final UserRepository userRepository;

    public UserDao(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> getAllLetters() {
        return userRepository.findAll();
    }

    public Optional<User> getLetterById(UUID id) {
        return userRepository.findById(id);
    }

    public User saveLetter(User user) {
        return userRepository.save(user);
    }

    public void deleteLetter(UUID id) {
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
}
