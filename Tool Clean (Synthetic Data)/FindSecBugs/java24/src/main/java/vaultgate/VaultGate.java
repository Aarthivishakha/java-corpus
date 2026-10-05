package vaultgate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Guards access to a vault by checking a caller-supplied token against
 * a salted hash, using only vetted, non-deprecated cryptographic
 * primitives.
 */
public final class VaultGate {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] salt;
    private final byte[] expectedHash;

    /**
     * Creates a gate that accepts only the given plaintext token,
     * hashed against a freshly generated random salt.
     *
     * @param token the plaintext token this gate should accept
     */
    public VaultGate(String token) {
        this.salt = new byte[16];
        RANDOM.nextBytes(salt);
        this.expectedHash = hash(token, salt);
    }

    /**
     * Checks whether the given token matches the one this gate was
     * created with.
     *
     * @param candidate the token presented by a caller
     * @return true if the candidate hashes to the expected value
     */
    public boolean accepts(String candidate) {
        byte[] candidateHash = hash(candidate, salt);
        return MessageDigest.isEqual(candidateHash, expectedHash);
    }

    private static byte[] hash(String token, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /**
     * Encodes this gate's salt for storage alongside the hash.
     *
     * @return the base64-encoded salt
     */
    public String encodedSalt() {
        return Base64.getEncoder().encodeToString(salt);
    }
}
