# 🌸 Bouquet Bliss - Flower Shop Mobile App

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/Database-Firebase-yellow.svg" alt="Database">
  <img src="https://img.shields.io/badge/Payment-ToyyibPay-blue.svg" alt="Payment">
  <img src="https://img.shields.io/badge/Status-Production%20Ready-brightgreen.svg" alt="Status">
</div>

## 📱 Overview

**Bouquet Bliss** is a comprehensive Android mobile application designed for flower shop businesses. It provides a complete solution for customers to browse, order, and purchase beautiful bouquets, while offering powerful admin tools for inventory and order management.

### ✨ Key Features

- 🔐 **User Authentication** - Secure login/register with Firebase Auth
- 🌹 **Bouquet Catalog** - Browse beautiful flower arrangements with detailed views
- 🛒 **Order Management** - Complete order processing from cart to delivery
- 💳 **Payment Integration** - Secure payments via ToyyibPay gateway
- 📄 **Invoice Generation** - Automatic PDF invoice creation
- 👨‍💼 **Admin Dashboard** - Comprehensive stock and order management
- 📱 **Role-based Access** - Separate interfaces for customers and administrators
- 🖼️ **Local Image Storage** - Cost-effective image management system

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Platform** | Android (Java) |
| **Build System** | Gradle |
| **Database** | Firebase Firestore |
| **Authentication** | Firebase Auth |
| **Image Storage** | Local Storage (Internal) |
| **Payment Gateway** | ToyyibPay |
| **PDF Generation** | iText7 |
| **HTTP Client** | OkHttp3 |
| **JSON Parsing** | Gson |

## 📸 Screenshots

*Coming Soon - Add your app screenshots here*

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest version)
- **Java Development Kit (JDK 11+)**
- **Android SDK** (API level 24+)
- **Firebase Account**
- **ToyyibPay Merchant Account**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Bouquet-Bliss.git
   cd Bouquet-Bliss
   ```

2. **Open in Android Studio**
   ```
   File → Open → Select the Bouquet-Bliss folder
   ```

3. **Firebase Setup**
   - Follow the detailed guide in [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md)
   - Download your `google-services.json` and place it in the `app/` directory

4. **ToyyibPay Configuration**
   - Copy `ToyyibPayService.java.template` to `ToyyibPayService.java`
   - Replace placeholder credentials with your actual ToyyibPay credentials
   - Follow the complete guide in [`TOYYIBPAY_SETUP.md`](TOYYIBPAY_SETUP.md)

5. **Build and Run**
   ```bash
   ./gradlew installDebug
   ```

## 📁 Project Structure

```
Bouquet-Bliss/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/bouquetbliss/
│   │   │   ├── adapters/           # RecyclerView adapters
│   │   │   ├── admin/              # Admin-specific activities
│   │   │   ├── auth/               # Authentication activities
│   │   │   ├── models/             # Data models
│   │   │   ├── services/           # Business logic services
│   │   │   ├── user/               # User-specific activities
│   │   │   └── utils/              # Utility classes
│   │   └── res/                    # Resources (layouts, drawables, etc.)
│   └── build.gradle                # App-level dependencies
├── docs/                           # Documentation files
├── FIREBASE_SETUP.md              # Firebase setup guide
├── TOYYIBPAY_SETUP.md             # Payment setup guide
├── PROJECT_DOCUMENTATION.md       # Complete project documentation
└── SYSTEM_MAINTENANCE_GUIDE.md    # Maintenance and modification guide
```

## 👥 User Roles

### 🛍️ Customer Features
- Browse bouquet catalog with search and filters
- View detailed bouquet information and images
- Add items to favorites
- Place orders with customization options
- Secure payment processing
- Order history and status tracking
- PDF invoice download

### 👨‍💼 Admin Features
- Comprehensive dashboard with analytics
- Stock management and inventory tracking
- Order processing and status updates
- Bouquet catalog management (add/edit/delete)
- Customer order management
- Sales reports and insights

## 💳 Payment Integration

The app integrates with **ToyyibPay**, Malaysia's leading payment gateway, supporting:

- 🏦 **Online Banking (FPX)**
- 💳 **Credit/Debit Cards**
- 📱 **e-Wallets** (Boost, GrabPay, TouchNGo, etc.)
- 🔲 **QRIS Payments**

### Payment Flow
1. Customer selects bouquets and proceeds to checkout
2. Order details are sent to ToyyibPay for bill creation
3. Customer is redirected to ToyyibPay's secure payment page
4. After payment, customer returns to the app
5. Payment status is verified and order is confirmed
6. PDF invoice is automatically generated

## 🖼️ Image Management

The app uses a **local image storage system** that offers:

- ✅ **100% Free** - No cloud storage costs
- ⚡ **Fast Loading** - Images load instantly from device storage
- 📱 **Offline Support** - Works without internet connection
- 🔒 **Privacy** - Images remain on device

Images are stored in the app's internal storage with paths like `local://bouquet_images/filename.jpg`.

## 🔐 Security Features

- **Firebase Authentication** with email/password
- **Firestore Security Rules** for data protection
- **Local Image Storage** (no external image URLs)
- **Secure Payment Processing** via ToyyibPay
- **Input Validation** and sanitization
- **Role-based Access Control**

## 📊 Database Schema

### Collections

**users/**
```json
{
  "uid": "firebase_auth_uid",
  "email": "user@example.com", 
  "fullName": "John Doe",
  "userType": "user", // or "admin"
  "phoneNumber": "+60123456789",
  "createdAt": 1699123456789
}
```

**bouquets/**
```json
{
  "id": "bouquet_123",
  "name": "Rose Bouquet",
  "description": "Beautiful red roses",
  "price": 89.90,
  "quantity": 15,
  "category": "Romantic",
  "imageUrls": ["local://bouquet_images/img123.jpg"],
  "availability": "Available"
}
```

**orders/**
```json
{
  "orderId": "ORD_123456",
  "userId": "firebase_auth_uid",
  "items": [...],
  "totalAmount": 179.80,
  "paymentStatus": "paid",
  "paymentMethod": "toyyibpay"
}
```

## 🧪 Testing

### Test Accounts
Create these accounts in Firebase Console for testing:

| Email | Password | Role | Purpose |
|-------|----------|------|---------|
| admin@test.com | admin123 | admin | Test admin features |
| user@test.com | user123 | user | Test customer features |

### Testing Checklist
- [ ] User registration and login
- [ ] Browse bouquets and search functionality
- [ ] Order placement and payment flow
- [ ] Admin stock management
- [ ] Invoice generation
- [ ] Payment verification

## 📚 Documentation

- [`PROJECT_DOCUMENTATION.md`](PROJECT_DOCUMENTATION.md) - Complete project documentation
- [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md) - Firebase configuration guide
- [`TOYYIBPAY_SETUP.md`](TOYYIBPAY_SETUP.md) - Payment gateway setup
- [`SYSTEM_MAINTENANCE_GUIDE.md`](SYSTEM_MAINTENANCE_GUIDE.md) - Maintenance and modification guide

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🐛 Known Issues

- Images are lost when the app is uninstalled (limitation of local storage)
- Payment testing requires actual ToyyibPay merchant account

## 🎯 Future Enhancements

- [ ] Push notifications for order updates
- [ ] Customer reviews and ratings system
- [ ] Delivery tracking integration
- [ ] Multiple payment gateway support
- [ ] Cloud image storage option
- [ ] Customer loyalty program
- [ ] Sales analytics dashboard

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Firebase** for backend services
- **ToyyibPay** for payment gateway integration
- **iText7** for PDF generation
- **OkHttp3** for HTTP client
- **Material Design** for UI components

## 📞 Support

For support and questions:

- 📧 **Email**: [your-email@example.com]
- 🐛 **Issues**: [GitHub Issues](https://github.com/yourusername/Bouquet-Bliss/issues)
- 📖 **Documentation**: Check the `docs/` folder for detailed guides

## ⭐ Star History

If you found this project helpful, please consider giving it a star!

---

<div align="center">
  <p>Made with ❤️ for flower shop businesses</p>
  <p>© 2025 Bouquet Bliss. All rights reserved.</p>
</div>
