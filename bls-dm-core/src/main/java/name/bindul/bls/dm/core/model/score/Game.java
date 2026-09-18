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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import name.bindul.bls.dm.core.model.BlsEntity;

@Data
public class Game implements BlsEntity, Iterable<Frame> {

    private String playerId; // Unique identifier for the player
    private String gameId; // Unique identifier for the game

    @Setter(value = AccessLevel.PRIVATE)
    private List<Frame> frames = new ArrayList<>(10);
    private int scratchScore; // Total score without handicaps
    
    private boolean isHandicapped; // Indicates if the game is a handicap game
    private int handicap; // Handicap value for the game
    private int handicappedScore; // Total score with handicaps
    
    private boolean isSeriesGame; // Indicates if the game is part of a series
    private int seriesGameNumber; // The game number in the series
    
    // TODO Add labels
    
    public void addFrame(Frame frame) {
        if (frames.size() < 10) {
            frame.setFrameNumber(frames.size() + 1); // Set the frame number based on the current size
            frames.add(frame);
        } else {
            throw new IllegalStateException("Cannot add more than 10 frames to a game.");
        }
    }
    
    public List<Frame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    @Override
    public Iterator<Frame> iterator() {
        return CollectionUtils.emptyIfNull(frames).iterator();
    }
    
    public void calculateGameScoreAndLabels () {
        
        // Setup the ball scores and labels for each frame
        setupBallScoresAndLabels();
        
        int scoreAccumulator = 0;
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            if (null != frame) {
            
                // Calculate the frame score including bonuses for strikes and spares
                int frameScore = frame.getFramePinfall();
                // TODO Add logic to calculate bonuses for strikes and spares based on subsequent frames
                frame.setFrameScore(frameScore);
                
                scoreAccumulator += frame.getFrameScore();
                frame.setCumulativeScore(scoreAccumulator);
            }
        }
        
        this.scratchScore = scoreAccumulator;
        if (isHandicapped) {
            this.handicappedScore = scratchScore + handicap;
        }
    }

    private void setupBallScoresAndLabels() {
        for (Frame frame : frames) {
            int frameScoreAccumulator = 0;
            for (int b = 0; b < frame.getBallRollCount(); b++) { // We assume frame validates the max ball rolls in the frame 
                BallRoll ballRoll = frame.getBallRoll(b);
                if (null != ballRoll.getPinsStanding()) {
                    int pinsStanding = ballRoll.getPinsStanding().getNumberOfPinsStanding();
                    ballRoll.setPinsKnockedDown(10 - pinsStanding - (frameScoreAccumulator % 10));
                }
                // TODO Set ball roll score labels (STRIKE, SPARE, etc.) based on the pins knocked down and the frame state
                // TODO Conditional exit if the frame is a strike or a spare OR it is an open frame and the second ball roll is completed
                frameScoreAccumulator += ballRoll.getPinsKnockedDown();
            }
            frame.setFramePinfall(frameScoreAccumulator);
            // TODO Set frame score labels (STRIKE, SPARE, etc.) based on the frame score and the ball rolls
        }
    }
}
