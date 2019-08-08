package com.springboot.core.encryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**字符串 DESede(3DES) 加密
 * ECB模式/使用PKCS7方式填充不足位,目前给的密钥是192位
 * 3DES（即Triple DES）是DES向AES过渡的加密算法（1999年，NIST将3-DES指定为过渡的
 * 加密标准），是DES的一个更安全的变形。它以DES为基本模块，通过组合分组方法设计出分组加
 * 密算法，其具体实现如下：设Ek()和Dk()代表DES算法的加密和解密过程，K代表DES算法使用的
 * 密钥，P代表明文，C代表密表，这样，
 * 3DES加密过程为：C=Ek3(Dk2(Ek1(P)))
 * 3DES解密过程为：P=Dk1((EK2(Dk3(C)))
 */
public class ThreeDesTool {
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    //定义加密算法，有DES、DESede(即3DES)、Blowfish
    private static final String ALGORITHM = "DESede";
    /**
     * 加密方法
     * @param src 源数据的字符串
     * @return base64过的加密字符串
     */
    public static String encryptMode(String src,String key) throws Exception {
        SecretKey deskey = new SecretKeySpec(build3DesKey(key), ALGORITHM);
        Cipher c1 = Cipher.getInstance(ALGORITHM);
        c1.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] doFinal = c1.doFinal(src.getBytes(ENCODING));
        return Base64.getEncoder().encodeToString(doFinal);
    }


    /**
     * 解密函数
     * @param src 密文的字符串
     * @return
     */
    public static String decryptMode(String src,String key) throws Exception {
        SecretKey deskey = new SecretKeySpec(build3DesKey(key), ALGORITHM);
        Cipher c1 = Cipher.getInstance(ALGORITHM);
        c1.init(Cipher.DECRYPT_MODE, deskey);    //初始化为解密模式
        byte[] doFinal = c1.doFinal(Base64.getDecoder().decode(src));
        return new String(doFinal,ENCODING);
    }


    /**
     * 根据字符串生成密钥字节数组
     * @param keyStr 密钥字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException{
        byte[] key = new byte[24];
        byte[] temp = keyStr.getBytes(ENCODING);
        if(key.length > temp.length){
            //如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, temp.length);
        }else{
            //如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
            System.arraycopy(temp, 0, key, 0, key.length);
        }
        return key;
    }
}
