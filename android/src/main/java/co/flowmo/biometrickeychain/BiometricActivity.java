package co.flowmo.biometrickeychain;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;

public class BiometricActivity extends AppCompatActivity {
    private int maxAttempts;
    private int counter = 0;
    private KeystoreManager keystoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_activity);

        String keyAlias = getIntent().getStringExtra("keyAlias");
        Cipher cipher;
        boolean encryptionMode = getIntent().hasExtra("plainText");
        maxAttempts = getIntent().getIntExtra("maxAttempts", 1);
        keystoreManager = new KeystoreManager();

        try {
            cipher = encryptionMode ? keystoreManager.getEncryptCipher(keyAlias) : keystoreManager.getDecryptCipher(keyAlias);
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);
            createBiometricPrompt(encryptionMode).authenticate(createBiometricPromptInfo(), cryptoObject);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

    }

    private BiometricPrompt.PromptInfo createBiometricPromptInfo() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getIntent().getStringExtra("title"))
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_STRONG).build();
    }

    private BiometricPrompt createBiometricPrompt(boolean encryptionMode) {
        Executor executor;
        executor = this.getMainExecutor();

        return new BiometricPrompt(this, executor, encryptionMode ? createEncryptionCallback() : createDecryptionCallback());
    }

    private BiometricPrompt.AuthenticationCallback createDecryptionCallback() {
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NotNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finishActivity(errString.toString());
            }
            @Override
            public void onAuthenticationSucceeded(@NotNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Cipher decryptCipher = Objects.requireNonNull(result.getCryptoObject()).getCipher();
                String stringToDecrypt = getIntent().getStringExtra("cipherText");

                assert decryptCipher != null;
                try {
                    String decryptedString = keystoreManager.decryptString(stringToDecrypt, decryptCipher);
                    successfulFinishActivity(decryptedString);
                } catch (GeneralSecurityException e) {
                    finishActivity("Failed to decrypt string");
                    e.printStackTrace();
                }
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                counter++;
                if (counter == maxAttempts) {
                    finishActivity("Max number of attempts exceeded");
                }
            }
        };
    }

    private BiometricPrompt.AuthenticationCallback createEncryptionCallback() {
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NotNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finishActivity(errString.toString());
            }
            @Override
            public void onAuthenticationSucceeded(@NotNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // decrypt and pass back plain text string
                Cipher encryptCipher = Objects.requireNonNull(result.getCryptoObject()).getCipher();
                String stringToEncrypt = getIntent().getStringExtra("plainText");
                assert encryptCipher != null;
                try {
                    String encryptedString = keystoreManager.encryptString(stringToEncrypt, encryptCipher);
                    successfulFinishActivity(encryptedString);
                } catch (GeneralSecurityException e) {
                    finishActivity("Failed to encrypt string");
                    e.printStackTrace();
                }
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                counter++;
                if (counter == maxAttempts) {
                    finishActivity("Max number of attempts exceeded");
                }
            }
        };
    }

    private void finishActivity(String error) {
        Intent intent = new Intent();
        intent.putExtra("result", "failed");
        intent.putExtra("errorDetails", error);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void successfulFinishActivity(String decryptedString) {
        Intent intent = new Intent();
        intent.putExtra("result", "success");
        intent.putExtra("value", decryptedString);
        setResult(RESULT_OK, intent);
        finish();
    }
}
