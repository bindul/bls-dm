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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Represents the pins in a bowling game. This class can be used to track the state of the pins, such as which pins are
 * standing and which have been knocked down.
 * 
 * Pins are numbered 1-10 according to standard American bowling:
 * <pre>
 * 7   8   9  10
 *   4   5   6
 *     2   3
 *       1
 * </pre>
 */
public class PinsStanding {
	
	/**
	 * Adjacency list defining which physical pins touch each other.
	 * See the diagram above for pin numbering. See documentation of #isSplit() for the definition of a split.
	 * This reachability graph only looks at the adjacent pins looking back from the front of the lane, not backwards. 
	 * For example, 8 is reachable from 4, but 4 is not reachable from 8. This is done to cover the second rule of a 
	 * split, which states that there must be at least one knocked down pin between two standing pins.
	 * 
     * [Index - 1] matches the pin number (1-10).
     */
    private static final int[][] USBC_SPLIT_PIN_ADJACENCY = {
        {2, 3},              // Pin 1 touches 2, 3
        {4, 5},        // Pin 2 touches 1, 3, 4, 5
        {5, 6},        // Pin 3 touches 1, 2, 5, 6
        {7, 8},        // Pin 4 touches 2, 5, 7, 8
        {8, 9},  // Pin 5 touches 2, 3, 4, 6, 8, 9
        {9, 10},       // Pin 6 touches 3, 5, 9, 10
        {},              // Pin 7 touches 4, 8
        {},        // Pin 8 touches 4, 5, 7, 9
        {},       // Pin 9 touches 5, 6, 8, 10
        {}               // Pin 10 touches 6, 9
    };
	
	/**
	 * Each element represents a pin: true for standing, false for knocked down.
	 * Index 0 corresponds to pin 1, index 1 to pin 2, etc.
	 */
	private final boolean[] pinStates = new boolean[10]; // 10 pins in a standard bowling game

	public PinsStanding () {
		resetPins (); // initialize all pins to standing by default
	}

	/**
	 * Constructor that initializes pin states from an array of pin numbers currently standing.
	 * This constructor is useful for deserialization from data.
	 * 
	 * Example: new PinsStanding(new int[]{1, 3, 5}) means pins 1, 3, and 5 are standing.
	 *
	 * @param standingPinNumbers an array of pin numbers (1-10) that are still standing
	 * @throws IllegalArgumentException if any pin number is not between 1 and 10
	 */
	public PinsStanding (int[] standingPinNumbers) {
		setStandingPins (standingPinNumbers);
	}

	/**
	 * Knocks down a specific pin.
	 *
	 * @param pinIndex the index of the pin (1-10)
	 * @throws IllegalArgumentException if the pin index is not between 1 and 10
	 */
	public void knockDownPin (int pinIndex) {
		validatePinIndex (pinIndex);
		pinStates[pinIndex - 1] = false; // Knock down the pin
	}

	/**
	 * Sets the state of a specific pin.
	 *
	 * @param pinIndex the index of the pin (1-10)
	 * @param isStanding true if the pin is standing, false if it is knocked down
	 * @throws IllegalArgumentException if the pin index is not between 1 and 10
	 */
	public void setPinState (int pinIndex, boolean isStanding) {
		validatePinIndex (pinIndex);
		pinStates[pinIndex - 1] = isStanding;
	}

	/**
	 * Resets all pins to standing.
	 */
	public void resetPins () {
		resetPins0 (true); // Set all pins to standing
	}
	
	private void resetPins0 (boolean standing) {
		Arrays.fill (pinStates, standing);
	}

	/**
	 * Checks if a specific pin is standing.
	 *
	 * @param pinIndex the index of the pin (1-10)
	 * @return true if the pin is standing, false if it is knocked down
	 * @throws IllegalArgumentException if the pin index is not between 1 and 10
	 */
	public boolean isPinStanding(int pinIndex) {
		validatePinIndex(pinIndex);
		return pinStates[pinIndex - 1];
	}

	/**
	 * Returns a defensive copy of the pin states where index 0 corresponds to pin 1.
	 */
	public boolean[] getPinStates() {
		return pinStates.clone();
	}

	/**
	 * Returns an array of pin numbers that are currently standing.
	 * Useful for JSON serialization and debugging.
	 * 
	 * Example: If pins 1, 3, 5 are standing, returns [1, 3, 5]
	 *
	 * @return an array of pin numbers (1-10) that are standing
	 */
	public int[] getStandingPinNumbers() {
		int[] standing = new int[getNumberOfPinsStanding()];
		int index = 0;
		for (int i = 0; i < pinStates.length; i++) {
			if (pinStates[i]) {
				standing[index++] = i + 1; // Pin numbers are 1-indexed
			}
		}
		return standing;
	}
	
	/**
	 * Sets the pin states based on an array of pin numbers that are currently standing.
	 * This method is useful for deserialization from data.
	 * 
	 * Example: setStandingPins(new int[]{1, 3, 5}) means pins 1, 3, and 5 are standing, and all others are knocked down.
	 *
	 * @param standingPinNumbers an array of pin numbers (1-10) that should be set to standing
	 * @throws IllegalArgumentException if any pin number is not between 1 and 10
	 */
	public void setStandingPins(int[] standingPinNumbers) {
		if (standingPinNumbers != null) {
			// Knock down all pins except those in the standingPinNumbers array
			for (int pinNum : standingPinNumbers) {
				if (pinNum < 1 || pinNum > 10) {
					throw new IllegalArgumentException("Pin number must be between 1 and 10, got: " + pinNum);
				}
			}
			// We do it in two passes to avoid overwriting the value if there are validation errors
			resetPins0(false); // Start with all pins knocked down
			for (int pinNum : standingPinNumbers) {
				pinStates[pinNum - 1] = true; // Set specified pins to standing
			}
		}
	}
	
	/**
	 * Sets the pin states based on an array of pin numbers that are knocked down.
	 * This method is useful for deserialization from data.
	 * 
	 * Example: setKnockedDownPins(new int[]{2, 4, 6}) means pins 2, 4, and 6 are knocked down, and all others are standing.
	 *
	 * @param knockedDownPinNumbers an array of pin numbers (1-10) that should be set to knocked down
	 * @throws IllegalArgumentException if any pin number is not between 1 and 10
	 */
	public void setKnockedDownPins(int[] knockedDownPinNumbers) {
		if (knockedDownPinNumbers != null) {
			resetPins0(true); // Start with all pins standing
			// Knock down specified pins
			for (int pinNum : knockedDownPinNumbers) {
				if (pinNum < 1 || pinNum > 10) {
					throw new IllegalArgumentException("Pin number must be between 1 and 10, got: " + pinNum);
				}
				pinStates[pinNum - 1] = false; // Set specified pins to knocked down
			}
		}
	}

	/**
	 * Returns the number of pins currently standing.
	 *
	 * @return the count of pins that are standing (true values in pinStates)
	 */
	public int getNumberOfPinsStanding() {
		int count = 0;
		for (boolean pinState : pinStates) {
			if (pinState) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Determines if the current pin configuration is a split.
	 * A split occurs when there are at least two standing pins with at least one knocked down pin between them.
	 * 
	 * <p>USBC definition of a split:
	 * A split is a setup of pins left standing after the first delivery, provided the head pin is down and at least one
	 * pin is down:
	 * <ul><li>Between two or more standing pins; e.g., 7-9 or 3-10.</li>
	 * <li>Immediately ahead of two or more standing pins; e.g., 5-6.</li></ul>
	 *
	 * @return true if the current pin configuration is a split, false otherwise
	 */
	public boolean isSplit() {
		// Rule 1: The headpin (Pin 1) must be knocked down
		// Rule 2: At least two pins must remain standing
		if (pinStates[0] || getNumberOfPinsStanding() < 2) {
			return false;
		}

		final Set<Integer> visited = new HashSet<>();
		int standingPinGroupCount = 0;
		
		final List<Integer> standingPins = Arrays.stream(getStandingPinNumbers()).boxed().toList();
		for (int pin : standingPins) {
			if (!visited.contains(pin)) {
				// Start a new group of connected standing pins
				standingPinGroupCount++;
				bfsTraverseGroup(pin, standingPins, visited);
			}
		}
		
		// Rule 3: Pins must be divided into 2 or more distinct groups
		return standingPinGroupCount >= 2;
	}

	/**
	 * Traverse the group of pins connected to the starting pin using BFS and mark them as visited.
	 */
	private void bfsTraverseGroup(int startPin, List<Integer> standingPins, Set<Integer> visited) {
		final Queue<Integer> queue = new LinkedList<>();
		
		visited.add(startPin);
		queue.add(startPin);
		
		while (!queue.isEmpty()) {
			int currentPin = queue.poll();
			for (int neighbor : USBC_SPLIT_PIN_ADJACENCY[currentPin - 1]) {
				if (standingPins.contains(neighbor) && !visited.contains(neighbor)) {
					visited.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
	}

	private void validatePinIndex(int pinIndex) {
		if (pinIndex < 1 || pinIndex > pinStates.length) {
			throw new IllegalArgumentException("Pin index must be between 1 and " + pinStates.length);
		}
	}
}