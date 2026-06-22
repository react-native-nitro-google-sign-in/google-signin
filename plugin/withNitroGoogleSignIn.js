const {
  AndroidConfig,
  IOSConfig,
  createRunOncePlugin,
  withPlugins,
  withInfoPlist,
  withPodfile,
} = require('@expo/config-plugins')
const { mergeContents } = require('@expo/config-plugins/build/utils/generateCode')

const pkg = require('../package.json')

/**
 * @typedef {import('@expo/config-plugins').ExpoConfig} ExpoConfig
 * @typedef {import('@expo/config-plugins').ConfigPlugin} ConfigPlugin
 */

/**
 * @typedef {object} NitroGoogleSignInPluginOptions
 * @property {string} [iosUrlScheme] Reversed iOS client ID (`REVERSED_CLIENT_ID`), e.g. `com.googleusercontent.apps.123-abc`.
 * @property {string} [iosGoogleServicesFile] Path to `GoogleService-Info.plist` (alternative to `expo.ios.googleServicesFile`).
 * @property {string} [androidGoogleServicesFile] Path to `google-services.json` (alternative to `expo.android.googleServicesFile`).
 */

/**
 * @param {NitroGoogleSignInPluginOptions | undefined} options
 */
function validateWithoutFirebaseOptions(options) {
  const prefix = 'react-native-nitro-google-signin config plugin'
  if (!options?.iosUrlScheme) {
    throw new Error(
      `${prefix}: Missing \`iosUrlScheme\`. Provide it in plugin options, or set ` +
        '`expo.ios.googleServicesFile` / `iosGoogleServicesFile` for Firebase-style setup.',
    )
  }
  if (!options.iosUrlScheme.startsWith('com.googleusercontent.apps.')) {
    throw new Error(
      `${prefix}: \`iosUrlScheme\` must start with "com.googleusercontent.apps.": ` +
        JSON.stringify(options),
    )
  }
}

/** @type {ConfigPlugin<NitroGoogleSignInPluginOptions>} */
const withGoogleUrlScheme = (config, options) => {
  return withInfoPlist(config, (config) => {
    const scheme = options.iosUrlScheme
    const infoPlist = config.modResults
    if (!IOSConfig.Scheme.hasScheme(scheme, infoPlist)) {
      config.modResults = IOSConfig.Scheme.appendScheme(scheme, infoPlist)
    }
    return config
  })
}

/** @type {ConfigPlugin<NitroGoogleSignInPluginOptions>} */
const withNitroGoogleSignInWithoutFirebase = (config, options) => {
  validateWithoutFirebaseOptions(options)
  return withPlugins(config, [(cfg) => withGoogleUrlScheme(cfg, options)])
}

/** @type {ConfigPlugin<NitroGoogleSignInPluginOptions>} */
const withGoogleServicesFilePaths = (config, options) => {
  if (options.iosGoogleServicesFile) {
    config.ios = {
      ...config.ios,
      googleServicesFile: options.iosGoogleServicesFile,
    }
  }
  if (options.androidGoogleServicesFile) {
    config.android = {
      ...config.android,
      googleServicesFile: options.androidGoogleServicesFile,
    }
  }
  return config
}

const GOOGLE_SIGN_IN_PODFILE_TAG = 'react-native-nitro-google-signin-google-pods'

/** Pods required for static CocoaPods / Expo 56 when GoogleSignIn pulls AppCheckCore. */
const GOOGLE_SIGN_IN_PODFILE_PODS = `  pod 'AppCheckCore', '< 11.3.0', :modular_headers => true
  pod 'GoogleUtilities', :modular_headers => true
  pod 'RecaptchaInterop', :modular_headers => true`

/**
 * @param {string} src
 */
function addGoogleSignInCocoaPods(src) {
  return mergeContents({
    tag: GOOGLE_SIGN_IN_PODFILE_TAG,
    src,
    newSrc: GOOGLE_SIGN_IN_PODFILE_PODS,
    anchor: /use_native_modules/,
    offset: 0,
    comment: '#',
  })
}

/** @type {ConfigPlugin} */
const withGoogleSignInCocoaPods = (config) => {
  return withPodfile(config, (config) => {
    let results
    try {
      results = addGoogleSignInCocoaPods(config.modResults.contents)
    } catch (error) {
      if (/** @type {NodeJS.ErrnoException} */ (error).code === 'ERR_NO_MATCH') {
        throw new Error(
          'react-native-nitro-google-signin config plugin: could not patch ios/Podfile. ' +
            'Ensure the Podfile contains use_native_modules! inside the app target.',
        )
      }
      throw error
    }
    if (results.didMerge || results.didClear) {
      config.modResults.contents = results.contents
    }
    return config
  })
}

/** Firebase / Google Services: copies plist & json, adds Android plugin, iOS URL scheme from REVERSED_CLIENT_ID. */
/** @type {ConfigPlugin} */
const withNitroGoogleSignInFirebase = (config) => {
  return withPlugins(config, [
    AndroidConfig.GoogleServices.withClassPath,
    AndroidConfig.GoogleServices.withApplyPlugin,
    AndroidConfig.GoogleServices.withGoogleServicesFile,
    IOSConfig.Google.withGoogle,
    IOSConfig.Google.withGoogleServicesFile,
  ])
}

/**
 * @param {ExpoConfig} config
 * @param {NitroGoogleSignInPluginOptions | undefined} options
 */
const withNitroGoogleSignInRoot = (config, options) => {
  let configWithPlugin
  if (options?.iosUrlScheme) {
    configWithPlugin = withNitroGoogleSignInWithoutFirebase(config, options)
  } else {
    const configWithPaths = withGoogleServicesFilePaths(config, options ?? {})
    const hasFirebaseFiles =
      configWithPaths.ios?.googleServicesFile != null ||
      configWithPaths.android?.googleServicesFile != null

    if (hasFirebaseFiles) {
      configWithPlugin = withNitroGoogleSignInFirebase(configWithPaths)
    } else {
      throw new Error(
        'react-native-nitro-google-signin config plugin: configure either:\n' +
          '  • Firebase: set expo.ios.googleServicesFile & expo.android.googleServicesFile, or pass iosGoogleServicesFile / androidGoogleServicesFile in plugin options\n' +
          '  • Without Firebase: pass iosUrlScheme (REVERSED_CLIENT_ID) in plugin options',
      )
    }
  }

  return withGoogleSignInCocoaPods(configWithPlugin)
}

module.exports = createRunOncePlugin(
  withNitroGoogleSignInRoot,
  pkg.name,
  pkg.version,
)
