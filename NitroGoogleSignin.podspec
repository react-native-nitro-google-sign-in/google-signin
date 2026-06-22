require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "NitroGoogleSignin"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported, :visionos => 1.0 }
  s.source       = { :git => "https://github.com/react-native-nitro-google-sign-in/google-signin.git", :tag => "#{s.version}" }

  s.source_files = [
    # Implementation (Swift)
    "ios/**/*.{swift}",
    # Autolinking/Registration (Objective-C++)
    "ios/**/*.{m,mm}",
    # Implementation (C++ objects)
    "cpp/**/*.{hpp,cpp}",
  ]

  load 'nitrogen/generated/ios/NitroGoogleSignin+autolinking.rb'
  add_nitrogen_files(s)

  s.dependency 'React-jsi'
  s.dependency 'React-callinvoker'
  # GoogleSignIn 9.2.0 can resolve AppCheckCore 11.3.0, which adds RecaptchaInterop and
  # breaks static CocoaPods integration on Expo 56. Cap GoogleSignIn until upstream resolves.
  # Do not add AppCheckCore as a direct dependency — NitroGoogleSignin is a Swift pod and
  # AppCheckCore does not define modules. See issue #24.
  s.dependency 'GoogleSignIn', '~> 9.0', '< 9.2.0'
  install_modules_dependencies(s)
end
