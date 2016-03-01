package com.ly.encryption;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author: Administrator
 * Date: 2016/3/1 Time: 14:15
 */
public class MD5Encrypt {

    private static final Charset ENCODING = StandardCharsets.UTF_8;
    //十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

    /**
     * MD5摘要
     * @param originString 原文
     */
    public static String encodeByMD5(String originString) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] results = md.digest(originString.getBytes(ENCODING));
        //将得到的字节数组变成字符串返回
        String result = byteArrayToHexString(results);
        return result;
    }

    /**
     * 轮换字节数组为十六进制字符串
     * @param b 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] b){
        StringBuffer resultSb = new StringBuffer();
        for(int i=0;i<b.length;i++){
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    //将一个字节转化成十六进制形式的字符串
    private static String byteToHexString(byte b){
        int n = b;
        if(n<0){
            n=256+n;
        }
        int d1 = n/16;
        int d2 = n%16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
