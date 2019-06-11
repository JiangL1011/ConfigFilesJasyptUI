package com.jl.jasypt.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 3DES加密解密方法
 *
 * @author 蒋领
 * @date 2019年06月11日
 */
class DesUtil {

    static String encrypt(String value, String salt) {
        String des = encode3Des(salt, value);
        String prefix = "DES#";
        return prefix + des;
    }

    static String decrypt(String value, String salt) {
        // 如果最后一个DES#不在第一个，则说明字符串中有多个DES#加密字符串
        if (value.lastIndexOf("DES#") == 0) {
            return resolvePropertyValue(value, salt);
        } else {
            int i = value.indexOf("@");
            String[] strings = value.substring(0, i).split(":");
            for (String str : strings) {
                String des = resolvePropertyValue(str, salt);
                value = value.replace(str, des);
            }
            return value;
        }
    }

    /**
     * 3DES加密
     *
     * @param key    密钥，24位
     * @param srcStr 将加密的字符串
     * @return lee on 2017-08-09 10:51:44
     */
    private static String encode3Des(String key, String srcStr) {
        byte[] keyByte = hex(key);
        byte[] src = srcStr.getBytes();
        try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keyByte, "DESede");
            //加密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return org.apache.tomcat.util.codec.binary.Base64.encodeBase64String(c1.doFinal(src));
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.getMessage();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private static byte[] hex(String key) {
        String f = DigestUtils.md5DigestAsHex(key.getBytes());
        byte[] keys = f.getBytes();
        byte[] enk = new byte[24];
        System.arraycopy(keys, 0, enk, 0, 24);
        return enk;
    }

    public static String resolvePropertyValue(String value, String key) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        // 值以DES#开头的均为3DES加密,需要解密
        if (value.startsWith("DES#")) {
            return resolveDESValue(value.substring(4), key);
        } else if (value.startsWith("mongodb") && value.contains("DES#")) {
            String[] arr = value.split(":");
            String userName = arr[1].substring(2);
            String password = arr[2].substring(0, arr[2].indexOf("@"));
            String userNameDe = resolveDESValue(userName.substring(4), key);
            String passwordDe = resolveDESValue(password.substring(4), key);
            value = value.replace(userName, userNameDe);
            value = value.replace(password, passwordDe);
        }
        // 不需要解密的值直接返回
        return value;
    }

    private static String resolveDESValue(String value, String key) {
        //将24位秘钥MD5加密
        byte[] keyByte = hex(key);
        //密文Base64解编码
        byte[] src = Base64.decode(value.getBytes());
        try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keyByte, "DESede");
            //解密
            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return new String(c1.doFinal(src));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
