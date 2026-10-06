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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import name.bindul.bls.dm.imports.ls.model.standings.LanePair;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.Matchup;

class StandingsMatchupsParserTest {

    @Test
    void parseMatchups_shouldParseValidMatchupLines() {
        List<String> lines = List.of(
            "Review of Last Week's Bowling.....",
            "HDCP HDCP HDCP HDCP Last Wk HDCP HDCP HDCP HDCP Last Wk",
            "Lanes Team Name -1- -2- -3- Total WON Team Name -1- -2- -3- Total WON",
            "1-2 Hookers & Bowl 752 811 877 2440 1 <---> Bada Bing!!!! 781 830 860 2471 3",
            "3-4 Captive Spare 902 969 805 2676 3 <---> Brutal Poodle 798 803 819 2420 1",
            "Individual High Averages"
        );
        
        LeagueStandings standings = new LeagueStandings();
        StandingsMatchupsParser.parseMatchups(lines, standings);

        List<Matchup> matchups = standings.getLastWeekMatchups();
        assertEquals(2, matchups.size());

        Matchup first = matchups.get(0);
        assertEquals(new LanePair (1, 2), first.getLanes());
        assertEquals("Hookers & Bowl", first.getTeam1().getName());
        assertEquals(752, first.getTeam1Scores().getGame1Score());
        assertEquals(811, first.getTeam1Scores().getGame2Score());
        assertEquals(877, first.getTeam1Scores().getGame3Score());
        assertEquals(2440, first.getTeam1Scores().getTotalScore());
        assertEquals(1, first.getTeam1PointsWon());
        assertEquals("Bada Bing!!!!", first.getTeam2().getName());
        assertEquals(781, first.getTeam2Scores().getGame1Score());
        assertEquals(830, first.getTeam2Scores().getGame2Score());
        assertEquals(860, first.getTeam2Scores().getGame3Score());
        assertEquals(2471, first.getTeam2Scores().getTotalScore());
        assertEquals(3, first.getTeam2PointsWon());
    }

    @Test
    void parseMatchups_shouldSkipMalformedContentLines() {
        List<String> lines = List.of(
            "Review of Last Week's Bowling.....",
            "1-2 Broken Team 752 811 877 2440 1 Bada Bing!!!! 781 830 860 2471 3",
            "Individual High Averages"
        );

        LeagueStandings standings = new LeagueStandings();
        StandingsMatchupsParser.parseMatchups(lines, standings);

        List<Matchup> matchups = standings.getLastWeekMatchups();

        assertNotNull(matchups);
        assertEquals(0, matchups.size());
    }
}
