export interface BiometricNativePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  getItem(key: string): Promise<{ value: string, error?: string }>;
  setItem(key: string, value: string): Promise<{ successful: boolean, error?: string }>;
  removeItem(key: string): Promise<{ successful: boolean, error?: string }>;
}
