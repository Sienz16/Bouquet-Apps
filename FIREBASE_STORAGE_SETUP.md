# Firebase Storage Setup for Bouquet Bliss

## Current Issue
Getting "object does not exist at location" error when uploading images to Firebase Storage.

## Solution: Configure Firebase Storage Security Rules

### Step 1: Open Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project: **bouquet-bliss**
3. Click on **Storage** in the left sidebar

### Step 2: Update Security Rules
1. Click on the **Rules** tab
2. Replace the current rules with:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow read/write access to bouquet images for authenticated users
    match /bouquet_images/{imageId} {
      allow read, write: if request.auth != null;
    }
    
    // Allow read/write access to test files for debugging
    match /test/{testFile} {
      allow read, write: if request.auth != null;
    }
    
    // Deny all other access
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

### Step 3: Publish Rules
1. Click **Publish** to save the new rules

### Alternative: Allow All Access (Development Only)
If you want to temporarily allow all access for development:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

⚠️ **Warning**: Only use the "allow all" rule during development. Always restrict access in production.

### Step 4: Test the Setup
After updating the rules, try uploading an image again in the app. The debug messages will help identify if the issue is resolved.

## Verification
- The app should now successfully upload images to the `bouquet_images/` folder
- You should see uploaded images in Firebase Storage console
- Error messages should disappear

## Security Best Practices
- Always require authentication for write access
- Limit file types and sizes in production
- Use proper folder structure for organization
- Regularly review and audit storage rules
