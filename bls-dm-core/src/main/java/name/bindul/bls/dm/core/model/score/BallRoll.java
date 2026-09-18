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
package name.bindul.bls.dm.core.model.score;

import lombok.Data;

@Data
public class BallRoll {

    private int rollNumber; // Roll number within the frame (1, 2, or 3 for the 10th frame)
    private int pinsKnockedDown; // Number of pins knocked down in this roll (0-10)
    private PinsStanding pinsStanding; // Represents the pins that are still standing after the roll
    
    // TODO Score labels for this roll, such as STRIKE, SPARE, etc. This is used for scoring and display purposes.
}
