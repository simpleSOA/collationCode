package com.ly.encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
    /**
     * 默认密钥
     */
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * aes加密后再base64编码
     *
     * @param sSrc 需要被加密的字符串
     * @param key  密钥
     * @return
     * @throws Exception
     */
    public static String encrypt(String sSrc, String key) throws Exception {
        if (key.length() != 16) {
            throw new RuntimeException("key length not equal 16. key length is " + key.length());
        }
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //防止linux下 随机生成key
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes(ENCODING));
        kgen.init(128, secureRandom);
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器
        byte[] byteContent = sSrc.getBytes(ENCODING);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);// 初始化
        byte[] result = cipher.doFinal(byteContent);
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 先base64解码，再aes解密
     * @param sSrc 需要被解密的字符串
     * @param key  密钥
     */
    public static String decrypt(String sSrc, String key) throws Exception {
        if (key.length() != 16) {
            throw new RuntimeException("key length not equal 16. key length is " + key.length());
        }
        byte[] content = Base64.getDecoder().decode(sSrc);// 先用base64解密
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //防止linux下 随机生成key
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes(ENCODING));
        kgen.init(128, secureRandom);
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");// 创建密码器
        cipher.init(Cipher.DECRYPT_MODE, keySpec);// 初始化
        byte[] result = cipher.doFinal(content);
        return new String(result, ENCODING);
    }
}
