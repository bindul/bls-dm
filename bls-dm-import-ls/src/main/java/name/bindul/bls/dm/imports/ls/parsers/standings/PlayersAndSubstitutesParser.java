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

import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.TEAM_ROSTERS_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.TEMP_SUBSTITUTES_MARKER;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.Player;
import name.bindul.bls.dm.imports.ls.model.standings.PlayerInfo;
import name.bindul.bls.dm.imports.ls.model.standings.TeamInfo;
import name.bindul.bls.dm.imports.ls.model.standings.TeamPlayers;
import name.bindul.bls.dm.imports.ls.parsers.shared.ColumnDescriptor;
import name.bindul.bls.dm.imports.ls.parsers.shared.MultiColumnTableParser;
import name.bindul.bls.dm.imports.ls.parsers.shared.ParserUtils;

@UtilityClass
@Log4j2
public class PlayersAndSubstitutesParser {

    // High High HDCP HDCP HDCP
    // Name Avg HDCP Pins  Gms Game Sers Game Sers -1- -2- -3- Total Total
    private static final String[][] TEAM_ROSTERS_SUBS_TABLE_HEADERS = {
            {"High", "High", "HDCP", "HDCP", "HDCP"},
            {"Name", "Avg", "HDCP", "Pins", "Gms", "Game", "Sers", "Game", "Sers", "-1-", "-2-", "-3-", "Total", "Total"}
    };
    private static final Pattern TEAM_NAME_HEADER_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*-\\s*(.+?)\\s*$");
    private static final Pattern PLAYER_GAME_SCORE_PATTERN = Pattern.compile("^([a-zA-Z]?)([\\d]{1,3})$");

    static void parseTeamRoster(List<String> lines, LeagueStandings standings) {
        Pair<Integer, Integer> sectionBoundary = ParserUtils.findSectionBoundaries(lines, TEAM_ROSTERS_MARKER, TEMP_SUBSTITUTES_MARKER);
        if (sectionBoundary == null) {
            return;
        }

        final List<TeamPlayers> players = new ArrayList<>();
        final Matcher teamNameMatcher = TEAM_NAME_HEADER_PATTERN.matcher("");

        final MultiColumnTableParser tableParser = new MultiColumnTableParser();
        final List<ColumnDescriptor> columns = tableParser.identifyColumns(lines, sectionBoundary);
        log.debug("Identified columns for team roster: {}", columns);
        if (columns.size() < 14) {
            log.warn("Expected at least 14 columns for team roster, but found {}. Players will not be parsed. Columns: {}", columns.size(), columns);
        }

        TeamPlayers currentTeam = null;
        for (int i = sectionBoundary.getLeft() + 1; i < sectionBoundary.getRight(); i++) {
            String line = lines.get(i);
            String[] tokens = line.trim().split("\\s+");

            if (tokens.length > 0 && !ParserUtils.matchesHeaderTokens(tokens, TEAM_ROSTERS_SUBS_TABLE_HEADERS)) {
                if (teamNameMatcher.reset(line).matches()) {
                    currentTeam = buildTeamPlayers(teamNameMatcher, standings);
                    players.add(currentTeam);
                    continue;
                }
                // Must be a player line
                final Player player = parsePlayerLine(line, columns);
                if (player != null && currentTeam != null) {
                    player.getPlayerInfo().setTeam(currentTeam.getTeamInfo());
                    currentTeam.getPlayers().add(player);
                }
            }
        }
        standings.setPlayers(players);
    }

    static void parseSubstitutes(List<String> lines, LeagueStandings standings) {
        Pair<Integer, Integer> sectionBoundary = ParserUtils.findSectionBoundaries(lines, TEMP_SUBSTITUTES_MARKER, null);
        if (sectionBoundary == null) {
            return;
        }

        final MultiColumnTableParser tableParser = new MultiColumnTableParser();
        final List<ColumnDescriptor> columns = tableParser.identifyColumns(lines, sectionBoundary);
        log.debug("Identified columns for substitutes: {}", columns);
        if (columns.size() < 14) {
            log.warn("Expected at least 14 columns for team roster, but found {}. Players will not be parsed. Columns: {}", columns.size(), columns);
        }

        final List<Player> substitutes = new ArrayList<>();
        for (int i = sectionBoundary.getLeft() + 1; i < sectionBoundary.getRight(); i++) {
            String line = lines.get(i).trim();
            String[] tokens = line.split("\\s+");

            if (tokens.length > 0 && !ParserUtils.matchesHeaderTokens(tokens, TEAM_ROSTERS_SUBS_TABLE_HEADERS)) {
                final Player player = parsePlayerLine(line, columns);
                if (player != null) {
                    substitutes.add(player);
                }
            }
        }
        standings.setSubstitutes(substitutes);
    }

    private static TeamPlayers buildTeamPlayers(final Matcher teamNameMatcher, LeagueStandings standings) {
        TeamPlayers currentTeam;
        int teamNumber = ParserUtils.parseInt(teamNameMatcher.group(1));
        String teamName = teamNameMatcher.group(2).trim();
   
        // Lets find the TeamInfo
        TeamInfo teamInfo = standings.getTeamByNumber(teamNumber);
        if (teamInfo == null) {
            teamInfo = standings.searchTeamByName(teamName);
        }
        if (teamInfo == null) {
            // If still not found, create a new TeamInfo
            teamInfo = new TeamInfo(teamNumber, 0);
            teamInfo.setName(teamName);
        }
        currentTeam = new TeamPlayers(teamInfo);
        currentTeam.setPlayers(new java.util.ArrayList<>());
        return currentTeam;
    }

    // Kathy Novak                             126       75        756      6    150    391     222      607      a130   a130    a130    390      606
    // Spencer Ederoff                          0         0          0      0     0      0                                                 0        0
    // Josh Dolan                              141       62        424      3    165    424     227      610                               0        0
    // Nicole Luchetta                          0         0          0      0     0      0                        v120   v120    v120    360      603
    private Player parsePlayerLine(String line, List<ColumnDescriptor> columns) {

        if (columns.size() < 14) {
            return null; // Logged at the call site, no point repeating the warning here
        }

        final PlayerInfo playerInfo = new PlayerInfo(ParserUtils.extractColumnValue(line, columns.get(0), true));
        final Player player = new Player(playerInfo);

        // Avg HDCP gets clubbed into the Avg column header
        player.setAvg(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(1), true)));
        player.setHdcp(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(2), true)));
        player.setScratchPins(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(3), true)));
        player.setGames(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(4), true)));
        player.setHighScratchGame(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(5), true)));
        player.setHighScratchSeries(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(6), true)));
        player.setAvgHdcpGame(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(7), true)));
        player.setAvgHdcpSeries(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(8), true)));

        // Game scores
        Matcher gameScoreMatcher = PLAYER_GAME_SCORE_PATTERN.matcher("");
        Pair<Character, Integer> game1 = parseGameScore(ParserUtils.extractColumnValue(line, columns.get(9), true), gameScoreMatcher);
        if (game1 != null) {
            player.setLastWeekGame1(game1.getRight());
            player.setLastWeekGame1Label(game1.getLeft());
        }
        Pair<Character, Integer> game2 = parseGameScore(ParserUtils.extractColumnValue(line, columns.get(10), true), gameScoreMatcher);
        if (game2 != null) {
            player.setLastWeekGame2(game2.getRight());
            player.setLastWeekGame2Label(game2.getLeft());
        }
        Pair<Character, Integer> game3 = parseGameScore(ParserUtils.extractColumnValue(line, columns.get(11), true), gameScoreMatcher);
        if (game3 != null) {
            player.setLastWeekGame3(game3.getRight());
            player.setLastWeekGame3Label(game3.getLeft());
        }
        player.setLastWeekScratchTotal(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(12), true)));
        player.setLastWeekHdcpTotal(ParserUtils.parseInt(ParserUtils.extractColumnValue(line, columns.get(13), true)));
        log.trace("Parsed player line: {} -> {}", line, player);
        return player;
    }

    private Pair<Character, Integer> parseGameScore(String columnValue, Matcher gameScoreMatcher) {
        if (gameScoreMatcher.reset(columnValue).matches()) {
            Character label = gameScoreMatcher.group(1).isEmpty() ? null : gameScoreMatcher.group(1).charAt(0);
            Integer score = ParserUtils.parseInt(gameScoreMatcher.group(2));
            return Pair.of(label, score);
        }
        return null;
    }

}
