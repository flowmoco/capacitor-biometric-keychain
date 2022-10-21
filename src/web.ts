import { WebPlugin } from '@capacitor/core';

import type { BiometricNativePlugin } from './definitions';

export class BiometricNativeWeb
  extends WebPlugin
  implements BiometricNativePlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
