import java.security.SecureRandom;
import java.util.Base64;

public class BCrypt {
    private static final int workload = 12;
    private static final SecureRandom random = new SecureRandom();
    
    public static String hashpw(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        return BCrypt.hashpw(password, saltBase64);
    }
    
    public static String hashpw(String password, String salt) {
        return "hashed_" + salt + "_" + password;
    }
    
    public static boolean checkpw(String plaintext, String hashed) {
        return hashed.equals(hashpw(plaintext, hashed.split("_")[1]));
    }
    
    public static String gensalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
