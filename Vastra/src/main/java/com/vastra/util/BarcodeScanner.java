package com.vastra.util;

import javafx.application.Platform;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

/**
 * Utility class to handle hardware barcode scanner input.
 * Most barcode scanners work as keyboard wedge devices - they simulate keyboard input.
 * This class accumulates rapid key inputs and detects when a complete barcode has been scanned.
 */
public class BarcodeScanner {

    private static final long SCAN_TIMEOUT_MS = 100; // Time between characters for a scan
    private StringBuilder scanBuffer = new StringBuilder();
    private long lastScanTime = 0;
    private Consumer<String> onBarcodeScanned;
    private TextField scanField;

    public BarcodeScanner(TextField scanField, Consumer<String> onBarcodeScanned) {
        this.scanField = scanField;
        this.onBarcodeScanned = onBarcodeScanned;
        setupScannerListener();
    }

    /**
     * Setup listener on the text field to detect rapid input from barcode scanner
     */
    private void setupScannerListener() {
        scanField.textProperty().addListener((obs, oldVal, newVal) -> {
            long currentTime = System.currentTimeMillis();

            // If too much time has passed, this is manual typing, not a scan
            if (currentTime - lastScanTime > SCAN_TIMEOUT_MS && scanBuffer.length() > 0) {
                scanBuffer.setLength(0);
            }

            lastScanTime = currentTime;

            // Barcode scanners typically send Enter/Return at the end
            if (newVal.contains("\n") || newVal.contains("\r")) {
                String barcode = newVal.replace("\n", "").replace("\r", "").trim();
                if (!barcode.isEmpty()) {
                    processScan(barcode);
                }
                Platform.runLater(() -> scanField.clear());
                scanBuffer.setLength(0);
            }
        });

        // Alternative: Listen for Enter key on the field
        scanField.setOnAction(e -> {
            String barcode = scanField.getText().trim();
            if (!barcode.isEmpty()) {
                processScan(barcode);
                scanField.clear();
            }
        });
    }

    /**
     * Process the scanned barcode
     */
    private void processScan(String barcode) {
        if (onBarcodeScanned != null) {
            Platform.runLater(() -> onBarcodeScanned.accept(barcode));
        }
    }

    /**
     * Enable/disable the scanner
     */
    public void setEnabled(boolean enabled) {
        scanField.setDisable(!enabled);
        if (enabled) {
            Platform.runLater(() -> scanField.requestFocus());
        }
    }

    /**
     * Request focus on scan field
     */
    public void requestFocus() {
        Platform.runLater(() -> scanField.requestFocus());
    }
}