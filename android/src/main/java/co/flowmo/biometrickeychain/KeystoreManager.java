package co.flowmo.biometrickeychain;

import android.os.Build;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class KeystoreManager {
    public KeyStore keyStore;
    public static final String DECRYPT_ALIAS_PREFIX = "DECRYPT_";
    public static final String ENCRYPT_ALIAS_PREFIX = "ENCRYPT_";
    public static final String BIOMETRIC_NATIVE_SHARED_PREFERENCES = "BiometricNativeSharedPreferences";

    public KeyStore getKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        if (keyStore == null) {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        }
        return keyStore;
    }

    public String encryptString(String stringToEncrypt, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey(ENCRYPT_ALIAS_PREFIX.concat(alias)));
        byte[] cipherText = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));
        return new String(cipherText, StandardCharsets.UTF_8);
    }

    public Cipher getDecryptCipher(String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, getKey(DECRYPT_ALIAS_PREFIX.concat(alias)));
        return cipher;
    }

    public String decryptString(String stringToDecrypt, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
        byte[] cipherText = cipher.doFinal(stringToDecrypt.getBytes(StandardCharsets.UTF_8));
        return new String(cipherText, StandardCharsets.UTF_8);
    }

    private SecretKey getKey(String alias) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableEntryException {
        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry)getKeyStore().getEntry(alias, null);

        if (secretKeyEntry == null) {
            generateKeys(alias);
        }

        assert secretKeyEntry != null;
        return secretKeyEntry.getSecretKey();
    }

    private void generateKeys(String alias) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();

        // This time we do specify "AndroidKeyStore".
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        // Now we import the encryption key, with no authentication requirements.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyStore.setEntry(
                    ENCRYPT_ALIAS_PREFIX.concat(alias),
                    new KeyStore.SecretKeyEntry(secretKey),
                    new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());

            // And the decryption key, this time requiring user authentication.
            keyStore.setEntry(
                    DECRYPT_ALIAS_PREFIX.concat(alias),
                    new KeyStore.SecretKeyEntry(secretKey),
                    new KeyProtection.Builder(KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setUserAuthenticationRequired(true)
                            .build());
        }
    }
}
