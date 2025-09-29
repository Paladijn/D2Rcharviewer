/*
   Copyright 2024-2025 Paladijn (paladijn2960+d2rsavegameparser@gmail.com)

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
package io.github.paladijn.d2rcharviewer.calculator;

import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rsavegameparser.model.CharacterType;
import io.github.paladijn.d2rsavegameparser.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisplayStatsCalculatorTest {

    private final DisplayStatsCalculator cut = new DisplayStatsCalculator("", false, false, false);

    @Test
    void simpleChar() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Dierentuin.d2s"));

        assertThat(result.name()).isEqualTo("Dierentuin");
        assertThat(result.type()).isEqualTo(CharacterType.NECROMANCER);
        assertThat(result.level()).isEqualTo(16);
        assertThat(result.fasterRunWalk()).isEqualTo(30);
        assertThat(result.attributes().strength()).isEqualTo(42);
        assertThat(result.attributes().dexterity()).isEqualTo(25);
        assertThat(result.attributes().vitality()).isEqualTo(70);
        assertThat(result.attributes().energy()).isEqualTo(25);
        assertThat(result.resistances().fire()).isEqualTo(28);
        assertThat(result.resistances().lightning()).isEqualTo(31);
        assertThat(result.resistances().cold()).isEqualTo(53);
        assertThat(result.resistances().poison()).isEqualTo(10);
        assertThat(result.mf()).isEqualTo(45);
        assertThat(result.gf()).isZero();
        assertThat(result.gold()).isEqualTo("16");
        assertThat(result.goldInStash()).isEqualTo("5K");
        assertThat(result.runes()).isEqualTo("Nef, Eth, Ith (2), Tal (3), Ral");
    }

    @Test
    void newChar() {
        final  DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar.d2s"));

        assertThat(result.name()).isEqualTo("Wandelaar");
        assertThat(result.type()).isEqualTo(CharacterType.PALADIN);
        assertThat(result.breakpoints().nextFHR()).isEqualTo(7);
        assertThat(result.breakpoints().nextFCR()).isEqualTo(9);
    }

    @Test
    void goldenStatueBroken() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-stat.d2s"));

        assertThat(result.type()).isEqualTo(CharacterType.PALADIN);
        // this used to throw an exception as j34 has an extra byte (8 bits) in the item list
    }

    @Test
    void removeRunewordAlreadyMade() {
        final DisplayStatsCalculator calculatorWithoutDuplicates = new DisplayStatsCalculator("", true, false, false);
        final DisplayStats result = calculatorWithoutDuplicates.getDisplayStats(Path.of("src/test/resources/2.5/Fierljepper.d2s"));

        // Stealth is already made, so should be skipped
        assertThat(result.runes()).isEqualTo("Nef (3), Eth, Ith, Tal (2), Ral");
        assertThat(result.runewords()).isEmpty();
    }

    @Test
    void calculateNightmareResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-nm.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(34);
        assertThat(result.resistances().lightning()).isEqualTo(55);
        assertThat(result.resistances().cold()).isEqualTo(17);
        assertThat(result.resistances().poison()).isEqualTo(60);
    }

    @Test
    void calculateAnyaResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-anya.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(44);
        assertThat(result.resistances().lightning()).isEqualTo(65);
        assertThat(result.resistances().cold()).isEqualTo(27);
        assertThat(result.resistances().poison()).isEqualTo(70);
    }

    @Test
    void calculateAddedMaxResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.8/Sparkles-above75percent.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(82);
        assertThat(result.resistances().lightning()).isEqualTo(33); // 8 + 25 from Hsaru's set bonus
        assertThat(result.resistances().cold()).isEqualTo(20);
        assertThat(result.resistances().poison()).isEqualTo(45);
    }

    @Test
    void calculateSpeedRunItems() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.8/Sparkles-above75percent.d2s"));

        assertThat(result.speedRunItems().fullRejuvs()).isEqualTo(3);
        assertThat(result.speedRunItems().smallRejuvs()).isEqualTo(2);
        assertThat(result.speedRunItems().chippedGems()).isEqualTo(3);
    }

    @Test
    void shouldCalcRemainingXPfor99() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.80273/Goatunnheim_lvl99.d2s"));
        assertThat(result.level()).isEqualTo(99);
        assertThat(result.percentToNext()).isEqualTo("100");
    }

    @Test
    void brokenJewels() {
        // This character has 6 adjusted jewels that lack a prefix and postfix id. Prior to parser version 1.3.2 this would throw a ParserException.
        assertThatCode(() -> cut.getDisplayStats(Path.of("src/test/resources/1.6.80273/Goatunnheim_wrong_jewels.d2s"))).doesNotThrowAnyException();
    }

    @Test
    void shouldNotApplyStealthBonus() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/NoStealth.d2s"));
        assertThat(result.breakpoints().fCR()).isZero();
        assertThat(result.breakpoints().fHR()).isEqualTo(27);
        assertThat(result.fasterRunWalk()).isEqualTo(50);
    }

    @Test
    void shouldNotApplyCharmBonus() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/NoStealth.d2s"));

        // This should not apply the bonus of the lvl 22 req Amber GC
        assertThat(result.resistances().lightning()).isEqualTo(26);
    }

    @Test
    void countKeys() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/Keys.d2s"));

        assertThat(result.keys().terror()).isEqualTo(1);
        assertThat(result.keys().hate()).isEqualTo(1);
        assertThat(result.keys().destruction()).isEqualTo(1);
    }

    @Test
    void throwExceptionOnEmptySaveGame() {
        final ParseException parseException = assertThrows(ParseException.class,
                () -> cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/nobytes.d2s")));

        assertThat(parseException.getMessage()).isEqualTo("Less than 335 bytes read (0), either the file is locked, or this is not a valid .d2s file");
    }

    @Test
    void requirementsMetOnKanosSpirit() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/rtltq_Kano.d2s"));

        assertThat(result.breakpoints().fCR()).isEqualTo(60);
        assertThat(result.breakpoints().fHR()).isEqualTo(80);
    }

    @Test
    void requirementsMetOnTalRasha() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/Koelkast.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(9);
        assertThat(result.resistances().lightning()).isEqualTo(50);
        assertThat(result.resistances().cold()).isEqualTo(-79);
        assertThat(result.resistances().poison()).isEqualTo(-3);
        assertThat(result.mf()).isEqualTo(240);
        assertThat(result.breakpoints().fCR()).isEqualTo(40);
    }

    @Test
    void shouldAddAliBabaMagicAndGoldFind() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.84219/Lohengrin.d2s"));

        assertThat(result.mf()).isEqualTo(310);
        assertThat(result.gf()).isEqualTo(536);
    }
}
