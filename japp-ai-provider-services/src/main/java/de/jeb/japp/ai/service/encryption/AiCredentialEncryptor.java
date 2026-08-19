package de.jeb.japp.ai.service.encryption;

/**
 * Encrypts/decrypts AI provider API keys for storage in
 * AiProviderConfiguration.encryptedApiKey. The encryption key itself never
 * lives in the database — see the implementation for where it comes from.
 */
public interface AiCredentialEncryptor {

    /** @throws IllegalStateException if no encryption key is configured */
    String encrypt(String plaintext);

    /** @throws IllegalStateException if no encryption key is configured */
    String decrypt(String ciphertext);

    /** Whether an encryption key is configured at all — checked before any encrypt/decrypt attempt. */
    boolean isAvailable();
}
