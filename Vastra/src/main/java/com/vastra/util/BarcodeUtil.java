package com.vastra.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}