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

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Overrides PDFTextStripper to not trim whitespace from the extracted text. This is important for preserving the layout 
 * of the text in the PDF, which may be necessary for parsing tables or other structured data.
 * 
 * Most of this class is inspired by StackOverflow answer: 
 * https://stackoverflow.com/questions/45840842/pdfbox-maintaining-pdf-structure-when-extracting-text
 */
public class LayoutTextStripper extends PDFTextStripper {

    private final float fixedCharWidth;

    private boolean endsWithWS = true;
    private boolean needsWS = false;
    private int chars = 0;
    private float pageLeft = 0;

    public LayoutTextStripper() {
        this(4.0f);
    }

    public LayoutTextStripper(float fixedCharWidth) {
        super();
        // Crucial: Forces PDFBox to process characters from left to right
        this.fixedCharWidth = fixedCharWidth;
        setSortByPosition(true);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        PDRectangle cropBox = page.getCropBox();
        pageLeft = cropBox.getLowerLeftX();
        beginLine();
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        super.writeLineSeparator();
        beginLine();
    }

    private void beginLine() {
        endsWithWS = true;
        needsWS = false;
        chars = 0;
    }

    @Override
    protected void writeWordSeparator() throws IOException {
        needsWS = true;
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        float recentEnd = 0;
        for (TextPosition textPosition: textPositions) {
            String textHere = textPosition.getUnicode();
            float start = textPosition.getXDirAdj();
            boolean spacePresent = endsWithWS || textHere.startsWith(" ");

            if (needsWS || spacePresent || Math.abs(start - recentEnd) > 1) {
                int spacesToInsert = calculateSpacesToInsert(chars, start, needsWS && !spacePresent);
                for (; spacesToInsert > 0; spacesToInsert--) {
                    writeString(" ");
                    chars++;
                }
            }

            writeString(textHere);
            chars += textHere.length();

            needsWS = false;
            endsWithWS = textHere.endsWith(" ");
            recentEnd = getEndX(textPosition);
        }
    }

    int calculateSpacesToInsert(int charsInLineAlready, float chunkStart, boolean spaceRequired) {
        int indexNow = charsInLineAlready;
        int indexToBe = (int)((chunkStart - pageLeft) / fixedCharWidth);
        int spacesToInsert = indexToBe - indexNow;
        if (spacesToInsert < 1 && spaceRequired)
            spacesToInsert = 1;

        return spacesToInsert;
    }

    float getEndX(TextPosition textPosition) {
        return textPosition.getXDirAdj() + textPosition.getWidthDirAdj();
    }
}
