package co.flowmo.biometrickeychain;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "BiometricNative")
public class BiometricNativePlugin extends Plugin {

    private BiometricNative implementation = new BiometricNative();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void getItem(PluginCall call) {
        String key = call.getString("key");

        JSObject ret = new JSObject();
        ret.put("value", implementation.getItem(key));
        call.resolve(ret);
    }

    @PluginMethod
    public void setItem(PluginCall call) {
        String key = call.getString("key");
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("successful", implementation.setItem(key, value));
        call.resolve(ret);
    }

    @PluginMethod
    public void removeItem(PluginCall call) {
        String key = call.getString("key");

        JSObject ret = new JSObject();
        ret.put("successful", implementation.removeItem(key));
        call.resolve(ret);
    }
}
