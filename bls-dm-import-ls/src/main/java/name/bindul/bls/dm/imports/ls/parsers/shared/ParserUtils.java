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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ParserUtils {

    private static final Pattern INTEGER_TOKEN_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern FRACTION_TOKEN_PATTERN = Pattern.compile("^(\\d+)\\s?([\\u00BC-\\u00BE\\u2150-\\u215E]?)$");
    private static final Pattern NUMBER_TOKEN_PATTER = Pattern.compile("^\\d+(?:\\.\\d{0,2})?$");
    private static final Map<Character, Float> FRACTION_UNICODE_MAP = Map.ofEntries(
            Map.entry('\u00BC', 0.25f), // ¼
            Map.entry('\u00BD', 0.5f),  // ½
            Map.entry('\u00BE', 0.75f), // ¾
            Map.entry('\u2150', 1f / 7f), // ⅐
            Map.entry('\u2151', 1f / 9f), // ⅑
            Map.entry('\u2152', 1f / 10f), // ⅒
            Map.entry('\u2153', 1f / 3f), // ⅓
            Map.entry('\u2154', 2f / 3f), // ⅔
            Map.entry('\u2155', 1f / 5f), // ⅕
            Map.entry('\u2156', 2f / 5f), // ⅖
            Map.entry('\u2157', 3f / 5f), // ⅗
            Map.entry('\u2158', 4f / 5f), // ⅘
            Map.entry('\u2159', 1f / 6f), // ⅙
            Map.entry('\u215A', 5f / 6f), // ⅚
            Map.entry('\u215B', 1f / 8f), // ⅛
            Map.entry('\u215C', 3f / 8f), // ⅜
            Map.entry('\u215D', 5f / 8f), // ⅝
            Map.entry('\u215E', 7f / 8f)   // ⅞
    );

    public static String removeExtraWhitespaces (String line) {
        return line == null ? "" : line.replaceAll("\\s{2,}", " ").replace('\u00A0', ' ').trim();
    }

    public static String joinTokens(String[] tokens, int startInclusive, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = startInclusive; i < endExclusive; i++) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(tokens[i]);
        }
        return builder.toString().trim();
    }

    public static int parseInt(String token) {
        String digitsOnly = token == null ? "" : token.replaceAll("\\D", "");
        if (digitsOnly.isBlank() || digitsOnly.length() > 9) {
            return 0;
        }
        return Integer.parseInt(digitsOnly);
    }

    public static float parseFraction(String token) {
        final Matcher matcher = FRACTION_TOKEN_PATTERN.matcher(token == null ? "" : token.trim());
        if (matcher.matches()) {
            String wholePart = matcher.group(1);
            String fractionPart = matcher.group(2);
            float value = Float.parseFloat(wholePart);

            if (!fractionPart.isEmpty()) {
                char fractionChar = fractionPart.charAt(0);
                Float fractionValue = FRACTION_UNICODE_MAP.get(fractionChar);
                if (fractionValue != null) {
                    value += fractionValue;
                }
            }
            return value;
        }
        return 0.0f;
    }

    public static List<String> normalizeLines(String text, boolean removeWhitespaces) {
        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\\R")) {
            String line = removeWhitespaces ? removeExtraWhitespaces(rawLine) : rawLine.replace('\u00A0', ' ');
            if (!line.trim().isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static boolean matchesHeaderTokens (String[] tokens, String[][] headers) {
        for (String[] header : headers) {
            if (tokens.length >= header.length) {
                boolean matches = true;
                for (int i = 0; i < header.length; i++) {
                    if (!tokens[i].equalsIgnoreCase(header[i])) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isIntegerToken(String token) {
        return INTEGER_TOKEN_PATTERN.matcher(token).matches();
    }

    public static boolean areIntegerTokens(String[] tokens, int startInclusive, int endExclusive) {
        Matcher matcher = INTEGER_TOKEN_PATTERN.matcher("");
        for (int i = startInclusive; i < endExclusive; i++) {
            if (!matcher.reset(tokens[i]).matches()) {
                return false;
            }
        }
        return true;
    }

    public static int findFirstIntegerTokenIndex(String[] tokens) {
        Matcher matcher = INTEGER_TOKEN_PATTERN.matcher("");
        for (int i = 0; i < tokens.length; i++) {
            if (matcher.reset(tokens[i]).matches()) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNumberToken(String token) {
        return NUMBER_TOKEN_PATTER.matcher(token).matches();
    }

    public static int findNextNumberTokenIndex(String[] tokens, int startIndex) {
        Matcher matcher = NUMBER_TOKEN_PATTER.matcher("");
        for (int i = startIndex; i < tokens.length; i++) {
            if (matcher.reset(tokens[i]).matches()) {
                return i;
            }
        }
        return -1;
    }

    public static Pair<Integer, Integer> findSectionBoundaries(List<String> lines, Pattern sectionMarker, Pattern nextSectionMarker) {
        int start = findLinePatternIndex(lines, sectionMarker);
        if (start < 0) {
            return null;
        }

        int end = lines.size(); // Substitutes section is usually at the end of the document
        if (null != nextSectionMarker) {
            end = findLinePatternIndex(lines, nextSectionMarker);
            if (end < 0) {
                end = lines.size();
            }
        }

        return Pair.of(start, end);
    }

    public static int findLinePatternIndex(List<String> lines, Pattern marker) {
        final Matcher matcher = marker.matcher("");
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (null != line && matcher.reset(line.strip()).matches()) {
                return i;
            }
        }
        return -1;
    }

    public static String extractColumnValue(String line, ColumnDescriptor column) {
        return extractColumnValue(line, column, false);
    }

    public static String extractColumnValue(String line, ColumnDescriptor column, boolean trim) {
        if (line != null && line.length() >= (column.start() + column.length())) {
            String value = line.substring(column.start(), column.start() + column.length());
            return trim ? value.trim() : value;
        }
        return null;
    }
}
