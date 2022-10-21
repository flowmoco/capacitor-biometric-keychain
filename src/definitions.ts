export interface BiometricNativePlugin {
  getItem(key: string): Promise<{ value: string, error?: any }>;
  setItem(key: string, value: string): Promise<{ error?: any }>;
  removeItem(key: string): Promise<{ error?: any }>;
}
