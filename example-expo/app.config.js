/** @type {import('expo/config').ExpoConfig} */
module.exports = {
  name: 'Nitro Google Sign-In (Expo)',
  slug: 'nitro-google-signin-expo',
  version: '1.0.0',
  orientation: 'portrait',
  icon: './assets/icon.png',
  userInterfaceStyle: 'light',
  scheme: 'nitrogooglesigninexpo',
  newArchEnabled: true,
  plugins: ['expo-dev-client', 'react-native-nitro-google-signin'],
  ios: {
    supportsTablet: true,
    bundleIdentifier: 'com.nitrogooglesigninexample',
    googleServicesFile: './GoogleService-Info.plist',
  },
  android: {
    package: 'com.nitrogooglesigninexample',
    googleServicesFile: './google-services.json',
    adaptiveIcon: {
      backgroundColor: '#E6F4FE',
      foregroundImage: './assets/android-icon-foreground.png',
      backgroundImage: './assets/android-icon-background.png',
      monochromeImage: './assets/android-icon-monochrome.png',
    },
  },
  web: {
    favicon: './assets/favicon.png',
  },
}
