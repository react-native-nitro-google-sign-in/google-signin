import React, { useState, Fragment } from 'react';
import {
  ActivityIndicator,
  Image,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {
  GoogleOneTapSignIn,
  isNoSavedCredentialFoundResponse,
  isSuccessResponse,
  OneTapSuccessData,
} from 'react-native-nitro-google-signin';

/** Example scope — enable the Calendar API in Google Cloud for your project. */
const CALENDAR_READONLY_SCOPE =
  'https://www.googleapis.com/auth/calendar.readonly';

function App(): React.JSX.Element {
  const [status, setStatus] = useState<string>('Tap to sign in');
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<OneTapSuccessData | null>(null);
  const [extraScopesStatus, setExtraScopesStatus] = useState<string | null>(
    null,
  );

  const startSignInFlow = async () => {
    setLoading(true);
    setStatus('Signing in…');
    try {
      GoogleOneTapSignIn.configure({
        webClientId:
          '662359549373-tr50ibd41c85pcb6mefk76ouaocooc6u.apps.googleusercontent.com',
        // iosClientId: 'YOUR_IOS_CLIENT_ID.apps.googleusercontent.com',
      });
      await GoogleOneTapSignIn.checkPlayServices();
      let response = await GoogleOneTapSignIn.signIn();
      console.log('response signIn', response);

      if (isNoSavedCredentialFoundResponse(response)) {
        response = await GoogleOneTapSignIn.createAccount();
        console.log('response createAccount', response);
      }
      if (isNoSavedCredentialFoundResponse(response)) {
        response = await GoogleOneTapSignIn.presentExplicitSignIn();
        console.log('response presentExplicitSignIn', response);
      }

      if (isSuccessResponse(response)) {
        setStatus(
          `Signed in as ${response.data.user.email ?? response.data.user.id}`,
        );
        console.log('response.data', response.data);
        setUser(response.data);
      } else {
        setStatus('Sign-in cancelled');
      }
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Sign-in failed');
    } finally {
      setLoading(false);
    }
  };

  const requestAdditionalScopes = async () => {
    if (!user) {
      setExtraScopesStatus('Sign in first, then request additional scopes.');
      return;
    }

    setLoading(true);
    setExtraScopesStatus('Requesting calendar read access…');
    try {
      const result = await GoogleOneTapSignIn.requestScopes([
        CALENDAR_READONLY_SCOPE,
      ]);

      if (result.serverAuthCode) {
        setExtraScopesStatus(
          `Scope granted. Server auth code received (${result.serverAuthCode.slice(0, 12)}…).`,
        );
      } else {
        setExtraScopesStatus(
          'Scope granted (or already granted). No new server auth code.',
        );
      }
    } catch (e) {
      setExtraScopesStatus(
        e instanceof Error ? e.message : 'Failed to request scopes',
      );
    } finally {
      setLoading(false);
    }
  };

  const signOut = async () => {
    try {
      setLoading(true);
      setStatus('Signing out…');
      await GoogleOneTapSignIn.signOut();
      setUser(null);
      setExtraScopesStatus(null);
      setStatus('Signed out');
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'Sign out failed');
    } finally {
      setLoading(false);
    }
  };

  const isSignedIn = user != null;

  return (
    <View style={styles.container}>
      {loading ? <ActivityIndicator size="large" /> : null}
      {/* show whole returned user details */}
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
        <Pressable
          style={styles.button}
          onPress={startSignInFlow}
          disabled={loading}
        >
          <Text style={styles.buttonLabel}>Sign in with Google</Text>
        </Pressable>
      ) : (
        <Fragment>
          <Pressable
            style={[styles.button, styles.secondaryButton]}
            onPress={requestAdditionalScopes}
            disabled={loading}
          >
            <Text style={styles.buttonLabel}>Request calendar read access</Text>
          </Pressable>
          <Pressable style={styles.button} onPress={signOut} disabled={loading}>
            <Text style={styles.buttonLabel}>Sign out</Text>
          </Pressable>
        </Fragment>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
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
  button: {
    backgroundColor: '#4285F4',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
  },
  secondaryButton: {
    backgroundColor: '#34A853',
  },
  buttonLabel: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  photo: {
    width: 100,
    height: 100,
    borderRadius: 50,
  },
});

export default App;
