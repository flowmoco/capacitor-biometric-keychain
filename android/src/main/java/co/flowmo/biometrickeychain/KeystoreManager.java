package co.flowmo.biometrickeychain;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

public class KeystoreManager {
    private KeyStore keyStore;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final byte[] FIXED_IV = new byte[12];
    public static final String BIOMETRIC_NATIVE_SHARED_PREFERENCES = "NativeBiometricSharedPreferences";

    public String decryptString(String stringToDecrypt, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        byte[] encryptedData = Base64.decode(stringToDecrypt, Base64.DEFAULT);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
}

    public String encryptString(String stringToEncrypt, String KEY_ALIAS) throws GeneralSecurityException, IOException {
        Cipher cipher;
        cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getKey(KEY_ALIAS), new GCMParameterSpec(128, FIXED_IV));
        byte[] encodedBytes = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public Cipher getDecryptCipher(String KEY_ALIAS) throws GeneralSecurityException, IOException {
        Cipher cipher;
        cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getKey(KEY_ALIAS), new GCMParameterSpec(128, FIXED_IV));
        return cipher;
    }

    private Key generateKey(String KEY_ALIAS) throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
               KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .setUserAuthenticationRequired(true)
                .build()
        );
        return generator.generateKey();
    }

    private Key getKey(String KEY_ALIAS) throws GeneralSecurityException, IOException {
        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) getKeyStore().getEntry(KEY_ALIAS, null);
        if (secretKeyEntry != null) {
            return secretKeyEntry.getSecretKey();
        }
        return generateKey(KEY_ALIAS);
    }

    private KeyStore getKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        if (keyStore == null) {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
        }
        return keyStore;
    }
}
