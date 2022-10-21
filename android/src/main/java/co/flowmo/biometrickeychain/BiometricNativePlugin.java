package co.flowmo.biometrickeychain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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

@CapacitorPlugin(name = "BiometricNative")
public class BiometricNativePlugin extends Plugin {
    private KeyStore keyStore;
    private final String DECRYPT_ALIAS_PREFIX = "DECRYPT_";
    private final String ENCRYPT_ALIAS_PREFIX = "ENCRYPT_";
    private static final String BIOMETRIC_NATIVE_SHARED_PREFERENCES = "BiometricNativeSharedPreferences";

    @PluginMethod
    public void getItem(PluginCall call) {
        String key = call.getString("key");

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String encryptedData = sharedPreferences.getString(key, null);
        try {
            String decryptedString = decryptString(encryptedData, key);
            JSObject ret = new JSObject();
            ret.put("value", decryptedString);
            call.resolve(ret);
        } catch (GeneralSecurityException | IOException e) {
            call.reject("Failed to get item", e);
            e.printStackTrace();
        }

    }

    @PluginMethod
    public void setItem(PluginCall call) {
        String key = call.getString("key");
        String value = call.getString("value");

        if (key != null && value != null) {
            try {
                SharedPreferences.Editor editor = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(key, encryptString(value, key));
                editor.apply();
                call.resolve();
            } catch (GeneralSecurityException | IOException e) {
                call.reject("Failed to save item", e);
                e.printStackTrace();
            }
        }
    }

    @PluginMethod
    public void removeItem(PluginCall call) {
        String key = call.getString("key", null);

        if(key != null){
            try {
                getKeyStore().deleteEntry(ENCRYPT_ALIAS_PREFIX.concat(key));
                getKeyStore().deleteEntry(DECRYPT_ALIAS_PREFIX.concat(key));
                SharedPreferences.Editor editor = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();
                call.resolve();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                call.reject("Failed to delete", e);
            }
        }else{
            call.reject("No key was provided");
        }
    }

    private KeyStore getKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        if (keyStore == null) {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        }
        return keyStore;
    }

    private String encryptString(String stringToEncrypt, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, getKey(ENCRYPT_ALIAS_PREFIX.concat(alias)));
        byte[] cipherText = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));
        return new String(cipherText, StandardCharsets.UTF_8);
    }

    private String decryptString(String stringToDecrypt, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES");
//        Authenticate before getting the key
        cipher.init(Cipher.DECRYPT_MODE, getKey(DECRYPT_ALIAS_PREFIX.concat(alias)));
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
