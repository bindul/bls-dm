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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Unit tests for the Game class, focusing on score calculations, attributes, and labels.
 * 
 * This test class provides:
 * - Infrastructure for loading game test data from JSON files
 * - Validation of game structure and data deserialization
 * - Placeholder tests for future score calculation implementation
 * 
 * Test data is organized by level of detail:
 * - full-pin-data-game.json: Complete game with detailed pin standing information
 * - pin-counts-only-game.json: Game with only pin counts per roll
 * - game-totals-only.json: Minimal game data with only total scores
 */
public class GameScoreCalculationTest {

	private static ObjectMapper objectMapper;
	private static final String TEST_DATA_PATH = "/test-data/games/";

	/**
	 * Initialize the ObjectMapper for JSON deserialization. Called once before all tests.
	 */
	@BeforeAll
	static void setUp() {
		objectMapper = new ObjectMapper();
		
		// Register a custom deserializer for PinsStanding to handle array of pin numbers deserialization
		SimpleModule module = new SimpleModule();
		module.addDeserializer(PinsStanding.class, new JsonDeserializer<PinsStanding>() {
			@Override
			public PinsStanding deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				JsonNode node = p.getCodec().readTree(p);
				if (node.isArray()) {
					// Convert JSON array of pin numbers to int array
					int[] pinNumbers = new int[node.size()];
					for (int i = 0; i < node.size(); i++) {
						pinNumbers[i] = node.get(i).asInt();
					}
					return new PinsStanding(pinNumbers);
				}
				return null;
			}
		});
		objectMapper.registerModule(module);
		objectMapper.findAndRegisterModules();
	}

	/**
	 * Helper method to load a game from a JSON file in the test resources directory.
	 *
	 * @param filename the name of the JSON file (relative to test-data/games/)
	 * @return the deserialized Game object
	 * @throws IOException if the file cannot be read or parsed
	 */
	private Game loadGameFromJson(String filename) throws IOException {
		String resourcePath = TEST_DATA_PATH + filename;
		InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
		if (inputStream == null) {
			throw new IOException("Test resource not found: " + resourcePath);
		}
		return objectMapper.readValue(inputStream, Game.class);
	}

	/**
	 * Helper method to convert a JSON array of strings to an EnumSet of Game.GameAttribute.
	 *
	 * @param node the JSON node containing the string array
	 * @return the EnumSet of Game.GameAttribute
	 */
	private EnumSet<Game.GameAttribute> jsonArrayToAttributeSet(JsonNode node) {
		EnumSet<Game.GameAttribute> attributes = EnumSet.noneOf(Game.GameAttribute.class);
		if (node != null && node.isArray()) {
			node.forEach(item -> {
				try {
					attributes.add(Game.GameAttribute.valueOf(item.asText()));
				} catch (IllegalArgumentException e) {
					// Ignore unknown attributes
				}
			});
		}
		return attributes;
	}

	// ============================================================================
	// ACTIVE TESTS - These tests validate basic game structure and functionality
	// ============================================================================

	/**
	 * Test that a game can be loaded successfully from JSON with full pin data.
	 */
	@Test
	void testGameWithFullPinDataLoads() throws IOException {
		Game game = loadGameFromJson("full-pin-data-game.json");

		assertNotNull(game, "Game should not be null");
		assertNotNull(game.getFrames(), "Game should have frames");
		assertEquals(10, game.getFrames().size(), "Game should have exactly 10 frames");
		assertTrue(game.getScratchScore() > 0, "Scratch score should be greater than 0");
	}

	/**
	 * Test that a game can be loaded successfully from JSON with pin counts only.
	 */
	@Test
	void testGameWithPinCountsOnlyLoads() throws IOException {
		Game game = loadGameFromJson("pin-counts-only-game.json");

		assertNotNull(game, "Game should not be null");
		assertNotNull(game.getFrames(), "Game should have frames");
		assertEquals(10, game.getFrames().size(), "Game should have exactly 10 frames");
		assertTrue(game.getScratchScore() > 0, "Scratch score should be greater than 0");
	}

	/**
	 * Test that a game with only totals can be loaded successfully from JSON.
	 */
	@Test
	void testGameTotalsOnlyLoads() throws IOException {
		Game game = loadGameFromJson("game-totals-only.json");

		assertNotNull(game, "Game should not be null");
		assertTrue(game.getScratchScore() > 0, "Scratch score should be greater than 0");
	}

	/**
	 * Test that game attributes are properly parsed from JSON.
	 */
	@Test
	void testGameAttributesParsing() throws IOException {
		Game game = loadGameFromJson("pin-counts-only-game.json");

		if (game.getAttributes() != null) {
			assertTrue(!game.getAttributes().isEmpty(), "Attributes should not be empty if present");
		}
	}

	/**
	 * Test that getTotalScore() correctly returns scratch score for non-handicap games.
	 */
	@Test
	void testGetTotalScoreWithoutHandicap() throws IOException {
		Game game = loadGameFromJson("pin-counts-only-game.json");
		// Ensure this game doesn't have HANDICAP_GAME attribute
		if (game.getAttributes() != null && !game.getAttributes().contains(Game.GameAttribute.HANDICAP_GAME)) {
			assertEquals(game.getScratchScore(), game.getTotalScore(),
					"Total score should equal scratch score for non-handicap games");
		}
	}

	/**
	 * Test that getTotalScore() correctly includes handicap for handicap games.
	 */
	@Test
	void testGetTotalScoreWithHandicap() throws IOException {
		Game game = loadGameFromJson("game-totals-only.json");
		// Ensure this game has HANDICAP_GAME attribute and handicap value
		if (game.getAttributes() != null && game.getAttributes().contains(Game.GameAttribute.HANDICAP_GAME)) {
			int expectedTotal = game.getScratchScore() + game.getHandicap();
			assertEquals(expectedTotal, game.getTotalScore(),
					"Total score should include handicap for handicap games");
		}
	}

	/**
	 * Parameterized test to load all game data files and verify they deserialize correctly.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "full-pin-data-game.json", "pin-counts-only-game.json", "game-totals-only.json" })
	void testGameLoadsSuccessfully(String dataFile) throws IOException {
		Game game = loadGameFromJson(dataFile);

		assertNotNull(game, "Game should load successfully from " + dataFile);
		assertTrue(game.getScratchScore() >= 0, "Scratch score should be non-negative");
		assertTrue(game.getHandicap() >= 0, "Handicap should be non-negative");
	}

	// ============================================================================
	// COMMENTED-OUT TESTS - To be implemented as Game calculation features develop
	// ============================================================================

	/**
	 * Test scratch score calculation based on pins knocked down in all rolls.
	 * This test will validate that scratchScore is correctly calculated from individual roll data.
	 */
	// @ParameterizedTest
	// @ValueSource(strings = {"full-pin-data-game.json", "pin-counts-only-game.json"})
	// void testScratchScoreCalculation(String dataFile) throws IOException {
	// 	Game game = loadGameFromJson(dataFile);
	// 	game.calculateStats();
	//
	// 	int calculatedScore = game.getFrames().stream()
	// 		.mapToInt(Frame::getFrameScore)
	// 		.sum();
	// 	assertEquals(game.getScratchScore(), calculatedScore, 
	// 		"Scratch score should equal sum of all frame scores");
	// }

	/**
	 * Test that the scratch score is correctly calculated from individual rolls.
	 * This validates the raw scoring without strike/spare bonuses.
	 */
	// @Test
	// void testRawScoreCalculation() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (Frame frame : game.getFrames()) {
	// 		int expectedRawScore = frame.getRolls().stream()
	// 			.mapToInt(BallRoll::getPinsKnockedDown)
	// 			.sum();
	// 		assertEquals(expectedRawScore, frame.getRawScore(),
	// 			"Frame raw score should equal sum of pins knocked down in all rolls");
	// 	}
	// }

	/**
	 * Test frame score calculation with strike bonuses (10 pins on first roll + next two rolls).
	 * This validates bonus scoring for strikes.
	 */
	// @Test
	// void testFrameScoreWithStrikes() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (int i = 0; i < game.getFrames().size(); i++) {
	// 		Frame frame = game.getFrames().get(i);
	// 		if (frame.getRawScore() == 10 && frame.getRolls().size() == 1) {
	// 			// This is a strike, verify bonus calculation
	// 			if (i < 9) { // Not the 10th frame
	// 				int nextTwoRolls = getNextTwoRolls(game, i);
	// 				assertEquals(10 + nextTwoRolls, frame.getFrameScore(),
	// 					"Strike frame score should include next two rolls as bonus");
	// 			}
	// 		}
	// 	}
	// }

	/**
	 * Test frame score calculation with spare bonuses (two rolls totaling 10 pins + next roll).
	 * This validates bonus scoring for spares.
	 */
	// @Test
	// void testFrameScoreWithSpares() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (int i = 0; i < game.getFrames().size(); i++) {
	// 		Frame frame = game.getFrames().get(i);
	// 		if (frame.getRawScore() == 10 && frame.getRolls().size() == 2) {
	// 			// This is a spare, verify bonus calculation
	// 			if (i < 9) { // Not the 10th frame
	// 				int nextRoll = getNextRoll(game, i);
	// 				assertEquals(10 + nextRoll, frame.getFrameScore(),
	// 					"Spare frame score should include next roll as bonus");
	// 			}
	// 		}
	// 	}
	// }

	/**
	 * Test cumulative score calculation across all frames.
	 * This validates that cumulative scores increase monotonically and correctly.
	 */
	// @Test
	// void testCumulativeScoreCalculation() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	int previousCumulative = 0;
	// 	for (Frame frame : game.getFrames()) {
	// 		assertTrue(frame.getCumulativeScore() >= previousCumulative,
	// 			"Cumulative score should increase or stay same");
	// 		previousCumulative = frame.getCumulativeScore();
	// 	}
	//
	// 	// Final cumulative score should equal scratch score
	// 	assertEquals(game.getScratchScore(), game.getFrames().get(9).getCumulativeScore(),
	// 		"Final cumulative score should equal game scratch score");
	// }

	/**
	 * Test that a perfect game (all 12 strikes) is detected and labeled correctly.
	 * A perfect game in bowling is 12 strikes across 10 frames (with 3 rolls in the 10th frame).
	 */
	// @Test
	// void testPerfectGameDetection() throws IOException {
	// 	// Would need a perfect-game.json test file
	// 	// Game game = loadGameFromJson("perfect-game.json");
	// 	// game.calculateStats();
	// 	//
	// 	// assertEquals(300, game.getScratchScore(), "Perfect game should score exactly 300");
	// 	// assertTrue(game.getScoreLabels().contains(ScoreLabel.PERFECT_GAME),
	// 	// 	"Perfect game should have PERFECT_GAME label");
	// }

	/**
	 * Test that a clean game (no strikes or spares but no open frames) is detected correctly.
	 * Actually, a clean game in bowling means no strikes or spares (all open frames).
	 */
	// @Test
	// void testCleanGameDetection() throws IOException {
	// 	// Would need a clean-game.json test file
	// 	// Game game = loadGameFromJson("clean-game.json");
	// 	// game.calculateStats();
	// 	//
	// 	// assertTrue(game.getScoreLabels().contains(ScoreLabel.CLEAN_GAME),
	// 	// 	"Game with no spares or strikes should have CLEAN_GAME label");
	// }

	/**
	 * Test strike label detection for frames where all 10 pins are knocked down on the first roll.
	 */
	// @Test
	// void testStrikeLabeling() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (Frame frame : game.getFrames()) {
	// 		if (frame.getRolls().size() == 1 && frame.getRolls().get(0).getPinsKnockedDown() == 10
	// 			&& frame.getFrameNumber() < 10) {
	// 			assertTrue(frame.getScoreLabels().contains(ScoreLabel.STRIKE),
	// 				"Frame with strike should have STRIKE label");
	// 		}
	// 	}
	// }

	/**
	 * Test spare label detection for frames where exactly 10 pins are knocked down across two rolls.
	 */
	// @Test
	// void testSpareLabeling() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (Frame frame : game.getFrames()) {
	// 		if (frame.getRolls().size() == 2 && frame.getRawScore() == 10) {
	// 			assertTrue(frame.getScoreLabels().contains(ScoreLabel.SPARE),
	// 				"Frame with spare should have SPARE label");
	// 		}
	// 	}
	// }

	/**
	 * Test gutter ball detection for rolls where 0 pins are knocked down.
	 */
	// @Test
	// void testGutterBallLabeling() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (Frame frame : game.getFrames()) {
	// 		for (BallRoll roll : frame.getRolls()) {
	// 			if (roll.getPinsKnockedDown() == 0) {
	// 				assertTrue(roll.getScoreLabels().contains(ScoreLabel.GUTTER),
	// 					"Roll with 0 pins knocked down should have GUTTER label");
	// 			}
	// 		}
	// 	}
	// }

	/**
	 * Test split detection for rolls with specific pin patterns still standing.
	 * A split is when the head pin is down but non-adjacent pins remain standing.
	 */
	// @Test
	// void testSplitDetection() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (Frame frame : game.getFrames()) {
	// 		for (BallRoll roll : frame.getRolls()) {
	// 			if (roll.getPinsStanding() != null && isSplit(roll.getPinsStanding())) {
	// 				assertTrue(roll.getScoreLabels().contains(ScoreLabel.SPLIT),
	// 					"Roll with split pin pattern should have SPLIT label");
	// 			}
	// 		}
	// 	}
	// }

	/**
	 * Test open frame detection for frames where less than 10 pins total are knocked down
	 * across all rolls (without strikes or spares).
	 */
	// @Test
	// void testOpenFrameLabeling() throws IOException {
	// 	Game game = loadGameFromJson("full-pin-data-game.json");
	// 	game.calculateStats();
	//
	// 	for (Frame frame : game.getFrames()) {
	// 		if (frame.getRawScore() < 10 && frame.getFrameNumber() < 10) {
	// 			assertTrue(frame.getScoreLabels().contains(ScoreLabel.OPEN),
	// 				"Frame with less than 10 pins should have OPEN label");
	// 		}
	// 	}
	// }

	/**
	 * Test series game attribute assignment.
	 */
	// @Test
	// void testSeriesGameAttribute() throws IOException {
	// 	Game game = loadGameFromJson("pin-counts-only-game.json");
	//
	// 	if (game.getAttributes() != null && game.getAttributes().contains(Game.GameAttribute.SERIES_GAME)) {
	// 		assertTrue(true, "Game is marked as part of a series");
	// 	}
	// }

	/**
	 * Test handicap game attribute assignment.
	 */
	// @Test
	// void testHandicapGameAttribute() throws IOException {
	// 	Game game = loadGameFromJson("game-totals-only.json");
	//
	// 	if (game.getAttributes() != null && game.getAttributes().contains(Game.GameAttribute.HANDICAP_GAME)) {
	// 		assertTrue(game.getHandicap() > 0, "Handicap game should have positive handicap value");
	// 	}
	// }

	// ============================================================================
	// HELPER METHODS (to be used once calculation tests are uncommented)
	// ============================================================================

	/**
	 * Helper to get the next roll after a given frame.
	 * Used for strike/spare bonus calculations.
	 */
	// private int getNextRoll(Game game, int frameIndex) {
	// 	if (frameIndex + 1 < game.getFrames().size()) {
	// 		Frame nextFrame = game.getFrames().get(frameIndex + 1);
	// 		return nextFrame.getRolls().get(0).getPinsKnockedDown();
	// 	}
	// 	return 0;
	// }

	/**
	 * Helper to get the next two rolls after a given frame.
	 * Used for strike bonus calculations.
	 */
	// private int getNextTwoRolls(Game game, int frameIndex) {
	// 	int total = 0;
	// 	if (frameIndex + 1 < game.getFrames().size()) {
	// 		Frame nextFrame = game.getFrames().get(frameIndex + 1);
	// 		total += nextFrame.getRolls().get(0).getPinsKnockedDown();
	// 		if (nextFrame.getRolls().size() > 1) {
	// 			total += nextFrame.getRolls().get(1).getPinsKnockedDown();
	// 		} else if (frameIndex + 2 < game.getFrames().size()) {
	// 			total += game.getFrames().get(frameIndex + 2).getRolls().get(0).getPinsKnockedDown();
	// 		}
	// 	}
	// 	return total;
	// }

	/**
	 * Helper to detect if a pin standing represents a split.
	 * A split has the head pin (pin 1) down but non-adjacent pins standing.
	 */
	// private boolean isSplit(PinsStanding pinsStanding) {
	// 	if (!pinsStanding.isPinStanding(1)) { // Head pin is down
	// 		// Check for non-adjacent pins standing - simplified check
	// 		int standingCount = pinsStanding.getNumberOfPinsStanding();
	// 		return standingCount > 0 && standingCount <= 8; // Not a gutter, not a fill
	// 	}
	// 	return false;
	// }
}
