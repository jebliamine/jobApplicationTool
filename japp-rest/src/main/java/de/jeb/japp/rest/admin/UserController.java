package de.jeb.japp.rest.admin;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.UpdateUserRequest;
import de.jeb.japp.model.user.dto.UserDto;
import de.jeb.japp.user.service.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal User user) {
        return UserDto.from(user);
    }

    @PutMapping("/me")
    public UserDto updateCurrentUser(@AuthenticationPrincipal User user, @RequestBody UpdateUserRequest request) {
        return userProfileService.updateProfile(user, request);
    }
}
