package com.njupt.multifuncsignature.signature;

import android.content.Context;
import java.util.Base64;

import com.njupt.multifuncsignature.util.DESUtil;
import com.njupt.multifuncsignature.util.IOUtils;

import java.io.File;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;


/**
 * 密钥管理类
 */
public class KeyManager {

    private static volatile KeyManager mInstance = null;
    private static final String TAG = KeyManager.class.getSimpleName();
    public String KEY_STORE_PATH;
    public static final String PRIV_POSTFIX = ".pri";
    public static final String PUB_POSTFIX = ".pub";
    private Context mContext;
    private PrivateKey mPrivateKey;
    private PublicKey mPublicKey;
    private String mCurrentKeyName;


    private KeyManager(Context context) {
        this.mContext = context;
        KEY_STORE_PATH = context.getExternalFilesDir(null).getPath() + "/";
    }


    public static KeyManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (KeyManager.class) {
                if (mInstance == null) {
                    mInstance = new KeyManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     *生成一对密钥
     * @param keyName 密钥名称
     * @return -1：创建失败 0：创建成功
     */
    public int generateRSAKeyPair(String keyName) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("PSA");
            if (keyPairGenerator == null) {
                return -1;
            }
            KeyPair pair = keyPairGenerator.generateKeyPair();
            mPublicKey = pair.getPublic();
            mPrivateKey = pair.getPrivate();
            mCurrentKeyName = keyName;
            return 0;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            if (keyPairGenerator == null) {
                return null;
            }
            KeyPair pair = keyPairGenerator.generateKeyPair();
            return pair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 密钥导出到文件中
     * @param key 密钥
     * @param passward 如果是私钥，需要加密保存
     * @param fileName 文件名
     * @return
     */
    public void exportKeyToFile(Key key, String passward, String fileName) throws Exception {
        byte[] keyBytes = key.getEncoded();
        String keyBase64 = Base64.getEncoder().encodeToString(keyBytes);
        if (key instanceof PublicKey) {
            IOUtils.writeFile(keyBase64, new File(KEY_STORE_PATH + fileName));
        } else if (key instanceof PrivateKey) {
            String keyBase64AfterDes = DESUtil.encrypt(passward, keyBase64);
            IOUtils.writeFile(keyBase64AfterDes, new File(KEY_STORE_PATH + fileName));
        }
    }

    public Key importKeyFromFile(String fileName, String passward) throws Exception {
        String keyBase64 = IOUtils.readFile(new File(KEY_STORE_PATH + fileName));
        //私钥先解密
        if (fileName.endsWith("pri")) {
            keyBase64 = DESUtil.decrypt(passward, keyBase64);
        }
        byte[] encKeyBytes = Base64.getDecoder().decode(keyBase64);
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(encKeyBytes);
        if (fileName.endsWith(PRIV_POSTFIX)) {
            return KeyFactory.getInstance("RSA").generatePrivate(encodedKeySpec);
        }
        if (fileName.endsWith(PUB_POSTFIX)) {
            return KeyFactory.getInstance("RSA").generatePublic(encodedKeySpec);
        }
        return null;
    }

    public String[] getPrivkeysNameArray() {
        List<File> fileList = new ArrayList<>();
        File catolog = new File(KEY_STORE_PATH);
        if (!catolog.exists()) {
            catolog.mkdir();
            return null;
        }
        if (catolog.listFiles() == null) {
            return null;
        }
        for (File f : catolog.listFiles()) {
            if (f.isFile() && f.getName().endsWith(PRIV_POSTFIX)) {
                fileList.add(f);
            }
        }
        if (fileList.size() == 0) {
            return null;
        }
        String[] privkeysNames = new String[fileList.size()];
        for (int i = 0; i < fileList.size(); i++) {
            File f = fileList.get(i);
            privkeysNames[i] = f.getName();
        }
        return privkeysNames;
    }






}
