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
package name.bindul.bls.dm.imports.ls.parsers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParserUtilsTest {

    static final String[] HEADER_PREFIXES = {
            "bls-",
            "page",
            "name",
            "avg hdcp",
            "high",
            "hdcp",
            "pins",
            "week",
            "team standings",
            "team rosters",
            "temporary substitutes",
            "individual high averages",
            "season high scores",
            "last week's top scores"
        };

    @Test
    void testIsHeaderLine_KnownPrefixesReturnTrue() {
        assertTrue(ParserUtils.isHeaderLine("Page 1", HEADER_PREFIXES));
        assertTrue(ParserUtils.isHeaderLine("Name Avg Hdcp", HEADER_PREFIXES));
        assertTrue(ParserUtils.isHeaderLine("Week 2", HEADER_PREFIXES));
        assertTrue(ParserUtils.isHeaderLine("Team Standings In Each Division", HEADER_PREFIXES));
        assertTrue(ParserUtils.isHeaderLine("Last Week's Top Scores", HEADER_PREFIXES));
        assertTrue(ParserUtils.isHeaderLine("BLS-2026-01", HEADER_PREFIXES));
    }

    @Test
    void testIsHeaderLine_WordBoundaryNearMissesReturnFalse() {
        assertFalse(ParserUtils.isHeaderLine("weekend series", HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("nameplate data", HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("highscore chart", HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("pinsetter report", HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("pages summary", HEADER_PREFIXES));
    }

    @Test
    void testIsHeaderLine_NullBlankAndRegularTextReturnFalse() {
        assertFalse(ParserUtils.isHeaderLine(null, HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("", HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("   ", HEADER_PREFIXES));
        assertFalse(ParserUtils.isHeaderLine("Beer Fall 2026", HEADER_PREFIXES));
    }
}
