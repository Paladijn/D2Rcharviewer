/*
   Copyright 2024-2025 Paladijn (paladijn2960+d2rsavegameparser@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package io.github.paladijn.d2rcharviewer.model.diablorun;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record CompletedQuests(
        @JsonProperty("Normal")
        List<Integer> normal,
        @JsonProperty("Nightmare")
        List<Integer> nightmare,
        @JsonProperty("Hell")
        List<Integer> hell
) {
    public CompletedQuests {
        try {
            normal.addAll(List.of());
            throw new IllegalArgumentException("Normal should be an immutable list");
        } catch (UnsupportedOperationException uoe) {
            // expected behaviour, valid
        }

        try {
            nightmare.addAll(List.of());
            throw new IllegalArgumentException("Nightmares should be an immutable list");
        } catch (UnsupportedOperationException uoe) {
            // expected behaviour, valid
        }

        try {
            hell.addAll(List.of());
            throw new IllegalArgumentException("Hell should be an immutable list");
        } catch (UnsupportedOperationException uoe) {
            // expected behaviour, valid
        }
    }
}
