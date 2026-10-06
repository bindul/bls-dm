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
package name.bindul.bls.dm.imports.ls.parsers.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import lombok.extern.log4j.Log4j2;

@Log4j2
class MultiColumnTableParserTest {

    @Test
    void testParsePlayersTable() throws Exception {
        List<String> lines = parseFile("players.txt");
        MultiColumnTableParser parser = new MultiColumnTableParser();
        List<ColumnDescriptor> columns = parser.identifyColumns(lines, Pair.of(0, lines.size()));
        log.debug("Parsed player columns: {}", columns);

/*
                                                                             High    High    HDCP     HDCP                                   HDCP
    Name                                      Avg HDCP        Pins  Gms      Game    Sers   Game      Sers     -1-     -2-    -3-    Total   Total
    1 - Team A4
    Player P001                             177       29       1600      9    254    580     269      625       173    179     161    513      591
 */
        assertEquals(14, columns.size(), "Expected 14 columns to be parsed");
        // Name
        assertEquals(new ColumnDescriptor(5, 40, "Name"), columns.get(0));
        // TODO add assertions to validate the parsed columns
        // [ColumnDescriptor[start=0, length=41, name=Name], ColumnDescriptor[start=41, length=8, name=Avg HDCP], ColumnDescriptor[start=49, length=11, name=110], ColumnDescriptor[start=60, length=9, name=Pins Gms], ColumnDescriptor[start=69, length=5, name=9], ColumnDescriptor[start=74, length=7, name=High Game], ColumnDescriptor[start=81, length=8, name=High Sers], ColumnDescriptor[start=89, length=9, name=HDCP Game], ColumnDescriptor[start=98, length=11, name=HDCP Sers -1-], ColumnDescriptor[start=109, length=7, name=79], ColumnDescriptor[start=116, length=8, name=-2- -3-], ColumnDescriptor[start=124, length=6, name=59], ColumnDescriptor[start=130, length=9, name=Total], ColumnDescriptor[start=139, length=3, name=HDCP Total]]
    }

    @Test
    void testParseSubstitutesTable() throws Exception {
        List<String> lines = parseFile("substitutes.txt");
        MultiColumnTableParser parser = new MultiColumnTableParser();
        List<ColumnDescriptor> columns = parser.identifyColumns(lines, Pair.of(0, lines.size()));
        log.debug("Parsed substitutes columns: {}", columns);
        // TODO add assertions to validate the parsed columns
    }

//    @Test
//    void testParseAccolades1Table() throws Exception {
//        List<String> lines = parseFile("accolades.txt");
//        MultiColumnTableParser parser = new MultiColumnTableParser();
//        List<ColumnDescriptor> columns = parser.identifyColumns(lines, Pair.of(0, lines.size()));
//        log.debug("Parsed accolades table: {}", columns);
//        // TODO add assertions to validate the parsed columns
//    }

    private List<String> parseFile(String fileName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }
            try (Scanner scanner = new Scanner(is)) {
                return scanner.useDelimiter("\\A").hasNext() ? List.of(scanner.next().split("\\R")) : List.of();
            }
        }
    }
}
