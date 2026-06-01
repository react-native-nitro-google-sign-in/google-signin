#!/usr/bin/env node
/**
 * Copy demo videos from repo assets/ into docs/static/video/ before Docusaurus build.
 * Run locally: node docs/scripts/sync-media.mjs
 */
import { copyFileSync, mkdirSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '../..')

const copies = [
  ['assets/google-signin-android.webm', 'docs/static/video/google-signin-android.webm'],
  ['assets/google-signin-iOS.mov', 'docs/static/video/google-signin-ios.mov'],
]

mkdirSync(join(root, 'docs/static/video'), { recursive: true })

for (const [from, to] of copies) {
  const src = join(root, from)
  const dest = join(root, to)
  copyFileSync(src, dest)
  console.log(`sync-media: ${from} → ${to}`)
}
