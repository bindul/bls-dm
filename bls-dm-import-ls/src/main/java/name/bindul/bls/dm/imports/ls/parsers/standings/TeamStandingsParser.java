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

import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.REVIEW_LAST_WEEKS_BOWLING_MARKER;
import static name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfConstants.TEAM_STANDINGS_MARKER;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import name.bindul.bls.dm.imports.ls.model.standings.Division;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.TeamInfo;
import name.bindul.bls.dm.imports.ls.model.standings.TeamStats;
import name.bindul.bls.dm.imports.ls.parsers.ParserUtils;

@UtilityClass
@Log4j2
public class TeamStandingsParser {

    private static final String[][] TEAM_STANDINGS_HEADERS = {
            {"Points", "Points", "Team"},
            {"Place", "#", "Team", "Name"}
    };

    static void parseDivisionAndTeams(List<String> lines, LeagueStandings standings) {
        int start = ParserUtils.findLineIndex(lines, TEAM_STANDINGS_MARKER);
        if (start < 0) {
            return;
        }

        int end = ParserUtils.findLineIndex(lines, REVIEW_LAST_WEEKS_BOWLING_MARKER);
        if (end < 0) {
            end = lines.size();
        }

        List<Division> divisions = new ArrayList<>();
        List<TeamStats> allTeams = new ArrayList<>();
        Division currentDivision = null;

        for (int i = start + 1; i < end; i++) {
            final String line = lines.get(i).trim();
            String[] tokens = line.split("\\s+");

            // Check if the line is a division header
            if (!isTeamLine(tokens) && !isTeamStandingsHeaderToken(tokens)) {
                // This line is likely a division header
                currentDivision = new Division();
                currentDivision.setNumber(divisions.size() + 1);
                currentDivision.setName(line);
                currentDivision.setTeams(new ArrayList<>());
                divisions.add(currentDivision);
            }
            
            if (currentDivision != null && isTeamLine(tokens)) {
                TeamStats teamStats = parseTeamLine(tokens, currentDivision);
                if (teamStats != null) {
                    currentDivision.getTeams().add(teamStats.getTeamInfo());
                    allTeams.add(teamStats);
                }
            }
        }

        standings.setDivisions(divisions);
        standings.setTeamStandings(allTeams);
    }

    private TeamStats parseTeamLine(String[] tokens, Division division) {
        try {
            if (!isTeamLine(tokens)) {
                return null;
            }

            int statsStart = tokens.length - 10;

            // This is the first time we see a team, so we assume we create the team here
            final int teamNumber = ParserUtils.parseInt(tokens[1]);
            final TeamInfo teamInfo = new TeamInfo(teamNumber, division.getNumber());
            teamInfo.setName(ParserUtils.joinTokens(tokens, 2, statsStart));

            final TeamStats teamStats = new TeamStats(teamInfo);
            teamStats.setPosition(ParserUtils.parseInt(tokens[0]));
            teamStats.setPointsWon(ParserUtils.parseFraction(tokens[statsStart]));
            teamStats.setPointsLost(ParserUtils.parseFraction(tokens[statsStart + 1]));
            teamStats.setTeamAvg(ParserUtils.parseInt(tokens[statsStart + 2]));
            teamStats.setHdcp(ParserUtils.parseInt(tokens[statsStart + 3]));
            teamStats.setHdcpPins(ParserUtils.parseInt(tokens[statsStart + 4]));
            teamStats.setScratchPins(ParserUtils.parseInt(tokens[statsStart + 5]));
            teamStats.setHdcpGame(ParserUtils.parseInt(tokens[statsStart + 6]));
            teamStats.setHdcpSeries(ParserUtils.parseInt(tokens[statsStart + 7]));
            teamStats.setHighGame(ParserUtils.parseInt(tokens[statsStart + 8]));
            teamStats.setHighSeries(ParserUtils.parseInt(tokens[statsStart + 9]));
            return teamStats;
        } catch (RuntimeException e) {
            log.warn("Failed to parse team line: " + String.join(" ", tokens) + ". Error: " + e.getMessage(), e);
            return null;
        }
    }

    private boolean isTeamLine(String[] tokens) {
        return tokens.length >= 13 && ParserUtils.isIntegerToken(tokens[0]) && ParserUtils.isIntegerToken(tokens[1]) && !isTeamStandingsHeaderToken(tokens);
    }

    private boolean isTeamStandingsHeaderToken (String[] tokens) {
        return ParserUtils.matchesHeaderTokens(tokens, TEAM_STANDINGS_HEADERS);
    }
}
