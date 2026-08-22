'use strict'

const fs = require('node:fs')
const path = require('node:path')
const semverSatisfies = require('semver/functions/satisfies')
const semverPrerelease = require('semver/functions/prerelease')

const packageRoot = path.resolve(__dirname, '..')
const packageJson = require(path.join(packageRoot, 'package.json'))
const compatibilityFile = require(path.join(packageRoot, 'compatibility.json'))

const libraryVersion = packageJson.version
const reactNativeVersion = process.argv[2] ?? process.env.RN_VERSION
const nitroModulesVersion = process.argv[3] ?? process.env.NITRO_MODULES_VERSION

function readDependencyVersion(name) {
  let current = process.cwd()
  while (true) {
    const candidate = path.join(current, 'node_modules', name, 'package.json')
    if (fs.existsSync(candidate)) {
      return require(candidate).version
    }
    const parent = path.dirname(current)
    if (parent === current) {
      return null
    }
    current = parent
  }
}

function resolveVersion(explicit, packageName) {
  if (explicit) {
    return explicit
  }
  return readDependencyVersion(packageName)
}

function isSupported(libraryRange, dependencyName, dependencyVersion) {
  if (!dependencyVersion) {
    return true
  }

  if (semverPrerelease(dependencyVersion)) {
    return true
  }

  const supportedVersions = []

  for (const key in compatibilityFile) {
    if (!semverSatisfies(libraryVersion, key)) {
      continue
    }

    supportedVersions.push(...compatibilityFile[key][dependencyName])
  }

  if (supportedVersions.length === 0) {
    return true
  }

  return supportedVersions.some((version) =>
    semverSatisfies(dependencyVersion, version.endsWith('.x') ? version : `${version}.x`)
  )
}

function main() {
  const resolvedReactNativeVersion = resolveVersion(
    reactNativeVersion,
    'react-native'
  )
  const resolvedNitroModulesVersion = resolveVersion(
    nitroModulesVersion,
    'react-native-nitro-modules'
  )

  const docsUrl =
    'https://react-native-nitro-google-sign-in.github.io/docs/getting-started/compatibility'

  if (
    resolvedReactNativeVersion &&
    !isSupported(
      libraryVersion,
      'react-native',
      resolvedReactNativeVersion
    )
  ) {
    console.error(
      `[react-native-nitro-google-signin] React Native ${resolvedReactNativeVersion} is not compatible with ${libraryVersion}. See ${docsUrl}`
    )
    process.exit(1)
  }

  if (
    resolvedNitroModulesVersion &&
    !isSupported(
      libraryVersion,
      'react-native-nitro-modules',
      resolvedNitroModulesVersion
    )
  ) {
    console.error(
      `[react-native-nitro-google-signin] react-native-nitro-modules ${resolvedNitroModulesVersion} is not compatible with ${libraryVersion}. See ${docsUrl}`
    )
    process.exit(1)
  }
}

main()
