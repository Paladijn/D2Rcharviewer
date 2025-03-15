package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemPayload(
        @JsonProperty("GUID")
        int guid,
        @JsonProperty("Class")
        int itemClass,
        @JsonProperty("BaseItem")
        String baseItem,
        @JsonProperty("ItemName")
        String itemName,
        @JsonProperty("Quality")
        DRUNItemQuality quality,
        @JsonProperty("Location")
        ItemLocation location
) {
}
