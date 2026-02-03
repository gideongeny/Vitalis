# Vitalis 2026: World Class Health

![Vitalis Logo](app/src/main/res/drawable/logo.png)

Welcome to **Vitalis**, the world-class health and wellness platform for 2026. This application has been fully transformed into a vibrant, premium experience designed for the next generation of health-conscious users.

## 🌟 Key Features

- **Sky Blue Aesthetic**: A complete visual overhaul featuring a light, vibrant "Sky Blue Glass" theme. Gone are the dark backgrounds, replaced by airy gradients and clean white surfaces.
- **Robust Step Tracking**: Hardware-grade tracking logic that is reboot-proof. Vitalis ensures every step is counted, even after device restarts, with automated cloud synchronization.
- **Teal Medical Branding**: Unified brand identity featuring the modern Teal Medical Cross logo across the entire application.
- **Premium Onboarding**: Revamped Splash Screen with fluid animations and modernized authentication flows.
- **Multi-Method Authentication**: Email/Password, Google Sign-In, Phone Authentication, and Anonymous/Guest access with a fully scrollable login interface.
- **Health Suite**: Integrated tracking for Water, Steps, Calories, Weight, and Sleep with a sophisticated, easy-to-read interface.
- **Incremental Step Targets**: Dynamic goal setting that increases by 10,000 steps once reached, encouraging continuous movement.

## 📱 Screenshots

| Home & Tracking | Plans & Reminders |
|:---:|:---:|
| ![Home](screenshots/home_screen.jpg) | ![Plans](screenshots/plans_screen.jpg) |

| Health Reports | Specialized Profile |
|:---:|:---:|
| ![Stats](screenshots/stats_screen.jpg) | ![Profile](screenshots/profile_screen.jpg) |

## 🔐 Google Sign-In Configuration

To enable Google Sign-In, you must add your app's SHA-1 fingerprint to the Firebase Console:

1. Run `./gradlew signingReport` to get your debug/release SHA-1 fingerprints
2. Add the SHA-1 to your Firebase project under **Project Settings → Your apps → SHA certificate fingerprints**
3. Download the updated `google-services.json` and replace the existing file in `app/`

**Current Web Client ID**: `204352382670-h5sd6cf81d6lt8golegqc1mnvd83n187.apps.googleusercontent.com`

## 🔒 Authentication-Gated Features

To encourage user engagement and data persistence, certain premium features require authentication:

### Guest Mode (Anonymous Access)
- ✅ View all health metrics
- ✅ Track steps and water intake
- ✅ Edit weight (stored locally)
- ❌ Cannot edit age, gender, or height
- ❌ No cloud synchronization

### Authenticated Mode (Sign In Required)
- ✅ Full profile editing (age, gender, height, weight)
- ✅ Cloud data synchronization via Firebase
- ✅ Cross-device access to health data
- ✅ Persistent health history

When a guest user attempts to edit age, gender, or height, they'll see a friendly prompt encouraging them to sign in for full access.

## 🚀 Technical Highlights

- **Framework**: Native Android (Kotlin & XML)
- **Architecture**: MVVM with Hilt Dependency Injection
- **Database**: Room for secure, local data persistence
- **Build System**: Gradle 8.x with KSP and Kapt processors
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## 📦 Build Artifacts

- **Release APK**: [app-release.apk](app/build/outputs/apk/release/app-release.apk)
- **Release AAB**: [app-release.aab](app/build/outputs/bundle/release/app-release.aab)

---
**Vitalis** - *Redefining Human Potential.*
