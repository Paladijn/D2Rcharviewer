package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompletedQuests(
        @JsonProperty("Normal")
        List<Integer> normal,
        @JsonProperty("Nightmare")
        List<Integer> nightmare,
        @JsonProperty("Hell")
        List<Integer> hell
) {
}
