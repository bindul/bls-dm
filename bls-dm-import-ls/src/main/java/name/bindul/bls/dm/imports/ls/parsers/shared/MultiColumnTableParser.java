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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MultiColumnTableParser {

    private static final int MIN_COLUMN_WIDTH = 2;
    private static final int MAX_HEADER_LINES = 2;
    private static final int HEADER_SCAN_LIMIT = 6;

    // Expectation NBSP (x00A0) to be replaced with space (x20) before calling this method
    public List<ColumnDescriptor> identifyColumns(List<String> lines, Pair<Integer, Integer> sectionBoundary) {
        Objects.requireNonNull(lines, "lines cannot be null");
        Objects.requireNonNull(sectionBoundary, "sectionBoundary cannot be null");

        final List<String> sectionLines = extractSectionLines(lines, sectionBoundary);
        if (sectionLines.isEmpty()) {
            return List.of();
        }

        // TODO Working here
        final int commonIndent = determineCommonIndent(sectionLines);
        final List<String> normalizedLines = sectionLines.stream()
                .map(line -> trimTrailingWhitespace(stripCommonIndent(line, commonIndent)))
                .toList();
        final List<String> headerBlock = extractHeaderBlock(normalizedLines);
        final String dataRow = findBestDataRow(normalizedLines, headerBlock.size());
        List<ColumnSpan> spans = detectColumnSpansFromDataRow(dataRow);
        if (spans.isEmpty()) {
            spans = detectColumnSpansFromHeaders(headerBlock, normalizedLines);
        }
        if (spans.isEmpty()) {
            spans = detectColumnSpans(normalizedLines);
        }
        if (spans.isEmpty()) {
            return List.of();
        }

        final List<String> headerLines = detectHeaderLines(headerBlock.isEmpty() ? normalizedLines : headerBlock, spans);
        final List<ColumnDescriptor> columns = new ArrayList<>(spans.size());
        for (int i = 0; i < spans.size(); i++) {
            final ColumnSpan span = spans.get(i);
            final String dataRowFallback = dataRow == null ? "" : extractSpanText(dataRow, span);
            final String name = buildColumnName(headerLines, span, i, dataRowFallback);
            columns.add(new ColumnDescriptor(span.start(), span.length(), name));
        }

        log.debug("Detected {} columns: {}", columns.size(), columns.stream().map(ColumnDescriptor::name).collect(Collectors.joining(", ")));
        return List.copyOf(columns);
    }

    private List<String> extractSectionLines(List<String> lines, Pair<Integer, Integer> sectionBoundary) {
        final int sectionStart = Math.max(0, sectionBoundary.getLeft());
        final int sectionEnd = Math.min(lines.size(), sectionBoundary.getRight());
        if (sectionEnd <= sectionStart + 1) {
            return List.of();
        }
        final List<String> sectionLines = new ArrayList<>(Math.max(0, sectionEnd - sectionStart - 1));
        for (int i = sectionStart + 1; i < sectionEnd; i++) {
            final String line = lines.get(i);
            if (!line.isBlank()) {
                sectionLines.add(line);
            }
        }
        return sectionLines;
    }

    private int determineCommonIndent(List<String> sectionLines) {
        int commonIndent = Integer.MAX_VALUE;
        for (String line : sectionLines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            int indent = 0;
            while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                indent++;
            }
            commonIndent = Math.min(commonIndent, indent);
        }

        return commonIndent == Integer.MAX_VALUE ? 0 : commonIndent;
    }

    private List<String> extractHeaderBlock(List<String> normalizedLines) {
        final List<String> headerBlock = new ArrayList<>();
        for (String line : normalizedLines) {
            if (line.isBlank()) {
                continue;
            }

            headerBlock.add(line);
            if (looksLikeDataRow(line)) {
                headerBlock.remove(headerBlock.size() - 1);
                break;
            }
        }
        return headerBlock;
    }

    private boolean looksLikeDataRow(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }

        final int numericTokens = countNumericTokens(line);
        return numericTokens >= 2;
    }

    private List<ColumnSpan> detectColumnSpansFromHeaders(List<String> headerBlock, List<String> normalizedLines) {
        if (headerBlock.isEmpty()) {
            return List.of();
        }

        final String anchorLine = selectAnchorHeaderLine(headerBlock);
        if (anchorLine == null) {
            return List.of();
        }

        final List<HeaderToken> anchorTokens = tokenize(anchorLine);
        if (anchorTokens.size() < 2) {
            return List.of();
        }

        final String firstDataRow = headerBlock.size() < normalizedLines.size() ? normalizedLines.get(headerBlock.size()) : null;
        final int maxLineLength = normalizedLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(anchorLine.length());

        final List<ColumnSpan> spans = new ArrayList<>(anchorTokens.size());
        if (shouldKeepLeadingSpan(firstDataRow, anchorTokens.get(0).start())) {
            final int leadingWidth = anchorTokens.get(0).start();
            spans.add(new ColumnSpan(0, leadingWidth));
        }

        int groupStartIndex = 0;
        while (groupStartIndex < anchorTokens.size()) {
            int groupEndIndex = groupStartIndex;
            while (groupEndIndex + 1 < anchorTokens.size()
                    && anchorTokens.get(groupEndIndex + 1).start() - anchorTokens.get(groupEndIndex).endExclusive() <= 1) {
                groupEndIndex++;
            }

            final int start = anchorTokens.get(groupStartIndex).start();
            final int end = groupEndIndex + 1 < anchorTokens.size()
                    ? anchorTokens.get(groupEndIndex + 1).start()
                    : maxLineLength;
            if (end > start) {
                spans.add(new ColumnSpan(start, Math.max(MIN_COLUMN_WIDTH, end - start)));
            }

            groupStartIndex = groupEndIndex + 1;
        }

        return spans;
    }

    private String findBestDataRow(List<String> normalizedLines, int startIndex) {
        String bestRow = null;
        int bestNumericCount = 0;

        for (int i = Math.max(0, startIndex); i < normalizedLines.size(); i++) {
            final String line = normalizedLines.get(i);
            final int numericCount = countNumericTokens(line);
            if (numericCount > bestNumericCount) {
                bestNumericCount = numericCount;
                bestRow = line;
            }
        }

        return bestNumericCount >= 2 ? bestRow : null;
    }

    private List<ColumnSpan> detectColumnSpansFromDataRow(String dataRow) {
        if (dataRow == null || dataRow.isBlank()) {
            return List.of();
        }

        final List<HeaderToken> tokens = tokenize(dataRow);
        final List<HeaderToken> numericTokens = tokens.stream()
                .filter(token -> token.text().matches("^[-+]?\\d+(?:\\.\\d+)?$"))
                .collect(Collectors.toList());
        if (numericTokens.isEmpty()) {
            return List.of();
        }

        final int maxLineLength = dataRow.length();

        final List<ColumnSpan> spans = new ArrayList<>();
        final HeaderToken firstToken = tokens.get(0);
        if (!firstToken.text().matches("^[-+]?\\d+(?:\\.\\d+)?$")) {
            final int firstNumericStart = numericTokens.get(0).start();
            if (firstNumericStart > firstToken.start()) {
                spans.add(new ColumnSpan(firstToken.start(), firstNumericStart - firstToken.start()));
            }
        }

        for (int i = 0; i < numericTokens.size(); i++) {
            final int start = numericTokens.get(i).start();
            final int end = i + 1 < numericTokens.size() ? numericTokens.get(i + 1).start() : maxLineLength;
            if (end > start) {
                spans.add(new ColumnSpan(start, Math.max(MIN_COLUMN_WIDTH, end - start)));
            }
        }

        return spans;
    }

    private boolean shouldKeepLeadingSpan(String firstDataRow, int leadingWidth) {
        if (leadingWidth <= 0) {
            return false;
        }
        if (firstDataRow == null || firstDataRow.isBlank()) {
            return true;
        }

        final String trimmed = firstDataRow.stripLeading();
        if (trimmed.isEmpty()) {
            return false;
        }
        return !Character.isDigit(trimmed.charAt(0));
    }

    private List<ColumnSpan> detectColumnSpans(List<String> sectionLines) {
        final int maxLineLength = sectionLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        if (maxLineLength == 0) {
            return List.of();
        }

        final int[] separatorScores = new int[maxLineLength];
        for (String line : sectionLines) {
            int index = 0;
            while (index < line.length()) {
                if (Character.isWhitespace(line.charAt(index))) {
                    final int start = index;
                    while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                        index++;
                    }
                    final int gapWidth = index - start;
                    if (gapWidth >= 2) {
                        for (int i = start; i < index; i++) {
                            separatorScores[i]++;
                        }
                    }
                } else {
                    index++;
                }
            }
        }

        final int minSupport = Math.max(2, (int) Math.ceil(sectionLines.size() * 0.5d));
        final List<ColumnSpan> spans = new ArrayList<>();
        int currentStart = -1;
        for (int i = 0; i < separatorScores.length; i++) {
            if (separatorScores[i] < minSupport) {
                if (currentStart < 0) {
                    currentStart = i;
                }
            } else if (currentStart >= 0) {
                addSpan(spans, currentStart, i);
                currentStart = -1;
            }
        }
        if (currentStart >= 0) {
            addSpan(spans, currentStart, separatorScores.length);
        }

        return mergeSmallGaps(spans);
    }

    private String selectAnchorHeaderLine(List<String> headerBlock) {
        for (int i = headerBlock.size() - 1; i >= 0; i--) {
            final String line = headerBlock.get(i);
            if (countTokens(line) >= 2) {
                return line;
            }
        }
        return null;
    }

    private List<HeaderToken> tokenize(String line) {
        final List<HeaderToken> tokens = new ArrayList<>();
        int index = 0;
        while (index < line.length()) {
            while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                index++;
            }
            if (index >= line.length()) {
                break;
            }

            final int start = index;
            while (index < line.length() && !Character.isWhitespace(line.charAt(index))) {
                index++;
            }
            tokens.add(new HeaderToken(line.substring(start, index), start));
        }
        return tokens;
    }

    private void addSpan(List<ColumnSpan> spans, int start, int endExclusive) {
        final int length = endExclusive - start;
        if (length >= MIN_COLUMN_WIDTH) {
            spans.add(new ColumnSpan(start, length));
        }
    }

    private List<ColumnSpan> mergeSmallGaps(List<ColumnSpan> spans) {
        if (spans.size() < 2) {
            return spans;
        }

        final List<ColumnSpan> merged = new ArrayList<>(spans.size());
        ColumnSpan current = spans.get(0);
        for (int i = 1; i < spans.size(); i++) {
            final ColumnSpan next = spans.get(i);
            if (next.start() - current.endExclusive() <= 1) {
                current = new ColumnSpan(current.start(), next.endExclusive() - current.start());
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    private List<String> detectHeaderLines(List<String> sectionLines, List<ColumnSpan> spans) {
        final int scanLimit = Math.min(sectionLines.size(), HEADER_SCAN_LIMIT);
        final List<CandidateLine> candidates = new ArrayList<>();
        final int headerCoverageThreshold = Math.max(2, (int) Math.ceil(spans.size() * 0.5d));

        for (int i = 0; i < scanLimit; i++) {
            final String line = sectionLines.get(i);
            final int coverage = countCoveredSpans(line, spans);
            final int numericTokens = countNumericTokens(line);
            final boolean dataLike = numericTokens >= 2 || (numericTokens >= 1 && coverage >= headerCoverageThreshold);
            candidates.add(new CandidateLine(line, coverage, numericTokens, dataLike, i));
            if (dataLike) {
                break;
            }
        }

        final List<CandidateLine> headerCandidates = candidates.stream()
                .filter(candidate -> candidate.coverage() >= headerCoverageThreshold)
                .sorted(Comparator.comparingInt(CandidateLine::index))
                .limit(MAX_HEADER_LINES)
                .collect(Collectors.toList());

        if (!headerCandidates.isEmpty()) {
            return headerCandidates.stream().map(CandidateLine::line).collect(Collectors.toList());
        }

        return candidates.stream()
                .sorted(Comparator.comparingInt(CandidateLine::coverage).reversed().thenComparingInt(CandidateLine::index))
                .limit(MAX_HEADER_LINES)
                .sorted(Comparator.comparingInt(CandidateLine::index))
                .map(CandidateLine::line)
                .collect(Collectors.toList());
    }

    private int countCoveredSpans(String line, List<ColumnSpan> spans) {
        int covered = 0;
        for (ColumnSpan span : spans) {
            if (hasTextInSpan(line, span)) {
                covered++;
            }
        }
        return covered;
    }

    private boolean hasTextInSpan(String line, ColumnSpan span) {
        final int start = Math.min(span.start(), line.length());
        final int end = Math.min(span.endExclusive(), line.length());
        if (start >= end) {
            return false;
        }

        for (int i = start; i < end; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String buildColumnName(List<String> headerLines, ColumnSpan span, int columnIndex, String dataRowFallback) {
        final List<String> fragments = new ArrayList<>(headerLines.size());
        for (String headerLine : headerLines) {
            final String fragment = extractSpanText(headerLine, span);
            if (!fragment.isBlank()) {
                if (fragments.isEmpty() || !fragments.get(fragments.size() - 1).equalsIgnoreCase(fragment)) {
                    fragments.add(fragment);
                }
            }
        }

        final String name = String.join(" ", fragments).replaceAll("\\s+", " ").trim();
        if (!name.isBlank()) {
            return name;
        }

        final String fallback = dataRowFallback == null ? "" : dataRowFallback.replaceAll("\\s+", " ").trim();
        if (!fallback.isBlank()) {
            return fallback;
        }

        return "column-" + (columnIndex + 1);
    }

    private String extractSpanText(String line, ColumnSpan span) {
        if (line == null || line.isBlank()) {
            return "";
        }

        final List<String> fragments = new ArrayList<>();
        for (HeaderToken token : tokenize(line)) {
            final int tokenCenter = token.start() + (token.text().length() / 2);
            if (tokenCenter >= span.start() && tokenCenter < span.endExclusive()) {
                fragments.add(token.text());
            }
        }

        return String.join(" ", fragments).replaceAll("\\s+", " ").trim();
    }

    private int countNumericTokens(String line) {
        if (line == null || line.isBlank()) {
            return 0;
        }

        int numericTokens = 0;
        for (String token : line.trim().split("\\s+")) {
            if (token.matches("^[-+]?\\d+(?:\\.\\d+)?$")) {
                numericTokens++;
            }
        }
        return numericTokens;
    }

    private int countTokens(String line) {
        if (line == null || line.isBlank()) {
            return 0;
        }

        return tokenize(line).size();
    }

    private String stripCommonIndent(String line, int commonIndent) {
        if (line.isBlank() || commonIndent <= 0) {
            return line;
        }

        int actualIndent = 0;
        while (actualIndent < line.length() && actualIndent < commonIndent && Character.isWhitespace(line.charAt(actualIndent))) {
            actualIndent++;
        }
        return line.substring(actualIndent);
    }

    private String trimTrailingWhitespace(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }

        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    private record ColumnSpan(int start, int length) {
        private ColumnSpan {
            if (start < 0) {
                throw new IllegalArgumentException("start cannot be negative");
            }
            if (length <= 0) {
                throw new IllegalArgumentException("length must be positive");
            }
        }

        private int endExclusive() {
            return start + length;
        }
    }

    private record HeaderToken(String text, int start) {
        private int endExclusive() {
            return start + text.length();
        }
    }

    private record CandidateLine(String line, int coverage, int numericTokens, boolean dataLike, int index) {
    }
}
