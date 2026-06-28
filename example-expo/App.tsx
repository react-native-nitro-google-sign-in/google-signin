import { StatusBar } from 'expo-status-bar'
import React, { Fragment, useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import {
  GoogleOneTapSignIn,
  GoogleSignInButton,
  isCancelledResponse,
  isErrorWithCode,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
  statusCodes,
  type OneTapSuccessData,
} from 'react-native-nitro-google-signin'

const DRIVE_FULL_SCOPE = 'https://www.googleapis.com/auth/drive'

const WEB_CLIENT_ID = process.env.EXPO_PUBLIC_WEB_CLIENT_ID ?? ''

function formatGoogleSignInError(error: unknown, fallback: string): string {
  if (isErrorWithCode(error)) {
    return `${error.code}: ${error.message}`
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}

function formatSignInResponse(
  response: Awaited<ReturnType<typeof GoogleOneTapSignIn.createAccount>>
): string {
  if (isCancelledResponse(response)) {
    return `${statusCodes.SIGN_IN_CANCELLED}: Sign-in cancelled`
  }
  if (isNoSavedCredentialFoundResponse(response)) {
    return 'No saved Google account found'
  }
  return 'Sign-in did not complete'
}

export default function App() {
  const [status, setStatus] = useState('Sign in below (Expo dev build)')
  const [loading, setLoading] = useState(false)
  const [user, setUser] = useState<OneTapSuccessData | null>(null)
  const [extraScopesStatus, setExtraScopesStatus] = useState<string | null>(
    null
  )

  useEffect(() => {
    GoogleOneTapSignIn.configure({
      webClientId: WEB_CLIENT_ID,
    })
  }, [])

  const onSignInSuccess = (data: OneTapSuccessData) => {
    setUser(data)
    setStatus(`Signed in as ${data.user.email ?? data.user.id}`)
  }

  const onSignInError = (e: unknown) => {
    setStatus(formatGoogleSignInError(e, 'Sign-in failed'))
  }

  const chooseAnotherAccount = async () => {
    setLoading(true)
    setExtraScopesStatus(null)
    setStatus('Choose a Google account…')
    try {
      await GoogleOneTapSignIn.checkPlayServices()
      let response = await GoogleOneTapSignIn.createAccount()
      if (isNoSavedCredentialFoundResponse(response)) {
        response = await GoogleOneTapSignIn.presentExplicitSignIn()
      }
      if (isSuccessResponse(response)) {
        onSignInSuccess(response.data)
      } else {
        setStatus(formatSignInResponse(response))
      }
    } catch (e) {
      setStatus(formatGoogleSignInError(e, 'Account selection failed'))
    } finally {
      setLoading(false)
    }
  }

  const requestAdditionalScopes = async () => {
    if (!user) {
      setExtraScopesStatus('Sign in first, then request additional scopes.')
      return
    }

    setLoading(true)
    setExtraScopesStatus('Requesting Drive full access…')
    try {
      const result = await GoogleOneTapSignIn.requestScopes([DRIVE_FULL_SCOPE])
      if (result.serverAuthCode) {
        setExtraScopesStatus(
          `Scope granted. Server auth code received (${result.serverAuthCode.slice(0, 12)}…).`
        )
      } else {
        setExtraScopesStatus(
          'Scope granted (or already granted). No new server auth code.'
        )
      }
    } catch (e) {
      setExtraScopesStatus(
        formatGoogleSignInError(e, 'Failed to request scopes')
      )
    } finally {
      setLoading(false)
    }
  }

  const signOut = async () => {
    try {
      setLoading(true)
      setStatus('Signing out…')
      await GoogleOneTapSignIn.signOut()
      setUser(null)
      setExtraScopesStatus(null)
      setStatus('Signed out')
    } catch (e) {
      setStatus(formatGoogleSignInError(e, 'Sign out failed'))
    } finally {
      setLoading(false)
    }
  }

  const revokeAccess = async () => {
    try {
      setLoading(true)
      setStatus('Revoking access…')
      await GoogleOneTapSignIn.revokeAccess(user?.user.id ?? '')
      setUser(null)
      setExtraScopesStatus(null)
      setStatus('Access revoked')
    } catch (e) {
      setStatus(formatGoogleSignInError(e, 'Revoke access failed'))
    } finally {
      setLoading(false)
    }
  }

  const isSignedIn = user != null

  return (
    <View style={styles.root}>
      <StatusBar style="auto" />
      <ScrollView
        contentContainerStyle={styles.scroll}
        showsVerticalScrollIndicator={false}
      >
        {loading ? <ActivityIndicator size="large" /> : null}
        <Text style={styles.status}>{status}</Text>

        {user ? (
          <Fragment>
            <Text style={styles.status}>{`Name: ${user.user.name}`}</Text>
            <Text style={styles.status}>{`Email: ${user.user.email}`}</Text>
            <Image
              source={user.user.photo ? { uri: user.user.photo } : undefined}
              style={styles.photo}
            />
          </Fragment>
        ) : null}

        {extraScopesStatus ? (
          <Text style={styles.hint}>{extraScopesStatus}</Text>
        ) : null}

        {!isSignedIn ? (
          <View style={styles.signInButtonContainer} collapsable={false}>
            <GoogleSignInButton
              colorScheme="dark"
              size="wide"
              contentAlignment="center"
              disabled={loading}
              onSignInSuccess={onSignInSuccess}
              onSignInError={onSignInError}
              style={styles.signInButton}
            />
          </View>
        ) : (
          <View style={styles.actions}>
            <Text
              accessibilityRole="button"
              onPress={loading ? undefined : chooseAnotherAccount}
              style={[styles.action, loading && styles.actionDisabled]}
            >
              Choose another account
            </Text>
            <Text
              accessibilityRole="button"
              onPress={loading ? undefined : requestAdditionalScopes}
              style={[styles.action, loading && styles.actionDisabled]}
            >
              Request drive full access
            </Text>
            <Text
              accessibilityRole="button"
              onPress={loading ? undefined : signOut}
              style={[styles.action, loading && styles.actionDisabled]}
            >
              Sign out
            </Text>
            <Text
              accessibilityRole="button"
              onPress={loading ? undefined : revokeAccess}
              style={[styles.action, loading && styles.actionDisabled]}
            >
              Revoke access
            </Text>
          </View>
        )}
      </ScrollView>
    </View>
  )
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#fff',
  },
  scroll: {
    flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
    gap: 16,
  },
  status: {
    fontSize: 16,
    textAlign: 'center',
  },
  hint: {
    fontSize: 14,
    textAlign: 'center',
    color: '#555',
    paddingHorizontal: 8,
  },
  signInButtonContainer: {
    width: 312,
    height: 48,
    alignSelf: 'center',
  },
  signInButton: {
    width: 312,
    height: 48,
    flexShrink: 0,
  },
  actions: {
    gap: 12,
    alignItems: 'center',
  },
  action: {
    color: '#1A73E8',
    fontSize: 16,
    fontWeight: '500',
    paddingVertical: 8,
  },
  actionDisabled: {
    opacity: 0.5,
  },
  photo: {
    width: 100,
    height: 100,
    borderRadius: 50,
  },
})
