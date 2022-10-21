import Foundation

@objc public class BiometricNative: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
