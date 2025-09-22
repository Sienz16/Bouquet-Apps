# System Maintenance & Modification Guide

## 🔧 System Maintenance Documentation

### 📋 Regular Maintenance Checklist

#### Daily Tasks
- [ ] Monitor ToyyibPay dashboard for payment issues
- [ ] Check Firebase Console for authentication errors
- [ ] Review app crash reports (if deployed to Play Store)

#### Weekly Tasks
- [ ] Backup Firestore database
- [ ] Clean up temporary files and logs
- [ ] Monitor storage usage (local images)
- [ ] Check for dependency updates

#### Monthly Tasks
- [ ] Update Android dependencies in `build.gradle`
- [ ] Review and update Firebase security rules
- [ ] Test payment flow end-to-end
- [ ] Analyze user feedback and crash reports
- [ ] Clean up unused bouquet images

#### Quarterly Tasks
- [ ] Update Android Studio and SDK
- [ ] Review and update ToyyibPay integration
- [ ] Performance testing on different devices
- [ ] Security audit of authentication flow
- [ ] Code review and refactoring

---

## 🛠️ Common Modification Tasks

### 1. Adding New Bouquet Categories

**Files to modify:**
- `app/src/main/java/com/example/bouquetbliss/models/Bouquet.java`
- Any UI components that display categories

**Steps:**
1. Add new category constant in `Bouquet.Categories` class
2. Update `getAllCategories()` method
3. Test category filtering and display

### 2. Modifying Payment Flow

**Files to modify:**
- `app/src/main/java/com/example/bouquetbliss/services/ToyyibPayService.java`
- `app/src/main/java/com/example/bouquetbliss/user/payment/PaymentActivity.java`

**Common modifications:**
- Adding new payment methods
- Changing minimum payment amount
- Updating payment confirmation flow

### 3. Updating User Interface

**Files to modify:**
- Layout files in `app/src/main/res/layout/`
- Color schemes in `app/src/main/res/values/colors.xml`
- String resources in `app/src/main/res/values/strings.xml`

### 4. Database Schema Changes

**Files to modify:**
- Model classes in `app/src/main/java/com/example/bouquetbliss/models/`
- Firebase security rules
- Any activities that interact with changed models

---

## 🤖 Cursor AI Prompting Guide

### General Project Context Prompt

```
I'm working on the Bouquet Bliss Android app - a flower shop application built with Java, Firebase Firestore, and ToyyibPay payment integration. The app has user/admin roles, local image storage, and PDF invoice generation.

Key project structure:
- Models: User, Bouquet, Order in /models/ folder
- Services: ToyyibPayService, InvoiceGeneratorService in /services/ folder  
- User activities in /user/ folder, admin activities in /admin/ folder
- Local image storage using LocalImageManager utility class
- Firebase Auth for authentication, Firestore for database

Please help me with [specific task].
```

### Specific Task Prompts

#### 🐛 Bug Fixing Prompts

**Payment Issues:**
```
I'm having payment issues in my Bouquet Bliss app. The ToyyibPay integration in ToyyibPayService.java is returning [specific error]. The app uses production ToyyibPay endpoints. Can you help me debug this payment flow issue?

Current error: [paste error message]
Payment flow: User selects bouquet → PaymentActivity → ToyyibPayService.createBill() → redirect to ToyyibPay
```

**Firebase Connection Issues:**
```
My Bouquet Bliss app is having Firebase connection issues. Users can't authenticate or access Firestore data. The app uses Firebase Auth with email/password and Firestore for data storage.

Error details: [paste error]
Firebase setup: google-services.json is in app/ folder, package name is com.example.bouquetbliss
```

**Image Loading Problems:**
```
Images aren't loading in my Bouquet Bliss app. The app uses LocalImageManager for local image storage with paths like "local://bouquet_images/filename.jpg". Images are displayed in RecyclerViews for bouquet listings.

Issue: [describe specific image loading problem]
Relevant files: LocalImageManager.java, BrowseBouquetsAdapter.java
```

#### ✨ Feature Enhancement Prompts

**Adding New Features:**
```
I want to add [specific feature] to my Bouquet Bliss flower shop app. The app currently has user/admin roles, bouquet browsing, ordering with ToyyibPay payments, and PDF invoices.

Current architecture:
- Models: User, Bouquet, Order
- Services: ToyyibPayService, InvoiceGeneratorService  
- Database: Firebase Firestore
- Image storage: Local storage system

Please help me implement [feature] following the existing code patterns and architecture.
```

**UI/UX Improvements:**
```
I want to improve the UI/UX of [specific screen] in my Bouquet Bliss app. The app uses Material Design components and has separate user/admin interfaces.

Current screen: [describe current implementation]
Desired changes: [describe what you want to improve]
Relevant files: [mention layout files or activities]
```

#### 🔧 Maintenance Prompts

**Dependency Updates:**
```
I need to update dependencies in my Bouquet Bliss Android app. The app uses Firebase, OkHttp3, Gson, iText7 for PDF generation, and other libraries defined in build.gradle.

Current versions: [paste current dependencies from build.gradle]
Please help me safely update these dependencies and handle any breaking changes.
```

**Performance Optimization:**
```
My Bouquet Bliss app is experiencing performance issues with [specific area]. The app loads bouquet images locally, handles payment processing, and generates PDF invoices.

Performance issue: [describe specific problem]
Affected components: [mention relevant classes/activities]
Please help me optimize this for better performance.
```

**Security Updates:**
```
I need to review and update security in my Bouquet Bliss app. The app uses Firebase Auth, Firestore with security rules, ToyyibPay for payments, and local image storage.

Security concerns: [describe specific concerns]
Current setup: [mention current security measures]
Please help me improve the security implementation.
```

#### 📊 Database Modifications

**Firestore Schema Changes:**
```
I need to modify the Firestore database schema in my Bouquet Bliss app. Current collections are users, bouquets, and orders.

Current schema: [describe current structure]
Proposed changes: [describe what you want to change]
Migration strategy needed: [yes/no and details]

Please help me implement this schema change safely without breaking existing functionality.
```

**Data Migration:**
```
I need to migrate data in my Bouquet Bliss Firestore database from [old format] to [new format].

Current data structure: [describe current]
Target structure: [describe target]
Data volume: [approximate number of documents]

Please help me create a safe migration strategy.
```

#### 🎨 Design Changes

**Theme/Color Updates:**
```
I want to update the color scheme/theme of my Bouquet Bliss app. The app currently uses Material Design with custom colors defined in colors.xml.

Current theme: [describe current colors/theme]
Desired changes: [describe new design]
Scope: [which screens/components to update]

Please help me implement this design update consistently across the app.
```

**Layout Improvements:**
```
I want to improve the layout of [specific screen] in my Bouquet Bliss app to be more user-friendly and responsive.

Current layout: [describe current implementation]
Issues: [describe problems with current layout]
Target improvements: [describe desired changes]

Relevant files: [mention layout XML files]
```

---

## 🚨 Emergency Response Procedures

### App Crashes
1. **Check Firebase Console** for crash reports
2. **Review recent changes** in git history
3. **Test on different devices** to isolate the issue
4. **Roll back** to last stable version if critical

**Cursor Prompt for Crashes:**
```
My Bouquet Bliss app is crashing with this error: [paste error/stack trace]

Recent changes: [describe any recent modifications]
Affected devices/versions: [specify if known]
Steps to reproduce: [list steps]

Please help me identify and fix this crash quickly.
```

### Payment System Down
1. **Check ToyyibPay status** and announcements
2. **Verify credentials** and configuration
3. **Test with small amounts** first
4. **Notify users** if extended outage

**Cursor Prompt for Payment Issues:**
```
URGENT: Payment system is down in my Bouquet Bliss app. Users can't complete purchases through ToyyibPay.

Error details: [paste error messages]
ToyyibPay status: [check their status page]
Last working time: [when it last worked]

Please help me diagnose and fix this payment issue immediately.
```

### Database Connection Issues
1. **Check Firebase Console** status
2. **Verify internet connectivity**
3. **Check security rules** for recent changes
4. **Test authentication flow**

**Cursor Prompt for Database Issues:**
```
URGENT: Users can't access data in my Bouquet Bliss app. Firebase Firestore connection seems broken.

Symptoms: [describe what users are experiencing]
Error messages: [paste any error logs]
Recent changes: [any recent security rule or configuration changes]

Please help me restore database connectivity quickly.
```

---

## 📈 Performance Monitoring

### Key Metrics to Track
- **App startup time**
- **Image loading speed**
- **Payment completion rate**
- **Database query response time**
- **Crash-free session rate**

### Tools for Monitoring
- **Firebase Performance Monitoring**
- **Firebase Crashlytics**
- **Android Studio Profiler**
- **ToyyibPay Dashboard**

**Cursor Prompt for Performance Analysis:**
```
I want to analyze and improve performance of my Bouquet Bliss app. The app handles image loading, database queries, and payment processing.

Current performance issues: [describe specific problems]
Metrics showing problems: [mention specific metrics]
Target improvements: [what you want to achieve]

Please help me identify bottlenecks and implement performance improvements.
```

---

## 🔄 Version Control Best Practices

### Branch Strategy
```
main/master     - Production ready code
dev            - Development branch
feature/xxx    - New features
bugfix/xxx     - Bug fixes
hotfix/xxx     - Emergency fixes
```

### Commit Message Format
```
type(scope): description

Examples:
feat(payment): add support for multiple payment methods
fix(auth): resolve login timeout issue
docs(readme): update installation instructions
refactor(images): optimize local image storage
```

**Cursor Prompt for Version Control:**
```
I'm working on [feature/bugfix] for my Bouquet Bliss app. Please help me:

1. Create appropriate git commits with good commit messages
2. Follow the existing code patterns and structure
3. Ensure changes don't break existing functionality

Current task: [describe what you're working on]
Files involved: [list main files being modified]
```

---

## 📚 Documentation Updates

### When to Update Documentation
- Adding new features
- Changing configuration
- Updating dependencies
- Modifying database schema
- Changing security rules

### Documentation Files to Maintain
- `PROJECT_DOCUMENTATION.md` - Main project overview
- `FIREBASE_SETUP.md` - Firebase configuration
- `TOYYIBPAY_SETUP.md` - Payment setup
- `SYSTEM_MAINTENANCE_GUIDE.md` - This file
- Code comments in complex functions

**Cursor Prompt for Documentation:**
```
I've made changes to my Bouquet Bliss app and need to update the documentation accordingly.

Changes made: [describe modifications]
Affected functionality: [what features are impacted]
Files modified: [list key files changed]

Please help me update the relevant documentation files to reflect these changes.
```

---

## 🎯 Testing Strategies

### Manual Testing Checklist
- [ ] User registration and login
- [ ] Bouquet browsing and search
- [ ] Order placement flow
- [ ] Payment processing
- [ ] Invoice generation
- [ ] Admin stock management
- [ ] Admin order processing

### Automated Testing Setup
```java
// Example test structure for future implementation
@Test
public void testPaymentFlow() {
    // Test payment creation and verification
}

@Test  
public void testOrderCreation() {
    // Test order placement process
}
```

**Cursor Prompt for Testing:**
```
I want to add testing to my Bouquet Bliss app. The app has payment processing, order management, and user authentication.

Current testing: [describe current testing approach]
Areas to test: [specific functionality to test]
Testing framework preference: [JUnit, Espresso, etc.]

Please help me set up comprehensive testing for this Android app.
```

---

## 🛡️ Security Maintenance

### Security Checklist
- [ ] Firebase security rules are restrictive
- [ ] User input is properly validated
- [ ] Payment credentials are secure
- [ ] Local storage permissions are appropriate
- [ ] Authentication tokens are handled securely

### Security Updates Process
1. **Review security rules** monthly
2. **Update dependencies** for security patches
3. **Audit user permissions** and access levels
4. **Monitor for suspicious activity**
5. **Keep payment integration updated**

**Cursor Prompt for Security:**
```
I need to audit and improve security in my Bouquet Bliss app. The app handles user authentication, payment processing, and personal data.

Current security measures: [describe existing security]
Concerns: [specific security concerns]
Compliance requirements: [any specific requirements]

Please help me identify security vulnerabilities and implement improvements.
```

---

## 📞 Support & Escalation

### Internal Issues
1. **Check this documentation** first
2. **Search existing git issues/commits**
3. **Use Cursor AI** with specific prompts above
4. **Test on different devices/environments**

### External Service Issues
- **Firebase**: Check Firebase Status page and Console
- **ToyyibPay**: Check ToyyibPay status and contact support
- **Play Store**: Check Play Console for issues

### Emergency Contacts
- **Firebase Support**: Firebase Console → Support
- **ToyyibPay Support**: support@toyyibpay.com
- **Play Store Support**: Play Console → Support

---

## 🎉 Success Metrics

### App Health Indicators
- ✅ **99%+ crash-free sessions**
- ✅ **Payment success rate > 95%**
- ✅ **App startup time < 3 seconds**
- ✅ **User retention rate**
- ✅ **Positive user feedback**

### Business Metrics
- 📈 **Order completion rate**
- 📈 **Average order value**
- 📈 **Customer satisfaction**
- 📈 **Admin efficiency metrics**

**Cursor Prompt for Analytics:**
```
I want to implement analytics and monitoring in my Bouquet Bliss app to track business and technical metrics.

Metrics to track: [list specific metrics]
Current analytics: [describe existing analytics]
Integration preferences: [Firebase Analytics, etc.]

Please help me implement comprehensive analytics tracking.
```

---

*This guide should be updated whenever significant changes are made to the system architecture or maintenance procedures.*

**Last Updated: September 2025**
**Version: 1.0**
