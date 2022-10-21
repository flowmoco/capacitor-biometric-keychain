import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(BiometricNativePlugin)
public class BiometricNativePlugin: CAPPlugin {
    private let implementation = BiometricNative()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func getItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        call.resolve([
            "value": implementation.getItem(value)
        ])
    }
    
    @objc func setItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.setItem(key, value)
        ])
    }

    @objc func removeItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        call.resolve([
            "value": implementation.removeItem(value)
        ])
    }
}
