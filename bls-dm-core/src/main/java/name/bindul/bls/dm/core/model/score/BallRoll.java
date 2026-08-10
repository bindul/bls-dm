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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * A single roll of a bowling ball in a game. It records the number of pins knocked down in that roll.
 */
@Data
public class BallRoll {

	private int rollNumber; // 1 or 2, or 3 in the case of the 10th frame
	
	private int pinsKnockedDown; // 0 to 10

	/**
	 * Represents the pins that are still standing after the roll. Each bit corresponds to a pin, with 1 
	 * indicating the pin is standing and 0 indicating it has been knocked down.
	 */
	private PinsStanding pinsStanding;
	
	@Delegate @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE)
	private ScoreLabelColl scoreLabels = new ScoreLabelColl(ScoreComponent.BALL_ROLL);
}
