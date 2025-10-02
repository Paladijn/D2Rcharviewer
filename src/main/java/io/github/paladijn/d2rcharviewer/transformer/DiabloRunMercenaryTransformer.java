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

import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.Constants;
import io.github.paladijn.d2rcharviewer.model.diablorun.ItemPayload;
import io.github.paladijn.d2rcharviewer.model.diablorun.Mercenary;
import io.github.paladijn.d2rcharviewer.service.TranslationService;
import io.github.paladijn.d2rsavegameparser.model.Difficulty;
import io.github.paladijn.d2rsavegameparser.model.Item;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class DiabloRunMercenaryTransformer {
    private final TranslationService translationService;

    private final DiabloRunItemTransformer diabloRunItemTransformer;

    public DiabloRunMercenaryTransformer(TranslationService translationService, DiabloRunItemTransformer diabloRunItemTransformer) {
        this.translationService = translationService;
        this.diabloRunItemTransformer = diabloRunItemTransformer;
    }

    public Mercenary convertMercenary(io.github.paladijn.d2rsavegameparser.model.Mercenary mercenary, Difficulty difficulty) {
        if (mercenary == null) {
            return null;
        }

        final String name = getMercenaryName(mercenary.nameId(), mercenary.typeId());
        final int level = getLevelByXP(mercenary.experience());
        final int strength = calculateStrengthByMercenaryType(mercenary.typeId(), level);
        final int dexterity = calculateDexByMercenaryType(mercenary.typeId(), level);
        final int fireRes = mercenaryResistanceByLevel(level, difficulty, "fire", mercenary.items());
        final int coldRes = mercenaryResistanceByLevel(level, difficulty, "cold", mercenary.items());
        final int lightRes = mercenaryResistanceByLevel(level, difficulty, "light", mercenary.items());
        final int poisRes = mercenaryResistanceByLevel(level, difficulty, "poison", mercenary.items());
        final List<ItemPayload> mercItems = diabloRunItemTransformer.convertItems(mercenary.items(), true, level);

        return new Mercenary(name, mercenary.typeId(), level, mercenary.alive(), mercenary.experience(), strength, dexterity, fireRes, coldRes, lightRes, poisRes, List.of(), mercItems);
    }

    private int calculateStrengthByMercenaryType(short typeId, int level) {
        if (typeId < 6) {
            return 32 + level;
        }
        if (typeId < 15) {
            return 41 + (2 * level);
        }
        if (typeId < 24) {
            return 31 + level;
        }
        if (typeId < 30) {
            return 49 + (2 * level);
        }
        return 0;
    }

    private int calculateDexByMercenaryType(short typeId, int level) {
        if (typeId < 6) {
            return 39 + (2 * level);
        }
        if (typeId < 15) {
            return 26 + (2 * level);
        }
        if (typeId < 24) {
            return 25 + level;
        }
        if (typeId < 30) {
            return 29 + level;
        }
        return 0;
    }

    private int mercenaryResistanceByLevel(int level, Difficulty difficulty, String type, List<Item> equippedItems) {
        int baseRes = (level - 3) * 2;
        if (level < 4) {
            baseRes = 0;
        }

        final int sum = baseRes + DisplayStatsCalculator.getTotalPointsInProperty(type + "resist", equippedItems, List.of());
        final int max = Math.min(95, 75 + DisplayStatsCalculator.getTotalPointsInProperty("max" + type + "resist", equippedItems, List.of()));

        return Math.min(max, switch (difficulty) {
                    case NORMAL -> sum;
                    case NIGHTMARE -> sum - 40;
                    case HELL -> sum - 100;
                }
        );
    }

    private String getMercenaryName(short nameId, short typeId) {
        if (typeId < 6) {
            return translationService.getTranslationByKey("merc%02d".formatted(1 + nameId));
        }
        if (typeId < 15) {
            return translationService.getTranslationByKey("merca" + (201 + nameId));
        }
        if (typeId < 24) {
            return translationService.getTranslationByKey("merca" + (222 + nameId));
        }
        if (typeId < 30) {
            return translationService.getTranslationByKey("Merc" + (101 + nameId));
        }
        return "N.N. - %d [%d]".formatted(nameId, typeId);
    }


    private int getLevelByXP(int experience) {
        int level = 1;
        while (Constants.xpLevelsMerc[level] < experience) {
            level++;
            if (level > 99) {
                break;
            }
        }
        return level - 1;
    }
}
