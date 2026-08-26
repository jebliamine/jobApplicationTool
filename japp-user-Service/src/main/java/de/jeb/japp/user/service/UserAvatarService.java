package de.jeb.japp.user.service;

import de.jeb.japp.commons.exceptions.user.InvalidAvatarException;
import de.jeb.japp.dao.user.UserDao;
import de.jeb.japp.file.storage.services.AvatarStorageServiceInterface;
import de.jeb.japp.model.storage.StoredFile;
import de.jeb.japp.model.user.User;
import de.jeb.japp.model.user.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Business logic behind POST/DELETE /api/v1/users/me/avatar and GET /api/v1/users/{id}/avatar. */
@Service
public class UserAvatarService {

    private static final Logger log = LoggerFactory.getLogger(UserAvatarService.class);
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserDao userDao;
    private final AvatarStorageServiceInterface avatarStorageService;

    public UserAvatarService(UserDao userDao, AvatarStorageServiceInterface avatarStorageService) {
        this.userDao = userDao;
        this.avatarStorageService = avatarStorageService;
    }

    /** Replaces (or sets for the first time) the current user's avatar; the previous file, if any, is deleted after the new one is saved. */
    public UserDto upload(MultipartFile file, User currentUser) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException("Please choose an image to upload.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidAvatarException("Image must be smaller than 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidAvatarException("Only JPEG, PNG, WEBP, or GIF images are supported.");
        }

        String previousKey = currentUser.getAvatarStorageKey();

        StoredFile stored;
        try {
            stored = avatarStorageService.save(file, currentUser.getId());
        } catch (IOException e) {
            log.warn("Could not save uploaded avatar for user {}: {}", currentUser.getId(), e.getMessage());
            throw new InvalidAvatarException("Could not save the image. Please try again.");
        }

        currentUser.setAvatarStorageKey(stored.getStorageKey());
        currentUser.setAvatarContentType(stored.getContentType());
        User saved = userDao.updateUser(currentUser);

        if (previousKey != null) {
            deleteQuietly(previousKey);
        }

        return UserDto.from(saved);
    }

    /** Reverts to the initials-based avatar; a no-op (not an error) if there was nothing to remove. */
    public UserDto delete(User currentUser) {
        String key = currentUser.getAvatarStorageKey();
        if (key == null) {
            return UserDto.from(currentUser);
        }

        currentUser.setAvatarStorageKey(null);
        currentUser.setAvatarContentType(null);
        User saved = userDao.updateUser(currentUser);

        deleteQuietly(key);

        return UserDto.from(saved);
    }

    /** For GET /api/v1/users/{id}/avatar — empty when the user doesn't exist or has no avatar (controller returns 404 either way). */
    public Optional<AvatarResource> load(UUID userId) {
        return userDao.getUserById(userId)
                .filter(user -> user.getAvatarStorageKey() != null)
                .map(user -> new AvatarResource(avatarStorageService.load(user.getAvatarStorageKey()), user.getAvatarContentType()));
    }

    private void deleteQuietly(String storageKey) {
        try {
            avatarStorageService.delete(storageKey);
        } catch (IOException e) {
            log.warn("Could not delete the previous avatar file (now orphaned on disk): {}", storageKey);
        }
    }

    public record AvatarResource(Resource resource, String contentType) {
    }
}
