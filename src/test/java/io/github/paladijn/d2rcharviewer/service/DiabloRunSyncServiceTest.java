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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.diablorun.SyncRequest;
import io.github.paladijn.d2rcharviewer.transformer.DiabloRunItemTransformer;
import io.github.paladijn.d2rcharviewer.transformer.DiabloRunMercenaryTransformer;
import io.github.paladijn.d2rsavegameparser.model.D2Character;
import io.github.paladijn.d2rsavegameparser.parser.CharacterParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiabloRunSyncServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CharacterParser characterParser = new CharacterParser(false);

    private TranslationService translationService = new TranslationService(objectMapper, "enUS");

    private final DiabloRunItemTransformer diabloRunItemTransformer = new DiabloRunItemTransformer(translationService, Optional.empty());

    private final DiabloRunMercenaryTransformer diabloRunMercenaryTransformer = new DiabloRunMercenaryTransformer(translationService, diabloRunItemTransformer);

    private final DiabloRunSyncService cut = new DiabloRunSyncService(
            new DisplayStatsCalculator("", true, false, false),
            diabloRunMercenaryTransformer,
            diabloRunItemTransformer,
            objectMapper);

    public DiabloRunSyncServiceTest() {
        cut.equipmentOnly = false;
    }

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/1.6.81914/rtltq_Kano.d2s,src/test/resources/diablorun/output-kano.json",
            "src/test/resources/2.8/Sparkles-above75percent.d2s,src/test/resources/diablorun/output-sparkles.json"
    })
    void validSyncRequest(String characterFile, String expectedJSONFile) throws IOException {

        final String expectedJSON = Files.readString(Paths.get("", expectedJSONFile), StandardCharsets.UTF_8);

        final SyncRequest syncRequest = cut.createSyncRequest(getCharacter(characterFile));
        final String outcome = objectMapper.writeValueAsString(syncRequest);

        assertThat(outcome).isEqualTo(expectedJSON);
    }

    private D2Character getCharacter(String characterFile) throws IOException {
        final byte[] allBytes = Files.readAllBytes(Path.of(characterFile));

       return characterParser.parse(ByteBuffer.wrap(allBytes));
    }
}
