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
import io.github.paladijn.d2rcharviewer.model.diablorun.DRUNItemQuality;
import io.github.paladijn.d2rcharviewer.model.diablorun.ItemPayload;
import io.github.paladijn.d2rcharviewer.model.translation.DisplayProperty;
import io.github.paladijn.d2rcharviewer.service.TranslationService;
import io.github.paladijn.d2rsavegameparser.model.Item;
import io.github.paladijn.d2rsavegameparser.model.ItemLocation;
import io.github.paladijn.d2rsavegameparser.model.ItemPosition;
import io.github.paladijn.d2rsavegameparser.model.ItemProperty;
import io.github.paladijn.d2rsavegameparser.model.ItemQuality;
import io.github.paladijn.d2rsavegameparser.model.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiabloRunItemTransformerTest {

    final TranslationService translationService = new TranslationService(new ObjectMapper(), "jaJP");

    final DiabloRunItemTransformer cut = new DiabloRunItemTransformer(translationService, Optional.empty(), true);

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

    @Test
    void translateSpirit() {
        List<Item> spirit = List.of(new Item.ItemBuilder()
                        .runeword(true)
                        .invHeight(3)
                        .invWidth(2)
                        .itemName("Spirit")
                        .type("swor")
                        .code("lsd")
                        .socketed(true)
                        .addSocketedItem(new Item.ItemBuilder().code("r07").build())
                        .addSocketedItem(new Item.ItemBuilder().code("r10").build())
                        .addSocketedItem(new Item.ItemBuilder().code("r09").build())
                        .addSocketedItem(new Item.ItemBuilder().code("r11").build())
                        .quality(ItemQuality.SUPERIOR)
                        .itemType(ItemType.WEAPON)
                        .position(ItemPosition.LEFT_HAND)
                        .location(ItemLocation.EQUIPPED)
                        .addProperty(new ItemProperty(127, "item_allskills", new int[]{2}, 0, 158))
                        .addProperty(new ItemProperty(105, "item_fastercastrate", new int[]{25}, 0, 142))
                        .addProperty(new ItemProperty(99, "item_fastergethitrate", new int[]{55}, 0, 139))
                        .addProperty(new ItemProperty(50, "lightmindam", new int[]{1, 50, 0}, 7, 99))
                        .addProperty(new ItemProperty(51, "lightmaxdam", new int[]{50, 50, 0}, 7, 98))
                        .addProperty(new ItemProperty(54, "coldmindam", new int[]{3, 14, 75}, 7, 96))
                        .addProperty(new ItemProperty(55, "coldmaxdam", new int[]{14, 14, 75}, 7, 95))
                        .addProperty(new ItemProperty(57, "poisonmindam", new int[]{154, 154, 125}, 7, 92))
                        .addProperty(new ItemProperty(58, "poisonmaxdam", new int[]{154, 154, 125}, 7, 91))
                        .addProperty(new ItemProperty(60, "lifemaxdrain", new int[]{7, 7, 0}, 7, 88))
                        .addProperty(new ItemProperty(32, "armorclass_vs_missile", new int[]{250}, 0, 69))
                        .addProperty(new ItemProperty(3, "vitality", new int[]{22}, 0, 63))
                        .addProperty(new ItemProperty(9, "maxmana", new int[]{106}, 0, 55))
                        .addProperty(new ItemProperty(147, "item_absorbmagic", new int[]{8}, 0, 33))
                        .addProperty(new ItemProperty(59, "poisonlength", new int[]{125, 154, 125}, 7, -1))
                        .addProperty(new ItemProperty(56, "coldlength", new int[]{75, 14, 75}, 7, -1))
                .build());

        final List<ItemPayload> outcome = cut.convertItems(spirit, true, false, 25);

        assertThat(outcome).hasSize(1);
        final ItemPayload item = outcome.getFirst();
        assertThat(item.baseItem()).isEqualTo("ロングソード");
        assertThat(item.itemName()).isEqualTo("ロングソード [精霊 (タル + スル + オルト + アムン)]");
        assertThat(item.quality()).isEqualTo(DRUNItemQuality.WHITE);
        assertThat(item.properties()).hasSize(11);
        assertThat(item.properties()).hasToString("[全スキル向上（+2）, スキル発動速度（+25%）, ヒットリカバリー速度（+55%）, 電撃ダメージ上昇（1 - 50）, 冷気ダメージ上昇（3 - 14）, 毒ダメージ付与（合計+75ダメージを5秒間で与える）, 攻撃命中でライフを吸収（与ダメージの7%）, 遠隔攻撃に対する防御力（+250）, 生命力（+22）, マナ（+106）, 魔法吸収（+8）]");
    }

    @Test
    void validateUniqueNaming() {
        // uniques are different in the sense that they have the `itemtype [unique name]`as their item name
        final DiabloRunItemTransformer germanTransformer = new DiabloRunItemTransformer(new TranslationService(new ObjectMapper(), "deDE"), Optional.empty(), true);
        final List<Item> unique = List.of(new Item.ItemBuilder()
                        .itemType(ItemType.ARMOR)
                        .itemName("Lidless Wall")
                        .type("xsh")
                        .code("xsh")
                        .quality(ItemQuality.UNIQUE)
                        .position(ItemPosition.LEFT_HAND)
                        .location(ItemLocation.EQUIPPED)
                        .build());

        final List<ItemPayload> outcome = germanTransformer.convertItems(unique, false, false, 99);

        assertThat(outcome).hasSize(1);
        final ItemPayload item = outcome.getFirst();
        assertThat(item.itemName()).isEqualTo("Kampfschild [Lidlose Wand]");
        assertThat(item.quality()).isEqualTo(DRUNItemQuality.GOLD);
    }

    @Test
    void validateUniqueNamingOriginalBaseItem() {
        // the base item name should not be translated in this case
        final DiabloRunItemTransformer germanTransformer = new DiabloRunItemTransformer(new TranslationService(new ObjectMapper(), "deDE"), Optional.empty(), false);
        final List<Item> unique = List.of(new Item.ItemBuilder()
                .itemType(ItemType.ARMOR)
                .itemName("Lidless Wall")
                .type("xsh")
                .code("xsh")
                .quality(ItemQuality.UNIQUE)
                .position(ItemPosition.RIGHT_HAND)
                .location(ItemLocation.EQUIPPED)
                .build());

        final List<ItemPayload> outcome = germanTransformer.convertItems(unique, false, false, 99);

        assertThat(outcome).hasSize(1);
        final ItemPayload item = outcome.getFirst();
        assertThat(item.itemName()).isEqualTo("Grim Shield [Lidlose Wand]");
        assertThat(item.baseItem()).isEqualTo("Grim Shield");
        assertThat(item.quality()).isEqualTo(DRUNItemQuality.GOLD);
    }
}