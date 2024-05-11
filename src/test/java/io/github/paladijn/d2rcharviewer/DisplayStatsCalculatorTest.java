/*
   Copyright 2024 Paladijn (paladijn2960+d2rsavegameparser@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package io.github.paladijn.d2rcharviewer;

import io.github.paladijn.d2rcharviewer.calculator.BreakpointCalculator;
import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rsavegameparser.model.CharacterType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayStatsCalculatorTest {

    private DisplayStatsCalculator cut = new DisplayStatsCalculator(new BreakpointCalculator());

    @Test
    void simpleChar() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Dierentuin.d2s"));

        assertEquals("Dierentuin", result.name());
        assertEquals(CharacterType.NECROMANCER, result.type());
        assertEquals(16, result.level());
        assertEquals(30, result.fasterRunWalk());
        assertEquals(42, result.attributes().strength());
        assertEquals(25, result.attributes().dexterity());
        assertEquals(70, result.attributes().vitality());
        assertEquals(25, result.attributes().energy());
        assertEquals(28, result.resistances().fire());
        assertEquals(31, result.resistances().lightning());
        assertEquals(53, result.resistances().cold());
        assertEquals(10, result.resistances().poison());
        assertEquals(45, result.mf());
        assertEquals(0, result.gf());
        assertEquals("16", result.gold());
        assertEquals("5K", result.goldInStash());
        assertEquals("Nef, Eth, Ith (2), Tal (3), Ral", result.runes());
    }

    @Test
    void newChar() {
        final  DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar.d2s"));

        assertEquals("Wandelaar", result.name());
        assertEquals(CharacterType.PALADIN, result.type());
        assertEquals(7, result.breakpoints().nextFHR());
        assertEquals(9, result.breakpoints().nextFCR());
    }

    @Test
    void goldenStatueBroken() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-stat.d2s"));

        assertEquals(CharacterType.PALADIN, result.type());
        // this used to throw an exception as j34 has an extra byte (8 bits) in the item list
    }

    @Test
    @Disabled("need to overwrite removeDuplicateRuneword for this specific test")
    void removeRunewordAlreadyMade() throws NoSuchFieldException, IllegalAccessException {
        final DisplayStatsCalculator calculatorWithoutDuplicates = new DisplayStatsCalculator(new BreakpointCalculator());
        calculatorWithoutDuplicates.getClass().getDeclaredField("calculatorWithoutDuplicates").set("removeDuplicateRuneword", true);
        final DisplayStats result = calculatorWithoutDuplicates.getDisplayStats(Path.of("src/test/resources/2.5/Fierljepper.d2s"));

        // Stealth is already made, so should be skipped
        assertEquals("Nef (3), Eth, Ith, Tal (2), Ral", result.runes());
        assertEquals("", result.runewords());
    }

    @Test
    void calculateNightmareResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-nm.d2s"));

        assertEquals(34, result.resistances().fire());
        assertEquals(55, result.resistances().lightning());
        assertEquals(17, result.resistances().cold());
        assertEquals(60, result.resistances().poison());
    }

    @Test
    void calculateAnyaResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-anya.d2s"));

        assertEquals(44, result.resistances().fire());
        assertEquals(65, result.resistances().lightning());
        assertEquals(27, result.resistances().cold());
        assertEquals(70, result.resistances().poison());
    }

    @Test
    void calculateAddedMaxResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.8/Sparkles-above75percent.d2s"));

        assertEquals(82, result.resistances().fire());
        assertEquals(8, result.resistances().lightning());
        assertEquals(20, result.resistances().cold());
        assertEquals(45, result.resistances().poison());
    }
}