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
import name.bindul.bls.dm.core.model.BlsEntity;
import name.bindul.bls.dm.core.model.BlsStatsHolder;

/**
 * Represents a series of bowling games, which can be part of a league, tournament, or practice session. 
 * A series consists of multiple games played by a player.
 */
@Data
public class Series implements BlsEntity, BlsStatsHolder, Iterable<Game> {
	
	public enum SeriesType {
		REGULAR, // Regular series
		LEAGUE, // League series
		TOURNAMENT, // Tournament series
		PRACTICE // Practice series
	}
	
	private SeriesType type; // Type of the series
	private String playerId;
	private List<Game> games; // List of games in the series
	@Delegate @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE)
    private ScoreLabelColl scoreLabels = new ScoreLabelColl(ScoreComponent.SERIES);
	
    @Override
    public Iterator<Game> iterator() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void calculateStats(boolean forceRecalculate) {
        // TODO Auto-generated method stub    
    }

}
