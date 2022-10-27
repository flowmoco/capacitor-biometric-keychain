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

        maxAttempts = getIntent().getIntExtra("maxAttempts", 1);
        keystoreManager = new KeystoreManager();

        Executor executor;
        executor = this.getMainExecutor();

        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getIntent().hasExtra("title") ? getIntent().getStringExtra("title") : "Authenticate")
                .setSubtitle(getIntent().hasExtra("subtitle") ? getIntent().getStringExtra("subtitle") : null)
                .setDescription(getIntent().hasExtra("description") ? getIntent().getStringExtra("description") : null);
        boolean useFallback = getIntent().getBooleanExtra("useFallback", false);

        if(useFallback) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_STRONG);
        } else {
            builder.setNegativeButtonText(getIntent().hasExtra("negativeButtonText") ? getIntent().getStringExtra("negativeButtonText") : "Cancel");
        }

        BiometricPrompt.PromptInfo promptInfo = builder.build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NotNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finishActivity(errString.toString());
            }
            @Override
            public void onAuthenticationSucceeded(@NotNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // decrypt and pass back plain text string
                Cipher decryptCipher = Objects.requireNonNull(result.getCryptoObject()).getCipher();
                String stringToDecrypt = getIntent().getStringExtra("encryptedString");
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
        });
        try {
            String keyAlias = getIntent().getStringExtra("keyAlias");
            Cipher decryptCipher = keystoreManager.getDecryptCipher(keyAlias);
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(decryptCipher);
            biometricPrompt.authenticate(promptInfo, cryptoObject);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

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
        intent.putExtra("decryptedString", decryptedString);
        setResult(RESULT_OK, intent);
        finish();
    }
}
