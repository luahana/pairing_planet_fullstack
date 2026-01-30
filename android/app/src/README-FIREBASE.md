# Firebase Setup for Android

## Overview

This app uses Firebase for authentication. There are two flavors:
- **dev** - Development environment (com.cookstemma.app.dev)
- **prod** - Production environment (com.cookstemma.app)

## Setup Instructions

### 1. Download google-services.json

For each flavor, download the `google-services.json` file from the Firebase Console:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select the appropriate project:
   - `cookstemma-dev` for development
   - `cookstemma-prod` for production
3. Go to Project Settings > General
4. Under "Your apps", find the Android app (or add one if it doesn't exist)
   - For dev: package name should be `com.cookstemma.app.dev`
   - For prod: package name should be `com.cookstemma.app`
5. Download the `google-services.json` file

### 2. Place the Files

Place the downloaded files in the appropriate directories:

```
android/app/src/
├── dev/
│   └── google-services.json    <- Dev config (cookstemma-dev project)
└── prod/
    └── google-services.json    <- Prod config (cookstemma-prod project)
```

### 3. Build the App

Build with the appropriate flavor:

```bash
# Development build
./gradlew assembleDevDebug

# Production build
./gradlew assembleProdRelease
```

## Firebase Projects

| Flavor | Firebase Project | Package Name | API Base URL |
|--------|------------------|--------------|--------------|
| dev | cookstemma-dev | com.cookstemma.app.dev | http://10.0.2.2:4000/api/v1 |
| prod | cookstemma-prod | com.cookstemma.app | https://api.cookstemma.com/api/v1 |

## Important Notes

- Never commit real `google-services.json` files to version control
- The placeholder files in this directory contain template structure only
- Each developer should download their own config from Firebase Console
