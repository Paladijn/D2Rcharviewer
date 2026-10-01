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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rcharviewer.service.TranslationService;
import io.github.paladijn.d2rsavegameparser.model.CharacterType;
import io.github.paladijn.d2rsavegameparser.parser.ParseException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DisplayStatsCalculatorTest {

    private static final Logger log = LoggerFactory.getLogger(DisplayStatsCalculatorTest.class);
    private final TranslationService translationService = new TranslationService(new ObjectMapper(), "enUS");
    private final DisplayStatsCalculator cut = new DisplayStatsCalculator("", false, false, false, translationService);

    @Test
    @Disabled("Update to 105")
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
    @Disabled("Update to 105")
    void newChar() {
        final  DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar.d2s"));

        assertThat(result.name()).isEqualTo("Wandelaar");
        assertThat(result.type()).isEqualTo(CharacterType.PALADIN);
        assertThat(result.breakpoints().nextFHR()).isEqualTo(7);
        assertThat(result.breakpoints().nextFCR()).isEqualTo(9);
    }

    @Test
    @Disabled("Update to 105")
    void goldenStatueBroken() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-stat.d2s"));

        assertThat(result.type()).isEqualTo(CharacterType.PALADIN);
        // this used to throw an exception as j34 has an extra byte (8 bits) in the item list
    }

    @Test
    @Disabled("Update to 105")
    void removeRunewordAlreadyMade() {
        final DisplayStatsCalculator calculatorWithoutDuplicates = new DisplayStatsCalculator("", true, false, false, translationService);
        final DisplayStats result = calculatorWithoutDuplicates.getDisplayStats(Path.of("src/test/resources/2.5/Fierljepper.d2s"));

        // Stealth is already made, so should be skipped
        assertThat(result.runes()).isEqualTo("Nef (3), Eth, Ith, Tal (2), Ral");
        assertThat(result.runewords()).isEmpty();
    }

    @Test
    @Disabled("Update to 105")
    void calculateNightmareResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-nm.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(34);
        assertThat(result.resistances().lightning()).isEqualTo(55);
        assertThat(result.resistances().cold()).isEqualTo(17);
        assertThat(result.resistances().poison()).isEqualTo(60);
    }

    @Test
    @Disabled("Update to 105")
    void calculateAnyaResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.5/Wandelaar-anya.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(44);
        assertThat(result.resistances().lightning()).isEqualTo(65);
        assertThat(result.resistances().cold()).isEqualTo(27);
        assertThat(result.resistances().poison()).isEqualTo(70);
    }

    @Test
    @Disabled("Update to 105")
    void calculateAddedMaxResistances() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.8/Sparkles-above75percent.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(82);
        assertThat(result.resistances().lightning()).isEqualTo(33); // 8 + 25 from Hsaru's set bonus
        assertThat(result.resistances().cold()).isEqualTo(20);
        assertThat(result.resistances().poison()).isEqualTo(45);
    }

    @Test
    @Disabled("Update to 105")
    void calculateSpeedRunItems() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/2.8/Sparkles-above75percent.d2s"));

        assertThat(result.speedRunItems().fullRejuvs()).isEqualTo(3);
        assertThat(result.speedRunItems().smallRejuvs()).isEqualTo(2);
        assertThat(result.speedRunItems().chippedGems()).isEqualTo(3);
    }

    @Test
    @Disabled("Update to 105")
    void shouldCalcRemainingXPfor99() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.80273/Goatunnheim_lvl99.d2s"));
        assertThat(result.level()).isEqualTo(99);
        assertThat(result.percentToNext()).isEqualTo("100");
    }

    @Test
    @Disabled("Update to 105")
    void brokenJewels() {
        // This character has 6 adjusted jewels that lack a prefix and postfix id. Prior to parser version 1.3.2 this would throw a ParserException.
        assertThatCode(() -> cut.getDisplayStats(Path.of("src/test/resources/1.6.80273/Goatunnheim_wrong_jewels.d2s"))).doesNotThrowAnyException();
    }

    @Test
    @Disabled("Update to 105")
    void shouldNotApplyStealthBonus() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/NoStealth.d2s"));
        assertThat(result.breakpoints().fCR()).isZero();
        assertThat(result.breakpoints().fHR()).isEqualTo(27);
        assertThat(result.fasterRunWalk()).isEqualTo(50);
    }

    @Test
    @Disabled("Update to 105")
    void shouldNotApplyCharmBonus() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/NoStealth.d2s"));

        // This should not apply the bonus of the lvl 22 req Amber GC
        assertThat(result.resistances().lightning()).isEqualTo(26);
    }

    @Test
    @Disabled("Update to 105")
    void countKeys() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/Keys.d2s"));

        assertThat(result.keys().terror()).isEqualTo(1);
        assertThat(result.keys().hate()).isEqualTo(1);
        assertThat(result.keys().destruction()).isEqualTo(1);
        assertThat(result.keys().totalKeys()).isEqualTo(3);
    }

    @Test
    void throwExceptionOnEmptySaveGame() {
        assertThatExceptionOfType(ParseException.class)
                .isThrownBy(() -> cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/nobytes.d2s")))
                .withMessage("Less than 335 bytes read (0), either the file is locked, or this is not a valid .d2s file");
    }

    @Test
    @Disabled("Update to 105")
    void requirementsMetOnKanosSpirit() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.81914/rtltq_Kano.d2s"));

        assertThat(result.breakpoints().fCR()).isEqualTo(60);
        assertThat(result.breakpoints().fHR()).isEqualTo(80);
    }

    @Test
    @Disabled("Update to 105")
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
    @Disabled("Update to 105")
    void shouldAddAliBabaMagicAndGoldFind() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.6.84219/Lohengrin.d2s"));

        assertThat(result.mf()).isEqualTo(310);
        assertThat(result.gf()).isEqualTo(536);
    }

    @Test
    @Disabled("Update to 105")
    void shouldIgnoreUnidentifiedCharmStsats() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/1.7.90898/Fjoerich.d2s"));

        assertThat(result.maxHP()).isEqualTo(902); // Should not count the +6 from the small charm
    }

    @Test
    void resOnJewel() {
        final DisplayStats result = cut.getDisplayStats(Path.of("src/test/resources/3.1.92029/Morvath.d2s"));

        assertThat(result.resistances().fire()).isEqualTo(-40);
        assertThat(result.resistances().lightning()).isEqualTo(-16);
        assertThat(result.resistances().cold()).isEqualTo(0);
        assertThat(result.resistances().poison()).isEqualTo(4);
    }


    @Test
    void broken() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of("broken"), "*.d2s")) {
            for (Path entry : stream) {
                try {
                    final DisplayStats result = cut.getDisplayStats(entry);
                    assertThat(result.name()).isEqualTo("Morvath");
                } catch (Exception e) {
                    log.debug("D2s file failed -> {}", entry);
                    throw new RuntimeException("broken character " + entry, e);
                }
            }
        }

        // 16, 0, -64, 16, 5, 8, -16, -110, 9, -59, -44, -39, 78, -105, -29, 6, 64, 64, -112, 64, 37, -62, -120, 17, 45, -12, -27, 64, 1, -123, 37, 62, -56, 89, -122, -114, -20, -1, -44 ||, 66, 7, -93, 112, 0, 0, 16, 40, -128,
        //final DisplayStats result = cut.getDisplayStats(Path.of("broken/20260221-001025_Morvath.d2s")); // Bane's Oathmaker
        // 16, 0, -128, 16, 5, 16, 68, 58, -65, -48, -11, 45, 121, 34, 42, 8, 1, -58, 3, 3, 12, 74, 27, -14, 95, -47, -32, 127, 35, -49, 127, -14, 43, 55, 70, -31, 0, 0, 16, 8, -128, 4, 13, 17, -128,
        // 16, 0, -128,  0, 5,  0, 68, 58, -65, -48, -11, 45, 121, 34, 42, 8, 1, -58, 3, 3, 12, 74, 27, -14, 95, -47, -32, 127, 35, -49, 127
        //final DisplayStats result = cut.getDisplayStats(Path.of("broken/20260222-110904_Morvath.d2s")); // Hsaru's iron heel
        // 0,  0, -128, 16, 5, -120, -60, -86, 9, -60, 90, -104, 99, -116, 98, 0, 76, 0, -62, -127, 112, 34, 28, 24, -44, 127, 112, -44, -1, -26, || -123, 79, 74, -31, 0, 0, 16, 0, -128,
        // 16, 0, -128, 16, 5, -120, -60, -86, 9, -60, 90, -104, 99, -116, 98, 0, 76, 0, -62, -127, 112, 34, 28, 24, -44, 127, 112, -44, -1, -26, || -123, 79, 74, -31, 0, 0, 16, 8, -128,
        // 16, 0, -128,  0, 5,   84, -60, -86, 9, -60, 90, -104, 99, -116, 98, 0, 76, 0, -62, -127, 112, 34, 28, 24, -44, 127, 112, -44, 127
        // 16, 0, -128, 16, 5, 0, -112, -128, -122, -21, -71, 63, -70, 94, 97, 3, 96, 58, 24, 17, 100, -56, -72, -125, -30, 63, 56, -24, -65, 13, 10, 15, 75, -1, -53, -81,|| -124, 46, -123, 3, 0, 16, 0, -128 -- arctic
        // 16, 0, -128, 16, 5, 12, -124, -29, 27, -94, 125, -62, -8, 112, -123, 28, -40, -126, 71, 7, -32, -125, 27, 78, 112, 75, -127, -19, 79, -2, 31, 46, || 108, 92, 10, 7, 0, 16, 8, -128 - Szabi
        // 16, 0, -128, 16, 5, 0, -48, 97, 23, -104, -12, -82, 110, 119, 71, 45, -72, -39, 32, 66, -79, 98, 19, -88, -71, 64, 97, 0, 41, -111, -69, -96, -22, 4, -2, -37, || 47, 88, 92, 10, 7, 0, 16, 8, -128 -- husaldol evo
        // 16, 0, -128, 16, 5, 76, -60, -82, 27, 24, 78, 11, 55, 111, 5, 2, -104, 0, -124, 3, 33, 74, -64, 82, -26, 63, 56, -22, -65, || -10, 66, -62, -91, 112, 0, 0, 16, 0, -128, == ceglaw's pincers
        // 16, 0, -128, 16, 5, 0, 80, 15, 5, -111, -118, -100, 53, 90, -15, 0, -104, 32, 67, 66, -8, 32, -125, 17, -62, 127, 107, -48, 127, || -47, 5, 100, 75, -31, 0, 0, 16, 8, -128, -- isenhart's case (2)
        // 16, 0, -128, 16, 5, 4, 80, 15, -123, -22, 52, -68, -40, 91, -15, 0, -100, 32, 67, 66, -8, 32, -125, 17, -62, 127, 107, -48, 127, || -9, -123, 94, 75, -31, 0, 0, 16, 0, -128 -- Isenhart's case (1)
        // 16, 0, -128, 16, 5, 8, 68, 1, 27, -10, 11, -66, -15, 98, -123, 31, 120, 80, 0, 36, -56, -119, -80, 100, -119, -115, -116, 37, 50, 62, 16, 105, 80, -1, -73, 23, || 112, 45, -123, 3, 0, 16, 8, -128, -- Sander's superstition
        // 16, 0, -128, 16, 5, 16, 68, 58, -65, -48, -11, 45, 121, 34, 42, 8, 1, -58, 3, 3, 12, 74, 27, -14, 95, -47, -32, 127, 35, -49, 127, || -14, 43, 55, 70, -31, 0, 0, 16, 8, -128, -- Bane's Oathmaker (1)
        // 16, 0, -128, 16, 5, 12, 68, 58, -65, -32, 95, -33, 58, 16, 43, 8, 1, 6, 5, 3, 12, 74, 27, -14, 95, -47, -32, 127, 35, -49, 127, || -94, 5, 38, 75, -31, 0, 0, 16, 8, -128, -- Bane's oathmaker (2)
        // 16, 0, -128, 16, 5, 4, 80, -4, -66, 3, 106, -58, 126, 98, -85, -48, 3, 40, 24, -47, 0, 0, 120, -7, 80, 5, 24, 25, 35, -78, 100, -78, -94, 6, -58, 127, || -66, -123, 36, 75, -31, 0, 0, 16, 8, -128, -- Naj's circlet
        // 16, 0, -128, 16, 5, 8, 68, -6, -103, -48, -19, 111, -82, 112, 42, 42, 64, 7, 60, 32, 24, 31, 70, 0, 5, -17, -1, 1, 41, -1, 33, -56, -8, 127, -49, || -16, 94, 41, 28, 0, 0, 16, 8, -128, -- milabrega's orb
        // 16, 0, -128, 16, 5, 8, -60, 114, 13, 3, 123, 19, -119, -89, -125, 8, -96, 116, 32, -128, 22, 65, -122, -116, 9, 50, -28, 48, -61, -116, 37, 93, 80, -100, 100, -1, 35, -81, || -90, 43, -123, 3, 0, 16, 8, -128, -- Ichorsting
        // 16, 0, -128, 16, 5, 8, 68, -66, 99, -128, 76, 16, 57, 76, 29, 89, -128, 14, 48, 34, 0, 40, -127, -16, 70, 68, 65, 66, -38, 22, -83, -1, -112, || 87, -52, -107, -62, 1, 0, 16, 8, -128, -- Rockfleece
        // 16, 0, -128, 16, 5, 8, 4, 96, 27, -70, 32, 92, 36, -110, -121, 5, -72, 81, 1, -128, 74, -124, 70, -115, 38, -84, 33, 35, 80, -32, 127, -16, 66, 0, -86, 112, 0, 0, 16, 8, -128,
        // 0, 0, -128, 16, 5, 16, 68, 40, 8, 83, -12, -21, -25, -57, -61, 7, -112, 76, 16, -127, 5, -117, 9, 60, -100, -112, -106, 66, -38, 10, 105, 45, -92, 117, 49, -22, -63, -45, -56, -13, -33, -18, -86, 1, 85, 56, 0, 0, 16, 0, -128,
//        Malah's Potion :: byte[] bytes = {16, 32, -128, 0, 5, -112, -28, 39, -122, -128, -50, 24, 57, -32, 19, 32, -37, 127, 16, 0, -128, 0, 5, 4, -28, -89, 82, 120, 62, -39};
//        2026-02-24 23:57:49,145 INFO  [io.github.paladijn.d2rsavegameparser.internal.parser.BitReader] (main) Malah's Potion in Hex
//
//        10 20 80 00 05 90 E4 27 86 80 CE 18 39 E0 13 20
//        DB 7F 10 00 80 00 05 04 E4 A7 52 78 3E D9

    }
}
