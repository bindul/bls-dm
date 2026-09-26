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
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.REVIEW_LAST_WEEKS_BOWLING_MARKER;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.Matchup;
import name.bindul.bls.dm.imports.ls.model.standings.MatchupScores;
import name.bindul.bls.dm.imports.ls.model.standings.TeamInfo;
import name.bindul.bls.dm.imports.ls.parsers.ParserUtils;

@UtilityClass
@Log4j2
public class StandingsMatchupsParser {

    private static final String[][] LAST_WEEKS_BOWLING_HEADERS = {
            {"HDCP", "HDCP", "HDCP", "HDCP", "Last", "Wk", "HDCP", "HDCP", "HDCP", "HDCP", "Last", "Wk"},
            {"Lanes", "Team", "Name", "-1-", "-2-", "-3-", "Total", "WON", "Team", "Name", "-1-", "-2-", "-3-", "Total", "WON"}
    };

    /*
HDCP HDCP HDCP HDCP Last Wk HDCP HDCP HDCP HDCP Last Wk
Lanes Team Name -1- -2- -3- Total WON Team Name -1- -2- -3- Total WON
1-2 Hookers & Bowl 752 811 877 2440 1 <---> Bada Bing!!!! 781 830 860 2471 3
3-4 Captive Spare 902 969 805 2676 3 <---> Brutal Poodle 798 803 819 2420 1
     */

    static void parseMatchups(List<String> lines, LeagueStandings standings) {
        int start = ParserUtils.findLineIndex(lines, REVIEW_LAST_WEEKS_BOWLING_MARKER);
        if (start < 0) {
            return;
        }

        int end = ParserUtils.findLineIndex(lines, INDIVIDUAL_HIGH_AVERAGES_MARKER);
        if (end < 0) {
            end = lines.size();
        }

        final List<Matchup> matchups = new ArrayList<>();

        for (int i = start + 1; i < end; i++) {
            final String line = lines.get(i).trim();
            String[] tokens = line.split("\\s+");

            // If the line is a matchup header, skip it, otherwise, parse the matchup
            if (!isMatchupHeaderToken(tokens)) {
                Matchup matchup = parseMatchupLine(tokens, standings);
                if (matchup != null) {
                    matchups.add(matchup);
                }
            }
        }

        standings.setLastWeekMatchups(matchups);
    }

    private static Matchup parseMatchupLine(String[] tokens, LeagueStandings standings) {
        // 1-2 Hookers & Bowl 752 811 877 2440 1 <---> Bada Bing!!!! 781 830 860 2471 3
        // 3-4 Captive Spare 902 969 805 2676 3 <---> Brutal Poodle 798 803 819 2420 1
        try {
            final int versusIndex = findVersusTokenIndex(tokens);
            if (tokens.length < 13 || !tokens[0].contains("-") || versusIndex < 0) {
                return null;
            }

            final int[] lanes = parseLanes(tokens[0]);
            if (lanes == null) {
                return null;
            }

            final int leftScoreStart = versusIndex - 5;
            final int rightScoreStart = tokens.length - 5;
            if (leftScoreStart <= 1 || rightScoreStart <= versusIndex + 1) {
                return null;
            }

            if (!ParserUtils.areIntegerTokens(tokens, leftScoreStart, versusIndex)
                || !ParserUtils.areIntegerTokens(tokens, rightScoreStart, tokens.length)) {
                return null;
            }

            final TeamInfo team1 = findOrCreateTeam(standings, ParserUtils.joinTokens(tokens, 1, leftScoreStart));
            final TeamInfo team2 = findOrCreateTeam(standings, ParserUtils.joinTokens(tokens, versusIndex + 1, rightScoreStart));
            if (team1.getName().isBlank() || team2.getName().isBlank()) {
                return null;
            }

            final Matchup matchup = new Matchup();
            matchup.setLanes(lanes);
            matchup.setTeam1(team1);
            matchup.setTeam2(team2);
            matchup.setTeam1Scores(buildScores(tokens, leftScoreStart));
            matchup.setTeam2Scores(buildScores(tokens, rightScoreStart));
            matchup.setTeam1PointsWon(ParserUtils.parseFraction(tokens[leftScoreStart + 4]));
            matchup.setTeam2PointsWon(ParserUtils.parseFraction(tokens[rightScoreStart + 4]));
            return matchup;
        } catch (RuntimeException ex) {
            log.warn("Failed to parse matchup line: " + String.join(" ", tokens) + ". Error: " + ex.getMessage(), ex);
            return null;
        }
    }

    private static TeamInfo findOrCreateTeam(LeagueStandings standings, String teamName) {
        TeamInfo teamStats = standings.searchTeamByName(teamName); // Matchups can truncate team names, so we need to search by name
        if (teamStats == null) {
            teamStats = new TeamInfo(-1, -1); // Create a new TeamInfo with default values if not found
            teamStats.setName(teamName);
        }
        return teamStats;
    }

    private static int findVersusTokenIndex(String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].contains("<") && tokens[i].contains(">")) {
                return i;
            }
        }
        return -1;
    }

    private static int[] parseLanes(String laneToken) {
        String[] parts = laneToken.split("-");
        if (parts.length != 2 || !ParserUtils.isIntegerToken(parts[0]) || !ParserUtils.isIntegerToken(parts[1])) {
            return new int[0];
        }
        return new int[] {ParserUtils.parseInt(parts[0]), ParserUtils.parseInt(parts[1])};
    }

    private static MatchupScores buildScores(String[] tokens, int scoreStart) {
        MatchupScores scores = new MatchupScores();
        scores.setGame1Score(ParserUtils.parseInt(tokens[scoreStart]));
        scores.setGame2Score(ParserUtils.parseInt(tokens[scoreStart + 1]));
        scores.setGame3Score(ParserUtils.parseInt(tokens[scoreStart + 2]));
        scores.setTotalScore(ParserUtils.parseInt(tokens[scoreStart + 3]));
        return scores;
    }

    private boolean isMatchupHeaderToken (String[] tokens) {
        return ParserUtils.matchesHeaderTokens(tokens, LAST_WEEKS_BOWLING_HEADERS);
    }
}
