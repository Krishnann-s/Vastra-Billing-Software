package com.vastra.ui.controllers;

import com.vastra.dao.ProductDAO;
import com.vastra.util.BarcodeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class ProductFormController {
    @FXML private TextField nameField;
    @FXML private TextField variantField;
    @FXML private TextField mrpField;
    @FXML private TextField sellPriceField;
    @FXML private TextField gstField;
    @FXML private TextField stockField;

    @FXML
    public void onSave() {
        try {
            String prod_id = UUID.randomUUID().toString();
            String prod_name = nameField.getText().trim();
            String variant = variantField.getText().trim();
            int mrp = (int) (Double.parseDouble(mrpField.getText())* 100);
            int sellPrice = (int) (Double.parseDouble(sellPriceField.getText()) * 100);
            int gst = Integer.parseInt(gstField.getText());
            int stock = Integer.parseInt(stockField.getText());

            if(prod_name.isEmpty()) {
                showError("Name is required");
                return;
            }

            ProductDAO.insertProduct(prod_id, prod_name, variant, mrp, sellPrice, gst, stock);

            BarcodeUtil.generateCode128(prod_id, "labels/" + prod_id + ".png");

            showSuccess("Product added successfully");
            closeWindow();
        } catch (Exception e) {
            showError("Invalid input or database error\n" + e.getMessage());
        }
    }
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showSuccess(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}
