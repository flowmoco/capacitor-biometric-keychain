# capacitor-biometric-keychain

Uses Keychain and Keystore on ios and android respectively to give a secure localStorage like API that uses a biometric lock for read and update operations

Capacitor v4 and above
Android minSdkVersion 23
iOS min version 13.0

## Install

```bash
npm install capacitor-biometric-keychain
npx cap sync
```

## API

<docgen-index>

* [`getItem(...)`](#getitem)
* [`setItem(...)`](#setitem)
* [`removeItem(...)`](#removeitem)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getItem(...)

```typescript
getItem(options: { key: string; }) => Promise<{ value: string; error?: any; }>
```

Async get an item from the secure storage. Will invoke device biometric authentication.

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ key: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; error?: any; }&gt;</code>

--------------------


### setItem(...)

```typescript
setItem(options: { key: string; value: string; }) => Promise<{ error?: any; }>
```

Async set an item in secure storage. Will invoke device biometric authentication on Android and only on iOS if overwriting an existing key.

| Param         | Type                                         |
| ------------- | -------------------------------------------- |
| **`options`** | <code>{ key: string; value: string; }</code> |

**Returns:** <code>Promise&lt;{ error?: any; }&gt;</code>

--------------------


### removeItem(...)

```typescript
removeItem(options: { key: string; }) => Promise<{ error?: any; }>
```

Async remove an item from the secure storage. Will not invoke device biometric authentication either platform.

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ key: string; }</code> |

**Returns:** <code>Promise&lt;{ error?: any; }&gt;</code>

--------------------

</docgen-api>
