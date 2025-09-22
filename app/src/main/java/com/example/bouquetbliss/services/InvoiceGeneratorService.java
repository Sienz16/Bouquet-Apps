package com.example.bouquetbliss.services;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.bouquetbliss.models.Order;
import com.example.bouquetbliss.models.OrderItem;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceGeneratorService {
    private static final String TAG = "InvoiceGenerator";
    private final Context context;

    public InvoiceGeneratorService(Context context) {
        this.context = context;
    }

    public interface InvoiceGenerationCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }

    /**
     * Generate PDF invoice for a completed order
     */
    public void generateInvoice(Order order, InvoiceGenerationCallback callback) {
        try {
            // Create invoices directory if it doesn't exist
            File invoicesDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "invoices");
            if (!invoicesDir.exists()) {
                invoicesDir.mkdirs();
            }

            // Generate filename
            String fileName = "invoice_" + order.getFormattedOrderId() + "_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(invoicesDir, fileName);

            // Create PDF document
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Create fonts
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Add title
            Paragraph title = new Paragraph("INVOICE")
                    .setFont(titleFont)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Add company info
            Paragraph companyInfo = new Paragraph("Bouquet Bliss\nYour Trusted Florist\nPhone: +60 12-345 6789\nEmail: info@bouquetbliss.com")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);
            document.add(companyInfo);

            // Add invoice details table
            Table invoiceDetailsTable = new Table(2);
            invoiceDetailsTable.setWidth(UnitValue.createPercentValue(100));

            // Left column - Invoice details
            Cell leftCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Invoice Number: " + order.getFormattedOrderId()).setFont(headerFont).setFontSize(12))
                    .add(new Paragraph("Order Date: " + formatDate(order.getOrderDate())).setFont(normalFont).setFontSize(10))
                    .add(new Paragraph("Payment Date: " + formatDate(order.getPaymentDate())).setFont(normalFont).setFontSize(10))
                    .add(new Paragraph("Payment Method: " + getPaymentMethodDisplay(order.getPaymentMethod())).setFont(normalFont).setFontSize(10));

            // Right column - Customer details
            Cell rightCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Bill To:").setFont(headerFont).setFontSize(12))
                    .add(new Paragraph(order.getCustomerName()).setFont(normalFont).setFontSize(10))
                    .add(new Paragraph(order.getCustomerEmail()).setFont(normalFont).setFontSize(10))
                    .add(new Paragraph(order.getCustomerPhone()).setFont(normalFont).setFontSize(10))
                    .add(new Paragraph(order.getDeliveryAddress()).setFont(normalFont).setFontSize(10));

            invoiceDetailsTable.addCell(leftCell);
            invoiceDetailsTable.addCell(rightCell);
            document.add(invoiceDetailsTable);

            // Add some space
            document.add(new Paragraph("\n"));

            // Add items table
            Table itemsTable = new Table(4);
            itemsTable.setWidth(UnitValue.createPercentValue(100));

            // Table headers
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Item").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Quantity").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Unit Price").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(headerFont)).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // Add items
            for (OrderItem item : order.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getBouquetName()).setFont(normalFont)));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFont(normalFont)).setTextAlignment(TextAlignment.CENTER));
                itemsTable.addCell(new Cell().add(new Paragraph(String.format("RM %.2f", item.getUnitPrice())).setFont(normalFont)).setTextAlignment(TextAlignment.RIGHT));
                itemsTable.addCell(new Cell().add(new Paragraph(String.format("RM %.2f", item.getSubtotal())).setFont(normalFont)).setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(itemsTable);

            // Add totals
            Table totalsTable = new Table(2);
            totalsTable.setWidth(UnitValue.createPercentValue(50));
            totalsTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

            totalsTable.addCell(new Cell().add(new Paragraph("Subtotal:").setFont(headerFont)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph(String.format("RM %.2f", order.getTotalAmount())).setFont(normalFont)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            totalsTable.addCell(new Cell().add(new Paragraph("Total Amount:").setFont(titleFont).setFontSize(14)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            totalsTable.addCell(new Cell().add(new Paragraph(String.format("RM %.2f", order.getPaidAmount())).setFont(titleFont).setFontSize(14)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            document.add(totalsTable);

            // Add payment status
            document.add(new Paragraph("\n"));
            Paragraph paymentStatus = new Paragraph("Payment Status: PAID")
                    .setFont(headerFont)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GREEN)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(paymentStatus);

            // Add special instructions if any
            if (order.getSpecialInstructions() != null && !order.getSpecialInstructions().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Special Instructions:").setFont(headerFont).setFontSize(12));
                document.add(new Paragraph(order.getSpecialInstructions()).setFont(normalFont).setFontSize(10));
            }

            // Add flower color preference if any
            if (order.getFlowerColorPreference() != null && !order.getFlowerColorPreference().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Flower Color Preference:").setFont(headerFont).setFontSize(12));
                document.add(new Paragraph(order.getFlowerColorPreference()).setFont(normalFont).setFontSize(10));
            }

            // Add footer
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Thank you for choosing Bouquet Bliss!\nFor any queries, please contact us at info@bouquetbliss.com")
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);

            // Close document
            document.close();

            Log.i(TAG, "Invoice generated successfully: " + pdfFile.getAbsolutePath());
            callback.onSuccess(pdfFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found error", e);
            callback.onError("Could not create PDF file: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error generating invoice", e);
            callback.onError("Error generating invoice: " + e.getMessage());
        }
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String getPaymentMethodDisplay(String paymentMethod) {
        switch (paymentMethod) {
            case "toyyibpay":
                return "ToyyibPay";
            case "cash_on_delivery":
                return "Cash on Delivery";
            default:
                return "Unknown";
        }
    }

    /**
     * Check if external storage is available for writing
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
