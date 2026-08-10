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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class PinsStandingTest {

	@Test
	void testPinsStanding() {
		PinsStanding ps = new PinsStanding();
		// default constructor should set all pins to standing
		assertEquals(10, ps.getNumberOfPinsStanding());
		assertArrayEquals(new int[] {1,2,3,4,5,6,7,8,9,10}, ps.getStandingPinNumbers());
		// head pin should be standing by default
		assertTrue(ps.isPinStanding(1));
	}

	@Test
	void testPinsStandingIntArray() {
		// valid array sets only those pins standing
		PinsStanding ps = new PinsStanding(new int[] {1,3,5});
		assertEquals(3, ps.getNumberOfPinsStanding());
		assertArrayEquals(new int[] {1,3,5}, ps.getStandingPinNumbers());

		// invalid pin numbers should throw
		assertThrows(IllegalArgumentException.class, () -> new PinsStanding(new int[] {0}));
		assertThrows(IllegalArgumentException.class, () -> new PinsStanding(new int[] {11}));
	}

	@Test
	void testKnockDownPin() {
		PinsStanding ps = new PinsStanding();
		ps.knockDownPin(1);
		assertFalse(ps.isPinStanding(1));
		assertEquals(9, ps.getNumberOfPinsStanding());

		// invalid indices
		assertThrows(IllegalArgumentException.class, () -> ps.knockDownPin(0));
		assertThrows(IllegalArgumentException.class, () -> ps.knockDownPin(12));
	}

	@Test
	void testSetPinState() {
		PinsStanding ps = new PinsStanding();
		ps.setPinState(1, false);
		assertFalse(ps.isPinStanding(1));
		ps.setPinState(1, true);
		assertTrue(ps.isPinStanding(1));

		assertThrows(IllegalArgumentException.class, () -> ps.setPinState(0, true));
		assertThrows(IllegalArgumentException.class, () -> ps.setPinState(11, false));
	}

	@Test
	void testResetPins() {
		PinsStanding ps = new PinsStanding(new int[] {2,4});
		// start with only 2 and 4 standing
		assertEquals(2, ps.getNumberOfPinsStanding());
		ps.resetPins();
		assertEquals(10, ps.getNumberOfPinsStanding());
		for (int i = 1; i <= 10; i++) {
			assertTrue(ps.isPinStanding(i));
		}
	}

	@Test
	void testIsPinStanding() {
		PinsStanding ps = new PinsStanding();
		assertTrue(ps.isPinStanding(5));
		ps.knockDownPin(5);
		assertFalse(ps.isPinStanding(5));

		assertThrows(IllegalArgumentException.class, () -> ps.isPinStanding(0));
		assertThrows(IllegalArgumentException.class, () -> ps.isPinStanding(20));
	}

	@Test
	void testGetPinStates() {
		PinsStanding ps = new PinsStanding();
		boolean[] states = ps.getPinStates();
		assertEquals(10, states.length);
		// defensive copy: mutating returned array should not change internal state
		states[0] = false;
		assertTrue(ps.isPinStanding(1));
	}

	@Test
	void testGetStandingPinNumbers() {
		PinsStanding ps = new PinsStanding(new int[] {2,4,6});
		assertArrayEquals(new int[] {2,4,6}, ps.getStandingPinNumbers());

		// no pins standing
		ps.setStandingPins(new int[] {});
		assertEquals(0, ps.getNumberOfPinsStanding());
		assertArrayEquals(new int[] {}, ps.getStandingPinNumbers());
	}

	@Test
	void testSetStandingPins() {
		PinsStanding ps = new PinsStanding();
		ps.setStandingPins(new int[] {3,7});
		assertEquals(2, ps.getNumberOfPinsStanding());
		assertArrayEquals(new int[] {3,7}, ps.getStandingPinNumbers());

		// invalid pins
		assertThrows(IllegalArgumentException.class, () -> ps.setStandingPins(new int[] {0}));
		assertThrows(IllegalArgumentException.class, () -> ps.setStandingPins(new int[] {12}));
		
		// null array should not change anything
		int[] currentStanding = ps.getStandingPinNumbers();
		ps.setStandingPins(null);
		assertArrayEquals(currentStanding, ps.getStandingPinNumbers());
	}
	
	@Test
	void testSetKnockedDownPins() {
		PinsStanding ps = new PinsStanding();
		ps.setKnockedDownPins(new int[] {1, 2, 3});
		assertEquals(7, ps.getNumberOfPinsStanding());
		assertArrayEquals(new int[] {4,5,6,7,8,9,10}, ps.getStandingPinNumbers());

		// invalid pins
		assertThrows(IllegalArgumentException.class, () -> ps.setKnockedDownPins(new int[] {0}));
		assertThrows(IllegalArgumentException.class, () -> ps.setKnockedDownPins(new int[] {12}));
		
		// null array should not change anything
		int[] currentStanding = ps.getStandingPinNumbers();
		ps.setKnockedDownPins(null);
		assertArrayEquals(currentStanding, ps.getStandingPinNumbers());
	}

	@Test
	void testGetNumberOfPinsStanding() {
		PinsStanding ps = new PinsStanding();
		assertEquals(10, ps.getNumberOfPinsStanding());
		ps.knockDownPin(1);
		ps.knockDownPin(2);
		assertEquals(8, ps.getNumberOfPinsStanding());
		ps.setStandingPins(new int[] {});
		assertEquals(0, ps.getNumberOfPinsStanding());
	}

	// Non split pins - https://en.wikipedia.org/wiki/Split_(bowling)
	static int[][] nonSplitPins = {
		{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, // All pins standing
		{}, // No pins standing
		{5}, // Single pin standing
		{1, 2, 3, 4, 5, 6, 7, 8, 9}, // All but one pin standing
		{1, 2, 3, 4, 5, 6, 7, 8}, // All but two pins standing
		{1, 2, 3, 4, 5}, // All but five pins standing
		{6, 10}, // Two adjacent pins standing
		{1}, // Head pin only
		{1, 2, 4, 10}, // Washouts
		{1, 2, 8, 10},
		{1, 3, 6, 7},
		{1, 2, 10},
		{1, 3, 7},
		{1, 3, 7, 9},
		{1, 2, 4, 6, 10},
		{1, 3, 4, 6, 7},
		{1, 4},
		{1, 6}
	};
	
	@ParameterizedTest
	@FieldSource("nonSplitPins")
	void testIsSplit_not_split(int[] standingPins) {
		assertFalse(new PinsStanding(standingPins).isSplit(), () -> "Pins " + java.util.Arrays.toString(standingPins) + " should not be a split");
	}
	
	// Split pins - https://en.wikipedia.org/wiki/Split_(bowling)
	static int[][] splitPins = {
		{7, 10}, // Goal posts, Bedposts, Snake eyes
		{7, 9}, {8, 10}, // Cincinnati
		{4, 6}, {4, 6, 7}, {4, 6, 10}, // Cincinnati similar
		{5, 7}, {5, 10}, // Woolworth, Kresge, Dime store
		{2, 6}, {3, 4}, {4, 9}, {6, 8}, // Wollworth, Kresge, Dime similar
		{5, 7, 10}, // Sour apple, Lilly, Full Murray
		{3, 7}, {2, 10}, // 3-7, 2-10
		{2, 7}, {3, 10}, // Baby split, Murphy
		{2, 9}, {3, 8}, // Baby split, Murphy similar
		{2, 7, 10}, {3, 7, 10}, // Cocked hat, Christmas tree
		{4, 7, 10}, {6, 7, 10},
		{4, 10},
		{4, 9}, {6, 8},
		{2, 6}, {3, 4}, {5, 7}, {5, 10},
		{4, 6, 7, 10}, // Big four
		{2, 3}, {4, 5}, {5, 6}, {7, 8}, {8, 9}, {9, 10}, // Fit splits, Steam fitter (for the 4–5)
		{4, 5, 7}, {5, 6, 10},
		{2, 3, 4}, {2, 3, 6}, {4, 5, 8}, {5, 6, 8},
		{4, 6, 7, 8, 10}, {4, 6, 7, 9, 10}, // Greek church
		{4, 6, 8, 10}, {4, 6, 7 ,9}, // Greek church similar
		{3 ,4 ,6 ,7 ,10}, {2 ,4 ,6 ,7 ,10}, // Big five
		};
		
	@ParameterizedTest
	@FieldSource("splitPins")
	void testIsSplit_split(int[] standingPins) {
		assertTrue(new PinsStanding(standingPins).isSplit(), () -> "Pins " + java.util.Arrays.toString(standingPins) + " should be a split");		
	}
}
