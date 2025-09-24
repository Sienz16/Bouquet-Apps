# How The System Works - Bouquet Bliss App

## 📱 System Overview / Gambaran Keseluruhan Sistem

**Bouquet Bliss** is a comprehensive Android mobile application for flower shop businesses with dual-language support (English/Malay). The system provides complete order management, payment processing, rating system, and delivery confirmation features.

**Bouquet Bliss** adalah aplikasi Android yang komprehensif untuk perniagaan kedai bunga dengan sokongan dwi-bahasa (Inggeris/Melayu). Sistem ini menyediakan pengurusan pesanan lengkap, pemprosesan pembayaran, sistem penilaian, dan ciri pengesahan penghantaran.

---

## 👥 User Roles / Peranan Pengguna

### 1. **Customer/User (Pelanggan)**
- Browse and order bouquets / Melayari dan memesan buket
- Make payments via ToyyibPay / Membuat pembayaran melalui ToyyibPay
- Track order status / Menjejaki status pesanan
- Confirm delivery / Mengesahkan penghantaran
- Rate completed orders / Menilai pesanan yang selesai

### 2. **Admin (Pentadbir)**
- Manage bouquet inventory / Menguruskan inventori buket
- Process and approve orders / Memproses dan meluluskan pesanan
- Update order status / Mengemaskini status pesanan
- View customer ratings / Melihat penilaian pelanggan
- Generate reports / Menjana laporan

---

## 🔄 Complete System Flow / Aliran Sistem Lengkap

## **CUSTOMER JOURNEY / PERJALANAN PELANGGAN**

### **Phase 1: Registration & Login / Fasa 1: Pendaftaran & Log Masuk**

#### English Flow:
1. **App Launch** → Welcome screen with login/register options
2. **Registration** → Enter name, email, password → Firebase Auth creates account
3. **Login** → Email/password authentication → Redirect to dashboard based on role
4. **User Dashboard** → Browse available bouquets

#### Aliran Bahasa Melayu:
1. **Pelancaran App** → Skrin selamat datang dengan pilihan log masuk/daftar
2. **Pendaftaran** → Masukkan nama, emel, kata laluan → Firebase Auth cipta akaun
3. **Log Masuk** → Pengesahan emel/kata laluan → Ubah hala ke papan pemuka berdasarkan peranan
4. **Papan Pemuka Pengguna** → Layari buket yang tersedia

---

### **Phase 2: Browsing & Ordering / Fasa 2: Melayari & Memesan**

#### English Flow:
```
User Dashboard
    ↓
Browse Bouquets (Filter by category, price, occasion)
    ↓
Select Bouquet → View Details (Images, price, description, ratings)
    ↓
Add to Cart (Select quantity)
    ↓
Checkout Process:
    - Review order items
    - Enter delivery details (address, date, time)
    - Add special instructions
    - Choose flower color preferences
    ↓
Order Confirmation → Order created with PENDING status
```

#### Aliran Bahasa Melayu:
```
Papan Pemuka Pengguna
    ↓
Layari Buket (Tapis mengikut kategori, harga, majlis)
    ↓
Pilih Buket → Lihat Butiran (Gambar, harga, penerangan, penilaian)
    ↓
Tambah ke Troli (Pilih kuantiti)
    ↓
Proses Pembayaran:
    - Semak item pesanan
    - Masukkan butiran penghantaran (alamat, tarikh, masa)
    - Tambah arahan khas
    - Pilih keutamaan warna bunga
    ↓
Pengesahan Pesanan → Pesanan dicipta dengan status PENDING
```

---

### **Phase 3: Order Processing / Fasa 3: Pemprosesan Pesanan**

#### English Status Flow:
```
PENDING → APPROVED → IN_PREPARATION → READY → COMPLETED
   ↓         ↓            ↓            ↓         ↓
Admin     Admin       Admin        Admin    Customer
Review   Approves   Preparing    Ready    Confirms
Order     Order     Bouquet    for Pick   Delivery
                              up/Delivery
```

#### Aliran Status Bahasa Melayu:
```
PENDING → APPROVED → IN_PREPARATION → READY → COMPLETED
   ↓         ↓            ↓            ↓         ↓
Admin     Admin       Admin        Admin    Pelanggan
Semak    Luluskan   Menyediakan   Sedia   Sahkan
Pesanan  Pesanan     Buket       Ambil   Penghantaran
                               /Hantar
```

---

### **Phase 4: Payment Processing / Fasa 4: Pemprosesan Pembayaran**

#### English Payment Flow:
1. **Order Approved** → Payment button appears in order details
2. **Click Pay Now** → Redirected to ToyyibPay gateway
3. **ToyyibPay Interface** → Select payment method (FPX, Card, etc.)
4. **Payment Processing** → Real-time status checking
5. **Payment Success** → Order status updated, invoice generated automatically
6. **Payment Failed** → Retry option available

#### Aliran Pembayaran Bahasa Melayu:
1. **Pesanan Diluluskan** → Butang pembayaran muncul dalam butiran pesanan
2. **Klik Bayar Sekarang** → Ubah hala ke gateway ToyyibPay
3. **Interface ToyyibPay** → Pilih kaedah pembayaran (FPX, Kad, dll.)
4. **Pemprosesan Pembayaran** → Pemeriksaan status masa nyata
5. **Pembayaran Berjaya** → Status pesanan dikemaskini, invois dijana automatik
6. **Pembayaran Gagal** → Pilihan cuba semula tersedia

---

### **Phase 5: Delivery Confirmation / Fasa 5: Pengesahan Penghantaran**

#### English Delivery Flow:
1. **Order Status: READY** → "Confirm Delivery" button appears
2. **Customer Receives Order** → Click "Confirm Delivery"
3. **Confirmation Dialog** → "Have you received your order?"
4. **Customer Confirms** → Order automatically updates to COMPLETED
5. **Admin Notification** → Admin receives delivery confirmation alert

#### Aliran Penghantaran Bahasa Melayu:
1. **Status Pesanan: READY** → Butang "Sahkan Penghantaran" muncul
2. **Pelanggan Terima Pesanan** → Klik "Sahkan Penghantaran"
3. **Dialog Pengesahan** → "Adakah anda telah menerima pesanan anda?"
4. **Pelanggan Sahkan** → Pesanan automatik dikemaskini ke COMPLETED
5. **Notifikasi Admin** → Admin terima makluman pengesahan penghantaran

---

### **Phase 6: Rating System / Fasa 6: Sistem Penilaian**

#### English Rating Flow:
1. **Order Status: COMPLETED** → "Rate Order" button appears
2. **Click Rate Order** → Select bouquet to rate (if multiple items)
3. **Rating Dialog Opens** → Interactive 5-star rating system
4. **Customer Rates** → Select stars (1-5) + optional comment
5. **Submit Rating** → Rating saved to database
6. **Bouquet Statistics Updated** → Average rating recalculated

#### Aliran Penilaian Bahasa Melayu:
1. **Status Pesanan: COMPLETED** → Butang "Nilai Pesanan" muncul
2. **Klik Nilai Pesanan** → Pilih buket untuk dinilai (jika berbilang item)
3. **Dialog Penilaian Terbuka** → Sistem penilaian 5-bintang interaktif
4. **Pelanggan Menilai** → Pilih bintang (1-5) + komen pilihan
5. **Hantar Penilaian** → Penilaian disimpan ke pangkalan data
6. **Statistik Buket Dikemaskini** → Purata penilaian dikira semula

---

## **ADMIN JOURNEY / PERJALANAN ADMIN**

### **Phase 1: Admin Dashboard / Fasa 1: Papan Pemuka Admin**

#### English Admin Flow:
```
Admin Login → Admin Dashboard
    ↓
Dashboard Statistics:
    - Total Orders Count
    - Pending Orders (Need Action)
    - Total Revenue (From Completed Orders)
    - Quick Action Cards
```

#### Aliran Admin Bahasa Melayu:
```
Log Masuk Admin → Papan Pemuka Admin
    ↓
Statistik Papan Pemuka:
    - Jumlah Pesanan
    - Pesanan Pending (Perlu Tindakan)
    - Jumlah Hasil (Dari Pesanan Selesai)
    - Kad Tindakan Pantas
```

---

### **Phase 2: Order Management / Fasa 2: Pengurusan Pesanan**

#### English Order Management Flow:
```
Order Management → Three Tabs:
    ↓
1. PENDING TAB:
   - New orders awaiting approval
   - Actions: Approve/Reject with reason
   
2. APPROVED TAB:
   - Orders in progress
   - Update preparation status:
     * Not Started → Preparing → Ready for Pickup → Completed
   
3. COMPLETED TAB:
   - Finished orders
   - View customer ratings and feedback
   - Revenue tracking
```

#### Aliran Pengurusan Pesanan Bahasa Melayu:
```
Pengurusan Pesanan → Tiga Tab:
    ↓
1. TAB PENDING:
   - Pesanan baru menunggu kelulusan
   - Tindakan: Lulus/Tolak dengan sebab
   
2. TAB APPROVED:
   - Pesanan dalam proses
   - Kemaskini status penyediaan:
     * Belum Mula → Menyediakan → Sedia Ambil → Selesai
   
3. TAB COMPLETED:
   - Pesanan selesai
   - Lihat penilaian dan maklum balas pelanggan
   - Penjejakan hasil
```

---

### **Phase 3: Stock Management / Fasa 3: Pengurusan Stok**

#### English Stock Flow:
1. **Stock Dashboard** → View all bouquets with availability status
2. **Add New Bouquet** → Upload images, set price, description, category
3. **Update Stock** → Modify quantity, price, availability
4. **Stock Transactions** → Automatic tracking when orders completed
5. **Low Stock Alerts** → Visual indicators for items running low

#### Aliran Stok Bahasa Melayu:
1. **Papan Pemuka Stok** → Lihat semua buket dengan status ketersediaan
2. **Tambah Buket Baru** → Muat naik gambar, tetapkan harga, penerangan, kategori
3. **Kemaskini Stok** → Ubah kuantiti, harga, ketersediaan
4. **Transaksi Stok** → Penjejakan automatik bila pesanan selesai
5. **Amaran Stok Rendah** → Penunjuk visual untuk item yang semakin habis

---

### **Phase 4: Customer Rating Management / Fasa 4: Pengurusan Penilaian Pelanggan**

#### English Rating Management:
```
Order Details (Completed Orders)
    ↓
Customer Ratings Section:
    - View all ratings for the order
    - Star ratings (1-5) with visual display
    - Customer comments and feedback
    - Verified purchase badges
    - Rating timestamps
    ↓
Business Intelligence:
    - Identify service quality issues
    - Track bouquet performance
    - Customer satisfaction trends
```

#### Pengurusan Penilaian Bahasa Melayu:
```
Butiran Pesanan (Pesanan Selesai)
    ↓
Bahagian Penilaian Pelanggan:
    - Lihat semua penilaian untuk pesanan
    - Penilaian bintang (1-5) dengan paparan visual
    - Komen dan maklum balas pelanggan
    - Lencana pembelian yang disahkan
    - Cap masa penilaian
    ↓
Risikan Perniagaan:
    - Kenal pasti isu kualiti perkhidmatan
    - Jejak prestasi buket
    - Trend kepuasan pelanggan
```

---

## 🗄️ Database Structure / Struktur Pangkalan Data

### **Collections in Firestore:**

#### **1. users**
```json
{
  "uid": "user123",
  "email": "user@example.com",
  "name": "John Doe",
  "userType": "USER", // or "ADMIN"
  "createdAt": 1640995200000,
  "updatedAt": 1640995200000
}
```

#### **2. bouquets**
```json
{
  "id": "bouquet123",
  "name": "Rose Bouquet",
  "description": "Beautiful red roses",
  "price": 89.90,
  "quantity": 15,
  "category": "Romantic",
  "imageUrls": ["url1", "url2"],
  "availability": "Available", // "Limited", "Out of Stock"
  "averageRating": 4.5,
  "totalRatings": 23,
  "createdAt": 1640995200000,
  "updatedAt": 1640995200000
}
```

#### **3. orders**
```json
{
  "id": "order123",
  "customerId": "user123",
  "customerName": "John Doe",
  "customerEmail": "user@example.com",
  "customerPhone": "+60123456789",
  "items": [
    {
      "bouquetId": "bouquet123",
      "bouquetName": "Rose Bouquet",
      "unitPrice": 89.90,
      "quantity": 2,
      "subtotal": 179.80
    }
  ],
  "totalAmount": 179.80,
  "status": "completed", // pending, approved, in_preparation, ready, completed, rejected
  "preparationStatus": "completed",
  "paymentStatus": "paid",
  "paymentMethod": "toyyibpay",
  "transactionId": "txn123",
  "deliveryAddress": "123 Main St, KL",
  "deliveryDate": 1641081600000,
  "specialInstructions": "Make it extra beautiful",
  "isRated": true,
  "deliveryConfirmedAt": 1641168000000,
  "deliveryConfirmedBy": "customer",
  "orderDate": 1640995200000,
  "createdAt": 1640995200000,
  "updatedAt": 1640995200000
}
```

#### **4. ratings**
```json
{
  "id": "rating123",
  "orderId": "order123",
  "customerId": "user123",
  "customerName": "John Doe",
  "bouquetId": "bouquet123",
  "bouquetName": "Rose Bouquet",
  "stars": 5,
  "comment": "Beautiful flowers, excellent service!",
  "ratedAt": 1641168000000,
  "isVerifiedPurchase": true
}
```

#### **5. admin_notifications**
```json
{
  "id": "notif123",
  "type": "delivery_confirmed",
  "orderId": "order123",
  "orderNumber": "BB001",
  "customerName": "John Doe",
  "message": "Customer confirmed delivery for order BB001",
  "isRead": false,
  "createdAt": 1641168000000
}
```

---

## 🔧 Key Features Implementation / Pelaksanaan Ciri Utama

### **1. Authentication System / Sistem Pengesahan**
- **Firebase Auth** integration
- **Role-based access** (User/Admin)
- **Automatic role detection** based on email
- **Secure session management**

### **2. Payment Integration / Integrasi Pembayaran**
- **ToyyibPay gateway** integration
- **Real-time payment status** checking
- **Automatic invoice generation** on success
- **Payment retry mechanism** on failure

### **3. Rating System / Sistem Penilaian**
- **5-star interactive rating** with visual feedback
- **Optional comment system** for detailed feedback
- **Verified purchase badges** for authenticity
- **Real-time bouquet statistics** updates

### **4. Delivery Confirmation / Pengesahan Penghantaran**
- **Customer-initiated confirmation** for accuracy
- **Automatic order completion** on confirmation
- **Admin notifications** for delivery updates
- **Audit trail** for delivery tracking

### **5. Inventory Management / Pengurusan Inventori**
- **Real-time stock updates** on order completion
- **Automatic availability calculation** (Available/Limited/Out of Stock)
- **Stock transaction logging** for audit purposes
- **Low stock visual indicators**

---

## 📱 Technical Architecture / Seni Bina Teknikal

### **Frontend (Android)**
- **Language:** Java
- **UI Framework:** Material Design Components
- **Architecture:** MVVM pattern
- **Data Binding:** Android Data Binding
- **Image Loading:** Glide library

### **Backend Services**
- **Authentication:** Firebase Authentication
- **Database:** Cloud Firestore (NoSQL)
- **Storage:** Firebase Storage (for images)
- **Payment:** ToyyibPay API integration
- **PDF Generation:** iText7 library

### **Key Libraries Used:**
```gradle
// Firebase
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-storage'

// Payment
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// UI
implementation 'com.github.bumptech.glide:glide:4.16.0'

// PDF
implementation 'com.itextpdf:itext7-core:7.2.5'

// Translation
implementation 'com.google.mlkit:translate:17.0.2'
```

---

## 🚀 Getting Started for New Developers / Permulaan untuk Pembangun Baru

### **Setup Requirements:**
1. **Android Studio** Arctic Fox or newer
2. **Java 8+** development environment
3. **Firebase project** with Authentication, Firestore, and Storage enabled
4. **ToyyibPay merchant account** for payment integration

### **Initial Setup Steps:**
1. Clone the repository
2. Open in Android Studio
3. Add `google-services.json` to `app/` directory
4. Configure ToyyibPay credentials in `ToyyibPayService.java`
5. Build and run on device/emulator

### **Key Files to Understand:**
- `LoginActivity.java` - Authentication flow
- `UserOrderDetailsActivity.java` - Customer order management
- `OrderDetailsActivity.java` - Admin order management
- `RatingDialog.java` - Rating system implementation
- `ToyyibPayService.java` - Payment integration

---

## 📋 Testing Scenarios / Senario Ujian

### **Customer Testing Flow:**
1. Register new account → Login
2. Browse bouquets → Add to cart
3. Checkout with delivery details
4. Admin approves order
5. Make payment via ToyyibPay
6. Track order status updates
7. Confirm delivery when received
8. Rate the completed order

### **Admin Testing Flow:**
1. Login as admin
2. View dashboard statistics
3. Process pending orders (approve/reject)
4. Update preparation status
5. View customer ratings for completed orders
6. Manage bouquet inventory
7. Check stock transaction logs

---

## 🔍 Troubleshooting Common Issues / Menyelesaikan Isu Biasa

### **Payment Issues:**
- Verify ToyyibPay credentials
- Check internet connectivity
- Validate minimum payment amount (RM 1.00)

### **Rating System Issues:**
- Ensure order is completed and paid
- Check Firestore security rules
- Verify customer authentication

### **Delivery Confirmation Issues:**
- Confirm order status is "READY"
- Check customer authentication
- Verify Firestore permissions

---

This documentation provides a comprehensive guide for new developers to understand and work with the Bouquet Bliss system. The dual-language approach ensures accessibility for both English and Malay-speaking developers.

**Dokumentasi ini menyediakan panduan komprehensif untuk pembangun baru memahami dan bekerja dengan sistem Bouquet Bliss. Pendekatan dwi-bahasa memastikan kebolehcapaian untuk pembangun yang berbahasa Inggeris dan Melayu.**

