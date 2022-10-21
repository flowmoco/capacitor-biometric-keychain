export interface BiometricNativePlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
