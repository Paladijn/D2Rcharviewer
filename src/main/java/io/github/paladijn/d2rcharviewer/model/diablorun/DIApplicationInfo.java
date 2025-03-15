package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DIApplicationInfo(@JsonProperty("Version") String version) { }
