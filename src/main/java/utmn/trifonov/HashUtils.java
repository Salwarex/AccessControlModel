package utmn.trifonov;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String hash(String string) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(string.getBytes());
            StringBuilder hexStr = new StringBuilder();
            for(byte b : hashBytes){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexStr.append('0');
                hexStr.append(hex);
            }
            return hexStr.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isMatch(String provided, String hash){
        String hashProvided = hash(provided);
        //Logger.log("%s =? %s".formatted(hash, hashProvided));
        return hash.equals(hashProvided);
    }
}
