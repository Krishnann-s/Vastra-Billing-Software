package com.vastra.ui.controllers;

import com.vastra.dao.CustomerDAO;
import com.vastra.dao.ProductDAO;
import com.vastra.dao.SalesDAO;
import com.vastra.model.CartItem;
import com.vastra.model.Customer;
import com.vastra.model.Product;
import com.vastra.util.BarcodeUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML private TextField txtProductSearch;    // replaced scanField
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> nameColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    @FXML private Label totalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label customerPointsLabel;
    @FXML private TextField discountField;
    @FXML private Button btnPrintBarcodes;      // aligned with FXML fx:id

    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private Customer currentCustomer = null;

    @FXML
    public void initialize(){
        setupCartTable();

        // Use product search textbox instead of scan field
        if (txtProductSearch != null) {
            txtProductSearch.requestFocus();
            txtProductSearch.setOnAction(e -> handleProductSearch());
        }

        if (discountField != null) {
            discountField.setText("0");
        }
    }

    private void setupCartTable() {
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct().getDisplayName()));

        qtyColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        priceColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getProduct().getSellPrice()).asObject());

        totalColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getLineTotal()).asObject());

        cartTable.setItems(cartItems);
    }

    /**
     * Handles product search / manual barcode entry. The user types SKU / id and presses Enter.
     */
    private void handleProductSearch() {
        if (txtProductSearch == null) return;
        String code = txtProductSearch.getText().trim();
        if (code.isEmpty()) return;

        try {
            // Try to find product by ID or SKU. Replace with your DAO lookup method if different.
            Product product = ProductDAO.findById(code);
            if (product == null) {
                // Optionally try search by SKU or name - you can add more lookup attempts here
                product = ProductDAO.findBySku(code); // if you have this method; otherwise skip
            }

            if (product == null) {
                showError("Product not found: " + code);
                txtProductSearch.clear();
                return;
            }

            if (product.getStock() <= 0) {
                showError("Product out of stock: " + product.getDisplayName());
                txtProductSearch.clear();
                return;
            }

            addToCart(product);
            txtProductSearch.clear();
            updateTotals();

        } catch (Exception e) {
            showError("Error searching product: " + e.getMessage());
        }
    }

    private void addToCart(Product product) {
        // Check if product already in cart
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                if (item.getQuantity() < product.getStock()) {
                    item.incrementQuantity();
                    cartTable.refresh();
                    return;
                } else {
                    showError("Cannot add more. Only " + product.getStock() + " in stock");
                    return;
                }
            }
        }
        // Add new item
        cartItems.add(new CartItem(product, 1));
    }

    private void updateTotals() {
        double subtotal = 0;
        double tax = 0;

        for (CartItem item : cartItems) {
            subtotal += item.getLineTotal();
            tax += item.getTaxAmount();
        }

        double discount = 0;
        try {
            if (discountField != null && !discountField.getText().isEmpty()) {
                discount = Double.parseDouble(discountField.getText());
            }
        } catch (NumberFormatException e) {
            discount = 0;
        }

        double total = subtotal - discount;

        if (subtotalLabel != null) subtotalLabel.setText(String.format("₹%.2f", subtotal));
        if (taxLabel != null) taxLabel.setText(String.format("₹%.2f", tax));
        if (totalLabel != null) totalLabel.setText(String.format("₹%.2f", total));
    }

    @FXML
    public void onAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vastra/ui/fxml/product_form.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening product form: " + e.getMessage());
        }
    }

    @FXML
    public void onAddCustomer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Customer");
        dialog.setHeaderText("Enter customer phone number");
        dialog.setContentText("Phone:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(phone -> {
            try {
                Customer customer = CustomerDAO.findByPhone(phone);
                if (customer == null) {
                    // Create new customer
                    TextInputDialog nameDialog = new TextInputDialog();
                    nameDialog.setTitle("New Customer");
                    nameDialog.setHeaderText("Customer not found. Create new?");
                    nameDialog.setContentText("Name:");

                    Optional<String> nameResult = nameDialog.showAndWait();
                    if (nameResult.isPresent()) {
                        customer = CustomerDAO.createCustomer(nameResult.get(), phone, "");
                    }
                }

                if (customer != null) {
                    currentCustomer = customer;
                    if (customerNameLabel != null) customerNameLabel.setText(customer.getName());
                    if (customerPointsLabel != null) customerPointsLabel.setText(customer.getPoints() + " points");
                }

            } catch (Exception e) {
                showError("Error loading customer: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onRedeemPoints() {
        if (currentCustomer == null) {
            showError("Please add customer first");
            return;
        }

        if (currentCustomer.getPoints() < 100) {
            showError("Customer needs at least 100 points to redeem");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("100");
        dialog.setTitle("Redeem Points");
        dialog.setHeaderText("Customer has " + currentCustomer.getPoints() + " points");
        dialog.setContentText("Points to redeem (100 points = ₹100):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(pointsStr -> {
            try {
                int points = Integer.parseInt(pointsStr);
                if (points > currentCustomer.getPoints()) {
                    showError("Customer doesn't have enough points");
                    return;
                }
                if (points % 100 != 0) {
                    showError("Points must be in multiples of 100");
                    return;
                }
                if (discountField != null) {
                    discountField.setText(String.valueOf(points));
                    updateTotals();
                }
            } catch (NumberFormatException e) {
                showError("Invalid points value");
            }
        });
    }

    @FXML
    public void onCompleteSale() {
        if (cartItems.isEmpty()) {
            showError("Cart is empty");
            return;
        }

        ChoiceDialog<String> paymentDialog = new ChoiceDialog<>("CASH", "CASH", "CARD", "UPI");
        paymentDialog.setTitle("Payment Method");
        paymentDialog.setHeaderText("Select payment method");

        Optional<String> paymentResult = paymentDialog.showAndWait();
        if (!paymentResult.isPresent()) return;

        try {
            int discountCents = 0;
            if (discountField != null && !discountField.getText().isEmpty()) {
                discountCents = (int) (Double.parseDouble(discountField.getText()) * 100);
            }
            String customerId = currentCustomer != null ? currentCustomer.getId() : null;

            // Redeem points if used
            if (currentCustomer != null && discountCents > 0) {
                int pointsToRedeem = discountCents / 100; // 1 rupee = 1 point
                CustomerDAO.redeemPoints(currentCustomer.getId(), pointsToRedeem);
            }

            String saleId = SalesDAO.completeSale(
                    new ArrayList<>(cartItems),
                    customerId,
                    discountCents,
                    paymentResult.get()
            );

            showSuccess("Sale completed! ID: " + saleId);
            clearCart();

        } catch (Exception e) {
            showError("Error completing sale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onClearCart() {
        clearCart();
    }

    private void clearCart() {
        cartItems.clear();
        currentCustomer = null;
        if (customerNameLabel != null) customerNameLabel.setText("Walk-in Customer");
        if (customerPointsLabel != null) customerPointsLabel.setText("0 points");
        if (discountField != null) discountField.setText("0");
        updateTotals();
        if (txtProductSearch != null) txtProductSearch.requestFocus();
    }

    // Stub methods for buttons that don't have handlers yet
    @FXML public void onBulkImport() { showInfo("Bulk Import - Coming Soon!"); }
    @FXML public void onShowReports() { showInfo("Reports - Coming Soon!"); }
    @FXML public void onShowLowStock() { showInfo("Low Stock - Coming Soon!"); }
    @FXML public void onShowReturns() { showInfo("Returns - Coming Soon!"); }
    @FXML public void onShowSettings() { showInfo("Settings - Coming Soon!"); }
    @FXML public void onClearCustomer() {
        currentCustomer = null;
        if (customerNameLabel != null) customerNameLabel.setText("Walk-in Customer");
        if (customerPointsLabel != null) customerPointsLabel.setText("0 points");
    }
    @FXML public void onAddItemManually() { showInfo("Add Item Manually - Coming Soon!"); }
    @FXML public void onHoldSale() { showInfo("Hold Sale - Coming Soon!"); }
    @FXML public void onPrintBill() { showInfo("Print Bill - Coming Soon!"); }
    @FXML public void onEmailBill() { showInfo("Email Bill - Coming Soon!"); }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    private void onPrintBarcodes() {
        List<Product> products;
        try {
            products = ProductDAO.getAllProducts();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load products for barcode print: " + e.getMessage());
            return;
        }

        if (products == null || products.isEmpty()) {
            // show alert or notification
            System.out.println("No products to print.");
            return;
        }

        // 2. Create printable pages (grid of barcode labels)
        int labelsPerRow = 3;
        int labelsPerCol = 4;
        int labelsPerPage = labelsPerRow * labelsPerCol;

        List<Node> pages = new ArrayList<>();
        int index = 0;
        while (index < products.size()) {
            GridPane pageGrid = new GridPane();
            pageGrid.setHgap(10);
            pageGrid.setVgap(10);
            pageGrid.setPadding(new javafx.geometry.Insets(20));

            for (int r = 0; r < labelsPerCol; r++) {
                for (int c = 0; c < labelsPerRow; c++) {
                    if (index >= products.size()) break;
                    Product p = products.get(index++);

                    // Create a small VBox label: product name + barcode image + sku/price if desired
                    VBox labelBox = new VBox(4);
                    labelBox.setPrefWidth(180); // tweak to fit label size
                    labelBox.setPrefHeight(100);
                    Text name = new Text(p.getName());
                    name.setWrappingWidth(160);

                    // Generate barcode image: use SKU or product id
                    String code = p.getSku() != null ? p.getSku() : String.valueOf(p.getId());
                    Image barcodeImage = BarcodeUtil.generateBarcodeImage(code, 300, 80);
                    ImageView iv = new ImageView(barcodeImage);
                    iv.setPreserveRatio(true);
                    iv.setFitWidth(160); // adjust for printable label width

                    Text skuText = new Text(code);

                    labelBox.getChildren().addAll(name, iv, skuText);
                    pageGrid.add(labelBox, c, r);
                }
            }
            pages.add(pageGrid);
        }

        // 3. Print pages using PrinterJob
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            System.out.println("No printers available.");
            return;
        }

        // Optionally show print dialog to choose printer and settings
        Window window = btnPrintBarcodes.getScene().getWindow();
        boolean proceed = job.showPrintDialog(window);
        if (!proceed) {
            job.endJob();
            return;
        }

        PageLayout pageLayout = job.getJobSettings().getPageLayout();
        for (Node pageNode : pages) {
            // Optionally scale node to page printable width
            double scaleX = pageLayout.getPrintableWidth() / pageNode.prefWidth(-1);
            double scaleY = pageLayout.getPrintableHeight() / pageNode.prefHeight(-1);
            double scale = Math.min(scaleX, scaleY);
            pageNode.setScaleX(scale);
            pageNode.setScaleY(scale);

            boolean printed = job.printPage(pageNode);
            pageNode.setScaleX(1);
            pageNode.setScaleY(1);
            if (!printed) {
                System.out.println("Failed to print one page.");
                break;
            }
        }
        job.endJob();
    }

}
