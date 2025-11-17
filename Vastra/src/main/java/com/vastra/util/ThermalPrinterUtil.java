package com.vastra.util;

import com.vastra.model.CartItem;
import com.vastra.model.Customer;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for printing bills on thermal printers (58mm or 80mm)
 */
public class ThermalPrinterUtil {

    private static final int RECEIPT_WIDTH = 300; // pixels for 80mm paper
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Generate and print a sales receipt
     */
    public static boolean printReceipt(String invoiceNumber, List<CartItem> items,
                                       Customer customer, double subtotal, double tax,
                                       double discount, double total, String paymentMode) {

        VBox receipt = createReceipt(invoiceNumber, items, customer, subtotal, tax, discount, total, paymentMode);
        return printNode(receipt);
    }

    /**
     * Create receipt layout
     */
    private static VBox createReceipt(String invoiceNumber, List<CartItem> items,
                                      Customer customer, double subtotal, double tax,
                                      double discount, double total, String paymentMode) {

        VBox receipt = new VBox(5);
        receipt.setPadding(new Insets(10));
        receipt.setMaxWidth(RECEIPT_WIDTH);
        receipt.setAlignment(Pos.TOP_CENTER);

        // Store Header
        Text storeName = new Text("VASTRA STORE");
        storeName.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));

        Text storeInfo = new Text("Address Line 1\nAddress Line 2\nPhone: +91-XXXXXXXXXX\nGSTIN: XXXXXXXXXXXX");
        storeInfo.setFont(Font.font("Monospaced", 10));

        Text separator1 = new Text("----------------------------------------");
        separator1.setFont(Font.font("Monospaced", 10));

        // Invoice details
        Text invoiceInfo = new Text(
                "Invoice: " + invoiceNumber + "\n" +
                        "Date: " + LocalDateTime.now().format(DATE_FORMAT) + "\n" +
                        "Cashier: Admin"
        );
        invoiceInfo.setFont(Font.font("Monospaced", 10));

        // Customer details
        String customerText = "Customer: " + (customer != null ? customer.getName() : "Walk-in");
        if (customer != null && customer.getPhone() != null) {
            customerText += "\nPhone: " + customer.getPhone();
            customerText += "\nPoints: " + customer.getPoints();
        }
        Text customerInfo = new Text(customerText);
        customerInfo.setFont(Font.font("Monospaced", 10));

        Text separator2 = new Text("----------------------------------------");
        separator2.setFont(Font.font("Monospaced", 10));

        // Items header
        Text itemsHeader = new Text(String.format("%-20s %3s %6s %8s", "Item", "Qty", "Price", "Total"));
        itemsHeader.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));

        Text separator3 = new Text("----------------------------------------");
        separator3.setFont(Font.font("Monospaced", 10));

        receipt.getChildren().addAll(storeName, storeInfo, separator1, invoiceInfo, customerInfo,
                separator2, itemsHeader, separator3);

        // Items
        for (CartItem item : items) {
            String itemName = item.getProduct().getName();
            if (itemName.length() > 20) itemName = itemName.substring(0, 17) + "...";

            Text itemLine = new Text(String.format("%-20s %3d %6.2f %8.2f",
                    itemName,
                    item.getQuantity(),
                    item.getProduct().getSellPrice(),
                    item.getLineTotal()
            ));
            itemLine.setFont(Font.font("Monospaced", 10));
            receipt.getChildren().add(itemLine);
        }

        Text separator4 = new Text("----------------------------------------");
        separator4.setFont(Font.font("Monospaced", 10));

        // Totals
        StringBuilder totalsBuilder = new StringBuilder();
        totalsBuilder.append(String.format("%-28s %10.2f\n", "Subtotal:", subtotal));
        totalsBuilder.append(String.format("%-28s %10.2f\n", "Tax:", tax));
        if (discount > 0) {
            totalsBuilder.append(String.format("%-28s %10.2f\n", "Discount:", discount));
        }
        totalsBuilder.append("----------------------------------------\n");
        totalsBuilder.append(String.format("%-28s %10.2f", "TOTAL:", total));

        Text totalsText = new Text(totalsBuilder.toString());
        totalsText.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));

        Text separator5 = new Text("----------------------------------------");
        separator5.setFont(Font.font("Monospaced", 10));

        Text paymentInfo = new Text("Payment Mode: " + paymentMode);
        paymentInfo.setFont(Font.font("Monospaced", 10));

        receipt.getChildren().addAll(separator4, totalsText, separator5, paymentInfo);

        // Points earned
        if (customer != null) {
            int pointsEarned = (int) (total / 100);
            Text pointsText = new Text("Points Earned: " + pointsEarned);
            pointsText.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            receipt.getChildren().add(pointsText);
        }

        Text separator6 = new Text("----------------------------------------");
        separator6.setFont(Font.font("Monospaced", 10));

        Text footer = new Text(
                "Thank you for shopping with us!\n" +
                        "Visit us again!\n" +
                        "Goods once sold cannot be returned"
        );
        footer.setFont(Font.font("Monospaced", 9));

        receipt.getChildren().addAll(separator6, footer);

        return receipt;
    }

    /**
     * Print a JavaFX node
     */
    private static boolean printNode(Node node) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            System.err.println("No printer available");
            return false;
        }

        // Configure for thermal printer (smaller paper size)
        PageLayout pageLayout = job.getPrinter().createPageLayout(
                Paper.A5, // or create custom paper size for thermal printer
                PageOrientation.PORTRAIT,
                Printer.MarginType.HARDWARE_MINIMUM
        );

        job.getJobSettings().setPageLayout(pageLayout);

        boolean success = job.printPage(node);
        if (success) {
            job.endJob();
        }
        return success;
    }

    /**
     * Print barcode labels
     */
    public static boolean printBarcodeLabel(String productName, String barcode, double price) {
        VBox label = new VBox(5);
        label.setPadding(new Insets(5));
        label.setAlignment(Pos.CENTER);

        Text name = new Text(productName);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Barcode would be generated as image here
        Text barcodeText = new Text(barcode);
        barcodeText.setFont(Font.font("Monospaced", 10));

        Text priceText = new Text("₹ " + String.format("%.2f", price));
        priceText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        label.getChildren().addAll(name, barcodeText, priceText);

        return printNode(label);
    }
}