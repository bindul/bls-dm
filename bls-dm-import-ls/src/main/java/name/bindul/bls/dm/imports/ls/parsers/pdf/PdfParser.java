/*
 * Copyright (c) 2026. Bindul Bhowmik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.bindul.bls.dm.imports.ls.parsers.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class PdfParser {

    private static final float DEFAULT_OCR_DPI = 300f;
    private final ConcurrentMap<OcrOptions, Path> ocrDataPaths = new ConcurrentHashMap<>();

    public String extractText(Path pdfPath) throws IOException {
        return extractText(pdfPath, OcrOptions.disabled());
    }

    public String extractText(Path pdfPath, OcrOptions ocrOptions) throws IOException {
        validatePdfPath(pdfPath);
        Objects.requireNonNull(ocrOptions, "ocrOptions cannot be null");

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            String nativeText = extractNativeText(document);
            if (!nativeText.isBlank() || !ocrOptions.enabled()) {
                return nativeText;
            }

            return extractTextWithOcr(document, ocrOptions);
        }
    }

    private String extractNativeText(PDDocument document) throws IOException {
        final LayoutTextStripper stripper = new LayoutTextStripper();
        stripper.setSortByPosition(true);
        stripper.setAddMoreFormatting(true);
        return stripper.getText(document);
    }

    private String extractTextWithOcr(PDDocument document, OcrOptions ocrOptions) throws IOException {
        try {
            Tesseract tesseract = createTesseract(ocrOptions);
            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder text = new StringBuilder();
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, ocrOptions.dpi(), ImageType.RGB);
                text.append(tesseract.doOCR(image)).append('\n');
            }
            return text.toString();
        } catch (TesseractException ex) {
            throw new IOException("Unable to OCR PDF", ex);
        }
    }

    private Tesseract createTesseract(OcrOptions ocrOptions) throws IOException {
        if (!ocrOptions.enabled()) {
            throw new IllegalStateException("Tesseract OCR is not enabled");
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(getOrCreateOcrDataPath(ocrOptions).toString());
        tesseract.setLanguage(ocrOptions.language());
        tesseract.setPageSegMode(ocrOptions.pageSegMode());
        return tesseract;
    }

    private Path getOrCreateOcrDataPath(OcrOptions ocrOptions) throws IOException {
        Path cachedDataPath = ocrDataPaths.get(ocrOptions);
        if (cachedDataPath != null) {
            return cachedDataPath;
        }

        Path directory = Files.createTempDirectory("bls-ls-tesseract-");
        directory.toFile().deleteOnExit();

        Path modelFile = directory.resolve(ocrOptions.language() + ".traineddata");
        try (InputStream input = PdfParser.class.getClassLoader().getResourceAsStream(ocrOptions.modelResource())) {
            if (input == null) {
                throw new IOException("OCR model resource not found: " + ocrOptions.modelResource());
            }
            Files.copy(input, modelFile, StandardCopyOption.REPLACE_EXISTING);
        }
        modelFile.toFile().deleteOnExit();

        Path previous = ocrDataPaths.putIfAbsent(ocrOptions, directory);
        return previous != null ? previous : directory;
    }

    private void validatePdfPath(Path pdfPath) {
        Objects.requireNonNull(pdfPath, "pdfPath cannot be null");
        if (!Files.exists(pdfPath)) {
            throw new IllegalArgumentException("PDF file does not exist: " + pdfPath);
        }
    }

    public record OcrOptions(boolean enabled, String language, String modelResource, int pageSegMode, float dpi) {
        public OcrOptions {
            if (enabled) {
                language = requireNotBlank(language, "language cannot be null or blank");
                modelResource = requireNotBlank(modelResource, "modelResource cannot be null or blank");
            }
            if (pageSegMode <= 0) {
                pageSegMode = 3;
            }
            if (dpi <= 0) {
                dpi = DEFAULT_OCR_DPI;
            }
        }

        public static OcrOptions disabled() {
            return new OcrOptions(false, "", "", 3, DEFAULT_OCR_DPI);
        }

        private static String requireNotBlank(String value, String message) {
            if (value == null || value.trim().isBlank()) {
                throw new IllegalArgumentException(message);
            }
            return value.trim();
        }
    }

}
