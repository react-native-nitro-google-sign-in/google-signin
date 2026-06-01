# Documentation source assets

Optional working copies of screenshots and demos. **Published copies** live under `docs/static/` and are referenced in the docs as `/img/...` and `/video/...`.

| Source file | Published path |
| ----------- | -------------- |
| `fingerprint.png` | `docs/static/img/guides/fingerprint.png` |
| `URL_scheme.png` | `docs/static/img/guides/ios-url-scheme.png` |
| `android-google-service-google-cloud.png` | `docs/static/img/guides/android-google-services-google-cloud.png` |
| `google-service-firebase.png` | `docs/static/img/guides/google-services-firebase.png` |
| `google-service-info-plist-firebase.png` | `docs/static/img/guides/google-service-info-plist-firebase.png` |
| `ios-google-service-plist-google-cloud.png` | `docs/static/img/guides/google-service-info-plist-google-cloud.png` |
| `google-signin-android.webm` | `docs/static/video/google-signin-android.webm` |
| `google-signin-iOS.mov` | `docs/static/video/google-signin-ios.mov` |

To refresh the site after updating a screenshot:

```bash
cp assets/<file> docs/static/img/guides/<target>   # or docs/static/video/
```
