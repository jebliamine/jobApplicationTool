package de.jeb.japp.rest.admin;

import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.ChangePasswordRequest;
import de.jeb.japp.model.user.dto.UpdateUserRequest;
import de.jeb.japp.model.user.dto.UserDto;
import de.jeb.japp.user.service.UserAvatarService;
import de.jeb.japp.user.service.UserPasswordService;
import de.jeb.japp.user.service.UserProfileService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserProfileService userProfileService;
    private final UserPasswordService userPasswordService;
    private final UserAvatarService userAvatarService;

    public UserController(
            UserProfileService userProfileService,
            UserPasswordService userPasswordService,
            UserAvatarService userAvatarService
    ) {
        this.userProfileService = userProfileService;
        this.userPasswordService = userPasswordService;
        this.userAvatarService = userAvatarService;
    }

    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal User user) {
        return UserDto.from(user);
    }

    @PutMapping("/me")
    public UserDto updateCurrentUser(@AuthenticationPrincipal User user, @RequestBody UpdateUserRequest request) {
        return userProfileService.updateProfile(user, request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user, @RequestBody ChangePasswordRequest request) {
        userPasswordService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public UserDto uploadAvatar(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal User user) {
        return userAvatarService.upload(file, user);
    }

    @DeleteMapping("/me/avatar")
    public UserDto deleteAvatar(@AuthenticationPrincipal User user) {
        return userAvatarService.delete(user);
    }

    /** Any authenticated user may view any other user's avatar — no more sensitive than the name/email already exposed via UserDto as an owner reference. */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable UUID id) {
        return userAvatarService.load(id)
                .map(avatar -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(avatar.contentType()))
                        .body(avatar.resource()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
