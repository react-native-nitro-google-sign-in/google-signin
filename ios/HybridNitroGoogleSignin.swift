import CryptoKit
import Foundation
import GoogleSignIn
import NitroModules
import UIKit

enum GoogleSignInNativeError: Error, LocalizedError {
  case playServicesNotAvailable(String)
  case oneTapStartFailed(String)
  case notConfigured
  case noActivity

  var errorDescription: String? {
    switch self {
    case .playServicesNotAvailable(let msg),
         .oneTapStartFailed(let msg):
      return msg
    case .notConfigured:
      return "Google Sign-In is not configured. Call configure() first."
    case .noActivity:
      return "No view controller available to present sign-in."
    }
  }

  var code: String {
    switch self {
    case .playServicesNotAvailable:
      return "PLAY_SERVICES_NOT_AVAILABLE"
    case .oneTapStartFailed:
      return "ONE_TAP_START_FAILED"
    case .notConfigured, .noActivity:
      return "IN_PROGRESS"
    }
  }
}

class HybridNitroGoogleSignin: HybridNitroGoogleSigninSpec {
  private var configured = false
  private var webClientId: String?
  private var offlineAccess = false
  private var configuredNonce: String?
  private var configuredScopes: [String] = []

  func configure(params: OneTapConfigureParams) throws {
    let resolvedWeb = try Self.resolveWebClientId(params.webClientId)
    webClientId = resolvedWeb

    let iosClientId = Self.variantToString(params.iosClientId)
      ?? Self.readPlistString(key: "CLIENT_ID")
    guard let iosClientId, !iosClientId.isEmpty else {
      throw GoogleSignInNativeError.notConfigured
    }

    offlineAccess = params.offlineAccess ?? false
    configuredNonce = Self.variantToString(params.nonce)
    configuredScopes = Self.variantToStringArray(params.scopes)

    let config = GIDConfiguration(
      clientID: iosClientId,
      serverClientID: resolvedWeb
    )
    GIDSignIn.sharedInstance.configuration = config
    configured = true
  }

  func checkPlayServices(showErrorResolutionDialog: Bool?) throws -> Promise<Void> {
    // Play Services are Android-only; always succeed on Apple platforms.
    return Promise.resolved()
  }

  func signIn() throws -> Promise<OneTapResponse> {
    try ensureConfigured()
    return Promise.async {
      if let user = GIDSignIn.sharedInstance.currentUser {
        return Self.success(from: user, serverAuthCode: nil)
      }
      return try await self.restorePreviousSignIn()
    }
  }

  func createAccount() throws -> Promise<OneTapResponse> {
    try ensureConfigured()
    return Promise.async {
      try await self.interactiveSignIn()
    }
  }

  func presentExplicitSignIn() throws -> Promise<OneTapResponse> {
    try ensureConfigured()
    return Promise.async {
      try await self.interactiveSignIn()
    }
  }

  func signOut() throws -> Promise<Void> {
    return Promise.async {
      GIDSignIn.sharedInstance.signOut()
    }
  }

  func revokeAccess(emailOrUniqueId: String) throws -> Promise<Void> {
    return Promise.async {
      try await withCheckedThrowingContinuation {
        (continuation: CheckedContinuation<Void, Error>) in
        GIDSignIn.sharedInstance.disconnect { error in
          if let error {
            continuation.resume(throwing: error)
          } else {
            continuation.resume()
          }
        }
      }
    }
  }

  func requestScopes(scopes: [String]) throws -> Promise<OneTapAuthorizationResult> {
    try ensureConfigured()
    return Promise.async {
      try await self.requestAdditionalScopes(scopes)
    }
  }

  // MARK: - Sign-in flows

  private func restorePreviousSignIn() async throws -> OneTapResponse {
    try await withCheckedThrowingContinuation { continuation in
      GIDSignIn.sharedInstance.restorePreviousSignIn { user, error in
        if let error = error as NSError? {
          if let response = Self.response(forSignInError: error) {
            continuation.resume(returning: response)
            return
          }
          continuation.resume(
            throwing: GoogleSignInNativeError.oneTapStartFailed(error.localizedDescription)
          )
          return
        }
        guard let user else {
          continuation.resume(returning: Self.noSavedCredential())
          return
        }
        continuation.resume(returning: Self.success(from: user, serverAuthCode: nil))
      }
    }
  }

  private func interactiveSignIn() async throws -> OneTapResponse {
    guard let presenting = Self.topViewController() else {
      throw GoogleSignInNativeError.noActivity
    }

    let additionalScopes = configuredScopes.isEmpty ? nil : configuredScopes
    let nonce = Self.resolveNonce(configuredNonce)

    return try await withCheckedThrowingContinuation { continuation in
      Self.signIn(
        presenting: presenting,
        additionalScopes: additionalScopes,
        nonce: nonce
      ) { result, error in
        if let error = error as NSError? {
          if let response = Self.response(forSignInError: error) {
            continuation.resume(returning: response)
            return
          }
          continuation.resume(
            throwing: GoogleSignInNativeError.oneTapStartFailed(error.localizedDescription)
          )
          return
        }
        guard let user = result?.user else {
          continuation.resume(returning: Self.cancelled())
          return
        }
        continuation.resume(
          returning: Self.success(from: user, serverAuthCode: result?.serverAuthCode)
        )
      }
    }
  }

  // MARK: - Mapping

  private static func success(from user: GIDGoogleUser, serverAuthCode: String?) -> OneTapResponse {
    let profile = user.profile
    let oneTapUser = OneTapUser(
      id: user.userID ?? "",
      email: optionalStringVariant(profile?.email),
      name: optionalStringVariant(profile?.name),
      givenName: optionalStringVariant(profile?.givenName),
      familyName: optionalStringVariant(profile?.familyName),
      photo: optionalStringVariant(profile?.imageURL(withDimension: 320)?.absoluteString)
    )
    let data = OneTapSuccessData(
      user: oneTapUser,
      idToken: user.idToken?.tokenString ?? "",
      serverAuthCode: optionalStringVariant(serverAuthCode)
    )
    return OneTapResponse(type: .success, data: .second(data))
  }

  private static func noSavedCredential() -> OneTapResponse {
    OneTapResponse(type: .nosavedcredentialfound, data: nil)
  }

  private static func cancelled() -> OneTapResponse {
    OneTapResponse(type: .cancelled, data: nil)
  }

  /// Maps known `GIDSignIn` errors to API responses. Returns `nil` if the error should be thrown.
  private static func response(forSignInError error: NSError) -> OneTapResponse? {
    switch error.code {
    case GIDSignInError.hasNoAuthInKeychain.rawValue:
      return noSavedCredential()
    case GIDSignInError.canceled.rawValue:
      return cancelled()
    case GIDSignInError.scopesAlreadyGranted.rawValue:
      guard let user = GIDSignIn.sharedInstance.currentUser else {
        return nil
      }
      return success(from: user, serverAuthCode: nil)
    default:
      return nil
    }
  }

  private static func optionalStringVariant(_ value: String?) -> Variant_NullType_String? {
    guard let value else {
      return .first(NullType.null)
    }
    return .second(value)
  }

  private static func variantToString(_ value: Variant_NullType_String?) -> String? {
    guard let value else { return nil }
    switch value {
    case .first:
      return nil
    case .second(let string):
      return string
    }
  }

  private static func variantToStringArray(_ value: Variant_NullType__String_?) -> [String] {
    guard let value else { return [] }
    switch value {
    case .first:
      return []
    case .second(let strings):
      return strings
    }
  }

  private func requestAdditionalScopes(_ scopes: [String]) async throws -> OneTapAuthorizationResult {
    guard let presenting = Self.topViewController() else {
      throw GoogleSignInNativeError.noActivity
    }
    guard let user = GIDSignIn.sharedInstance.currentUser else {
      throw GoogleSignInNativeError.oneTapStartFailed(
        "No signed-in Google user. Sign in before requesting additional scopes."
      )
    }

    return try await withCheckedThrowingContinuation { continuation in
      user.addScopes(scopes, presenting: presenting) { result, error in
        if let error = error as NSError? {
          if error.code == GIDSignInError.canceled.rawValue {
            continuation.resume(
              returning: OneTapAuthorizationResult(serverAuthCode: Self.optionalStringVariant(nil))
            )
            return
          }
          if error.code == GIDSignInError.scopesAlreadyGranted.rawValue {
            continuation.resume(
              returning: OneTapAuthorizationResult(
                serverAuthCode: Self.optionalStringVariant(result?.serverAuthCode)
              )
            )
            return
          }
          continuation.resume(
            throwing: GoogleSignInNativeError.oneTapStartFailed(error.localizedDescription)
          )
          return
        }
        continuation.resume(
          returning: OneTapAuthorizationResult(
            serverAuthCode: Self.optionalStringVariant(result?.serverAuthCode)
          )
        )
      }
    }
  }

  private static func resolveNonce(_ configured: String?) -> String {
    if let configured, !configured.isEmpty {
      return configured
    }
    return generateNonce()
  }

  private static func generateNonce() -> String {
    let raw = UUID().uuidString
    let digest = SHA256.hash(data: Data(raw.utf8))
    return digest.map { String(format: "%02x", $0) }.joined()
  }

  private static func signIn(
    presenting: UIViewController,
    additionalScopes: [String]?,
    nonce: String,
    completion: @escaping (GIDSignInResult?, Error?) -> Void
  ) {
    if let additionalScopes, !additionalScopes.isEmpty {
      GIDSignIn.sharedInstance.signIn(
        withPresenting: presenting,
        hint: nil,
        additionalScopes: additionalScopes,
        nonce: nonce,
        completion: completion
      )
    } else {
      GIDSignIn.sharedInstance.signIn(
        withPresenting: presenting,
        hint: nil,
        additionalScopes: nil,
        nonce: nonce,
        completion: completion
      )
    }
  }

  private func ensureConfigured() throws {
    guard configured, webClientId != nil else {
      throw GoogleSignInNativeError.notConfigured
    }
  }

  private static func resolveWebClientId(_ configuredId: String) throws -> String {
    if configuredId != "autoDetect" {
      return configuredId
    }
    if let fromPlist = readPlistString(key: "WEB_CLIENT_ID") {
      return fromPlist
    }
    throw GoogleSignInNativeError.notConfigured
  }

  private static func readPlistString(key: String) -> String? {
    guard
      let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
      let dict = NSDictionary(contentsOfFile: path) as? [String: Any],
      let value = dict[key] as? String
    else {
      return nil
    }
    return value
  }

  private static func topViewController() -> UIViewController? {
    let scenes = UIApplication.shared.connectedScenes
      .compactMap { $0 as? UIWindowScene }
    let window = scenes.flatMap(\.windows).first { $0.isKeyWindow }
    guard var top = window?.rootViewController else { return nil }
    while let presented = top.presentedViewController {
      top = presented
    }
    return top
  }
}
