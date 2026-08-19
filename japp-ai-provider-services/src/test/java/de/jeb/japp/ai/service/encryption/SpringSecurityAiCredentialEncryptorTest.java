package de.jeb.japp.ai.service.encryption;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringSecurityAiCredentialEncryptorTest {

    @Test
    void encryptsAndDecryptsRoundTrip() {
        SpringSecurityAiCredentialEncryptor encryptor = new SpringSecurityAiCredentialEncryptor("test-encryption-key");

        String ciphertext = encryptor.encrypt("my-secret-api-key");

        assertThat(ciphertext).isNotEqualTo("my-secret-api-key");
        assertThat(encryptor.decrypt(ciphertext)).isEqualTo("my-secret-api-key");
    }

    @Test
    void isAvailableWhenKeyIsConfigured() {
        assertThat(new SpringSecurityAiCredentialEncryptor("test-encryption-key").isAvailable()).isTrue();
    }

    @Test
    void isNotAvailableWhenKeyIsBlankOrMissing() {
        assertThat(new SpringSecurityAiCredentialEncryptor("").isAvailable()).isFalse();
        assertThat(new SpringSecurityAiCredentialEncryptor(null).isAvailable()).isFalse();
    }

    @Test
    void encryptThrowsWhenKeyIsMissing() {
        SpringSecurityAiCredentialEncryptor encryptor = new SpringSecurityAiCredentialEncryptor("");

        assertThatThrownBy(() -> encryptor.encrypt("secret")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decryptThrowsWhenKeyIsMissing() {
        SpringSecurityAiCredentialEncryptor encryptor = new SpringSecurityAiCredentialEncryptor("");

        assertThatThrownBy(() -> encryptor.decrypt("anything")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decryptingWithARotatedKeyFails() {
        SpringSecurityAiCredentialEncryptor original = new SpringSecurityAiCredentialEncryptor("key-one");
        String ciphertext = original.encrypt("my-secret-api-key");

        SpringSecurityAiCredentialEncryptor rotated = new SpringSecurityAiCredentialEncryptor("key-two");

        assertThatThrownBy(() -> rotated.decrypt(ciphertext)).isInstanceOf(RuntimeException.class);
    }
}
