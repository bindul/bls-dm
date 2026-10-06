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

import lombok.Data;

@Data
public class Matchup {

    private LanePair lanes;
    private TeamInfo team1;
    private MatchupScores team1Scores;
    private float team1PointsWon;
    private TeamInfo team2;
    private MatchupScores team2Scores;
    private float team2PointsWon;
}
