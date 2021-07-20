package com.springboot.core.encryption;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密
 * @author: Administrator
 * Date: 2016/3/1 Time: 14:04
 */
public class AESEncrypt {
    private static final String IV = "1234567890ars2xh";
    private static final String TRANSFORM = "AES/CBC/PKCS5Padding";
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private static Key getKey(String strKey) throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(strKey.getBytes(ENCODING));
        generator.init(256, secureRandom);
        return generator.generateKey();
    }


    public static String aesEncrypt(String content, String pkey) throws Exception{
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(pkey.getBytes(ENCODING));
        Key secureKey = getKey(pkey);
        Cipher cipher = Cipher.getInstance(TRANSFORM);
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secureKey, iv, sr);
        byte[] bytes = cipher.doFinal(content.getBytes(ENCODING));
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String aesDecode(String content, String pkey) throws Exception {
        byte[] decode = Base64.getDecoder().decode(content);
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(pkey.getBytes(ENCODING));
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(Cipher.DECRYPT_MODE, getKey(pkey), iv, sr);
        return new String(cipher.doFinal(decode));

    }
}
