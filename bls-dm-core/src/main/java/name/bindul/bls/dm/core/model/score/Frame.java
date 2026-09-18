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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Frame implements Iterable<BallRoll> {
    
    private int frameNumber; // Frame number (1-10)
    
    @Setter(value = AccessLevel.PRIVATE)
    private int ballRollCount; // Number of ball rolls in this frame
    @Setter(value = AccessLevel.PRIVATE) @Getter(value = AccessLevel.PRIVATE)
    private BallRoll[] ballRolls = new BallRoll[3]; // Array to hold the ball rolls for this frame
    
    private int framePinfall; // Pins dropped in this frame
    private int frameScore; // Score for this frame (including bonuses)
    private int cumulativeScore; // Cumulative score up to this frame

    // TODO Add labels
    
    public void addBallRoll(BallRoll ballRoll) {
        if ((frameNumber < 10 && ballRollCount < 2) || (frameNumber == 10 && ballRollCount < 3)) {
            ballRolls[ballRollCount++] = ballRoll;
        } else {
            throw new IllegalStateException("Cannot add more than 3 ball rolls to a frame.");
        }
    }
    
    public BallRoll getBallRoll(int index) {
        if (index < 0 || index >= ballRollCount) {
            throw new IndexOutOfBoundsException("Invalid ball roll index: " + index);
        }
        return ballRolls[index];
    }

    @Override
    public Iterator<BallRoll> iterator() {
        return new Iterator<BallRoll>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < ballRollCount;
            }

            @Override
            public BallRoll next() {
                return ballRolls[index++];
            }
        };
    }
}
