package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Hireling(
        @JsonProperty("Name")
        String name,
        @JsonProperty("Class")
        int classID,
        @JsonProperty("Level")
        int level,
        @JsonProperty("Experience")
        long experience,
        @JsonProperty("Strength")
        int strength,
        @JsonProperty("Dexterity")
        int dexterity,
        @JsonProperty("FireResist")
        int fireResist,
        @JsonProperty("ColdResist")
        int coldResist,
        @JsonProperty("LightningResist")
        int lightningResist,
        @JsonProperty("PoisonResist")
        int poisonResist,
        @JsonProperty("SkillIds")
        List<Integer> skillIds,

        @JsonProperty("Items")
        List<ItemPayload> items
) {
}
