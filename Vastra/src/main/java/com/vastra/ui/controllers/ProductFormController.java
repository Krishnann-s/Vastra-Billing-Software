package com.vastra.ui.controllers;

import com.vastra.dao.ProductDAO;
import com.vastra.util.BarcodeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ProductFormController {
    @FXML private TextField nameField;
    @FXML private TextField variantField;
    @FXML private TextField categoryField;
    @FXML private TextField brandField;
    @FXML private TextField skuField;
    @FXML private TextField mrpField;
    @FXML private TextField sellPriceField;
    @FXML private TextField purchasePriceField;
    @FXML private TextField gstField;
    @FXML private TextField hsnField;
    @FXML private TextField stockField;
    @FXML private TextField reorderField;
    @FXML private ComboBox<String> unitCombo;
    @FXML private TextArea descriptionField;

    @FXML
    public void initialize() {
        // Setup unit combo box
        if (unitCombo != null) {
            unitCombo.getItems().addAll("PCS", "KG", "GRAM", "LITER", "METER", "BOX", "DOZEN");
            unitCombo.setValue("PCS");
        }

        // Set default values
        if (gstField != null) gstField.setText("18");
        if (reorderField != null) reorderField.setText("5");
    }

    @FXML
    public void onSave() {
        try {
            // Validate required fields
            if (nameField.getText().trim().isEmpty()) {
                showError("Product name is required");
                nameField.requestFocus();
                return;
            }

            if (sellPriceField.getText().trim().isEmpty()) {
                showError("Selling price is required");
                sellPriceField.requestFocus();
                return;
            }

            if (stockField.getText().trim().isEmpty()) {
                showError("Stock quantity is required");
                stockField.requestFocus();
                return;
            }

            // Parse values
            String name = nameField.getText().trim();
            String variant = variantField.getText().trim();
            String category = categoryField.getText().trim();
            String brand = brandField.getText().trim();
            String sku = skuField.getText().trim();

            int mrp = (int) (Double.parseDouble(mrpField.getText().trim()) * 100);
            int sellPrice = (int) (Double.parseDouble(sellPriceField.getText().trim()) * 100);
            int purchasePrice = 0;
            if (!purchasePriceField.getText().trim().isEmpty()) {
                purchasePrice = (int) (Double.parseDouble(purchasePriceField.getText().trim()) * 100);
            }

            int gst = Integer.parseInt(gstField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            int reorderThreshold = Integer.parseInt(reorderField.getText().trim());

            // Validation
            if (sellPrice <= 0) {
                showError("Selling price must be greater than 0");
                return;
            }

            if (stock < 0) {
                showError("Stock cannot be negative");
                return;
            }

            if (gst < 0 || gst > 28) {
                showError("GST must be between 0 and 28");
                return;
            }

            // Check if SKU already exists
            if (!sku.isEmpty() && ProductDAO.findBySku(sku) != null) {
                showError("SKU already exists: " + sku);
                return;
            }

            // Insert product
            String productId = ProductDAO.insertProduct(
                    name, variant, mrp, sellPrice, gst, stock,
                    category, brand, sku
            );

            // Generate barcode image for printing
            try {
                String barcode = sku.isEmpty() ? productId : sku;
                BarcodeUtil.generateCode128(barcode, "labels/" + productId + ".png");
            } catch (Exception e) {
                System.err.println("Could not generate barcode image: " + e.getMessage());
            }

            showSuccess("Product added successfully!\nBarcode label created.");
            closeWindow();

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for price, GST, and stock fields");
        } catch (Exception e) {
            showError("Error saving product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

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
        alert.show();
    }
}