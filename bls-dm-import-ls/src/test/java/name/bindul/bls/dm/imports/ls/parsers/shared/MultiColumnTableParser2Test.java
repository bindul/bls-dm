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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MultiColumnTableParser2Test {

    @Test
    void testParseColumns_00_SingleCharacterColumn() throws Exception {
        List<String> lines = List.of(
                "    Player P008                               0        0          0      0      0      0                                                0        30"
        );
        MultiColumnTableParser2 parser = new MultiColumnTableParser2();
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(0, lines.size()));
        log.debug("Parsed single character columns: {}", columnGroups);
        assertEquals(1, columnGroups.size(), "Expected 1 column group to be parsed");
        assertEquals(9, columnGroups.get(0).getColumns().size(), "Expected 9 columns in the column group");
        ColumnDescriptor firstNumCol = columnGroups.get(0).getColumns().get(1);
        assertEquals("0", lines.get(0).substring(firstNumCol.start(), firstNumCol.start() + firstNumCol.length()) , "Expected first '0' to be at index 44");
    }

    @Test
    void testParseColumns_01_SimpleTable_LeftAligned() throws Exception {
        List<String> lines = parseFile("column-test.txt");
        MultiColumnTableParser2 parser = new MultiColumnTableParser2(2, MultiColumnTableParser2.DEFAULT_MAX_COL_COUNT_DIFF_TO_MERGE);
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(1, 5)); // See column-test.txt for the lines being parsed
        log.debug("Parsed simple table columns: {}", columnGroups);
        assertEquals(1, columnGroups.size(), "Expected 1 column group to be parsed");
        ColumnGroup columnGroup = columnGroups.get(0);
        assertEquals(4, columnGroup.getColumns().size(), "Expected 4 columns in the column group");
        assertEquals(new ColumnDescriptor(0, 7, null), columnGroup.getColumns().get(0), "Name column should start at 0 and have length 7");
        assertEquals(new ColumnDescriptor(12, 3, null), columnGroup.getColumns().get(1), "Age column should start at 12 and have length 3");
        assertEquals(new ColumnDescriptor(19, 11, null), columnGroup.getColumns().get(2), "City column should start at 19 and have length 11");
        assertEquals(new ColumnDescriptor(32, 7, null), columnGroup.getColumns().get(3), "Country column should start at 32 and have length 7");
        assertEquals(Arrays.asList(1, 2, 3, 4), columnGroup.getLines(), "Expected column indices to be [1, 2, 3, 4]");
    }

    @Test
    void testParseColumns_02_SimpleTable_MixedAligned() throws Exception {
        List<String> lines = parseFile("column-test.txt");
        MultiColumnTableParser2 parser = new MultiColumnTableParser2();
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(7, 11)); // See column-test.txt for the lines being parsed
        log.debug("Parsed left aligned table columns: {}", columnGroups);
        assertEquals(1, columnGroups.size(), "Expected 1 column group to be parsed");
        ColumnGroup columnGroup = columnGroups.get(0);
        assertEquals(3, columnGroup.getColumns().size(), "Expected 3 columns in the column group");
        assertEquals(new ColumnDescriptor(0, 7, null), columnGroup.getColumns().get(0), "Name column should start at 0 and have length 7");
        assertEquals(new ColumnDescriptor(12, 7, null), columnGroup.getColumns().get(1), "Average column should start at 12 and have length 7");
        assertEquals(new ColumnDescriptor(24, 6, null), columnGroup.getColumns().get(2), "Team column should start at 24 and have length 6");
    }

    @Test
    void testParseColumns_03_SimpleTable_ConsecutiveMixedAligned() throws Exception {
        List<String> lines = parseFile("column-test.txt");
        MultiColumnTableParser2 parser = new MultiColumnTableParser2();
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(13, 17)); // See column-test.txt for the lines being parsed
        log.debug("Parsed consecutive left aligned column table columns: {}", columnGroups);
        assertEquals(1, columnGroups.size(), "Expected 1 column group to be parsed");
        ColumnGroup columnGroup = columnGroups.get(0);
        assertEquals(4, columnGroup.getColumns().size(), "Expected 4 columns in the column group");
        assertEquals(new ColumnDescriptor(0, 7, null), columnGroup.getColumns().get(0), "Name column should start at 0 and have length 7");
        assertEquals(new ColumnDescriptor(15, 4, null), columnGroup.getColumns().get(1), "Average column should start at 15 and have length 4");
        assertEquals(new ColumnDescriptor(24, 7, null), columnGroup.getColumns().get(2), "Score column should start at 24 and have length 7");
        assertEquals(new ColumnDescriptor(34, 5, null), columnGroup.getColumns().get(3), "Lanes column should start at 34 and have length 5");
    }

    @Test
    void testParseColumns_04_SimpleTable_FirstLeftAligned() throws Exception {
        List<String> lines = parseFile("column-test.txt");
        MultiColumnTableParser2 parser = new MultiColumnTableParser2(2, MultiColumnTableParser2.DEFAULT_MAX_COL_COUNT_DIFF_TO_MERGE);
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(19, 23)); // See column-test.txt for the lines being parsed
        log.debug("Parsed first left aligned column table columns: {}", columnGroups);
        assertEquals(1, columnGroups.size(), "Expected 1 column group to be parsed");
        ColumnGroup columnGroup = columnGroups.get(0);
        assertEquals(3, columnGroup.getColumns().size(), "Expected 4 columns in the column group");
        assertEquals(new ColumnDescriptor(2, 7, null), columnGroup.getColumns().get(0), "Average column should start at 12 and have length 7");
        assertEquals(new ColumnDescriptor(14, 7, null), columnGroup.getColumns().get(1), "Score column should start at 24 and have length 7");
        assertEquals(new ColumnDescriptor(24, 5, null), columnGroup.getColumns().get(2), "Lanes column should start at 34 and have length 5");
    }

    @Test
    void testParseColumns_05_PlayersTable_EmptyColumns_MultiLineHeaders () throws Exception {
        List<String> lines = parseFile("column-test.txt");
        MultiColumnTableParser2 parser = new MultiColumnTableParser2(MultiColumnTableParser2.DEFAULT_MIN_SPACE_BETWEEN_COLUMNS, 5);
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(25, 47));
        log.debug("Parsed players table columns: {}", columnGroups);

        assertEquals(3, columnGroups.size(), "Expected 3 column groups to be parsed"); // Title, Header, Data
        // TODO Additional assertions to verify the correctness of the parsed column groups, headers, and data columns.
    }

    @Test
    void testParseColumns_06_PlayersTable_EmptyColumns_MultiLineHeaders () throws Exception {
        List<String> lines = parseFile("column-test.txt");
        MultiColumnTableParser2 parser = new MultiColumnTableParser2(MultiColumnTableParser2.DEFAULT_MIN_SPACE_BETWEEN_COLUMNS, 3);
        List<ColumnGroup> columnGroups = parser.identifyColumns(lines, Pair.of(49, 54));
        log.debug("Parsed empty columns table columns: \n{}", prettyPrintColumnGroups(columnGroups));

        assertEquals(1, columnGroups.size(), "Expected 1 column groups to be parsed"); // Title, Header, Data
        ColumnGroup columnGroup = columnGroups.get(0);
        assertEquals(4, columnGroup.getColumns().size(), "Expected 4 columns in the column group");
    }

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

    private String prettyPrintColumnGroups(List<ColumnGroup> columnGroups) {
        StringBuilder sb = new StringBuilder();
        for (ColumnGroup group : columnGroups) {
            if (null == group) {
                continue;
            }
            sb.append("Column Group: lines ").append(group.getLines()).append("\n");
            for (ColumnDescriptor col : group.getColumns()) {
                sb.append("\tcol: ").append(col.start()).append(" [").append(col.length()).append("]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
