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
package io.github.paladijn.d2rcharviewer.calculator;

import io.github.paladijn.d2rcharviewer.model.Breakpoints;
import io.github.paladijn.d2rsavegameparser.model.CharacterType;
import io.github.paladijn.d2rsavegameparser.model.Item;
import io.github.paladijn.d2rsavegameparser.model.ItemProperty;

import java.util.List;

import static io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator.getPropertiesByNames;

public class BreakpointCalculator {

    private static final int[] AMAZON_FHR = {0, 6, 13, 20, 32, 52, 86, 174, 600};
    private static final int[] ASS_BARB_PALA_FHR = {0, 7, 15, 27, 48, 86, 200};
    private static final int[] DRUID_1H_FHR = {0, 3, 7, 13, 19, 29, 42, 63, 99, 174, 456 };
    private static final int[] DRUID_OTHER_FHR = {0, 5, 10, 16, 26, 39, 56, 86, 152, 377};
    private static final int[] NECRO_FHR = {0, 5, 10, 16, 26, 39, 56, 86, 152, 377};
    private static final int[] NECRO_TRANG_FHR = {0, 2, 6, 10, 16, 24, 34, 48, 72, 117};
    private static final int[] PALADIN_STAVES_SPEARS_FHR = {0, 3, 7, 13, 20, 32, 48, 75, 129, 280};
    private static final int[] SORCERESS_FHR = {0, 5, 9, 14, 20, 30, 42, 60, 86, 142, 280};

    private static final int[] AMAZON_FCR = {0, 7, 14, 22, 32, 48, 68, 99, 152};
    private static final int[] ASSASSIN_FCR = {0, 8, 16, 27, 42, 65, 102, 174};
    private static final int[] BARBARIAN_SORCERESS_FCR = {0, 9, 20, 37, 63, 105, 200};
    private static final int[] DRUID_HUMAN_FCR = {0, 4, 10, 19, 30, 46, 68, 99, 163};
    private static final int[] NECROMANCER_PALADIN_FCR = {0, 9, 18, 30, 48, 75, 125};
    private static final int[] NECROMANCER_TRANG_FCR = {0, 6, 11, 18, 24, 35, 48, 65, 86, 120, 180};

    public Breakpoints calculateBreakpoints(CharacterType characterType, List<Item> equippedItems, List<ItemProperty> equippedSetBenefits) {
        int fasterHitRecovery = getFasterHitRecovery(equippedItems, equippedSetBenefits);
        int nextFHR = getNextFHR(characterType, fasterHitRecovery);
        int fasterCastRate = getFasterCastRate(equippedItems, equippedSetBenefits);
        int nextFCR = getNextFCR(characterType, fasterCastRate);
        int fasterBlockRate = getFasterBlockRate(equippedItems, equippedSetBenefits);

        return new Breakpoints(fasterHitRecovery, nextFHR, fasterCastRate, nextFCR, fasterBlockRate, 0);
    }

    private int getNextFCR(CharacterType characterType, int fasterCastRate) {
        return switch (characterType) {
            case AMAZON -> getNextBreakpoint(fasterCastRate, AMAZON_FCR);
            case ASSASSIN -> getNextBreakpoint(fasterCastRate, ASSASSIN_FCR);
            case BARBARIAN, SORCERESS -> getNextBreakpoint(fasterCastRate, BARBARIAN_SORCERESS_FCR);
            case DRUID -> getNextBreakpoint(fasterCastRate, DRUID_HUMAN_FCR);
            case NECROMANCER, PALADIN -> getNextBreakpoint(fasterCastRate, NECROMANCER_PALADIN_FCR);
            case NONE -> 0;
        };
    }

    private int getNextFHR(CharacterType characterType, int fasterHitRecovery) {
        return switch (characterType) {
            case AMAZON -> getNextBreakpoint(fasterHitRecovery, AMAZON_FHR);
            case ASSASSIN, BARBARIAN -> getNextBreakpoint(fasterHitRecovery, ASS_BARB_PALA_FHR);
            case DRUID -> getNextBreakpoint(fasterHitRecovery, DRUID_1H_FHR);// should filter both on 1H and 'other'
            case NECROMANCER -> getNextBreakpoint(fasterHitRecovery, NECRO_FHR);
            case PALADIN -> getNextBreakpoint(fasterHitRecovery, ASS_BARB_PALA_FHR); // should filter on spear/saves
            case SORCERESS -> getNextBreakpoint(fasterHitRecovery, SORCERESS_FHR);
            case NONE -> 0;
        };
    }

    private int getNextBreakpoint(int current, int[] breakpoints) {
        for (int next: breakpoints) {
            if (current < next) {
                return next;
            }
        }
        return breakpoints[breakpoints.length - 1];
    }

    private int getFasterCastRate(List<Item> equippedItems, List<ItemProperty> equippedSetBenefits) {
        List<ItemProperty> propsFound = getPropertiesByNames(equippedItems, List.of("item_fastercastrate"));
        propsFound.addAll(equippedSetBenefits.stream().filter(ip -> ip.name().equals("item_fastercastrate")).toList());
        return propsFound.stream().mapToInt(itemProperty -> itemProperty.values()[0]).sum();
    }

    private int getFasterHitRecovery(List<Item> equippedItems, List<ItemProperty> equippedSetBenefits) {
        List<ItemProperty> propsFound = getPropertiesByNames(equippedItems, List.of("item_fastergethitrate"));
        propsFound.addAll(equippedSetBenefits.stream().filter(ip -> ip.name().equals("item_fastergethitrate")).toList());
        return propsFound.stream().mapToInt(itemProperty -> itemProperty.values()[0]).sum();
    }

    private int getFasterBlockRate(List<Item> equippedItems, List<ItemProperty> equippedSetBenefits) {
        List<ItemProperty> propsFound = getPropertiesByNames(equippedItems, List.of("item_fasterblockrate"));
        propsFound.addAll(equippedSetBenefits.stream().filter(ip -> ip.name().equals("item_fasterblockrate")).toList());
        return propsFound.stream().mapToInt(itemProperty -> itemProperty.values()[0]).sum();
    }
}
