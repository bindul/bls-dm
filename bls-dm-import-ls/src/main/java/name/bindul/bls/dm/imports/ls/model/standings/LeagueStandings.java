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
package name.bindul.bls.dm.imports.ls.model.standings;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import lombok.Data;

@Data
public class LeagueStandings {

    private LocalDate date;
    private int weekNumber;
    private String leagueName;
    private String dayOfWeek;
    private LocalTime startTime;
    private String bowlingCenter;
    private LeagueOfficer president;
    private LeagueOfficer secretary;
    private LeagueOfficer treasurer;

    private List<Division> divisions;
    private List<TeamStats> teamStandings;

    private List<Matchup> lastWeekMatchups;

    private List<AccoladeGroup> individualHighAverages;
    private List<AccoladeGroup> seasonHighScores;
    private List<AccoladeGroup> lastWeekHighScores;

    private List<TeamPlayers> players;
    private List<Player> substitutes;

    public TeamInfo getTeamByNumber(int teamNumber) {
        for (Division division : divisions) {
            for (TeamInfo teamInfo : division.getTeams()) {
                if (teamInfo.getNumber() == teamNumber) {
                    return teamInfo;
                }
            }
        }
        return null; // Team not found
    }

    public TeamInfo getTeamByName(String teamName) {
        for (Division division : divisions) {
            for (TeamInfo teamInfo : division.getTeams()) {
                if (teamInfo.getName().equalsIgnoreCase(teamName)) {
                    return teamInfo;
                }
            }
        }
        return null; // Team not found
    }

    public TeamInfo searchTeamByName(String teamName) {
        // TODO See if we we can cache this later?
        List<TeamInfo> allTeams = divisions.stream()
                .flatMap(division -> division.getTeams().stream())
                .toList();

        final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
        TeamInfo bestMatch = null;
        double highestSimilarity = -1.0;

        for (TeamInfo teamInfo : allTeams) {
            double similarity = jaroWinkler.apply(teamName, teamInfo.getName());
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestMatch = teamInfo;
            }
        }

        // Return the best match if the similarity is above a certain threshold (e.g., 0.8)
        return (highestSimilarity >= 0.8) ? bestMatch : null;
    }
}
