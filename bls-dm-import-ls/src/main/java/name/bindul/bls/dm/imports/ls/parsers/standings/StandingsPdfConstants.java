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

    static final String TEAM_STANDINGS_MARKER = "Team Standings In Each Division";
    static final String REVIEW_LAST_WEEKS_BOWLING_MARKER = "Review of Last Week's Bowling.....";
    static final String INDIVIDUAL_HIGH_AVERAGES_MARKER = "Individual High Averages";
    static final String SEASON_HIGH_SCORES_MARKER = "Season High Scores";
    static final String LAST_WEEK_TOP_SCORES_MARKER = "Last Week's Top Scores";
    static final String TEAM_ROSTERS_MARKER = "Team Rosters";
    static final String TEMP_SUBSTITUTES_MARKER = "Temporary Substitutes";
    static final Pattern PAGE_HEADER_LINE_PATTERN = Pattern.compile("^(\\d{1,2}/\\d{1,2}/\\d{4})\\s+Week\\s+(\\d+)(?:\\s+of\\s+(\\d+))?");
    static final Pattern PAGE_FOOTER_LINE_PATTERN = Pattern.compile("^(\\d{1,2}/\\d{1,2}/\\d{4})\\s+(\\d{1,2}:\\d{2}\\s+(?:am|pm))\\s+Page\\s+(\\d+)\\s+of\\s+(\\d+)?");
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
