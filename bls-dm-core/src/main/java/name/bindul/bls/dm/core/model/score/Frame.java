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

import java.util.Iterator;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import name.bindul.bls.dm.core.model.BlsStatsHolder;

/**
 * A single frame in a bowling game. A frame consists of one or two rolls, depending on whether the player rolls a 
 * strike or not. In the case of the 10th frame, there can be up to three rolls if the player rolls a strike or spare.
 */
@Data
public class Frame implements BlsStatsHolder, Iterable<BallRoll> {

	private int frameNumber; // 1 to 10
	private List<BallRoll> rolls; // 1 or 2 rolls, or 3 in the case of the 10th frame
	private int rawScore; // Score for the frame without bonuses from subsequent rolls
	private int frameScore; // Total score for the frame, including bonuses from subsequent rolls
	private int cumulativeScore; // Cumulative score up to this frame
	@Delegate @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE)
    private ScoreLabelColl scoreLabels = new ScoreLabelColl(ScoreComponent.FRAME);
	
    @Override
    public Iterator<BallRoll> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
	
	@Override
	public void calculateStats(boolean forceRecalculate) {
		
		// TODO Implement
		
		// Right now we blind re-calculate everything!
		if (rolls != null && !rolls.isEmpty()) {
			
			int frameScoreAccumulator = 0;
			for (int i = 0; i < 3 && i < rolls.size(); i++) {
				if (i > 1 && frameNumber < 10) {
					break; // Only 2 rolls in frames 1-9
				}
				
				BallRoll roll = rolls.get(i);
				if (roll.getRollNumber() == 0) {
					roll.setRollNumber(i + 1); // Set roll number if not already set
				}
				
//				if (roll.getPinsKnockedDown() == 0 && roll.getPinsStanding() != null) {
//					// Calculate pins knocked down based on pins standing
//					int pinsKnockedDown = 10 - roll.getPinsStanding().countStandingPins(); // Need to account for stuff like splits, fouls, etc. in a real implementation
//					roll.setPinsKnockedDown(pinsKnockedDown);
//				}
//				framesco
				
				// Compute labels
			}
			
			rawScore = rolls.stream().mapToInt(BallRoll::getPinsKnockedDown).sum();
		} else {
			rawScore = 0;
		}
	}
}
