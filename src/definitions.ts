export interface BiometricNativePlugin {
  getItem(options: { key: string }): Promise<{ value: string, error?: any }>;
  setItem(options: { key: string, value: string}): Promise<{ error?: any }>;
  removeItem(options: { key: string }): Promise<{ error?: any }>;
}
