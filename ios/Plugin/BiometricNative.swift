import Foundation

@objc public class BiometricNative: NSObject {
    enum KeychainError: Error {
        case noPassword
        case unexpectedPasswordData
        case duplicateItem
        case unhandledError(status: OSStatus)
    }

    @objc func getItemFromKeychain(_ itemName: String) throws -> String {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecMatchLimit as String: kSecMatchLimitOne,
                                    kSecAttrAccount as String: itemName,
                                    kSecReturnAttributes as String: true,
                                    kSecReturnData as String: true]

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess else { throw KeychainError.unhandledError(status: status) }

        guard let existingItem = item as? [String: Any],
              let itemData = existingItem[kSecValueData as String] as? Data,
              let value = String(data: itemData, encoding: .utf8)
        else {
            throw KeychainError.unexpectedPasswordData
        }

        return value
    }

    func storeItemInKeychainWithBiometrics(_ key: String, _ value: String) throws {
        let accessControl = SecAccessControlCreateWithFlags(
          nil,
          kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
          .userPresence,
          nil)!

        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword as String,
                                    kSecValueData as String: value.data(using: .utf8)!,
                                    kSecAttrAccount as String: key,
                                    kSecAttrAccessControl as String: accessControl]

        let status = SecItemAdd(query as CFDictionary, nil)
        guard status != errSecDuplicateItem else { throw KeychainError.duplicateItem }
        guard status == errSecSuccess else { throw KeychainError.unhandledError(status: status) }
    }

    func updateItemInKeychain(_ itemName: String, _ value: String) throws {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrAccount as String: itemName]

         let attributes: [String: Any] = [kSecValueData as String: value.data(using: .utf8)!]

        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)
        guard status != errSecItemNotFound else { throw KeychainError.noPassword }
        guard status == errSecSuccess else { throw KeychainError.unhandledError(status: status) }
    }

    func removeItemFromKeychain(_ key: String) throws {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrAccount as String: key]

        let status = SecItemDelete(query as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else {
             throw KeychainError.unhandledError(status: status)
        }
    }
}
