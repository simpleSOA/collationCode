package com.ly.encryption;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author: Administrator
 * Date: 2016/3/1 Time: 14:15
 */
public class MD5Encrypt {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    /**
     *十六进制下数字到字符的映射数组
     */
    private final static char [] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    private static final String MD5DIGEST = "MD5";

    /**
     * md5散列函数
     * @param src 需要散列的字符串
     * @return 大写后的散列值
     */
    public final static String md5(String src) {
        byte[] btInput = src.getBytes(ENCODING);
        MessageDigest mdInst = null;
        try {
            mdInst = MessageDigest.getInstance(MD5DIGEST);
        } catch (NoSuchAlgorithmException e) {
            //java平台支持md5，忽略它
        }
        mdInst.update(btInput);
        byte[] md = mdInst.digest();
        // 把密文转换成十六进制的字符串形式
        int j = md.length;
        char[] str = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = md[i];
            str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
            str[k++] = HEX_DIGITS[byte0 & 0xf];
        }
        return new String(str);
    }
}
