const path = require('node:path')
const { mkdir, copyFile } = require('node:fs/promises')

const copyJsonConfig = async () => {
  const source = path.join(
    process.cwd(),
    'nitrogen/generated/shared/json/GoogleSignInButtonConfig.json'
  )
  const destDir = path.join(
    process.cwd(),
    'lib/nitrogen/generated/shared/json'
  )
  const dest = path.join(destDir, 'GoogleSignInButtonConfig.json')

  try {
    await mkdir(destDir, { recursive: true })
    await copyFile(source, dest)
    console.log(`Successfully copied ${source} to ${dest}`)
  } catch (error) {
    console.error('Failed to copy GoogleSignInButtonConfig.json:', error)
    process.exit(1)
  }
}

copyJsonConfig()
