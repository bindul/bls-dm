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

import java.util.List;

import lombok.experimental.UtilityClass;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;

@UtilityClass
public class TeamRosterAndSubstitutesParser {

    static void parseTeamRoster(List<String> lines, LeagueStandings standings) {
        
    }

    static void parseSubstitutes(List<String> lines, LeagueStandings standings) {
        
    }

    /*
    private void parseRostersAndSubstitutes(List<String> lines, Map<Integer, Team> teamMap, List<Player> substitutes) {
        boolean inRosterSection = false;
        boolean inSubstituteSection = false;
        Integer currentTeamNumber = null;

        for (String line : lines) {
            if (line.equalsIgnoreCase(TEAM_ROSTERS_MARKER)) {
                inRosterSection = true;
                inSubstituteSection = false;
                currentTeamNumber = null;
                continue;
            }
            if (!inRosterSection) {
                continue;
            }
            if (line.equalsIgnoreCase(TEMP_SUBSTITUTES_MARKER)) {
                inSubstituteSection = true;
                currentTeamNumber = null;
                continue;
            }
            if (isRosterHeaderLine(line)) {
                Matcher matcher = TEAM_HEADER_PATTERN.matcher(line);
                if (matcher.matches()) {
                    currentTeamNumber = ParserUtils.parseInt(matcher.group(1));
                }
                continue;
            }

            Player player = parsePlayerLine(line);
            if (player == null) {
                continue;
            }

            if (inSubstituteSection) {
                substitutes.add(player);
                continue;
            }

            if (currentTeamNumber != null) {
                Team team = teamMap.get(currentTeamNumber);
                if (team != null) {
                    team.getPlayers().add(player);
                }
            }
        }
    }

    private boolean isRosterHeaderLine(String line) {
        return TEAM_HEADER_PATTERN.matcher(line).matches();
    }

    private Player parsePlayerLine(String line) {
        try {
            String[] tokens = line.split("\\s+");
            int firstNumericIndex = findFirstNumericTokenIndex(tokens);
            if (firstNumericIndex < 1 || tokens.length < firstNumericIndex + 8) {
                return null;
            }

            Player player = new Player();
            player.setName(ParserUtils.joinTokens(tokens, 0, firstNumericIndex));
            player.setAvg(ParserUtils.parseInt(tokens[firstNumericIndex]));
            player.setHdcp(ParserUtils.parseInt(tokens[firstNumericIndex + 1]));
            player.setScratchPins(ParserUtils.parseInt(tokens[firstNumericIndex + 2]));
            player.setGames(ParserUtils.parseInt(tokens[firstNumericIndex + 3]));
            player.setHighScratchGame(ParserUtils.parseInt(tokens[firstNumericIndex + 4]));
            player.setHighScratchSeries(ParserUtils.parseInt(tokens[firstNumericIndex + 5]));
            player.setAvgHdcpGame(ParserUtils.parseInt(tokens[firstNumericIndex + 6]));
            player.setAvgHdcpSeries(ParserUtils.parseInt(tokens[firstNumericIndex + 7]));

            List<Integer> trailingValues = new ArrayList<>();
            for (int i = firstNumericIndex + 8; i < tokens.length; i++) {
                if (isGameScoreToken(tokens[i])) {
                    trailingValues.add(ParserUtils.parseInt(tokens[i]));
                }
            }

            if (trailingValues.size() >= 2) {
                int totalIndex = trailingValues.size() - 2;
                if (totalIndex > 0) {
                    player.setLastWeekGame1(trailingValues.get(0));
                }
                if (totalIndex > 1) {
                    player.setLastWeekGame2(trailingValues.get(1));
                }
                if (totalIndex > 2) {
                    player.setLastWeekGame3(trailingValues.get(2));
                }
                player.setLastWeekScratchTotal(trailingValues.get(totalIndex));
                player.setLastWeekHdcpTotal(trailingValues.get(totalIndex + 1));
            }

            return player;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int findFirstNumericTokenIndex(String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            if (isGameScoreToken(tokens[i])) {
                return i;
            }
        }
        return -1;
    }
     */
}
