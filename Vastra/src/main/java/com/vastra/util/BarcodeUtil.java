package com.vastra.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.nio.file.Path;

public class BarcodeUtil {

    public static void generateCode128(String text, String outputPath) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, 400, 120);
        MatrixToImageWriter.writeToPath(matrix, "PNG", Path.of(outputPath));
    }
}
