package co.flowmo.biometrickeychain;

import static co.flowmo.biometrickeychain.KeystoreManager.*;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

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

@CapacitorPlugin(name = "BiometricNative")
public class BiometricNativePlugin extends Plugin {
    private KeystoreManager keystoreManager;

    public Boolean checkBiometricsAvailable() {
        Boolean available = false;
        BiometricManager biometricManager = BiometricManager.from(getContext());
        int canAuthenticate;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
            if (!keyguardManager.isDeviceSecure()) {
                return false;
            }
            canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        }

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return false;
        }

        return available;
    }

    @PluginMethod
    public void getItem(PluginCall call) {
        // Create intent and run BiometricActivity
        // Move decrypt function to onAuthenticationSuccess in BiometricPrompt in the BiometricActivity
        String key = call.getString("key");
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String encryptedData = sharedPreferences.getString(key, null);

        if (encryptedData == null) {
            call.reject("No key stored with that name");
            return;
        }

        Intent authIntent = createIntentForAuthentication(call, key,encryptedData);

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
                editor.putString(key, getKeystoreManager().encryptString(value, key));
                editor.apply();
                call.resolve();
            } catch (GeneralSecurityException | IOException e) {
                call.reject("Failed to save item", e);
                e.printStackTrace();
            }
        }
    }

    public KeystoreManager getKeystoreManager() {
        if (keystoreManager == null) {
            keystoreManager = new KeystoreManager();
        }
        return keystoreManager;
    }

    @PluginMethod
    public void removeItem(PluginCall call) {
        String key = call.getString("key", null);

        if(key != null){
            SharedPreferences.Editor editor = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            call.resolve();
        }else{
            call.reject("No key was provided");
        }
    }

    private Intent createIntentForAuthentication(PluginCall call, String key, String encryptedData) {
        Intent intent = new Intent(getContext(), BiometricActivity.class);

        intent.putExtra("title", "Authenticate");
        intent.putExtra("keyAlias", key);
        intent.putExtra("encryptedString", encryptedData);

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
