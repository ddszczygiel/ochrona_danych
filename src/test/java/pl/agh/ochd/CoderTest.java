package pl.agh.ochd;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CoderTest {

    private static Coder coder;

    @BeforeClass
    public static void setUp() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException {
        coder = new Coder();
    }

    @Test
    public void CoderTest() throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {

        //given
        String password = "lubiePlacki";
        //when
        String encrypted = coder.encrypt(password);
        String decrypted = coder.decrypt(encrypted);
        //then
        Assert.assertEquals(password, decrypted);
    }

}