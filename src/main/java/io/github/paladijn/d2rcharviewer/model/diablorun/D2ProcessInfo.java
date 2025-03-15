package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record D2ProcessInfo(
        @JsonProperty("Type")
        String type,
        @JsonProperty("Version")
        String version,
        @JsonProperty("CommandLineArgs")
        List<String> commandLineArgs
) {
}
