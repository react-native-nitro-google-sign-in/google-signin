import GoogleSignIn
import NitroModules
import UIKit

private final class GoogleSignInButtonContainer: UIView {
  let signInButton = GIDSignInButton()
  var onPress: () -> Void = {}

  var buttonStyle: GIDSignInButtonStyle = .standard {
    didSet {
      signInButton.style = buttonStyle
      invalidateIntrinsicContentSize()
    }
  }

  var contentAlignment: GoogleSignInButtonContentAlignment = .center {
    didSet {
      applyContentAlignment()
    }
  }

  private var horizontalConstraint: NSLayoutConstraint?

  override var intrinsicContentSize: CGSize {
    switch buttonStyle {
    case .wide:
      return CGSize(width: 312, height: 48)
    case .iconOnly:
      return CGSize(width: 48, height: 48)
    default:
      return CGSize(width: 230, height: 48)
    }
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
    signInButton.translatesAutoresizingMaskIntoConstraints = false
    signInButton.addTarget(self, action: #selector(handlePress), for: .touchUpInside)
    addSubview(signInButton)
    NSLayoutConstraint.activate([
      signInButton.centerYAnchor.constraint(equalTo: centerYAnchor),
      signInButton.widthAnchor.constraint(lessThanOrEqualTo: widthAnchor),
      signInButton.heightAnchor.constraint(lessThanOrEqualTo: heightAnchor),
      signInButton.heightAnchor.constraint(equalToConstant: 48),
    ])
    applyContentAlignment()
  }

  @available(*, unavailable)
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func applyContentAlignment() {
    horizontalConstraint?.isActive = false
    let constraint: NSLayoutConstraint
    switch contentAlignment {
    case .leading:
      constraint = signInButton.leadingAnchor.constraint(equalTo: leadingAnchor)
    case .trailing:
      constraint = signInButton.trailingAnchor.constraint(equalTo: trailingAnchor)
    case .center:
      constraint = signInButton.centerXAnchor.constraint(equalTo: centerXAnchor)
    }
    horizontalConstraint = constraint
    constraint.isActive = true
  }

  @objc private func handlePress() {
    guard signInButton.isEnabled else { return }
    onPress()
  }
}

class HybridGoogleSignInButton: HybridGoogleSignInButtonSpec {
  private let container = GoogleSignInButtonContainer()

  var view: UIView {
    container
  }

  override init() {
    super.init()
    container.onPress = { [weak self] in
      self?.onPress()
    }
    applyColorScheme()
    applySize()
    applyContentAlignment()
    applyDisabled()
  }

  var colorScheme: GoogleSignInButtonColorScheme = .light {
    didSet { applyColorScheme() }
  }

  var size: GoogleSignInButtonNativeSize = .standard {
    didSet { applySize() }
  }

  var contentAlignment: GoogleSignInButtonContentAlignment? = .center {
    didSet { applyContentAlignment() }
  }

  var disabled: Bool = false {
    didSet { applyDisabled() }
  }

  var onPress: () -> Void = {} {
    didSet { container.onPress = onPress }
  }

  private func applyColorScheme() {
    switch colorScheme {
    case .dark:
      container.signInButton.colorScheme = .dark
    case .light:
      container.signInButton.colorScheme = .light
    }
  }

  private func applySize() {
    switch size {
    case .wide:
      container.buttonStyle = .wide
    case .icon:
      container.buttonStyle = .iconOnly
    case .standard:
      container.buttonStyle = .standard
    }
  }

  private func applyContentAlignment() {
    container.contentAlignment = contentAlignment ?? .center
  }

  private func applyDisabled() {
    container.signInButton.isEnabled = !disabled
    container.isUserInteractionEnabled = !disabled
    container.signInButton.alpha = disabled ? 0.55 : 1
  }
}
