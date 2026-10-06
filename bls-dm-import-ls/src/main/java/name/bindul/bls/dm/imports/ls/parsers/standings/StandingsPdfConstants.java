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

import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StandingsPdfConstants {

    static final Pattern TEAM_STANDINGS_MARKER = Pattern.compile("Team\\s+Standings\\s+In\\s+Each\\s+Division");
    static final Pattern REVIEW_LAST_WEEKS_BOWLING_MARKER = Pattern.compile("Review\\s+of\\s+Last\\s+Week's\\s+Bowling.....");
    static final Pattern INDIVIDUAL_HIGH_AVERAGES_MARKER = Pattern.compile("Individual\\s+High\\s+Averages");
    static final Pattern SEASON_HIGH_SCORES_MARKER = Pattern.compile("Season\\s+High\\s+Scores");
    static final Pattern LAST_WEEK_TOP_SCORES_MARKER = Pattern.compile("Last\\s+Week's\\s+Top\\s+Scores");
    static final Pattern TEAM_ROSTERS_MARKER = Pattern.compile("Team\\s+Rosters");
    static final Pattern TEMP_SUBSTITUTES_MARKER = Pattern.compile("Temporary\\s+Substitutes");
    static final Pattern PAGE_HEADER_LINE_PATTERN = Pattern.compile("^\\s*(\\d{1,2}/\\d{1,2}/\\d{4})\\s+Week\\s*(\\d+)\\s*of\\s*(\\d+)"); // Don't take it to the end of the line, other parsers depend on the index of the end of this match
    static final Pattern PAGE_FOOTER_LINE_PATTERN = Pattern.compile("^\\s*(\\d{1,2}/\\d{1,2}/\\d{4})\\s+(\\d{1,2}:\\d{2}\\s+(?:am|pm))\\s+Page\\s+(\\d+)\\s+of\\s+(\\d+)?");
    static final String[] HEADER_PREFIXES = {
        "bls-",
        "page",
        "name",
        "avg hdcp",
        "high",
        "hdcp",
        "pins",
        "week",
        "team standings",
        "team rosters",
        "temporary substitutes",
        "individual high averages",
        "season high scores",
        "last week's top scores"
    };
}
