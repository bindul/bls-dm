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
package name.bindul.bls.dm.imports.ls;

import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.parsers.pdf.LayoutTextStripper;
import name.bindul.bls.dm.imports.ls.parsers.standings.StandingsPdfParser;
import tools.jackson.databind.ObjectMapper;

public class ParseLeagueSecretaryFile {

    public static void main(String[] args) {
        StandingsPdfParser parser = new StandingsPdfParser();
        try {
//            LeagueStandings standings = parser.parse(Path.of("src", "test", "resources", "13301609102026f202602standg00.pdf"));
//            System.out.println(standings);
//            printLeagueStandingsJson(standings);
            System.out.println("--------------------------------------------------");
            LeagueStandings standings2 = parser.parse(Path.of("src", "test", "resources", "13301609172026f202603standg00.pdf"));
//            System.out.println(standings2);
            System.out.println("--------------------------------------------------");
            LeagueStandings standings3 = parser.parse(Path.of("src", "test", "resources", "13301609032026f202601standg00.pdf"));
            printLeagueStandingsJson(standings3);
            
            
//            try (PDDocument document = Loader.loadPDF(Path.of("src", "test", "resources", "13301609032026f202601standg00.pdf").toFile())) {
//                LayoutTextStripper stripper = new LayoutTextStripper();
//                stripper.setSortByPosition(true);
//                stripper.setAddMoreFormatting(true);
//                System.out.println(stripper.getText(document));
//            }
            
        } catch (Exception e) {
            System.err.println("Error parsing PDF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printLeagueStandingsJson(LeagueStandings standings) {
        // Implement JSON printing logic here, e.g., using a library like Jackson or Gson
        // For example, using Jackson:
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(standings);
        System.out.println(json);
    }
}
