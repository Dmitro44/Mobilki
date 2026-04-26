# Firebase setup for LR4

This project now uses the standard Firebase Android setup through `google-services.json`.

## 1. Create a Firebase project

1. Open the Firebase console.
2. Create a new project.
3. Add an **Android app** with package name:

```text
com.example.seabattle
```

## 2. Download google-services.json

In Firebase Console:

- **Project settings**
- your Android app
- click **Download google-services.json**

Place that file into:

```text
LR4/app/google-services.json
```

This file is required for the app to initialize Firebase in the standard Android way.

## 3. Keep local.properties only for the Android SDK path

Your `local.properties` should only contain the Android SDK location, for example:

```properties
sdk.dir=/path/to/your/android/sdk
```

Do not put Firebase keys there anymore.

## 4. Enable required Firebase products

### Authentication

- Open **Authentication**
- Go to **Sign-in method**
- Enable **Email/Password**

This app now starts with a real email/password authentication screen.

### Cloud Firestore

- Create a Firestore database
- Use **Native mode**

## 5. Development security rules

For the first launch, use simple authenticated-only rules.

### Firestore rules

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

For the lab this is enough to get the app running. If needed, you can tighten the rules later so users only edit their own profile and only participants access a game.

## 6. What is already implemented in the app

- email/password sign-in and registration
- profile with nickname
- choosing one of two avatars
- storing selected avatar choice in Firestore
- creating a game by generated ID
- joining a game by ID
- host-authoritative turn updates
- history and stats screen
- graceful notification-permission denial handling

## 7. Important limitation

The host device acts as the authority for game state updates. That means if host app is closed or loses connection, the match can stop until the host returns.
