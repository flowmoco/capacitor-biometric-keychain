import Foundation

@objc public class BiometricNative: NSObject {
    enum KeychainError: Error {
        case noPassword
        case unexpectedPasswordData
        case biometricsUnsupported
        case duplicateItem
        case unhandledError(status: OSStatus)
    }

    @objc func getItemFromKeychain(_ service: String, _ itemName: String) throws -> String {
        guard let access = try createAccessControlRequiringBiometrics() else { throw KeychainError.biometricsUnsupported };
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecMatchLimit as String: kSecMatchLimitOne,
                                    kSecAttrAccount as String: itemName,
                                    kSecAttrService as String: service,
                                    kSecReturnAttributes as String: true,
                                    kSecAttrAccessControl as String: access,
                                    kSecReturnData as String: true]

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        print("KEY FROM GET", itemName)
        print("ITEM FROM GET", item)
        print("SERVICE FROM GET", service)
        print("STATUS FROM GET", status)
        guard status == errSecSuccess else { throw KeychainError.unhandledError(status: status) }

        guard let existingItem = item as? [String: Any],
              let itemData = existingItem[kSecValueData as String] as? Data,
              let value = String(data: itemData, encoding: .utf8)
        else {
            throw KeychainError.unexpectedPasswordData
        }

        return value
    }

    func storeItemInKeychainWithBiometrics(_ service: String, _ key: String, _ value: String) throws {
        guard let access = try createAccessControlRequiringBiometrics() else { throw KeychainError.biometricsUnsupported };
        
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword as String,
                                    kSecValueData as String: value.data(using: .utf8)!,
                                    kSecAttrAccount as String: key,
                                    kSecAttrService as String: service,
                                    kSecAttrAccessControl as String: access]

        let status = SecItemAdd(query as CFDictionary, nil)
        print("KEY FROM STORE", key)
        print("SERVICE FROM STORE", service)
        print("STATUS FROM STORE", status)
        guard status != errSecDuplicateItem else { throw KeychainError.duplicateItem }
        guard status == errSecSuccess else { throw KeychainError.unhandledError(status: status) }
    }

    func updateItemInKeychain(_ service: String, _ itemName: String, _ value: String) throws {
        guard let access = try createAccessControlRequiringBiometrics() else { throw KeychainError.biometricsUnsupported };

        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrService as String: service,
                                    kSecAttrAccessControl as String: access,
                                    kSecAttrAccount as String: itemName]

         let attributes: [String: Any] = [kSecValueData as String: value.data(using: .utf8)!]

        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)
        guard status != errSecItemNotFound else { throw KeychainError.noPassword }
        guard status == errSecSuccess else { throw KeychainError.unhandledError(status: status) }
    }

    func removeItemFromKeychain(_ service: String, _ key: String) throws {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword,
                                    kSecAttrService as String: service,
                                    kSecAttrAccount as String: key]

        let status = SecItemDelete(query as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else {
             throw KeychainError.unhandledError(status: status)
        }
    }
    
    func createAccessControlRequiringBiometrics() throws -> SecAccessControl? {
        var error: Unmanaged<CFError>?
        let access =
            SecAccessControlCreateWithFlags(nil,
                                            kSecAttrAccessibleWhenUnlocked,
                                            .userPresence,
                                            &error)

        if let error = error?.takeUnretainedValue() {
            throw error
        }

        return access
    }
}
