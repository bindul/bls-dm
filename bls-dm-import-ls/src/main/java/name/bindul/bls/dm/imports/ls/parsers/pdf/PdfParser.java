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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * 
 */
public class PdfParser {

	private static final int DEFAULT_OCR_DPI = 300;
	private static final Pattern HEADING_PATTERN = Pattern.compile(
		"^(?:\\d+(?:\\.\\d+)*\\s+)?[A-Z][A-Za-z0-9 '&()/_\\-]{2,80}$"
	);

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

	public List<PdfSection> extractSections(Path pdfPath, OcrOptions ocrOptions) throws IOException {
		validatePdfPath(pdfPath);
		Objects.requireNonNull(ocrOptions, "ocrOptions cannot be null");

		try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
			String fullText = extractText(document, ocrOptions);
			List<PdfSection> sections = extractBookmarkSections(document);
			if (sections.isEmpty()) {
				sections = extractHeadingSections(fullText);
			}
			if (sections.isEmpty()) {
				sections = List.of(new PdfSection("Document", fullText.strip()));
			}
			return List.copyOf(sections);
		}
	}

	public List<PdfSection> extractSections(Path pdfPath) throws IOException {
		return extractSections(pdfPath, OcrOptions.disabled());
	}

	private String extractText(PDDocument document, OcrOptions ocrOptions) throws IOException {
		String nativeText = extractNativeText(document);
		if (!nativeText.isBlank() || !ocrOptions.enabled()) {
			return nativeText;
		}
		return extractTextWithOcr(document, ocrOptions);
	}

	private String extractNativeText(PDDocument document) throws IOException {
		PDFTextStripper stripper = new PDFTextStripper();
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

	private List<PdfSection> extractBookmarkSections(PDDocument document) throws IOException {
		PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
		if (outline == null || outline.getFirstChild() == null) {
			return List.of();
		}

		List<BookmarkSection> bookmarks = new ArrayList<>();
		collectBookmarkSections(document, outline.getFirstChild(), "", bookmarks);
		if (bookmarks.isEmpty()) {
			return List.of();
		}

		bookmarks.sort((left, right) -> Integer.compare(left.startPage(), right.startPage()));
		List<PdfSection> sections = new ArrayList<>();
		for (int i = 0; i < bookmarks.size(); i++) {
			BookmarkSection current = bookmarks.get(i);
			int endPage = i + 1 < bookmarks.size() ? bookmarks.get(i + 1).startPage() - 1 : document.getNumberOfPages();
			if (current.startPage() > endPage) {
				continue;
			}
			String text = extractPages(document, current.startPage(), endPage).strip();
			if (!text.isBlank()) {
				sections.add(new PdfSection(current.title(), text));
			}
		}
		return sections;
	}

	private void collectBookmarkSections(
		PDDocument document,
		PDOutlineItem outlineItem,
		String parentTitle,
		List<BookmarkSection> bookmarks
	) throws IOException {
		PDOutlineItem current = outlineItem;
		while (current != null) {
			String title = normalizeSectionTitle(parentTitle, current.getTitle());
			Integer startPage = resolveBookmarkPage(document, current);
			if (startPage != null && !title.isBlank()) {
				bookmarks.add(new BookmarkSection(title, startPage));
			}
			if (current.getFirstChild() != null) {
				collectBookmarkSections(document, current.getFirstChild(), title, bookmarks);
			}
			current = current.getNextSibling();
		}
	}

	private Integer resolveBookmarkPage(PDDocument document, PDOutlineItem outlineItem) throws IOException {
		PDDestination destination = outlineItem.getDestination();
		if (destination == null && outlineItem.getAction() instanceof PDActionGoTo goTo) {
			destination = goTo.getDestination();
		}

		if (destination instanceof PDPageDestination pageDestination) {
			PDPage page = pageDestination.getPage();
			if (page != null) {
				int index = document.getPages().indexOf(page);
				if (index >= 0) {
					return index + 1;
				}
			}

			int pageNumber = pageDestination.getPageNumber();
			if (pageNumber >= 0) {
				return pageNumber + 1;
			}
		}

		return null;
	}

	private List<PdfSection> extractHeadingSections(String fullText) {
		String[] lines = fullText.split("\\R");
		List<Integer> headingIndices = new ArrayList<>();
		for (int i = 0; i < lines.length; i++) {
			if (isLikelyHeading(lines[i])) {
				headingIndices.add(i);
			}
		}

		if (headingIndices.isEmpty()) {
			return List.of();
		}

		List<PdfSection> sections = new ArrayList<>();
		for (int i = 0; i < headingIndices.size(); i++) {
			int startIndex = headingIndices.get(i);
			int endIndex = i + 1 < headingIndices.size() ? headingIndices.get(i + 1) : lines.length;
			String title = normalizeSectionTitle("", lines[startIndex]);
			StringBuilder content = new StringBuilder();
			for (int lineIndex = startIndex + 1; lineIndex < endIndex; lineIndex++) {
				String line = lines[lineIndex].stripTrailing();
				if (!line.isBlank()) {
					if (!content.isEmpty()) {
						content.append('\n');
					}
					content.append(line);
				}
			}
			String sectionText = content.length() == 0 ? fullText.strip() : content.toString().strip();
			if (!title.isBlank() && !sectionText.isBlank()) {
				sections.add(new PdfSection(title, sectionText));
			}
		}
		return sections;
	}

	private String extractPages(PDDocument document, int startPage, int endPage) throws IOException {
		PDFTextStripper stripper = new PDFTextStripper();
		stripper.setSortByPosition(true);
		stripper.setAddMoreFormatting(true);
		stripper.setStartPage(startPage);
		stripper.setEndPage(endPage);
		return stripper.getText(document);
	}

	private boolean isLikelyHeading(String line) {
		String normalized = normalize(line);
		if (normalized.isBlank() || normalized.length() > 80) {
			return false;
		}
		if (normalized.endsWith(".")) {
			return false;
		}
		if (normalized.equals(normalized.toUpperCase(Locale.ROOT)) && normalized.chars().anyMatch(Character::isLetter)) {
			return true;
		}
		return HEADING_PATTERN.matcher(normalized).matches();
	}

	private String normalizeSectionTitle(String parentTitle, String title) {
		String normalizedTitle = normalize(title);
		if (parentTitle == null || parentTitle.isBlank()) {
			return normalizedTitle;
		}
		String normalizedParent = normalize(parentTitle);
		if (normalizedParent.isBlank()) {
			return normalizedTitle;
		}
		if (normalizedTitle.isBlank()) {
			return normalizedParent;
		}
		return normalizedParent + " / " + normalizedTitle;
	}

	private void validatePdfPath(Path pdfPath) {
		Objects.requireNonNull(pdfPath, "pdfPath cannot be null");
		if (!Files.exists(pdfPath)) {
			throw new IllegalArgumentException("PDF file does not exist: " + pdfPath);
		}
	}

	private String normalize(String line) {
		return line == null ? "" : line.trim().replace('\u00A0', ' ');
	}

	public record OcrOptions(boolean enabled, String language, String modelResource, int pageSegMode, int dpi) {
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

	private record BookmarkSection(String title, int startPage) {
	}

}
