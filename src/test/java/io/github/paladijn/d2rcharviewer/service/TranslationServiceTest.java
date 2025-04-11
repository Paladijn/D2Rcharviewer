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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationServiceTest {
    final TranslationService german = new TranslationService(new ObjectMapper(), "deDE");

    @Test
    void removedBlocksInAffix() {
        assertThat(german.getTranslationByKey("Warrior's")).isEqualTo("kampfstarke");
    }

    @Test
    void removedSingleBlockInItemName() {
        assertThat(german.getTranslationByKey("xvb")).isEqualTo("Hailederstiefel");
    }
}
