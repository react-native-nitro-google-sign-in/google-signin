---
sidebar_position: 4
---

# Prompt templates

Install the skill first:

```bash
npx skills add react-native-nitro-google-signin/google-signin -g -y
```

Then use these prompts in any agent with the skill installed. Optional repo rules: [Rules & workflow](/docs/agents/rules).

## Install in bare React Native

```text
Add react-native-nitro-google-signin to this React Native app.

Use the react-native-nitro-google-signin skill.
webClientId: MY_WEB_CLIENT_ID.apps.googleusercontent.com
Implement the full One Tap flow with helpers.
```

## Install in Expo (dev client)

```text
Integrate react-native-nitro-google-signin in this Expo app.

Use the react-native-nitro-google-signin skill.
expo-dev-client, config plugin, google-services files, autoDetect.
Document prebuild and rebuild steps.
```

## Fix DEVELOPER_ERROR (Android)

```text
Google Sign-In fails on Android with DEVELOPER_ERROR.

Use the react-native-nitro-google-signin skill.
Check SHA-1, package name, and google-services setup.
```

## Add native sign-in button

```text
Add GoogleSignInButton from react-native-nitro-google-signin.

Use the react-native-nitro-google-signin skill.
credentialManager behavior, 48dp height.
```

## Audit existing integration

```text
Audit my Google Sign-In integration against react-native-nitro-google-signin.

Use the react-native-nitro-google-signin skill.
List gaps and fix them.
```
