package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemLocation(
        @JsonProperty("X")
        int x,
        @JsonProperty("Y")
        int y,
        @JsonProperty("Width")
        int width,
        @JsonProperty("Height")
        int height,
        @JsonProperty("BodyLocation")
        int bodyLocation,
        @JsonProperty("Container")
        int container
) {
}
