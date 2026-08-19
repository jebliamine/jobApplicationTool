package de.jeb.japp.user.service;

import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.user.service.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserValidator userValidator;
    @Mock
    private UserDao userDao;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userValidator, userDao);
    }

    @Test
    void countAllUsersDelegatesToUserDao() {
        when(userDao.countAll()).thenReturn(7L);

        assertThat(userService.countAllUsers()).isEqualTo(7L);
    }
}
