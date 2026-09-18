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
package name.bindul.bls.dm.core.model.scoreorig;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import name.bindul.bls.dm.core.model.score.PinsStanding;

//@Builder
@Data @Builder
public class BallRoll {
    /**
     * Number of pins knocked down in this roll. Valid values are from 0 to 10.
     */
    private int pinsKnockedDown; // 0 to 10

    /**
     * Represents the pins that are still standing after the roll. Each bit corresponds to a pin, with 1 
     * indicating the pin is standing and 0 indicating it has been knocked down.
     */
    private PinsStanding pinsStanding;
    
    /**
     * Holds the score labels associated with this roll, such as STRIKE, SPARE, etc. This is used for 
     * scoring and display purposes.
     */
    @Builder.Default @Setter(lombok.AccessLevel.PRIVATE)
    private ScoreLabelColl scoreLabels = new ScoreLabelColl(ScoreComponent.BALL_ROLL);
}