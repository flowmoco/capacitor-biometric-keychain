package co.flowmo.biometrickeychain;

import static co.flowmo.biometrickeychain.KeystoreManager.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResult;
import androidx.biometric.BiometricManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@CapacitorPlugin(name = "BiometricNative")
public class BiometricNativePlugin extends Plugin {
    private final KeystoreManager keystoreManager;

    public BiometricNativePlugin() {
        this.keystoreManager = new KeystoreManager();
    }

    public Boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(getContext());
        int canAuthenticateResult = 0;
//        String biometricSecurityLevel = getConfig().getString("biometricSecurityLevel");
//        int biometricLevel = BiometricManager.Authenticators.BIOMETRIC_WEAK;
//        if (biometricSecurityLevel.equals("STRONG")) {
//            biometricLevel = BiometricManager.Authenticators.BIOMETRIC_STRONG;
//        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            canAuthenticateResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        }

        switch (canAuthenticateResult) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            default:
                return false;
        }
    }

    @PluginMethod
    public void getItem(PluginCall call) {
        String key = call.getString("key");

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String encryptedData = sharedPreferences.getString(key, null);

        if (!isBiometricAvailable()) {
            call.reject("Biometrics not available");
            return;
        }

        // Create intent and run BiometricActivity
        // Move decrypt function to onAuthenticationSuccess in BiometricPrompt in the BiometricActivity

        Intent authIntent = createIntentForAuthentication(call);

        bridge.saveCall(call);
        startActivityForResult(call, authIntent, "authenticationResult");
    }

    @PluginMethod
    public void setItem(PluginCall call) {
        String key = call.getString("key");
        String value = call.getString("value");

        if (key != null && value != null) {
            try {
                SharedPreferences.Editor editor = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                editor.putString(key, keystoreManager.encryptString(value, key));
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
                keystoreManager.getKeyStore().deleteEntry(ENCRYPT_ALIAS_PREFIX.concat(key));
                keystoreManager.getKeyStore().deleteEntry(DECRYPT_ALIAS_PREFIX.concat(key));
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

    private Intent createIntentForAuthentication(PluginCall call) {
        Intent intent = new Intent(getContext(), BiometricActivity.class);

        intent.putExtra("title", "Authenticate");
        intent.putExtra("keyAlias", call.getString("key"));

        if(call.hasOption("subtitle"))
            intent.putExtra("subtitle", call.getString("subtitle"));

        if(call.hasOption("description"))
            intent.putExtra("description", call.getString("description"));

        if(call.hasOption("negativeButtonText"))
            intent.putExtra("negativeButtonText", call.getString("negativeButtonText"));

        if(call.hasOption("maxAttempts"))
            intent.putExtra("maxAttempts", call.getInt("maxAttempts"));

        return intent;
    }

    @ActivityCallback
    private void authenticationResult(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            if (data.hasExtra("result")) {
                switch (data.getStringExtra("result")) {
                    case "success":
                        String decryptedString = data.getStringExtra("decryptedString");
                        JSObject ret = new JSObject();
                        ret.put("value", decryptedString);
                        call.resolve(ret);
                        break;
                    case "failed":
                        call.reject(data.getStringExtra("errorDetails"), data.getStringExtra("errorCode"));
                        break;
                    default:
                        call.reject("Verification error: " + data.getStringExtra("result"));
                        break;
                }
            }
        }
    }
}
