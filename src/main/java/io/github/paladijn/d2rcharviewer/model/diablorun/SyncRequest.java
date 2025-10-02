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

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record SyncRequest(
        String version,
        D2ProcessInfo processInfo,

        long seed,

        String name,
        String title,
        int charClass,
        boolean isExpansion,
        boolean isHardcore,
        boolean isDead,
        int act,
        int difficulty,
        int level,
        long experience,
        int strength,
        int dexterity,
        int vitality,
        int energy,

        int fireResist,
        int coldResist,
        int lightningResist,
        int poisonResist,

        long gold,
        long goldStash,

        long life,
        long lifeMax,
        long mana,
        long manaMax,

        int fasterCastRate,
        int fasterHitRecovery,
        int fasterRunWalk,
        int increasedAttackSpeed,
        int magicFind,
        int goldFind,

        CompletedQuests completedQuests,

        List<ItemPayload> items,
        List<ItemPayload> corpseItems,

        Mercenary mercenary
) {
    public SyncRequest {
        try {
            items.addAll(List.of());
            throw new IllegalArgumentException("added items should be an immutable list");
        } catch (UnsupportedOperationException _) {
            // expected behaviour, valid
        }

        try {
            corpseItems.addAll(List.of());
            throw new IllegalArgumentException("added corpse items should be an immutable list");
        } catch (UnsupportedOperationException _) {
            // expected behaviour, valid
        }
    }
}
