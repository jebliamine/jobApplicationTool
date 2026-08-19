package de.jeb.japp.ai.service.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Application-level AES encryption via Spring Security Crypto (already a
 * transitive dependency through spring-boot-starter-security — no new
 * dependency needed). The key comes from AI_CREDENTIALS_ENCRYPTION_KEY
 * (bound as ai.credentials.encryption-key) and is never persisted anywhere —
 * without it, this component simply cannot encrypt/decrypt, which is
 * intentional: the application must still start, but any attempt to store or
 * read a credential fails clearly rather than silently treating ciphertext
 * as plaintext.
 *
 * The salt below is not a secret — Spring Security Crypto uses it only for
 * key derivation strengthening; the real secrecy comes entirely from the
 * password (AI_CREDENTIALS_ENCRYPTION_KEY). A random IV is still applied per
 * encryption call internally.
 */
@Component
public class SpringSecurityAiCredentialEncryptor implements AiCredentialEncryptor {

    private static final String KEY_DERIVATION_SALT = "d9b970f7a97b4d4b";

    private final TextEncryptor delegate;

    public SpringSecurityAiCredentialEncryptor(@Value("${ai.credentials.encryption-key:}") String encryptionKey) {
        this.delegate = (encryptionKey != null && !encryptionKey.isBlank())
                ? Encryptors.text(encryptionKey, KEY_DERIVATION_SALT)
                : null;
    }

    @Override
    public boolean isAvailable() {
        return delegate != null;
    }

    @Override
    public String encrypt(String plaintext) {
        requireAvailable();
        return delegate.encrypt(plaintext);
    }

    @Override
    public String decrypt(String ciphertext) {
        requireAvailable();
        return delegate.decrypt(ciphertext);
    }

    private void requireAvailable() {
        if (delegate == null) {
            throw new IllegalStateException(
                    "AI_CREDENTIALS_ENCRYPTION_KEY is not configured; cannot encrypt or decrypt provider credentials.");
        }
    }
}
