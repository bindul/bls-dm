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
import java.util.regex.Pattern;

import name.bindul.bls.dm.imports.ls.model.standings.Division;
import name.bindul.bls.dm.imports.ls.model.standings.LeagueStandings;
import name.bindul.bls.dm.imports.ls.model.standings.Matchup;
import name.bindul.bls.dm.imports.ls.parsers.ParserUtils;
import name.bindul.bls.dm.imports.ls.parsers.config.LeagueSecretaryConfig;
import name.bindul.bls.dm.imports.ls.parsers.pdf.PdfParser;

/**
 * Parses a League Secretary standings PDF into the full {@link LeagueStandings} model.
 */
public class StandingsPdfParser {

    private static final Pattern TEAM_HEADER_PATTERN = Pattern.compile("^(\\d+)\\s*-\\s+(.+)$");

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
        final List<String> lines = ParserUtils.normalizeLines(text);
        cleanHeadersAndFooters(lines, true);
        LeagueStandings standings = new LeagueStandings();

        StandingsMetadataParser.parseMetadata(lines, standings);

        TeamStandingsParser.parseDivisionAndTeams(lines, standings);
        StandingsMatchupsParser.parseMatchups(lines, standings);

        // Parse rosters and substitutes before accolades, as accolades reference players in the rosters or substitutes
        TeamRosterAndSubstitutesParser.parseTeamRoster(lines, standings);
        TeamRosterAndSubstitutesParser.parseSubstitutes(lines, standings);

//      standings.setIndividualHighAverages(parseAccoladeGroups(lines, INDIVIDUAL_HIGH_AVERAGES_MARKER));
//      standings.setSeasonHighScores(parseAccoladeGroups(lines, SEASON_HIGH_SCORES_MARKER));
//      standings.setLastWeekHighScores(parseAccoladeGroups(lines, LAST_WEEK_TOP_SCORES_MARKER));

//      List<Player> substitutes = new ArrayList<>();
//      parseRostersAndSubstitutes(lines, teamMap, substitutes);
//      standings.setSubstitutes(substitutes);
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

/*


    private List<AccoladeGroup> parseAccoladeGroups(List<String> lines, String marker) {
        int start = ParserUtils.findLineIndex(lines, marker);
        if (start < 0) {
            return new ArrayList<>();
        }

        int end = findNextSectionMarker(lines, start + 1);
        List<AccoladeGroup> groups = new ArrayList<>();
        for (int i = start + 1; i < end; i++) {
            String line = lines.get(i);
            if (isAccoladeHeaderLine(line)) {
                continue;
            }
            AccoladeGroup group = parseAccoladeGroupLine(line, marker);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    private int findNextSectionMarker(List<String> lines, int startIndex) {
        for (int i = startIndex; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.equalsIgnoreCase(TEAM_STANDINGS_MARKER)
                || line.equalsIgnoreCase(INDIVIDUAL_HIGH_AVERAGES_MARKER)
                || line.equalsIgnoreCase(SEASON_HIGH_SCORES_MARKER)
                || line.equalsIgnoreCase(LAST_WEEK_TOP_SCORES_MARKER)
                || line.equalsIgnoreCase(TEAM_ROSTERS_MARKER)
                || line.equalsIgnoreCase(TEMP_SUBSTITUTES_MARKER)) {
                return i;
            }
        }
        return lines.size();
    }

    private boolean isAccoladeHeaderLine(String line) {
        return line.startsWith("Handicap ")
            || line.startsWith("Team ")
            || line.startsWith("Scratch ")
            || line.startsWith("High ")
            || line.startsWith("Weekly ")
            || line.startsWith("Name ")
            || line.startsWith("Lanes ")
            || line.startsWith("HDCP ")
            || line.startsWith("Pins ");
    }

    private AccoladeGroup parseAccoladeGroupLine(String line, String sectionMarker) {
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2 || isAccoladeHeaderLine(line)) {
            return null;
        }

        int index = 0;
        String holderType = null;
        if (!isGameScoreToken(tokens[0])) {
            holderType = tokens[0];
            index = 1;
        }

        if (index >= tokens.length || !isGameScoreToken(tokens[index])) {
            return null;
        }

        List<Accolade> accolades = new ArrayList<>();
        while (index < tokens.length) {
            if (!isGameScoreToken(tokens[index])) {
                index++;
                continue;
            }

            int position = ParserUtils.parseInt(tokens[index++]);
            StringBuilder holderName = new StringBuilder();
            while (index < tokens.length && !isGameScoreToken(tokens[index])) {
                if (holderName.length() > 0) {
                    holderName.append(' ');
                }
                holderName.append(tokens[index++]);
            }

            if (holderName.length() == 0) {
                continue;
            }

            Accolade accolade = new Accolade();
            accolade.setPosition(position);
            accolade.setHolderName(holderName.toString());
            accolade.setValue(Integer.toString(position));
            accolades.add(accolade);
        }

        if (accolades.isEmpty()) {
            return null;
        }

        AccoladeGroup group = new AccoladeGroup();
        group.setAccoladeHolderType(holderType);
        group.setDivision(null);
        group.setAccoladeType(sectionMarker);
        group.setGender(holderType != null && (holderType.equalsIgnoreCase("Men") || holderType.equalsIgnoreCase("Women"))
            ? holderType
            : null);
        group.setAccolades(accolades);
        return group;
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