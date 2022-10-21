import Foundation

@objc public class BiometricNative: NSObject {
    enum KeychainError: Error{
        case noPassword
        case unexpectedPasswordData
        case duplicateItem
        case unhandledError(status: OSStatus)
    }
    
    typealias JSObject = [String:Any]
    
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }

    @objc public func getItem(_ key: String) -> String {
        print(key)
        return key
    }

    @objc public func setItem(_ key: String, _ value: String) -> Bool {
        print(key)
        print(value)
        return true
    }

    @objc public func removeItem(_ key: String,) -> Bool {
        print(key)
        return true
    }
}
