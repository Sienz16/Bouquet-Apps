# ToyyibPay Integration Setup Guide

## Overview
This guide will help you set up ToyyibPay payment gateway integration for your Bouquet Bliss app.

## Prerequisites
1. ToyyibPay merchant account
2. Valid Malaysian business registration
3. Android development environment

## Step 1: Get ToyyibPay Credentials

### Create ToyyibPay Account
1. Visit [ToyyibPay Developer Portal](https://dev.toyyibpay.com/)
2. Register for a merchant account
3. Complete the verification process
4. Get your credentials from the dashboard

### Required Credentials
- **User Secret Key**: Your unique secret key for API authentication
- **Category Code**: Category code for your business type (florist/retail)

## Step 2: Configure Credentials

### Update ToyyibPayService.java
1. Open `app/src/main/java/com/example/bouquetbliss/services/ToyyibPayService.java`
2. Replace the placeholder credentials:

```java
// Replace these with your actual ToyyibPay credentials
private static final String USER_SECRET_KEY = "your_actual_secret_key_here";
private static final String CATEGORY_CODE = "your_actual_category_code_here";
```

### Example Configuration
```java
private static final String USER_SECRET_KEY = "abcd1234-efgh-5678-ijkl-mnop9012qrst";
private static final String CATEGORY_CODE = "v4rz67w8";
```

## Step 3: Test Integration

### Sandbox Testing
1. Use ToyyibPay sandbox environment for testing
2. The current implementation uses the dev environment: `https://dev.toyyibpay.com/`
3. For production, change the BASE_URL to: `https://toyyibpay.com/`

### Test Payment Flow
1. Create a test order in your app
2. Proceed to payment
3. Use ToyyibPay test cards for payment
4. Verify payment status updates correctly
5. Check if invoice is generated after successful payment

## Step 4: Production Deployment

### Switch to Production
1. Update BASE_URL in ToyyibPayService.java:
```java
private static final String BASE_URL = "https://toyyibpay.com/";
```

2. Use your production credentials
3. Test thoroughly before going live

### Security Considerations
1. Never commit your actual credentials to version control
2. Consider using BuildConfig or environment variables for credentials
3. Implement proper error handling and logging
4. Monitor payment transactions regularly

## Features Included

### Payment Gateway Integration
- ✅ ToyyibPay bill creation
- ✅ Payment status checking
- ✅ Automatic payment verification
- ✅ Return URL handling
- ✅ Error handling and retry mechanism

### PDF Invoice Generation
- ✅ Automatic invoice generation after successful payment
- ✅ Professional invoice layout with company branding
- ✅ Order details and payment information
- ✅ PDF download functionality
- ✅ File storage in app's external directory

### Order Management
- ✅ Payment status tracking
- ✅ Transaction ID storage
- ✅ Payment method recording
- ✅ Payment date tracking
- ✅ Integration with existing order system

## Supported Payment Methods
- Online Banking (FPX)
- Credit/Debit Cards
- e-Wallets (Boost, GrabPay, etc.)
- QRIS payments

## Troubleshooting

### Common Issues
1. **Invalid credentials**: Double-check your secret key and category code
2. **Network errors**: Ensure internet connectivity and API endpoints are accessible
3. **Payment status not updating**: Check callback URL configuration
4. **Invoice not generating**: Verify external storage permissions

### Debug Logging
The integration includes comprehensive logging. Check Android Studio logcat for:
- `ToyyibPayService` logs for API communication
- `InvoiceGenerator` logs for PDF generation
- `PaymentActivity` logs for payment flow

## Support
- ToyyibPay Developer Documentation: https://dev.toyyibpay.com/api-documentation/
- ToyyibPay Support: support@toyyibpay.com
- For app-specific issues, check the code comments and error logs

## Security Notes
- All payment processing happens on ToyyibPay's secure servers
- No sensitive payment data is stored locally
- Invoice files are stored in app-specific external directory
- Payment status is verified server-side with ToyyibPay

## Next Steps
After setup:
1. Test the complete payment flow
2. Customize invoice template if needed
3. Set up payment notification webhooks (optional)
4. Monitor payment success rates
5. Implement analytics for payment tracking
