/// <reference types="@capacitor/cli" />

declare module '@capacitor/cli' {
  export interface PluginsConfig {
    BiometricNative?: {
      /**
       * Constant string set up for identifying the keychain stored items
       *
       * @since 3.0.0
       * @example "APP_SERVICE"
       */
      iosService?: string;
    };
  }
}

export interface BiometricNativePlugin {
  /**
   * Async get an item from the secure storage. Will invoke device biometric authentication.
   * @param {{ key: string }} options
   * @returns {Promise<{ value: string; error?: any; }>}
   */
  getItem(options: { key: string }): Promise<{ value: string; error?: any; }>;
  /**
   * Async set an item in secure storage. Will invoke device biometric authentication on Android and only on iOS if overwriting an existing key.
   * @param {{ key: string; value: string }} options
   * @returns {Promise<{ error?: any }>}
   */
  setItem(options: { key: string; value: string }): Promise<{ error?: any }>;
  /**
   * Async remove an item from the secure storage. Will not invoke device biometric authentication either platform.
   * @param {{ key: string }} options
   * @returns {Promise<{ error?: any }>}
   */
  removeItem(options: { key: string }): Promise<{ error?: any }>;
}
