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
package io.github.paladijn.d2rcharviewer.service;

import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsServiceTest {

    private final DisplayStatsCalculator displayStatsCalculator = new DisplayStatsCalculator("", true, false, false);
    private final StatisticsService cut = new StatisticsService(displayStatsCalculator);

    @ParameterizedTest
    @CsvSource({"src/test/resources/1.6.81914/Keys.d2s,src/test/resources/output/Keys.html",
            "src/test/resources/1.6.80273/Goatunnheim_wrong_jewels.d2s,src/test/resources/output/Goatunnheim_wrong_jewels.html",
            "src/test/resources/2.8/Sparkles-above75percent.d2s,src/test/resources/output/Sparkles-above75percent.html"})
    void validateCharacterOutput(String filename, String expectedFile) throws IOException {
        Locale.setDefault(Locale.UK); // otherwise the formatting will fail on 0.0 -> 0,0
        final String characterOutput = Files.readString(Paths.get("","src/test/resources/templates/character.html"), StandardCharsets.UTF_8);
        final DisplayStats displayStats = displayStatsCalculator.getDisplayStats(Paths.get("", filename));
        final String outcome = cut.replaceValues(characterOutput, displayStats);

        final String expected = Files.readString(Paths.get("", expectedFile), StandardCharsets.UTF_8);

        assertThat(outcome).isEqualTo(expected);
    }
}
