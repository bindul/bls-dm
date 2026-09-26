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
package name.bindul.bls.dm.imports.ls.parsers.standings;

import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueOfficer;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.parsers.ParserUtils;

@UtilityClass
public class StandingsMetadataParser {

    private static final Pattern DAY_TIME_PATTERN = Pattern.compile(
        "^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\s+(\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm))\\b"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
    private static final Pattern PRESIDENT_LINE_PATTERN = Pattern.compile(
        "^President:\\s+(.+?)\\s+" + EMAIL_PATTERN.pattern() + "\\b.*$"
    );
    private static final Pattern SECRETARY_TREASURER_LINE_PATTERN = Pattern.compile(
        "^(Secretary(?:/Treasurer)?|Treasurer):\\s+(.+?)\\s+" + EMAIL_PATTERN.pattern() + "\\b.*$"
    );
    private static final Pattern SECRETARY_SEGMENT_PATTERN = Pattern.compile(
        "Secretary:\\s+(.+?)\\s+" + EMAIL_PATTERN.pattern() + "\\b",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TREASURER_SEGMENT_PATTERN = Pattern.compile(
        "Treasurer:\\s+(.+?)\\s+" + EMAIL_PATTERN.pattern() + "\\b",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OFFICER_LANE_SUFFIX_PATTERN = Pattern.compile("\\s+is on lane\\s+\\d+\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEAGUE_NAME_PRIORITY_PATTERN = Pattern.compile(
        ".*\\b(?:Spring|Summer|Fall|Winter)\\b.*\\b\\d{4}\\b.*",
        Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    void parseMetadata(List<String> lines, LeagueStandings standings) {
        for (String line : lines) {
            Matcher matcher = PAGE_HEADER_LINE_PATTERN.matcher(line);
            if (matcher.lookingAt()) {
                standings.setDate(LocalDate.parse(matcher.group(1), DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                standings.setWeekNumber(ParserUtils.parseInt(matcher.group(2)));
                String leagueFromDateLine = extractLeagueNameFromHeaderLine(line);
                if (leagueFromDateLine != null) {
                    standings.setLeagueName(leagueFromDateLine);
                }
                break;
            }
        }

        int dateIndex = findDateLineIndex(lines);
        int startIndex = dateIndex >= 0 ? dateIndex + 1 : 0;
        int endIndex = ParserUtils.findLineIndex(lines, TEAM_STANDINGS_MARKER);
        if (endIndex < 0) {
            endIndex = Math.min(lines.size(), startIndex + 12);
        }

        parseScheduleMetadata(lines, startIndex, endIndex, standings);
        parseOfficerMetadata(lines, startIndex, endIndex, standings);

        String leagueName = findLeagueName(lines, startIndex, endIndex);
        if (leagueName != null && standings.getLeagueName() == null) {
            standings.setLeagueName(leagueName);
        }
    }

    private String extractLeagueNameFromHeaderLine(String line) {
        Matcher matcher = PAGE_HEADER_LINE_PATTERN.matcher(line);
        if (!matcher.lookingAt()) {
            return null;
        }
        int pageIndex = line.indexOf(" Page ", matcher.end());
        if (pageIndex < 0) {
            return null;
        }

        String leagueName = ParserUtils.normalize(line.substring(matcher.end(), pageIndex));
        return leagueName.isBlank() ? null : leagueName;
    }

    private void parseScheduleMetadata(List<String> lines, int startIndex, int endIndex, LeagueStandings standings) {
        for (int i = startIndex; i < endIndex && i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher matcher = DAY_TIME_PATTERN.matcher(line);
            if (matcher.lookingAt()) {
                standings.setDayOfWeek(ParserUtils.normalize(matcher.group(1)));
                standings.setStartTime(parseStartTime(matcher.group(2)));
                String center = extractBowlingCenter(line, matcher.end());
                if (!center.isBlank()) {
                    standings.setBowlingCenter(center);
                }
                return;
            }
        }
    }

    private void parseOfficerMetadata(List<String> lines, int startIndex, int endIndex, LeagueStandings standings) {
        for (int i = startIndex; i < endIndex && i < lines.size(); i++) {
            String line = lines.get(i);

            Matcher presidentMatcher = PRESIDENT_LINE_PATTERN.matcher(line);
            if (presidentMatcher.matches()) {
                standings.setPresident(buildOfficer(presidentMatcher.group(1), presidentMatcher.group(2)));
                continue;
            }

            Matcher secTreasMatcher = SECRETARY_TREASURER_LINE_PATTERN.matcher(line);
            if (secTreasMatcher.matches()) {
                String role = ParserUtils.normalize(secTreasMatcher.group(1));
                LeagueOfficer officer = buildOfficer(secTreasMatcher.group(2), secTreasMatcher.group(3));
                if (officer != null) {
                    if ("secretary/treasurer".equalsIgnoreCase(role)) {
                        standings.setSecretary(officer);
                        standings.setTreasurer(officer);
                    } else if ("secretary".equalsIgnoreCase(role)) {
                        standings.setSecretary(officer);
                    } else if ("treasurer".equalsIgnoreCase(role)) {
                        standings.setTreasurer(officer);
                    }
                }
            }

            LeagueOfficer secretary = extractOfficerFromSegment(line, SECRETARY_SEGMENT_PATTERN);
            if (secretary != null) {
                standings.setSecretary(secretary);
            }

            LeagueOfficer treasurer = extractOfficerFromSegment(line, TREASURER_SEGMENT_PATTERN);
            if (treasurer != null) {
                standings.setTreasurer(treasurer);
            }
        }
    }

    private LeagueOfficer extractOfficerFromSegment(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            return null;
        }
        return buildOfficer(matcher.group(1), matcher.group(2));
    }

    private LocalTime parseStartTime(String timeToken) {
        if (timeToken == null || timeToken.isBlank()) {
            return null;
        }
        String normalizedTime = ParserUtils.normalize(timeToken).replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        try {
            return LocalTime.parse(normalizedTime, START_TIME_FORMATTER);
        } catch (RuntimeException _) {
            return null;
        }
    }

    private LeagueOfficer buildOfficer(String rawName, String email) {
        String name = sanitizeOfficerName(rawName);
        String normalizedEmail = ParserUtils.normalize(email);
        if (name.isBlank() || normalizedEmail.isBlank()) {
            return null;
        }

        LeagueOfficer officer = new LeagueOfficer();
        officer.setName(name);
        officer.setEmail(normalizedEmail);
        return officer;
    }

    private String sanitizeOfficerName(String rawName) {
        String normalizedName = ParserUtils.normalize(rawName);
        normalizedName = OFFICER_LANE_SUFFIX_PATTERN.matcher(normalizedName).replaceFirst("");
        return ParserUtils.normalize(normalizedName);
    }

    private String findLeagueName(List<String> lines, int startIndex, int endIndex) {
        String fallback = null;
        for (int i = startIndex; i < endIndex && i < lines.size(); i++) {
            String line = lines.get(i);
            if (!isLikelyLeagueNameLine(line)) {
                continue;
            }
            if (LEAGUE_NAME_PRIORITY_PATTERN.matcher(line).matches()) {
                return line;
            }
            if (fallback == null) {
                fallback = line;
            }
        }
        return fallback;
    }

    private boolean isLikelyLeagueNameLine(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        if (ParserUtils.isHeaderLine(line, HEADER_PREFIXES) || PAGE_HEADER_LINE_PATTERN.matcher(line).lookingAt()) {
            return false;
        }
        if (DAY_TIME_PATTERN.matcher(line).lookingAt()) {
            return false;
        }
        String lower = line.toLowerCase(Locale.ROOT);
        return !lower.startsWith("president")
            && !lower.startsWith("important message")
            && !lower.startsWith("secretary")
            && !lower.startsWith("treasurer");
    }

    private int findDateLineIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (PAGE_HEADER_LINE_PATTERN.matcher(lines.get(i)).lookingAt()) {
                return i;
            }
        }
        return -1;
    }

    private String extractBowlingCenter(String line, int timeEndIndex) {
        String remainder = ParserUtils.normalize(line.substring(timeEndIndex));
        if (remainder.isBlank()) {
            return "";
        }

        String lower = remainder.toLowerCase(Locale.ROOT);
        int lanesIndex = indexOfLaneSuffix(lower);
        String center = lanesIndex >= 0 ? remainder.substring(0, lanesIndex) : remainder;
        return ParserUtils.normalize(center);
    }

    private int indexOfLaneSuffix(String lowerLine) {
        int lanesIndex = lowerLine.indexOf(" lanes ");
        int laneIndex = lowerLine.indexOf(" lane ");

        if (lanesIndex < 0) {
            return laneIndex;
        }
        if (laneIndex < 0) {
            return lanesIndex;
        }
        return Math.min(lanesIndex, laneIndex);
    }
}
