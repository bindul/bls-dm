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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.parsers.config.LeagueSecretaryConfig;
import name.bindul.bls.dm.imports.ls.parsers.pdf.PdfParser;
import name.bindul.bls.dm.imports.ls.parsers.shared.ParserUtils;

/**
 * Parses a League Secretary standings PDF into the full {@link LeagueStandings} model.
 */
public class StandingsPdfParser {

    private static final String OCR_ENABLED_KEY = "standings.ocr.enabled";
    private static final String OCR_LANGUAGE_KEY = "standings.ocr.language";
    private static final String OCR_MODEL_RESOURCE_KEY = "standings.ocr.model-resource";

    private final PdfParser pdfParser;
    private final PdfParser.OcrOptions ocrOptions;

    public StandingsPdfParser() {
        LeagueSecretaryConfig config = LeagueSecretaryConfig.load();
        this.pdfParser = new PdfParser();
        this.ocrOptions = new PdfParser.OcrOptions(
            config.requireBoolean(OCR_ENABLED_KEY),
            config.requireString(OCR_LANGUAGE_KEY),
            config.requireString(OCR_MODEL_RESOURCE_KEY),
            3,
            300
        );
    }

    public LeagueStandings parse(Path pdfPath) throws IOException {

        final String text = pdfParser.extractText(pdfPath, ocrOptions);
        final List<String> lines = ParserUtils.normalizeLines(text, false);

        cleanHeadersAndFooters(lines, true);
        LeagueStandings standings = new LeagueStandings();

        StandingsMetadataParser.parseMetadata(lines, standings);

        TeamStandingsParser.parseDivisionAndTeams(lines, standings);
        StandingsMatchupsParser.parseMatchups(lines, standings);

        // Parse rosters and substitutes before accolades, as accolades reference players in the rosters or substitutes
        PlayersAndSubstitutesParser.parseTeamRoster(lines, standings);
        PlayersAndSubstitutesParser.parseSubstitutes(lines, standings);

//        StandingsAccoladesParser.parseIndividualHighAverages(lines, standings);
        StandingsAccoladesParser.parseSeasonHighScores(lines, standings);

        return standings;
    }

    private void cleanHeadersAndFooters(List<String> lines, boolean leaveFirstPageHeader) {
        final Iterator<String> linesItr = lines.iterator();
        final Matcher headerMatcher = StandingsPdfConstants.PAGE_HEADER_LINE_PATTERN.matcher("");
        final Matcher footerMatcher = StandingsPdfConstants.PAGE_FOOTER_LINE_PATTERN.matcher("");
        boolean firstHeaderProcessed = false;

        while (linesItr.hasNext()) {
            String line = linesItr.next();
            if (line == null || line.trim().isEmpty()) {
                linesItr.remove();
            } else if (headerMatcher.reset(line).lookingAt() || footerMatcher.reset(line).lookingAt()) {
                if (!firstHeaderProcessed && headerMatcher.lookingAt()) {
                    firstHeaderProcessed = true;
                    if (leaveFirstPageHeader) {
                        continue;
                    }
                }
                linesItr.remove();
            }
        }
    }
}
