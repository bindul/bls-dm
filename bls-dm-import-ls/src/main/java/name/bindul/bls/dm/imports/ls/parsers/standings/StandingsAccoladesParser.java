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

import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.INDIVIDUAL_HIGH_AVERAGES_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.LAST_WEEK_TOP_SCORES_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.SEASON_HIGH_SCORES_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.TEAM_ROSTERS_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.TEAM_STANDINGS_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.TEMP_SUBSTITUTES_MARKER;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.imports.ls.model.standings.Accolade;
import name.bindul.bls.dm.imports.ls.model.standings.AccoladeGroup;
import name.bindul.bls.dm.imports.ls.model.standings.Division;
import name.bindul.bls.dm.imports.ls.model.standings.DivisionInfo;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.PlayerInfo;
import name.bindul.bls.dm.imports.ls.model.standings.TeamInfo;
import name.bindul.bls.dm.imports.ls.parsers.shared.MultiColumnTableParser;
import name.bindul.bls.dm.imports.ls.parsers.shared.ParserUtils;

@UtilityClass
@Log4j2
public class StandingsAccoladesParser {
    
    private static final Pattern INDIVIDUAL_HIGH_AVERAGES_FORMAT = Pattern.compile("^(?:Men|Women)\\s+[\\d\\.]+\\s+[\\w]+.*$");

//    static void parseAccolades(List<String> lines, LeagueStandings standings) {
//        standings.setSeasonHighScores(parseAccoladeGroups(lines, SEASON_HIGH_SCORES_MARKER));
//        standings.setLastWeekHighScores(parseAccoladeGroups(lines, LAST_WEEK_TOP_SCORES_MARKER));
//    }

    static void parseIndividualHighAverages(List<String> lines, LeagueStandings standings) {
        Pair<Integer, Integer> sectionBoundary = ParserUtils.findSectionBoundaries(lines, INDIVIDUAL_HIGH_AVERAGES_MARKER, SEASON_HIGH_SCORES_MARKER);

        DivisionInfo currentDivision = null;
        Matcher accoladeMatcher = INDIVIDUAL_HIGH_AVERAGES_FORMAT.matcher("");

        List<AccoladeGroup<PlayerInfo>> individualHighAverages = new ArrayList<>();

        for (int i = sectionBoundary.getLeft() + 1; i < sectionBoundary.getRight(); i++) {
            String line = lines.get(i).trim();
            String[] tokens = line.split("\\s+");

            if (accoladeMatcher.reset(line).matches()) {
                // This line represents an accolade entry
                AccoladeGroup<PlayerInfo> individualHighAverageGroup = parseIndividualHighAvgAccoladeGroup(tokens,
                        currentDivision, standings);
                individualHighAverages.add(individualHighAverageGroup);
            } else {
                // Assume this is a division header line
                DivisionInfo division = standings.getDivisionByName(line);
                if (division != null) {
                    currentDivision = division;
                }
            }
        }
        standings.setIndividualHighAverages(individualHighAverages);
    }

    static void parseSeasonHighScores(List<String> lines, LeagueStandings standings) {
        Pair<Integer, Integer> sectionBoundary = ParserUtils.findSectionBoundaries(lines, SEASON_HIGH_SCORES_MARKER, LAST_WEEK_TOP_SCORES_MARKER);
        final Set<String> divisionNames = standings.getDivisions().stream()
                .map(Division::getDivisionInfo)
                .map(DivisionInfo::getName)
                .collect(java.util.stream.Collectors.toSet());

        List<AccoladeGroup<TeamInfo>> seasonTeamHighScores = new ArrayList<>();
        List<AccoladeGroup<PlayerInfo>> seasonIndividualHighScores = new ArrayList<>();

        MultiColumnTableParser tableParser = new MultiColumnTableParser();
        tableParser.identifyColumns(lines, sectionBoundary);

        DivisionInfo currentDivision = null;
        List<String> divisionAccoladeLineAccumulators = new ArrayList<>();
        for (int i = sectionBoundary.getLeft() + 1; i < sectionBoundary.getRight(); i++) {
            String line = lines.get(i).trim();
            if (isDivisionHeaderLine(line, divisionNames)) {
                // If we have accumulated lines for the previous division, we can process them here
                processDivisionAccoladeLines(divisionAccoladeLineAccumulators, currentDivision, standings,
                        seasonTeamHighScores, seasonIndividualHighScores);
                currentDivision = standings.getDivisionByName(divisionNames.stream().filter(line::contains).findFirst().orElse(null));
            }
            divisionAccoladeLineAccumulators.add(line);
        }
        if (currentDivision != null) {
            processDivisionAccoladeLines(divisionAccoladeLineAccumulators, currentDivision, standings,
                    seasonTeamHighScores, seasonIndividualHighScores);
        }

        standings.setSeasonTeamHighScores(seasonTeamHighScores);
        standings.setSeasonIndividualHighScores(seasonIndividualHighScores);
    }

    private static void processDivisionAccoladeLines(List<String> divisionAccoladeLineAccumulators,
            DivisionInfo currentDivision, LeagueStandings standings, List<AccoladeGroup<TeamInfo>> seasonTeamHighScores,
            List<AccoladeGroup<PlayerInfo>> seasonIndividualHighScores) {

        if (currentDivision == null || divisionAccoladeLineAccumulators.isEmpty()) {
            return;
        }

        System.out.println("Processing accolades for division: " + currentDivision);
        divisionAccoladeLineAccumulators.forEach(line -> System.out.println(line));
        
        divisionAccoladeLineAccumulators.clear();
    }

    //region Individual High Averages Parsing

    private static AccoladeGroup<PlayerInfo> parseIndividualHighAvgAccoladeGroup(String[] tokens,
            DivisionInfo currentDivision, LeagueStandings standings) {
        // Men 216.60 Bruce Self 208.33 Devon Anderson 204.67 David Dirth
        // Women 171.17 Dawn Gates 163.17 Heather Kuhn 148.67 Shirlee Jae Illsley
    
        AccoladeGroup<PlayerInfo> individualHighAverageGroup = new AccoladeGroup<>();
        individualHighAverageGroup.setAccoladeType("Individual High Average");
        individualHighAverageGroup.setDivision(currentDivision);
        individualHighAverageGroup.setGender(tokens[0]);
        individualHighAverageGroup.setAccoladeHolderType(AccoladeGroup.AccoladeHolderType.PLAYER);
        individualHighAverageGroup.setAccolades(new ArrayList<>());
    
        int position = 1;
        int currentToken = 1;
        Accolade<PlayerInfo> lastAccolade = null;
        // last value to set correct position for each accolade
        while (currentToken < tokens.length) {
            int averageValueIndex = ParserUtils.findNextNumberTokenIndex(tokens, currentToken);
            if (averageValueIndex > 0) {
                String average = tokens[currentToken++];
                StringBuilder holderName = new StringBuilder();
                while (currentToken < tokens.length && !ParserUtils.isNumberToken(tokens[currentToken])) {
                    if (!holderName.isEmpty()) {
                        holderName.append(' ');
                    }
                    holderName.append(tokens[currentToken++]);
                }
    
                Accolade<PlayerInfo> accolade = buildIndividualHighAvgAccolade(standings, position, lastAccolade, average,
                        holderName);
                individualHighAverageGroup.getAccolades().add(accolade);
                lastAccolade = accolade;
                position++;
            } else {
                currentToken ++;
            }
        }
        return individualHighAverageGroup;
    }

    private static Accolade<PlayerInfo> buildIndividualHighAvgAccolade(LeagueStandings standings, int position,
            Accolade<PlayerInfo> lastAccolade, String average, StringBuilder holderName) {
        Accolade<PlayerInfo> accolade = new Accolade<>();
        accolade.setValue(average);
        // These are players
        accolade.setHolder(standings.searchPlayerByName(holderName.toString()));
        if (lastAccolade != null && lastAccolade.getValue().equals(average)) {
            accolade.setPosition(lastAccolade.getPosition());
        } else {
            accolade.setPosition(position);
        }
        return accolade;
    }

    //endregion

    //region High Scores Parsing

    private static boolean isDivisionHeaderLine(String line, Set<String> divisionNames) {
        if (line == null || line.isEmpty()) {
            return false;
        }
        return divisionNames.stream().anyMatch(line::contains);
    }

    //endregion

    // Below the line -------------------------------------------------------------------------------------------------
    
//    private List<AccoladeGroup> parseAccoladeGroups(List<String> lines, String marker) {
//        int start = ParserUtils.findLineIndex(lines, marker);
//        if (start < 0) {
//            return new ArrayList<>();
//        }
//
//        int end = findNextSectionMarker(lines, start + 1);
//        List<AccoladeGroup> groups = new ArrayList<>();
//        for (int i = start + 1; i < end; i++) {
//            String line = lines.get(i);
//            if (isAccoladeHeaderLine(line)) {
//                continue;
//            }
//            AccoladeGroup group = parseAccoladeGroupLine(line, marker);
//            if (group != null) {
//                groups.add(group);
//            }
//        }
//        return groups;
//    }
//
//    private int findNextSectionMarker(List<String> lines, int startIndex) {
//        for (int i = startIndex; i < lines.size(); i++) {
//            String line = lines.get(i);
//            if (line.equalsIgnoreCase(TEAM_STANDINGS_MARKER)
//                || line.equalsIgnoreCase(INDIVIDUAL_HIGH_AVERAGES_MARKER)
//                || line.equalsIgnoreCase(SEASON_HIGH_SCORES_MARKER)
//                || line.equalsIgnoreCase(LAST_WEEK_TOP_SCORES_MARKER)
//                || line.equalsIgnoreCase(TEAM_ROSTERS_MARKER)
//                || line.equalsIgnoreCase(TEMP_SUBSTITUTES_MARKER)) {
//                return i;
//            }
//        }
//        return lines.size();
//    }
//
//    private boolean isAccoladeHeaderLine(String line) {
//        return line.startsWith("Handicap ")
//            || line.startsWith("Team ")
//            || line.startsWith("Scratch ")
//            || line.startsWith("High ")
//            || line.startsWith("Weekly ")
//            || line.startsWith("Name ")
//            || line.startsWith("Lanes ")
//            || line.startsWith("HDCP ")
//            || line.startsWith("Pins ");
//    }
//
//    private AccoladeGroup parseAccoladeGroupLine(String line, String sectionMarker) {
//        String[] tokens = line.split("\\s+");
//        if (tokens.length < 2 || isAccoladeHeaderLine(line)) {
//            return null;
//        }
//
//        int index = 0;
//        String holderType = null;
//        if (!ParserUtils.isIntegerToken(tokens[0])) {
//            holderType = tokens[0];
//            index = 1;
//        }
//
//        if (index >= tokens.length || !ParserUtils.isIntegerToken(tokens[index])) {
//            return null;
//        }
//
//        List<Accolade> accolades = new ArrayList<>();
//        while (index < tokens.length) {
//            if (!ParserUtils.isIntegerToken(tokens[index])) {
//                index++;
//                continue;
//            }
//
//            int position = ParserUtils.parseInt(tokens[index++]);
//            StringBuilder holderName = new StringBuilder();
//            while (index < tokens.length && !ParserUtils.isIntegerToken(tokens[index])) {
//                if (holderName.length() > 0) {
//                    holderName.append(' ');
//                }
//                holderName.append(tokens[index++]);
//            }
//
//            if (holderName.length() == 0) {
//                continue;
//            }
//
//            Accolade accolade = new Accolade();
//            accolade.setPosition(position);
////            accolade.setHolderName(holderName.toString());
//            accolade.setValue(Integer.toString(position));
//            accolades.add(accolade);
//        }
//
//        if (accolades.isEmpty()) {
//            return null;
//        }
//
//        AccoladeGroup group = new AccoladeGroup();
////        group.setAccoladeHolderType(holderType);
//        group.setDivision(null);
//        group.setAccoladeType(sectionMarker);
//        group.setGender(holderType != null && (holderType.equalsIgnoreCase("Men") || holderType.equalsIgnoreCase("Women"))
//            ? holderType
//            : null);
//        group.setAccolades(accolades);
//        return group;
//    }
}
