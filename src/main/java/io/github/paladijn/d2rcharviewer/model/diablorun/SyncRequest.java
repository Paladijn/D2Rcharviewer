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
package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SyncRequest(
        @JsonProperty("Event")
        String event,
        @JsonProperty("Headers")
        String headers,
        @JsonProperty("DIApplicationInfo")
        DIApplicationInfo applicationInfo,
        @JsonProperty("D2ProcessInfo")
        D2ProcessInfo processInfo,

        @JsonProperty("Seed")
        int seed,
        @JsonProperty("Seed_is_arg")
        boolean seedIsArg,
        @JsonProperty("NewCharacter")
        boolean newCharacter,

        @JsonProperty("Name")
        String name,
        @JsonProperty("Guid")
        String guid,
        @JsonProperty("CharClass")
        int charClass,
        @JsonProperty("IsExpansion")
        boolean isExpansion,
        @JsonProperty("IsHardcore")
        boolean isHardcore,
        @JsonProperty("IsDead")
        boolean isDead,

        @JsonProperty("Area")
        int area,
        @JsonProperty("Difficulty")
        int difficulty,
        @JsonProperty("PlayersX")
        Integer playersX,

        @JsonProperty("Deaths")
        int deaths,
        @JsonProperty("Level")
        int level,
        @JsonProperty("Experience")
        long experience,
        @JsonProperty("Strength")
        int strength,
        @JsonProperty("Dexterity")
        int dexterity,
        @JsonProperty("Vitality")
        int vitality,
        @JsonProperty("Energy")
        int energy,

        @JsonProperty("FireResist")
        int fireResist,
        @JsonProperty("ColdResist")
        int coldResist,
        @JsonProperty("LightningResist")
        int lightningResist,
        @JsonProperty("PoisonResist")
        int poisonResist,

        @JsonProperty("Gold")
        long gold,
        @JsonProperty("GoldStash")
        long goldStash,

        @JsonProperty("Life")
        long life,
        @JsonProperty("LifeMax")
        long lifeMax,
        @JsonProperty("Mana")
        long mana,
        @JsonProperty("ManaMax")
        long manaMax,

        @JsonProperty("FasterCastRate")
        int fasterCastRate,
        @JsonProperty("FasterHitRecovery")
        int fasterHitRecovery,
        @JsonProperty("FasterRunWalk")
        int fasterRunWalk,
        @JsonProperty("IncreasedAttackSpeed")
        int increasedAttackSpeed,
        @JsonProperty("MagicFind")
        int magicFind,

        @JsonProperty("CompletedQuests")
        CompletedQuests completedQuests,

        @JsonProperty("InventoryTab")
        Integer inventoryTab,
        @JsonProperty("ClearItems")
        boolean clearItems,
        @JsonProperty("AddedItems")
        List<ItemPayload> addedItems,
        @JsonProperty("RemovedItems")
        List<ItemPayload> removedItems,

        @JsonProperty("Hireling")
        Hireling hireling
) {
    public SyncRequest {
        try {
            addedItems.addAll(List.of());
            throw new IllegalArgumentException("added items should be an immutable list");
        } catch (UnsupportedOperationException uoe) {
            // expected behaviour, valid
        }

        try {
            removedItems.addAll(List.of());
            throw new IllegalArgumentException("added items should be an immutable list");
        } catch (UnsupportedOperationException uoe) {
            // expected behaviour, valid
        }
    }
}
