package com.ly;

import com.ly.encryption.AESEncrypt;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AESEncryptTest {

    @Test
    public void encrypt(){
        try {
            String encrypt = AESEncrypt.encrypt("hello world", "1234567890123456");
            System.out.println(encrypt);
            String decrypt = AESEncrypt.decrypt(encrypt, "1234567890123456");
            Assert.assertTrue("hello world".equals(decrypt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
