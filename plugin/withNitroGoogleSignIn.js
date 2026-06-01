const {
  AndroidConfig,
  IOSConfig,
  createRunOncePlugin,
  withPlugins,
  withInfoPlist,
} = require('@expo/config-plugins')

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
  if (options?.iosUrlScheme) {
    return withNitroGoogleSignInWithoutFirebase(config, options)
  }

  const configWithPaths = withGoogleServicesFilePaths(config, options ?? {})
  const hasFirebaseFiles =
    configWithPaths.ios?.googleServicesFile != null ||
    configWithPaths.android?.googleServicesFile != null

  if (hasFirebaseFiles) {
    return withNitroGoogleSignInFirebase(configWithPaths)
  }

  throw new Error(
    'react-native-nitro-google-signin config plugin: configure either:\n' +
      '  • Firebase: set expo.ios.googleServicesFile & expo.android.googleServicesFile, or pass iosGoogleServicesFile / androidGoogleServicesFile in plugin options\n' +
      '  • Without Firebase: pass iosUrlScheme (REVERSED_CLIENT_ID) in plugin options',
  )
}

module.exports = createRunOncePlugin(
  withNitroGoogleSignInRoot,
  pkg.name,
  pkg.version,
)
