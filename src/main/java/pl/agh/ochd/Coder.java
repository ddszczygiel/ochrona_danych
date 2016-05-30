package pl.agh.ochd;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Coder {

    private final String SALT = "dominikszczygiel";
    private final String KEY = "dominikszczygiel";
    private final Cipher encrypter;
    private final Cipher decrypter;

    public Coder() throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {

        encrypter = Cipher.getInstance("AES/ECB/PKCS5Padding");
        decrypter = Cipher.getInstance("AES/ECB/PKCS5Padding");
        encrypter.init(Cipher.ENCRYPT_MODE, keyToSpec(SALT + KEY));
        decrypter.init(Cipher.DECRYPT_MODE, keyToSpec(SALT + KEY));
    }

    public String encrypt(String value) throws UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {

        return Base64.getEncoder().encodeToString(encrypter.doFinal(value.getBytes("UTF-8")));
    }

    public String decrypt(String encryptedValue) throws UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {

        return new String(decrypter.doFinal(Base64.getDecoder().decode(encryptedValue.getBytes("UTF-8"))));
    }

    private SecretKeySpec keyToSpec(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        byte[] bytes = (SALT + key).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha.digest(bytes);
        digest = Arrays.copyOf(digest, 16);
        return new SecretKeySpec(digest, "AES");
    }

}