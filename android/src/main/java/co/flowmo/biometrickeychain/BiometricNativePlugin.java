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

@CapacitorPlugin(name = "BiometricNative")
public class BiometricNativePlugin extends Plugin {

    private boolean biometricsNotAvailable(PluginCall call) {
        boolean available = true;
        BiometricManager biometricManager = BiometricManager.from(getContext());
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                available = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                call.reject("No biometrics enrolled");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                call.reject("Hardware unavailable");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                call.reject("No biometric hardware on device");
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                call.reject("Security update required");
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                call.reject("Biometrics unsupported");
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                call.reject("Biometrics status unknown");
                break;
        }
        return available;
    }

    @PluginMethod
    public void getItem(PluginCall call) {
        String key = call.getString("key");

        if (biometricsNotAvailable(call)) {
            return;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String encryptedData = sharedPreferences.getString(key, null);

        if (encryptedData == null) {
            call.reject("No key stored with that name");
            return;
        }

        Intent authIntent = createIntentForAuthentication(key);
        authIntent.putExtra("cipherText", encryptedData);

        bridge.saveCall(call);
        startActivityForResult(call, authIntent, "authenticationDecryptResult");
    }

    @PluginMethod
    public void setItem(PluginCall call) {
        String key = call.getString("key");
        String value = call.getString("value");

        if (key != null && value != null) {
            if (biometricsNotAvailable(call)) {
                return;
            }

            Intent authIntent = createIntentForAuthentication(key);
            authIntent.putExtra("plainText", value);

            bridge.saveCall(call);
            startActivityForResult(call, authIntent, "authenticationEncryptResult");
        }
    }

    @PluginMethod
    public void removeItem(PluginCall call) {
        String key = call.getString("key", null);

        try {
            SharedPreferences.Editor editor = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
            new KeystoreManager().getKeyStore().deleteEntry(key);
            editor.remove(key);
            editor.apply();
            call.resolve();
        } catch (GeneralSecurityException | IOException e) {
            call.reject("Failed to delete entry", e);
        }
    }

    private Intent createIntentForAuthentication(String key) {
        Intent intent = new Intent(getContext(), BiometricActivity.class);

        intent.putExtra("title", "Authenticate");
        intent.putExtra("keyAlias", key);

        return intent;
    }

    @ActivityCallback
    private void authenticationDecryptResult(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            if (data.hasExtra("result")) {
                switch (data.getStringExtra("result")) {
                    case "success":
                        String decryptedString = data.getStringExtra("value");
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

    @ActivityCallback
    private void authenticationEncryptResult(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            if (data.hasExtra("result")) {
                switch (data.getStringExtra("result")) {
                    case "success":
                        SharedPreferences.Editor editor = getContext().getSharedPreferences(BIOMETRIC_NATIVE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                        editor.putString(call.getString("key"), data.getStringExtra("value"));
                        editor.apply();
                        call.resolve();
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
