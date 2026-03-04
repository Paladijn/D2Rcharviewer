package io.github.paladijn.d2rcharviewer.model;

import io.github.paladijn.d2rsavegameparser.model.ItemQuality;

import java.time.LocalDateTime;

public record ChronicleStats(
        int chronicleSetsDiscovered,
        int chronicleUniquesDiscovered,
        int chronicleRunewordsDiscovered,
        int totalChronicleDiscovered,
        String itemName,
        ItemQuality quality,
        String monsterName,
        LocalDateTime found
) {
}
