import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(BiometricNativePlugin)
public class BiometricNativePlugin: CAPPlugin {
    private let implementation = BiometricNative()

    @objc func getItem(_ call: CAPPluginCall) {
        let service = getConfig().getString("iosService") ?? ""
        let key = call.getString("key") ?? ""
        do {
            let value = try implementation.getItemFromKeychain(service, key)
            call.resolve(["value": value])
        } catch {
            call.reject(error.localizedDescription)
        }
    }

    @objc func setItem(_ call: CAPPluginCall) {
        let service = getConfig().getString("iosService") ?? ""
        let key = call.getString("key") ?? ""
        let value = call.getString("value") ?? ""
        do {
            try implementation.storeItemInKeychainWithBiometrics(service, key, value)
            call.resolve()
        } catch BiometricNative.KeychainError.duplicateItem {
            do {
                try implementation.updateItemInKeychain(service, key, value)
                call.resolve()
            } catch {
                call.reject(error.localizedDescription)
            }
        } catch {
            call.reject(error.localizedDescription)
        }
    }

    @objc func removeItem(_ call: CAPPluginCall) {
        let service = getConfig().getString("iosService") ?? ""
        let key = call.getString("key") ?? ""
        do {
            try implementation.removeItemFromKeychain(service, key)
            call.resolve()
        } catch {
            call.reject(error.localizedDescription)
        }
    }
}
