import { registerPlugin } from '@capacitor/core';

import type { BiometricNativePlugin } from './definitions';

const BiometricNative = registerPlugin<BiometricNativePlugin>(
  'BiometricNative',
  {
    web: () => import('./web').then(m => new m.BiometricNativeWeb()),
  },
);

export * from './definitions';
export { BiometricNative };
