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
        let key = call.getString("key") ?? ""
        do {
            let value = try implementation.getItemFromKeychain(key)
            call.resolve(["value": value])
        } catch {
            call.reject(error.localizedDescription)
        }
    }
    
    @objc func setItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        let value = call.getString("value") ?? ""
        do {
            let value = try implementation.storeItemInKeychainWithBiometrics(key, value)
            call.resolve()
        } catch BiometricNative.KeychainError.duplicateItem {
            do {
                let value = try implementation.updateItemInKeychain(key, value)
                call.resolve()
            } catch {
                call.reject(error.localizedDescription)
            }
        } catch {
            call.reject(error.localizedDescription)
        }
    }

    @objc func removeItem(_ call: CAPPluginCall) {
        let key = call.getString("key") ?? ""
        do {
            let value = try implementation.removeItemFromKeychain(key)
            call.resolve()
        } catch {
            call.reject(error.localizedDescription)
        }
    }
}
