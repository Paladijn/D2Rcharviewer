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
package io.github.paladijn.d2rcharviewer.service;

import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class StatisticsService {
    private static final Logger log = getLogger(StatisticsService.class);

    @ConfigProperty(name = "savegame.location")
    private String savegameLocation;

    private final DisplayStatsCalculator calculator;

    public StatisticsService(DisplayStatsCalculator displayStatsCalculator) {
        this.calculator = displayStatsCalculator;
    }

    public DisplayStats getStatsForChar(String characterName) {
        final Path characterFile = Path.of(savegameLocation, characterName + ".d2s");
        return calculator.getDisplayStats(characterFile);
    }


    public DisplayStats getStatsForMostRecent(){
        final Path dir = Paths.get(savegameLocation);
        try (Stream<Path> pathStream = Files.list(dir)) {
            Optional<Path> lastUpdatedSaveGame = pathStream
                    .filter(file -> !Files.isDirectory(file) && file.toString().endsWith(".d2s"))
                    .max(Comparator.comparingLong(file -> file.toFile().lastModified()));

            if (lastUpdatedSaveGame.isEmpty()) {
                return null;
            }

            final Path characterFile = lastUpdatedSaveGame.get();
            log.info("found {}", characterFile);
            return calculator.getDisplayStats(characterFile);
        } catch (IOException e) {
            log.error("problem listing savegame files", e);
            return null;
        }
    }
}
