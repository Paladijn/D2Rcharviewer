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
package io.github.paladijn.d2rcharviewer.utils;

import org.slf4j.Logger;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

public interface SaveGameFolder {
    static String getSavegameFolder(String location) {
        if (".".equals(location)) {
            String newLocation = System.getenv("USERPROFILE")
                    + File.separator + "Saved Games"
                    + File.separator + "Diablo II Resurrected";
            final Logger log = getLogger(SaveGameFolder.class);
            log.warn("savegame.location property not configured, assuming {} is the location", newLocation);
            return newLocation;
        }
        return location;
    }
}
