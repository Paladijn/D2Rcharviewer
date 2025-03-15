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
        int playersX,

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
        int gold,
        @JsonProperty("GoldStash")
        int goldStash,

        @JsonProperty("Life")
        int life,
        @JsonProperty("LifeMax")
        int lifeMax,
        @JsonProperty("Mana")
        int mana,
        @JsonProperty("ManaMax")
        int manaMax,

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
        int inventoryTab,
        @JsonProperty("ClearItems")
        boolean clearItems,
        @JsonProperty("AddedItems")
        List<ItemPayload> addedItems,
        @JsonProperty("RemovedItems")
        List<ItemPayload> removedItems,

        @JsonProperty("Hireling")
        Hireling hireling
) {
}
