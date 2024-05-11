/*
   Copyright 2024 Paladijn (paladijn2960+d2rsavegameparser@gmail.com)

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
package io.github.paladijn.d2rcharviewer.model;

import io.github.paladijn.d2rsavegameparser.model.CharacterType;

public record DisplayStats(String name, CharacterType type, int level, boolean isHardcore, String percentToNext, DisplayAttributes attributes, Resistances resistances, Breakpoints breakpoints, int fasterRunWalk, int mf, int gf, String gold, String goldInStash, String runes, String runewords, String lastUpdated) { }
