---
sidebar_position: 2
---

# Installation

Install **both** this package and **react-native-nitro-modules**.

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs groupId="package-manager">
<TabItem value="bun" label="Bun" default>

```bash
bun add react-native-nitro-google-signin react-native-nitro-modules
```

</TabItem>
<TabItem value="yarn" label="Yarn">

```bash
yarn add react-native-nitro-google-signin react-native-nitro-modules
```

</TabItem>
<TabItem value="npm" label="npm">

```bash
npm install react-native-nitro-google-signin react-native-nitro-modules
```

</TabItem>
</Tabs>

## Native linking

Autolinking handles Android and iOS. After install, from your **app project root**:

```bash
bundle exec pod install --project-directory="ios"
```

Run `bundle install` once first if your app has a `Gemfile` (included in the default React Native template).

Rebuild your app (Metro alone is not enough for native modules).

## Expo

Use a [development build](https://docs.expo.dev/develop/development-builds/introduction/) — this library does **not** run in Expo Go.

Follow [Expo setup](/docs/setup/expo) for the config plugin and `prebuild`.

## After install

1. Complete [Google Cloud & config files](/docs/setup/google-cloud).
2. Configure [Android](/docs/setup/android) and/or [iOS](/docs/setup/ios).
3. Call `GoogleOneTapSignIn.configure()` before any sign-in method — see [Quick Start](/docs/getting-started/quick-start).
