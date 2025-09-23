# Firebase Setup Guide for Bouquet Bliss

## Prerequisites
1. Android Studio
2. Google Account
3. Internet Connection

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter project name: `bouquet-bliss`
4. Choose to enable/disable Google Analytics (optional)
5. Click "Create project"

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click "Add app" and select Android icon
2. Fill in the following details:
   - **Android package name**: `com.example.bouquetbliss`
   - **App nickname** (optional): `Bouquet Bliss`
   - **Debug signing certificate SHA-1**: (optional for now)

3. Click "Register app"

## Step 3: Download google-services.json

1. Download the `google-services.json` file
2. Copy this file to your app's `app/` directory (same level as `build.gradle.kts`)
3. The file structure should be:
   ```
   Bouquet-Bliss/
   ├── app/
   │   ├── google-services.json  ← Place it here
   │   ├── build.gradle.kts
   │   └── src/
   ```

## Step 4: Enable Authentication

1. In Firebase Console, go to "Authentication"
2. Click "Get started"
3. Go to "Sign-in method" tab
4. Enable "Email/Password" provider:
   - Toggle "Email/Password" to enabled
   - Click "Save"

## Step 5: Setup Firestore Database

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" for now (we'll secure it later)
4. Select a location closest to your users
5. Click "Done"

## Step 6: Create Firestore Security Rules

1. Go to "Firestore Database" → "Rules" tab
2. Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read and write their own profile
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      // Allow admins to read all user profiles
      allow read: if request.auth != null && 
                  exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
                  get(/databases/$(database)/documents/users/$(request.auth.uid)).data.userType == 'admin';
    }
    
    // Other collections will be added as we implement more modules
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Click "Publish"

## Step 7: Testing User Roles

Since we don't have a registration system yet, you can manually create test users:

1. Go to "Authentication" → "Users" tab
2. Click "Add user"
3. Create test users with different emails:
   - `admin@test.com` (password: `admin123`)
   - `technician@test.com` (password: `tech123`)
   - `verifier@test.com` (password: `verify123`)
   - `user@test.com` (password: `user123`)
   - `feedback@test.com` (password: `feedback123`)








4. Go to "Firestore Database"
5. Create a collection called `users`
6. For each user, create a document with their UID as the document ID:

### Example document structure:
```json
{
  "uid": "user_uid_from_auth",
  "email": "admin@test.com",
  "fullName": "Admin User",
  "userType": "admin",
  "phoneNumber": "",
  "profileImageUrl": "",
  "createdAt": 1699123456789,
  "updatedAt": 1699123456789
}
```

## User Types Available:
- `admin` - Full system access
- `technician` - Stock management and delivery
- `verifier` - Order verification
- `user` - Browse and order bouquets
- `feedback` - Manage customer feedback

## Step 8: Build and Test

1. Build your project in Android Studio
2. Run the app on device/emulator
3. Test login with the created test accounts
4. Verify that users are redirected to appropriate dashboards based on their role

## Troubleshooting

### Common Issues:

1. **Build Error: google-services.json not found**
   - Ensure the file is in the correct location (`app/google-services.json`)
   - Clean and rebuild the project

2. **Authentication Error**
   - Check that Email/Password is enabled in Firebase Console
   - Verify the package name matches exactly

3. **Firestore Permission Denied**
   - Check that test mode is enabled
   - Verify security rules are published correctly

4. **Network Error**
   - Ensure internet permissions are added to AndroidManifest.xml
   - Check device internet connection

### Need Help?

If you encounter any issues:
1. Check the Android Studio Logcat for detailed error messages
2. Verify all Firebase setup steps are completed
3. Ensure google-services.json is properly placed
4. Check Firebase Console for any configuration issues

## Next Steps

After successful setup, you can:
1. Implement user registration functionality
2. Implement the remaining modules (stock management, orders, etc.)
3. Add more security rules as features are developed
