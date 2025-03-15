package io.github.paladijn.d2rcharviewer.model.diablorun;

import io.github.paladijn.d2rsavegameparser.model.ItemQuality;

public enum DRUNItemQuality {
    NONE,
    WHITE,
    BLUE,
    GREEN,
    YELLOW,
    GOLD,
    ORANGE;

    public static DRUNItemQuality fromParsed(ItemQuality input) {
        return switch (input) {
            case NONE, UNKNOWN -> DRUNItemQuality.NONE;
            case INFERIOR, NORMAL, SUPERIOR -> WHITE;
            case MAGIC -> BLUE;
            case SET -> GREEN;
            case RARE -> YELLOW;
            case UNIQUE -> GOLD;
            case CRAFT -> ORANGE;
        };
    }
}
