# capacitor-biometric-keychain

Uses Keychain and Keystore on ios and android respectively to give a secure localStorage like API that uses a biometric lock for read and update operations

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

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ key: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; error?: any; }&gt;</code>

--------------------


### setItem(...)

```typescript
setItem(options: { key: string; value: string; }) => Promise<{ error?: any; }>
```

| Param         | Type                                         |
| ------------- | -------------------------------------------- |
| **`options`** | <code>{ key: string; value: string; }</code> |

**Returns:** <code>Promise&lt;{ error?: any; }&gt;</code>

--------------------


### removeItem(...)

```typescript
removeItem(options: { key: string; }) => Promise<{ error?: any; }>
```

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ key: string; }</code> |

**Returns:** <code>Promise&lt;{ error?: any; }&gt;</code>

--------------------

</docgen-api>
