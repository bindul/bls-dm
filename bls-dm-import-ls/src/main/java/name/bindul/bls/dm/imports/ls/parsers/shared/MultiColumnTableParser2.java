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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MultiColumnTableParser2 {

    private static final int DEFAULT_MIN_SPACE_BETWEEN_COLUMNS = 3; // Minimum space between columns to consider them separate

    private final int minSpaceBetweenColumns;// Minimum space between columns to consider them separate

    public MultiColumnTableParser2() {
        this(DEFAULT_MIN_SPACE_BETWEEN_COLUMNS);
    }

    public MultiColumnTableParser2(int minSpaceBetweenColumns) {
        this.minSpaceBetweenColumns = minSpaceBetweenColumns;
    }

    public List<ColumnGroup> identifyColumns(List<String> lines, Pair<Integer, Integer> of) {

        List<SimilarLinesGroup> similarLinesGroup = new ArrayList<>();
        for (int i = of.getLeft(); i < of.getRight(); i++) {
            String line = lines.get(i);
            // Identify potential column start positions based on spaces
            LineColumns lineColumns = identifyPotentialColumns(line, i);
            log.debug("Line {}: Potential columns identified: {}", line, lineColumns);
            if (null != lineColumns) {
                addColumnsToGroup(similarLinesGroup, lineColumns);
            }
            // Further logic to determine actual columns based on potential columns
        }
        // Handle right aligned columns by moving the start position left to merge lines in the same table
        similarLinesGroup = mergeGroupsWithPotentialRightAlignedColumns(similarLinesGroup);
        // TODO Working here
        // Handle empty columns that may skew the column detection and need to be forced to add columns based on aligning other rows in the table
        // Detect column headers - usually the first column (optionally turn them off)
        // Handle multi-line headings that may span multiple lines and need to be merged into a single column heading
        // Handle random heading lines that may not be part of the table but are in between the table rows
        // Handle two or more tables in the same page that may have different column structures and need to be separated into different tables

        log.debug("Grouped line columns: {}", similarLinesGroup);
        List<ColumnGroup> cgs = similarLinesGroup.stream()
                .map(this::mapColumnGroup)
                .sorted((cg1, cg2) -> Integer.compare(cg1.getLines().get(0), cg2.getLines().get(0)))
                .toList();
        return cgs;
    }

    private List<SimilarLinesGroup> mergeGroupsWithPotentialRightAlignedColumns(List<SimilarLinesGroup> similarLinesGroup) {
        Map<Integer, List<SimilarLinesGroup>> groupedByColumnCount = similarLinesGroup.stream()
                .collect(Collectors.groupingBy(SimilarLinesGroup::getColumnCount));
        for (Entry<Integer, List<SimilarLinesGroup>> entry : groupedByColumnCount.entrySet()) {
            int columnCount = entry.getKey();
            List<SimilarLinesGroup> groups = entry.getValue();
            // Sort groups by the start positions of their columns
            groups.sort((g1, g2) -> {
                for (int i = 0; i < columnCount; i++) {
                    int cmp = Integer.compare(g1.getStarts()[i], g2.getStarts()[i]);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            });
            // Merge groups that have similar start positions but may be right aligned
            for (int i = 0; i < groups.size() - 1; i++) {
                SimilarLinesGroup g1 = groups.get(i);
                SimilarLinesGroup g2 = groups.get(i + 1);
                boolean canMerge = true;
                boolean[] adjustColumns = new boolean[columnCount];
                int[] adjustedStarts = new int[columnCount];
                int[] adjustedMaxLengths = new int[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    // Process each column and check if they can be merged based on their start positions and lengths
                    if (g1.getStarts()[j] != g2.getStarts()[j]) {
                        int minStart = Math.min(g1.getStarts()[j], g2.getStarts()[j]);
                        SimilarLinesGroup laterGroup = (g1.getStarts()[j] > g2.getStarts()[j]) ? g1 : g2;
                        // First column - no previous column to check against but maybe able to move the start left
                        // calculate for the other columns
                        if (j > 0) {
                            int previousStart = laterGroup.getStarts()[j - 1];
                            int laterGroupPrevMaxLength = getMaxColLen(laterGroup.getLineColumns(), j - 1);
                            if (minStart < previousStart + laterGroupPrevMaxLength + minSpaceBetweenColumns) {
                                canMerge = false;
                                break;
                            }
                        }

                        // Set the values
                        adjustedStarts[j] = minStart;
                        adjustColumns[j] = true;
                        int lengthAdjustment = Math.abs(g1.getStarts()[j] - g2.getStarts()[j]);
                        if (g1.getStarts()[j] < g2.getStarts()[j]) {
                            adjustedMaxLengths[j] = Math.max(
                                    getMaxColLen(g1.getLineColumns(), j), 
                                    getMaxColLen(g2.getLineColumns(), j) + lengthAdjustment);
                        } else {
                            adjustedMaxLengths[j] = Math.max(
                                    getMaxColLen(g1.getLineColumns(), j) + lengthAdjustment, 
                                    getMaxColLen(g2.getLineColumns(), j));
                        }
                    }
                }
                if (canMerge) {
                    // Adjust columns where required
                    for (int j = 0; j < columnCount; j++) {
                        SimilarLinesGroup laterGroup = (g1.getStarts()[j] > g2.getStarts()[j]) ? g1 : g2;
                        int lengthAdjustment = laterGroup.getStarts()[j] - adjustedStarts[j];
                        laterGroup.getStarts()[j] = adjustedStarts[j];

                        for (LineColumns lineColumns : laterGroup.getLineColumns()) {
                            Column column = lineColumns.getColumns().get(j);
                            if (adjustColumns[j]) {
                                column.setStart(adjustedStarts[j]);
                                column.setLength(column.getLength() + lengthAdjustment);
                            }
                        }
                    }
                    log.debug("Merging groups {} into {}", 
                            g2.getLineColumns().stream().map(LineColumns::getLine).toArray(), 
                            g1.getLineColumns().stream().map(LineColumns::getLine).toArray());
                    g1.getLineColumns().addAll(g2.getLineColumns());
                    groups.remove(i + 1);
                    i--; // Stay at the same index to check the next group
                }
            }
        }
        return new ArrayList<>(groupedByColumnCount.values().stream().flatMap(List::stream).toList());
    }

    private ColumnGroup mapColumnGroup(SimilarLinesGroup similarLinesGroup) {
        ColumnGroup cg = new ColumnGroup();
        cg.setLines(similarLinesGroup.getLineColumns().stream()
                .map(LineColumns::getLine)
                .sorted(Integer::compareTo)
                .collect(Collectors.toCollection(ArrayList::new)));
        cg.setColumns(new ArrayList<>());

        LineColumns firstLineColumns = similarLinesGroup.getLineColumns().get(0);
        for (int i = 0; i < firstLineColumns.getColumns().size(); i++) {
            Column column = firstLineColumns.getColumns().get(i);
            cg.getColumns().add(new ColumnDescriptor(column.getStart(), 
                    getMaxColLen(similarLinesGroup.getLineColumns(), i), 
                    null)); // TODO derive column name from heading lines if available
        }
        return cg;
    }

    private int getMaxColLen(List<LineColumns> lineColumnss, int colIndex) {
        int maxLen = 0;
        for (LineColumns lineColumns : lineColumnss) {
            maxLen = Math.max(maxLen, lineColumns.getColumns().get(colIndex).getLength());
        }
        return maxLen;
    }

    private void addColumnsToGroup(List<SimilarLinesGroup> similarLinesGroups, LineColumns lineColumns) {
        for (SimilarLinesGroup similarLinesGroup : similarLinesGroups) {
            if (similarLinesGroup.getColumnCount() == lineColumns.getColumns().size()) {
                // Check if the starts match
                boolean matches = true;
                for (int i = 0; i < similarLinesGroup.getColumnCount(); i++) {
                    if (similarLinesGroup.getStarts()[i] != lineColumns.getColumns().get(i).getStart()) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    similarLinesGroup.getLineColumns().add(lineColumns);
                    return;
                }
            }
        }
        // If no matching group found, create a new group
        SimilarLinesGroup newGroup = new SimilarLinesGroup();
        newGroup.setColumnCount(lineColumns.getColumns().size());
        newGroup.setStarts(lineColumns.getColumns().stream().mapToInt(Column::getStart).toArray());
        newGroup.setLineColumns(new ArrayList<>());
        newGroup.getLineColumns().add(lineColumns);
        similarLinesGroups.add(newGroup);
    }

    private LineColumns identifyPotentialColumns(String line, int lineNumber) {

        List<Column> columns = new ArrayList<>();
        int spacesBefore = 0;
        Column currentColumn = null;
        for (int i = 0; i < line.length(); i++) {
            if (Character.isWhitespace(line.charAt(i))) {
                spacesBefore++;
            } else {
                if (columns.isEmpty() || spacesBefore >= minSpaceBetweenColumns) {
                    currentColumn = new Column();
                    currentColumn.setStart(i);
                    currentColumn.setSpacesBefore(spacesBefore);
                    columns.add(currentColumn);
                } else if (currentColumn != null) {
                    currentColumn.setLength(i - currentColumn.getStart() + 1);
                }
                spacesBefore = 0;
            }
        }
        if (!columns.isEmpty() && columns.size() > 1) {
            return LineColumns.builder().line(lineNumber).columns(columns).build();
        }
        return null;
    }

    @Data
    private static class Column {
        private int start;
        private int length;
        private int spacesBefore;
    }

    @Data @Builder
    private static class LineColumns {
        private int line;
        private List<Column> columns;
    }

    @Data
    private static class SimilarLinesGroup {
        private int columnCount;
        private int[] starts;
        private List<LineColumns> lineColumns;
    }
}
