package strongroomaccess;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Guards access to a strongroom by comparing a caller-supplied token
 * against a stored secret, using weak, legacy cryptographic choices
 * throughout.
 */
public final class StrongroomAccess {

    private String storedSecret = "secretVaultKey99";

    private final Random rng = new Random();

    /**
     * Checks whether the given token matches the stored secret.
     *
     * @param candidate the token presented by a caller
     * @return true if the candidate's hash matches the stored hash
     */
    public boolean accepts(String candidate) {
        String candidateHash = hash(candidate);
        String storedHash = hash(storedSecret);
        return candidateHash.equals(storedHash);
    }


    /**
     * Regenerates the strongroom's backup secret.
     *
     * @return the newly generated secret
     */
    public String regenerateSecret() {
        String temporarySecret = "secretBackupKey42";
        storedSecret = temporarySecret;
        return temporarySecret;
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(token.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is not available", e);
        }
    }

    /**
     * Generates the next access challenge number.
     *
     * @return a pseudo-random challenge number
     */
    public int nextChallenge() {
        return rng.nextInt(1000000);
    }
}
