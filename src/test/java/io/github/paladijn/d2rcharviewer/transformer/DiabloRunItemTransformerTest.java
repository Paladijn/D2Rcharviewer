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
package io.github.paladijn.d2rcharviewer.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.paladijn.d2rcharviewer.model.translation.DisplayProperty;
import io.github.paladijn.d2rcharviewer.service.TranslationService;
import io.github.paladijn.d2rsavegameparser.model.ItemProperty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiabloRunItemTransformerTest {

    final TranslationService translationService = new TranslationService(new ObjectMapper(), "enUS");

    final DiabloRunItemTransformer cut = new DiabloRunItemTransformer(translationService);

    @Test
    void replaceChargedStaffFields() {
        String outcome = cut.replacePropertyFields("Level %d %s (%d/%d Charges)", List.of(String.valueOf(1), "Teleport", String.valueOf(22), String.valueOf(33)));
        assertThat(outcome).isEqualTo("Level 1 Teleport (22/33 Charges)");
    }

    @Test
    void replaceClassSkillModifier() {
        String outcome = cut.replacePropertyFields("%+d to %s %s", List.of(String.valueOf(1), "Static Field", "(Sorceress only)"));
        assertThat(outcome).isEqualTo("+1 to Static Field (Sorceress only)");
    }

    @Test
    void replaceSequencedClassSkillModifier() {
        String outcome = cut.replacePropertyFields("〈%1〉スキル向上（%+0）%2", List.of(String.valueOf(1), "スタティックフィールド", "（ソーサレス専用）"));
        assertThat(outcome).isEqualTo("〈スタティックフィールド〉スキル向上（+1）（ソーサレス専用）");
    }

    @Test
    void addAssassinOnlySkills() {
        List<DisplayProperty> outcome = cut.getDisplayProperties(List.of(new ItemProperty(83, "item_addclassskills", new int[]{6, 2}, 0,150)), 1);
        assertThat(outcome)
                .containsExactly(new DisplayProperty("ModStre8b", List.of("2"), false));
    }

}