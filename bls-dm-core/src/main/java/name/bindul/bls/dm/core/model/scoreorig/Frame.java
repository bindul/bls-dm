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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import name.bindul.bls.dm.core.model.BlsStatsHolder;
import name.bindul.bls.dm.core.model.score.PinsStanding;

/**
 * A single frame in a bowling game. A frame consists of one or two rolls, depending on whether the player rolls a 
 * strike or not. In the case of the 10th frame, there can be up to three rolls if the player rolls a strike or spare.
 */
@Data
public class Frame implements BlsStatsHolder, Iterable<BallRoll> {

    /**
     * Frame number (1 to 10)
     */
	private int frameNumber; // 1 to 10
	@Setter(value = AccessLevel.PRIVATE)
	private List<BallRoll> rolls = new ArrayList<>(3); // 1 or 2 rolls, or 3 in the case of the 10th frame
	/**
	 * Number of rolls in this frame (1 or 2, or 3 in the case of the 10th frame)
	 */
	private int rollCount;
	
	private int rawScore; // Score for the frame without bonuses from subsequent rolls
	private int frameScore; // Total score for the frame, including bonuses from subsequent rolls
	private int cumulativeScore; // Cumulative score up to this frame
	
    private ScoreLabelColl scoreLabels = new ScoreLabelColl(ScoreComponent.FRAME);

	///
	/// Ball Roll Accessors
	/// 

	public void addRoll(int pinsKnockedDown, PinsStanding pinsStanding, ScoreLabel... scoreLabels) {
        if (rollCount >= rolls.size() || (frameNumber < 10 && rollCount >= 2)) {
            throw new IllegalStateException("Cannot add more rolls to this frame");
        }
        setRoll(rollCount + 1, pinsKnockedDown, pinsStanding, scoreLabels);
	}
	
	public void setRoll(int rollNumber, int pinsKnockedDown, PinsStanding pinsStanding, ScoreLabel... scoreLabels) {
        if (rollNumber < 1 || rollNumber > rolls.size() || rollNumber > rollCount + 1 || (frameNumber < 10 && rollNumber > 1)) {
            throw new IllegalArgumentException("Invalid roll index for the frame");
        }
        if (pinsKnockedDown < 0 || pinsKnockedDown > 10) {
            throw new IllegalArgumentException("Pins knocked down must be between 0 and 10");
        }
        
        BallRoll roll = BallRoll.builder()
                .pinsKnockedDown(pinsKnockedDown)
                .pinsStanding(pinsStanding)
                .build();
        if (scoreLabels != null && scoreLabels.length > 0) {
            roll.getScoreLabels().addAllScoreLabels(List.of(scoreLabels));
        }
        
        rolls.set(rollNumber - 1, roll);
        this.rollCount = Math.max(rollCount, rollNumber);
        // Recalculate pins knocked down as we may have reset preveious rolls
        calculatePinsKnockedDownFromStanding();
    }
	
    public BallRoll getBallRoll(int rollNumber) {
        validateRollNumber(rollNumber);
        return rolls.get(rollNumber - 1);
    }
	
	public int getPinsKnockedDownForRoll(int rollNumber) {
        BallRoll roll = getBallRoll(rollNumber);
        return (roll != null) ? roll.getPinsKnockedDown() : 0;
    }

	public PinsStanding getPinsStandingForRoll(int rollNumber) {
        BallRoll roll = getBallRoll(rollNumber);
        return (roll != null) ? roll.getPinsStanding() : null;
    }
	
	public Set<ScoreLabel> getScoreLabelsForRoll(int rollNumber) {
	    BallRoll roll = getBallRoll(rollNumber);
        return (roll != null) ? roll.getScoreLabels().getUnmodifiableScoreLabels() : Collections.emptySet();
    }
	
	public boolean addScoreLabelForRoll(int rollNumber, ScoreLabel label) {
	    BallRoll roll = getBallRoll(rollNumber);
        if (roll == null) {
            throw new IllegalStateException("Roll " + rollNumber + " has not been set yet");
        }
        return roll.getScoreLabels().addScoreLabel(label);
    }
	
    private void validateRollNumber(int rollNumber) {
        final int maxRolls = (frameNumber == 10) ? 3 : 2;
        if (rollNumber < 1 || rollNumber > maxRolls) {
            throw new IllegalArgumentException("Invalid roll index for the frame");
        }
    }
    
    @Override
    public Iterator<BallRoll> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
	// TODO Create Delegate methods for scoreLabels of each roll as needed
	
	///
    /// Calculations
    /// 
	
	private void calculatePinsKnockedDownFromStanding() {
	    int cumulativePinsKnockedDown = 0;
        for (int i = 0; i < rolls.length; i++) {
            BallRoll roll = rolls[i];
            // Assume no one entered a null ball roll here
            if (roll.pinsStanding != null) {
                int pinsKnockedDown = 10 - roll.pinsStanding.getNumberOfPinsStanding() - (cumulativePinsKnockedDown % 10);
                roll.pinsKnockedDown = pinsKnockedDown;
                cumulativePinsKnockedDown += pinsKnockedDown;
            } else {
                cumulativePinsKnockedDown += roll.pinsKnockedDown;
            }
        }
    }
	
	@Override
	public void calculateStats(boolean forceRecalculate) {
		
	    int pinsAccum = 0;
	    int rollsAccum = 0;
	    if (rolls != null && rolls.length > 0) {
	        final int maxRolls = (frameNumber == 10) ? 3 : 2;
	        for (int r = 0; r < maxRolls; r++) {
	            final BallRoll roll = rolls[r];
	            rollsAccum++;
	            
	            if (roll != null) {
	                roll.scoreLabels.clearCalculatedLabels(); // Clear any previously calculated labels

	                if (roll.scoreLabels.containsScoreLabel(ScoreLabel.FOUL)) {
	                    // This cannot be calculated, has to be set from the input
	                    continue;
	                }
	                if (null != roll.pinsStanding) {
                        // Calculate pins knocked down based on pins standing
                        int pinsKnockedDown = 10 - roll.pinsStanding.getNumberOfPinsStanding() - (pinsAccum % 10);
                        roll.pinsKnockedDown = pinsKnockedDown;
                        pinsAccum += pinsKnockedDown;
                        
                        if (roll.pinsStanding.isSplit()) {
                            roll.scoreLabels.addComputedScoreLabelIfNotPresent(ScoreLabel.SPLIT);
                        }
                    } else {
                        // Without pins standing we have to assume pins knocked down is already set correctly, so we just accumulate it
                        pinsAccum += roll.pinsKnockedDown;
                    }
	                
	                if (roll.pinsKnockedDown == 10) {
	                    roll.scoreLabels.addComputedScoreLabelIfNotPresent(ScoreLabel.STRIKE);
                    } else if (r > 0 && pinsAccum % 10 == 0) {
                        roll.scoreLabels.addComputedScoreLabelIfNotPresent(ScoreLabel.SPARE);
                    } else if (roll.pinsKnockedDown == 0) {
                        roll.scoreLabels.addComputedScoreLabelIfNotPresent(ScoreLabel.GUTTER_BALL);
                    }
	            }
	        }
	    }

	    // Cumulative score and frame score will be calculated at the game level, as they depend on previous frames and subsequent rolls for bonuses.
	    if (pinsAccum > 0) {
            rawScore = pinsAccum;
        }
	    if (rollsAccum > 0) {
            rollCount = rollsAccum;
        }
	    
	    clearCalculatedLabels(); // Clear any previously calculated labels for the frame itself
	    if (frameNumber < 10) {
            
        } else {
            // Frame 10 logic can be more complex due to the possibility of three rolls, but for now we can just check for strikes and spares
            if (rollCount >= 1 && rolls[0] != null && rolls[0].pinsKnockedDown == 10) {
                scoreLabels.addComputedScoreLabelIfNotPresent(ScoreLabel.STRIKE);
            }
            if (rollCount >= 2 && rolls[0] != null && rolls[1] != null && (rolls[0].pinsKnockedDown + rolls[1].pinsKnockedDown) == 10) {
                scoreLabels.addComputedScoreLabelIfNotPresent(ScoreLabel.SPARE);
            }
        }
	    
	    /*
	     *     public static final ScoreLabel OPEN_FRAME   = ScoreLabel.builder().component(ScoreComponent.FRAME).code("O").description("Open Frame").build();
    public static final ScoreLabel GUTTER_SPARE = ScoreLabel.builder().component(ScoreComponent.FRAME).code("G").description("Gutter Spare").build();
    public static final ScoreLabel SPLIT_PICKUP = ScoreLabel.builder().component(ScoreComponent.FRAME).code("P").description("Picked up Split").build();
	     */
	    
		// TODO Implement
		
//		// Right now we blind re-calculate everything!
//		if (rolls != null && !rolls.isEmpty()) {
//			
//			int frameScoreAccumulator = 0;
//			for (int i = 0; i < 3 && i < rolls.size(); i++) {
//				if (i > 1 && frameNumber < 10) {
//					break; // Only 2 rolls in frames 1-9
//				}
//				
//				BallRoll roll = rolls.get(i);
//				if (roll.getRollNumber() == 0) {
//					roll.setRollNumber(i + 1); // Set roll number if not already set
//				}
//				
////				if (roll.getPinsKnockedDown() == 0 && roll.getPinsStanding() != null) {
////					// Calculate pins knocked down based on pins standing
////					int pinsKnockedDown = 10 - roll.getPinsStanding().countStandingPins(); // Need to account for stuff like splits, fouls, etc. in a real implementation
////					roll.setPinsKnockedDown(pinsKnockedDown);
////				}
////				framesco
//				
//				// Compute labels
//			}
//			
//			rawScore = rolls.stream().mapToInt(BallRoll::getPinsKnockedDown).sum();
//		} else {
//			rawScore = 0;
//		}
	}
}
