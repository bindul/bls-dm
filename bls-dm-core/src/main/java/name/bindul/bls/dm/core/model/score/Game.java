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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import name.bindul.bls.dm.core.model.BlsEntity;
import name.bindul.bls.dm.core.model.BlsStatsHolder;

/**
 * Represents a single bowling game.
 */
@Data
public class Game implements BlsEntity, BlsStatsHolder, Iterable<Frame> {
	
	public enum GameAttribute {
		SERIES_GAME, // Game is part of a series
		HANDICAP_GAME, // Game is a handicap game
	}

	 //TODO Lane conditions for the game, e.g., house shot, sport shot, etc.
	
	private EnumSet<GameAttribute> attributes; // Type of the game
	private List<Frame> frames; // List of 10 frames in the game
	private int scratchScore; // Total score without handicaps
	@Delegate @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE)
    private ScoreLabelColl scoreLabels = new ScoreLabelColl(ScoreComponent.GAME);
	
	// Only applicable for handicap games
	private int handicap; // Handicap value for the game
	
	public int getTotalScore() {
		if (attributes != null && attributes.contains(GameAttribute.HANDICAP_GAME)) {
			return scratchScore + handicap;
		}
		return scratchScore;
	}
	
    @Override
    public Iterator<Frame> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void calculateStats(boolean forceRecalculate) {
        // TODO Auto-generated method stub
        
    }
}
