import { WebPlugin } from '@capacitor/core';

import type { BiometricNativePlugin } from './definitions';

export class BiometricNativeWeb
  extends WebPlugin
  implements BiometricNativePlugin
{
  getItem(): Promise<{ value: string; error?: string | undefined; }> {
    throw new Error('Method not implemented.');
  }
  setItem(): Promise<{ successful: boolean; error?: string | undefined; }> {
    throw new Error('Method not implemented.');
  }
  removeItem(): Promise<{ successful: boolean; error?: string | undefined; }> {
    throw new Error('Method not implemented.');
  }
}
