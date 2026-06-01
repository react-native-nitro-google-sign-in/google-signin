import clsx from 'clsx'
import Link from '@docusaurus/Link'
import useDocusaurusContext from '@docusaurus/useDocusaurusContext'
import Layout from '@theme/Layout'
import HomepageFeatures from '@site/src/components/HomepageFeatures'
import Heading from '@theme/Heading'
import styles from './index.module.css'

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext()
  return (
    <header className={styles.heroBanner}>
      <div className={clsx('container', styles.heroInner)}>
        <img
          className={styles.heroLogo}
          src={`${siteConfig.baseUrl}img/logo.svg`}
          alt=""
          width={72}
          height={72}
        />
        <Heading as="h1" className={styles.heroTitle}>
          {siteConfig.title}
        </Heading>
        <p className={styles.heroSubtitle}>{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link
            className={clsx('button button--lg', styles.btnPrimary)}
            to="/docs/getting-started/quick-start"
          >
            Quick Start →
          </Link>
          <Link
            className={clsx('button button--lg', styles.btnOutline)}
            to="/docs/getting-started/installation"
          >
            Installation
          </Link>
        </div>
      </div>
    </header>
  )
}

export default function Home(): React.JSX.Element {
  return (
    <Layout
      title="Home"
      description="Nitro-powered Universal Google Sign-In for React Native — Android, iOS, and Expo dev builds."
    >
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  )
}
