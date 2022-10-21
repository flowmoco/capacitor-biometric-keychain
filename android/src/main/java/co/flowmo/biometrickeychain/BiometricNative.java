package co.flowmo.biometrickeychain;

import android.util.Log;

public class BiometricNative {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    public String getItem(String key) {
      return "value";
    }

    public Boolean setItem(String key, String value) {
      return true;
    }
    
    public Boolean removeItem(String key) {
      return true;
    }
}
