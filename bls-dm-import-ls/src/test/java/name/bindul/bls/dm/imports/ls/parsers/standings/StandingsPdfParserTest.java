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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import name.bindul.bls.dm.imports.ls.model.standings.Division;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.TeamStats;
import name.bindul.bls.dm.imports.ls.parsers.standings.StandingsMetadataParser;
import name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfParser;

class StandingsPdfParserTest {

    private static final Path SAMPLE_PDF = Path.of("src", "test", "resources", "13301609102026f202602standg00.pdf");

    @Test
    void parse_shouldExtractLeagueMetadataFromSamplePdf() throws IOException {
        StandingsPdfParser parser = new StandingsPdfParser();

        LeagueStandings standings = parser.parse(SAMPLE_PDF);

        assertNotNull(standings);
        assertEquals("Beer Fall 2026", standings.getLeagueName());
        assertEquals("Arapahoe Bowling Center", standings.getBowlingCenter());
        assertEquals("Thursday", standings.getDayOfWeek());
        assertEquals(LocalTime.of(20, 0), standings.getStartTime());
        assertEquals(LocalDate.of(2026, 9, 10), standings.getDate());
        assertEquals(2, standings.getWeekNumber());
        assertNotNull(standings.getPresident());
        assertEquals("Jake Eskanos", standings.getPresident().getName());
        assertEquals("jeskanos1@gmail.com", standings.getPresident().getEmail());
        assertNotNull(standings.getSecretary());
        assertEquals("Chris Fiser", standings.getSecretary().getName());
        assertEquals("chrisfiser19@gmail.com", standings.getSecretary().getEmail());
        assertNotNull(standings.getTreasurer());
        assertEquals("Chris Fiser", standings.getTreasurer().getName());
        assertEquals("chrisfiser19@gmail.com", standings.getTreasurer().getEmail());
    }

    @Test
    void parseMetadata_shouldParseSeparateSecretaryAndTreasurerLines() throws Exception {
        LeagueStandings standings = new LeagueStandings();
        List<String> lines = List.of(
            "09/10/2026 Week 2 of 15 Beer Fall 2026 Page 1",
            "Thursday 08:00 pm Arapahoe Bowling Center Lanes 1 - 40",
            "President: Jake Eskanos is on Lane 39 jeskanos1@gmail.com",
            "Secretary: Chris Fiser chrisfiser19@gmail.com",
            "Treasurer: Alex Doe alex.doe@example.com",
            "Team Standings In Each Division"
        );

        StandingsMetadataParser.parseMetadata(lines, standings);

        assertEquals("Beer Fall 2026", standings.getLeagueName());
        assertEquals(LocalDate.of(2026, 9, 10), standings.getDate());
        assertEquals(2, standings.getWeekNumber());
        assertEquals("Thursday", standings.getDayOfWeek());
        assertEquals(LocalTime.of(20, 0), standings.getStartTime());
        assertEquals("Arapahoe Bowling Center", standings.getBowlingCenter());
        assertNotNull(standings.getPresident());
        assertEquals("Jake Eskanos", standings.getPresident().getName());
        assertEquals("jeskanos1@gmail.com", standings.getPresident().getEmail());
        assertNotNull(standings.getSecretary());
        assertEquals("Chris Fiser", standings.getSecretary().getName());
        assertEquals("chrisfiser19@gmail.com", standings.getSecretary().getEmail());
        assertNotNull(standings.getTreasurer());
        assertEquals("Alex Doe", standings.getTreasurer().getName());
        assertEquals("alex.doe@example.com", standings.getTreasurer().getEmail());
    }

    @Test
    void parse_shouldBuildFullLeagueStandingsFromSamplePdf() throws IOException {
        StandingsPdfParser parser = new StandingsPdfParser();

        LeagueStandings standings = parser.parse(SAMPLE_PDF);

        assertNotNull(standings);
        assertEquals("Beer Fall 2026", standings.getLeagueName());
        assertEquals("Arapahoe Bowling Center", standings.getBowlingCenter());
        assertEquals(LocalDate.of(2026, 9, 10), standings.getDate());
        assertEquals(2, standings.getWeekNumber());
    }

}
