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
package name.bindul.bls.dm.core.util;

import lombok.experimental.UtilityClass;
import name.bindul.bls.dm.core.model.scoreorig.Game;

/**
 * 
 */
@UtilityClass
public class BowlingScoreParser {
    // TODO See if this is the right home for this utility class.
    
    public static Game parseFrameScoreArray(String[][] frameScores) {
        // TODO Implement parsing logic to convert frameScores into a Game object.
        return null; // Placeholder return statement
    }
    
    public static Game parseFrameScore(String frameScores) {
        // TODO Implement parsing logic to convert frameScores into a Game object.
        return null; // Placeholder return statement
    }
    
    // TODO Have some sort of builder concept to add / remove items as someone is typing in the score. This will help with the UI and also with the parsing of the score.
}
