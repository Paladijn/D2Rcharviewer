package io.github.paladijn.d2rcharviewer.model.translation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslationItem(int id, @JsonProperty("Key") String key, String enUS, String zhTW, String deDE, String esES, String frFR, String itIT,
                              String koKR, String plPL, String esMX, String jaJP, String ptBR, String ruRU, String zhCN) {}
