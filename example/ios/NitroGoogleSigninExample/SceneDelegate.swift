import UIKit
import GoogleSignIn

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
  var window: UIWindow?

  func scene(
    _ scene: UIScene,
    willConnectTo session: UISceneSession,
    options connectionOptions: UIScene.ConnectionOptions
  ) {
    guard let windowScene = scene as? UIWindowScene else { return }

    let window = UIWindow(windowScene: windowScene)
    self.window = window
    (UIApplication.shared.delegate as? AppDelegate)?.window = window

    if let appDelegate = UIApplication.shared.delegate as? AppDelegate,
       let factory = appDelegate.reactNativeFactory {
      factory.startReactNative(
        withModuleName: "NitroGoogleSigninExample",
        in: window,
        launchOptions: appDelegate.launchOptions
      )
    }

    // Handle cold launch via URL redirect
    if let url = connectionOptions.urlContexts.first?.url {
      _ = GIDSignIn.sharedInstance.handle(url)
    }
  }

  func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
    // Handle URL redirect while app is running / foregrounded
    if let url = URLContexts.first?.url {
      _ = GIDSignIn.sharedInstance.handle(url)
    }
  }

  func sceneDidDisconnect(_ scene: UIScene) {}
  func sceneDidBecomeActive(_ scene: UIScene) {}
  func sceneWillResignActive(_ scene: UIScene) {}
  func sceneWillEnterForeground(_ scene: UIScene) {}
  func sceneDidEnterBackground(_ scene: UIScene) {}
}
