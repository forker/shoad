/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.ldap;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.codec.Base64;

/**
 *
 * @author romansergey
 */
public class PasswordUtil {

    public static boolean verifyPassword(String password, String b64hash) throws NoSuchAlgorithmException {
        MessageDigest algorithm = MessageDigest.getInstance("SHA");

        byte[] shaPasswordBytes = Base64.decode(b64hash.substring(6).getBytes());

        byte[] salt = Arrays.copyOfRange(shaPasswordBytes, 20, shaPasswordBytes.length);

        byte[] hash = Arrays.copyOf(shaPasswordBytes, 20);

        algorithm.update(password.getBytes());
        algorithm.update( salt );

        return MessageDigest.isEqual(hash, algorithm.digest());
    }
    
    public static String hashPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest algorithm = MessageDigest.getInstance("SHA");

        byte[] salt = new byte[8];
        
        new Random().nextBytes(salt);
        

        algorithm.update(password.getBytes());
        algorithm.update( salt );
        byte[] hash = algorithm.digest();
        
        return "{SSHA}" + new String(Base64.encode( ArrayUtils.addAll(hash, salt) ));
    }
    
    
    
    private static final char[] kDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] hexToBytes(char[] hex) {
        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++) {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;
            if (value > 127) {
                value -= 256;
            }
            raw[i] = (byte) value;
        }
        return raw;
    }

    public static byte[] hexToBytes(String hex) {
        return hexToBytes(hex.toCharArray());
    }
}
