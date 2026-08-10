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

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.extern.log4j.Log4j2;

/**
 * Represents a label attached to ball rolls or frames or games
 */
@Log4j2
@Value @Builder(toBuilder = true) @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScoreLabel {
	
	public static final ScoreLabel STRIKE       = ScoreLabel.builder().component(ScoreComponent.BALL_ROLL).code("X").description("Strike").build();
	public static final ScoreLabel SPARE        = ScoreLabel.builder().component(ScoreComponent.BALL_ROLL).code("/").description("Spare").build();
	public static final ScoreLabel FOUL         = ScoreLabel.builder().component(ScoreComponent.BALL_ROLL).code("F").description("Foul").build();
	public static final ScoreLabel SPLIT        = ScoreLabel.builder().component(ScoreComponent.BALL_ROLL).code("S").description("Split").build();
	public static final ScoreLabel GUTTER_BALL  = ScoreLabel.builder().component(ScoreComponent.BALL_ROLL).code("-").description("Gutter Ball / No Pins").build();
	
	public static final ScoreLabel OPEN_FRAME   = ScoreLabel.builder().component(ScoreComponent.FRAME).code("O").description("Open Frame").build();
	public static final ScoreLabel GUTTER_SPARE = ScoreLabel.builder().component(ScoreComponent.FRAME).code("G").description("Gutter Spare").build();
	public static final ScoreLabel SPLIT_PICKUP = ScoreLabel.builder().component(ScoreComponent.FRAME).code("P").description("Picked up Split").build();
	public static final ScoreLabel TURKEY       = ScoreLabel.builder().component(ScoreComponent.FRAME).code("3").description("Turkey").build();
	
	public static final ScoreLabel CLEAN_GAME 	= ScoreLabel.builder().component(ScoreComponent.GAME).code("C").description("Clean Game").build();
	public static final ScoreLabel PERFECT_GAME = ScoreLabel.builder().component(ScoreComponent.GAME).code("*").description("Perfect Game").build();
	public static final ScoreLabel GAME_200 	= ScoreLabel.builder().component(ScoreComponent.GAME).code("2").description("200+ Game").build();
	
	public static final ScoreLabel SERIES_600 	= ScoreLabel.builder().component(ScoreComponent.SERIES).code("6").description("600+ Series (3 Games)").build();
	public static final ScoreLabel SERIES_800 	= ScoreLabel.builder().component(ScoreComponent.SERIES).code("8").description("800+ Series (3 Games)").build();

	private static final String PARSE_REGEX = "^([BFGS]):([\\p{Print}])(_C:([YNyn]))?$";
	
	@NonNull @EqualsAndHashCode.Include
	private final ScoreComponent component;
	/**
	 * The accepted label code, note this is not validated today but is expected to be a single character
	 */
	@NonNull @EqualsAndHashCode.Include
	private final String code;
	/**
     * Indicates if the label was computed by the BLS engine or entered from external score sheets.
     */
    @With
    private final boolean computed;
	private final String description;
	
	public String serialize () {
		final StringBuilder formatted = new StringBuilder();
		formatted.append(component.code()).append(':').append(code);
		if (computed) {
			formatted.append('_').append("C:Y");
		}
		return formatted.toString();
	}
	
	public static ScoreLabel parse (String serializedLabel) throws ParseException {
	    final Pattern pattern = Pattern.compile(PARSE_REGEX);
	    final Matcher matcher = pattern.matcher(serializedLabel);
	    if (matcher.matches()) {
	        final ScoreLabelBuilder builder = ScoreLabel.builder();
	        builder.component(ScoreComponent.fromLabel(matcher.group(1)))
	            .code(matcher.group(2));
	        if (matcher.groupCount() >= 4) {
	            builder.computed("Y".equalsIgnoreCase(matcher.group(4)));
	        }
	        return builder.build();
	    }
	    log.info("Input string {} did not parse with the serialized score label format", serializedLabel);
		throw new ParseException("Serialized value [" + serializedLabel + "] could not be parsed into a ScoreLabel", matcher.end());
	}
}
