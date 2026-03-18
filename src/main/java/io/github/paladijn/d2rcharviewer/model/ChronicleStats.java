package io.github.paladijn.d2rcharviewer.model;

import io.github.paladijn.d2rsavegameparser.model.ItemQuality;

import java.time.LocalDateTime;

public record ChronicleStats(
        int chronicleSetsDiscovered,
        int maxSets,
        int setPercentage,
        int chronicleUniquesDiscovered,
        int maxUniques,
        int uniquePercentage,
        int chronicleRunewordsDiscovered,
        int maxRunewords,
        int runewordPercentage,
        int totalChronicleDiscovered,
        String itemName,
        ItemQuality quality,
        String monsterName,
        LocalDateTime found
) {
}
