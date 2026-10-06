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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PdfParserTest {

    private static final Path SAMPLE_PDF = Path.of("src", "test", "resources", "13301609102026f202602standg00.pdf");

    @Test
    void extractText_shouldReturnFullDocumentText() throws Exception {
        PdfParser parser = new PdfParser();

        String text = parser.extractText(SAMPLE_PDF);

        assertNotNull(text);
        assertFalse(text.isBlank());
        assertTrue(text.contains("James Bayarsaikhan"));
    }
}
