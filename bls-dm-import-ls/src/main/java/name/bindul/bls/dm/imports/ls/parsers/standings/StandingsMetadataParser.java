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
import name.bindul.bls.dm.imports.ls.parsers.shared.ParserUtils;

@UtilityClass
public class StandingsMetadataParser {

    private static final Pattern DATE_TIME_CENTER_LINE = Pattern.compile(
        "^\\s*(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\s+(\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm))\\b([\\w\\s]*)[Ll]anes?.*$"
    );
    private static final Pattern LEAGUE_OFFICER_LINE_PATTERN = Pattern.compile(
        "^\\s*(President|Secretary|Treasurer|Secretary/Treasurer):\\s+([\\w\\s]+)\\s([a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,})",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern OFFICER_LANE_SUFFIX_PATTERN = Pattern.compile("\\s+is on lane\\s+\\d+\\s*$", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    void parseMetadata(List<String> lines, LeagueStandings standings) {
        Matcher headerMatcher = PAGE_HEADER_LINE_PATTERN.matcher("");
        for (String line : lines) {
            if (headerMatcher.reset(line).lookingAt()) {
                standings.setDate(LocalDate.parse(headerMatcher.group(1), DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                standings.setWeekNumber(ParserUtils.parseInt(headerMatcher.group(2)));
                standings.setTotalWeeks(ParserUtils.parseInt(headerMatcher.group(3)));
                String leagueFromDateLine = extractLeagueNameFromHeaderLine(line);
                if (leagueFromDateLine != null) {
                    standings.setLeagueName(leagueFromDateLine);
                }
                break;
            }
        }

        int dateIndex = findDateLineIndex(lines);
        int startIndex = dateIndex >= 0 ? dateIndex + 1 : 0;
        int endIndex = ParserUtils.findLinePatternIndex(lines, TEAM_STANDINGS_MARKER);
        if (endIndex < 0) {
            endIndex = Math.min(lines.size(), startIndex + 12);
        }

        parseScheduleMetadata(lines, startIndex, endIndex, standings);
        parseOfficerMetadata(lines, startIndex, endIndex, standings);
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

        String leagueName = ParserUtils.removeExtraWhitespaces(line.substring(matcher.end(), pageIndex));
        return leagueName.isBlank() ? null : leagueName;
    }

    private void parseScheduleMetadata(List<String> lines, int startIndex, int endIndex, LeagueStandings standings) {
        Matcher matcher = DATE_TIME_CENTER_LINE.matcher("");
        for (int i = startIndex; i < endIndex && i < lines.size(); i++) {
            String line = lines.get(i);
            matcher.reset(line);
            if (matcher.lookingAt()) {
                standings.setDayOfWeek(ParserUtils.removeExtraWhitespaces(matcher.group(1)));
                standings.setStartTime(parseStartTime(matcher.group(2)));
                standings.setBowlingCenter(ParserUtils.removeExtraWhitespaces(matcher.group(3)));
                return;
            }
        }
    }

    private void parseOfficerMetadata(List<String> lines, int startIndex, int endIndex, LeagueStandings standings) {
        Matcher leagueOfficerMatcher = LEAGUE_OFFICER_LINE_PATTERN.matcher("");
        for (int i = startIndex; i < endIndex && i < lines.size(); i++) {
            String line = lines.get(i);

            if (leagueOfficerMatcher.reset(line).matches()) {
                String role = ParserUtils.removeExtraWhitespaces(leagueOfficerMatcher.group(1)).toLowerCase();
                LeagueOfficer officer = buildOfficer(leagueOfficerMatcher.group(2), leagueOfficerMatcher.group(3));
                if (role.contains("secretary")) {
                    standings.setSecretary(officer);
                }
                if (role.contains("treasurer")) {
                    standings.setTreasurer(officer);
                }
                if (role.contains("president")) {
                    standings.setPresident(officer);
                }
            }
        }
    }

    private LocalTime parseStartTime(String timeToken) {
        if (timeToken == null || timeToken.isBlank()) {
            return null;
        }
        String normalizedTime = ParserUtils.removeExtraWhitespaces(timeToken).replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        try {
            return LocalTime.parse(normalizedTime, START_TIME_FORMATTER);
        } catch (RuntimeException _) {
            return null;
        }
    }

    private LeagueOfficer buildOfficer(String rawName, String email) {
        String name = sanitizeOfficerName(rawName);
        String normalizedEmail = ParserUtils.removeExtraWhitespaces(email);
        if (name.isBlank() || normalizedEmail.isBlank()) {
            return null;
        }

        LeagueOfficer officer = new LeagueOfficer();
        officer.setName(name);
        officer.setEmail(normalizedEmail);
        return officer;
    }

    private String sanitizeOfficerName(String rawName) {
        String normalizedName = ParserUtils.removeExtraWhitespaces(rawName);
        normalizedName = OFFICER_LANE_SUFFIX_PATTERN.matcher(normalizedName).replaceFirst("");
        return ParserUtils.removeExtraWhitespaces(normalizedName);
    }

    private int findDateLineIndex(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (PAGE_HEADER_LINE_PATTERN.matcher(lines.get(i)).lookingAt()) {
                return i;
            }
        }
        return -1;
    }

}
