import clsx from 'clsx'
import Link from '@docusaurus/Link'
import Heading from '@theme/Heading'
import styles from './styles.module.css'

type FeatureItem = {
  title: string
  icon: string
  description: string
  to: string
}

const FeatureList: FeatureItem[] = [
  {
    title: 'Universal One Tap API',
    icon: '🎯',
    description:
      'Same flow as the licensed @react-native-google-signin/google-signin One Tap API — configure, signIn, createAccount, and presentExplicitSignIn.',
    to: '/docs/guide/usage',
  },
  {
    title: 'Fully native (Nitro + JSI)',
    icon: '⚡',
    description:
      'Android Credential Manager and iOS Google Sign-In SDK, bridged through Nitro Modules for direct JS-to-native calls.',
    to: '/docs/getting-started/requirements',
  },
  {
    title: 'Expo development builds',
    icon: '🧩',
    description:
      'Config plugin applies Google Services on Android, URL schemes on iOS, and copies Firebase config files when you use them.',
    to: '/docs/setup/expo',
  },
  {
    title: 'Official sign-in button',
    icon: '🔘',
    description:
      'Native GIDSignInButton / SignInButton as a Nitro HybridView with optional Credential Manager tap handling.',
    to: '/docs/guide/google-sign-in-button',
  },
  {
    title: 'TypeScript ready',
    icon: '📘',
    description:
      'Typed responses, status codes, and helpers: isSuccessResponse, isNoSavedCredentialFoundResponse, isCancelledResponse.',
    to: '/docs/guide/api-reference',
  },
  {
    title: 'New Architecture',
    icon: '🏗️',
    description:
      'Built for React Native 0.76+ with react-native-nitro-modules. Works in bare RN and Expo dev clients.',
    to: '/docs/getting-started/quick-start',
  },
]

function Feature({
  title,
  icon,
  description,
  to,
  index,
}: FeatureItem & { index: number }) {
  return (
    <div
      className={clsx('col col--4 margin-bottom--lg', styles.featureCol)}
      style={{ animationDelay: `${index * 80}ms` }}
    >
      <Link to={to} className={styles.featureCardShell}>
        <span className={styles.borderSpin} aria-hidden />
        <div className={styles.featureCard}>
          <div className={styles.featureGlow} aria-hidden />
          <div className={styles.featureIcon} aria-hidden>
            {icon}
          </div>
          <Heading as="h3" className={styles.featureTitle}>
            {title}
          </Heading>
          <p className={styles.featureDesc}>{description}</p>
          <span className={styles.featureCta}>Learn more →</span>
        </div>
      </Link>
    </div>
  )
}

export default function HomepageFeatures(): React.JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={styles.row}>
          {FeatureList.map((props, index) => (
            <Feature key={props.title} {...props} index={index} />
          ))}
        </div>
      </div>
    </section>
  )
}
