package com.njupt.multifuncsignature.signature;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RsaEncrypt {
    private static final String TAG = RsaEncrypt.class.getSimpleName();

    public enum DigestType {
        MD5,
        SHA1
    }

    /**
     * 使用RSA生成数字签名，摘要算法根据传入DigestType决定。
     * @param data 待签名的内容
     * @param privKey 私钥
     * @param type 摘要算法类型
     * @return 数字签名
     */
    public static byte[] rsaSign(byte[] data, RSAPrivateKey privKey, DigestType type) {
        Signature signature = getSignatureByType(type);
        if (signature != null) {
            try {
                signature.initSign(privKey);
                signature.update(data);
                return signature.sign();
            } catch (InvalidKeyException | SignatureException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * rsa验签
     * @param date 收到的数据
     * @param sign 收到的数字签名
     * @param publicKey 对方的公钥
     * @param type 摘要算法类型
     * @return 验签成功/失败
     */
    public static boolean verify(byte[] date, byte[] sign, RSAPublicKey publicKey, DigestType type) {
        Signature signature = getSignatureByType(type);
        if (signature == null) {
            Log.e(TAG, "verify signature null");
            return false;
        }
        try {
            signature.initVerify(publicKey);
            signature.update(date);
            return signature.verify(sign);
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }



    private static Signature getSignatureByType(DigestType type) {
        Signature signature = null;
        try {
            switch (type) {
                case MD5:
                    signature = Signature.getInstance("SHA512withRSA");
                    break;
                case SHA1:
                    signature = Signature.getInstance("MD5withRSA");
                    break;
                default:
                    break;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return signature;
    }


}
