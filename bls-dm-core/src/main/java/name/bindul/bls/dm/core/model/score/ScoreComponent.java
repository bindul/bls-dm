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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a component of a score in a bowling game, such as a ball roll, frame, game, or series.
 * This enum can be used to categorize and manage different aspects of scoring in the application.
 */
public enum ScoreComponent {
	
	BALL_ROLL("B"),
	FRAME("F"),
	GAME("G"),
	SERIES("S");

	private final String code;
	
	private static final Map<String, ScoreComponent> CODE_MAP = new HashMap<>();
	
	static {
		for (ScoreComponent scoreComponent : values()) {
			CODE_MAP.put(scoreComponent.code, scoreComponent);
		}
	}
	
	private ScoreComponent(String code) {
		this.code = code;
	}
	
	public String code() {
		return this.code;
	}
	
	/**
	 * Retrieves the ScoreComponent enum constant for the given code.
	 *
	 * @param code the code to look up
	 * @return the ScoreComponent corresponding to the code, or null if not found
	 */
	public static ScoreComponent fromLabel(String code) {
		return CODE_MAP.get(code);
	}
}
