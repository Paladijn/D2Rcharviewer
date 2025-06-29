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

import io.github.paladijn.d2rsavegameparser.model.ItemQuality;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
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
