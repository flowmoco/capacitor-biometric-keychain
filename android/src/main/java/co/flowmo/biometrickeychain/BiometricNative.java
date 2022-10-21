package co.flowmo.biometrickeychain;

import android.util.Log;

public class BiometricNative {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
