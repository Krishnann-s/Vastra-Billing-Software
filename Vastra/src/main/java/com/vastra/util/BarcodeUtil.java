package com.vastra.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.File;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BarcodeUtil {

    public static void generateCode128(String text, String outputPath) throws Exception {
        // Create labels directory if it doesn't exist
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, 400, 120);
        MatrixToImageWriter.writeToPath(matrix, "PNG", Paths.get(outputPath));
    }

    public static Image generateBarcodeImage(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            // small margin so barcode fits label
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.CODE_128, width, height, hints);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            // log and return null so the caller can handle it
            e.printStackTrace();
            return null;
        }
    }
}