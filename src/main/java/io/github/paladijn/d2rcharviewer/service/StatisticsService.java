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
package io.github.paladijn.d2rcharviewer.service;

import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rsavegameparser.parser.ParseException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class StatisticsService {
    private static final Logger log = getLogger(StatisticsService.class);

    private final DisplayStatsCalculator calculator;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    public StatisticsService(DisplayStatsCalculator displayStatsCalculator) {
        this.calculator = displayStatsCalculator;
        log.debug("Using TimeZone {}", ZoneId.systemDefault());
    }

    public DisplayStats getStatsForMostRecent(String location){
        final Path dir = Paths.get(location);
        try (Stream<Path> pathStream = Files.list(dir)) {
            final Optional<Path> lastUpdatedSaveGame = pathStream
                    .filter(file -> !Files.isDirectory(file) && file.toString().endsWith(".d2s"))
                    .max(Comparator.comparingLong(file -> file.toFile().lastModified()));

            if (lastUpdatedSaveGame.isEmpty()) {
                log.info("No recent saveGame found in {}", location);
                return null;
            }

            final Path characterFile = lastUpdatedSaveGame.get();
            log.info("Found {}", characterFile);
            return calculator.getDisplayStats(characterFile);
        } catch (IOException e) {
            log.error("problem listing savegame files", e);
        } catch (ParseException pe) {
            log.error("Could not parse savegame", pe);
        }
        return null;
    }

    public String replaceValues(final String characterOutput, final DisplayStats statsForMostRecent) {
        final StringBuilder sbResult = new StringBuilder();
        int currentIndex = 0;
        int nextTokenIndex = characterOutput.indexOf("${");
        if (nextTokenIndex == -1) {
            log.warn("could not find tokens to replace, returning original");
            return characterOutput;
        }

        do {
            sbResult.append(characterOutput, currentIndex, nextTokenIndex);

            int nextClosingBrace = characterOutput.indexOf("}", nextTokenIndex);
            if (nextClosingBrace == -1) {
                throw new RuntimeException("Could not find closing brace for token starting at index " + nextTokenIndex);
            }

            sbResult.append(mapValue(characterOutput.substring(nextTokenIndex + 2, nextClosingBrace), statsForMostRecent));

            currentIndex = nextClosingBrace + 1;
            nextTokenIndex = characterOutput.indexOf("${", nextClosingBrace);
        } while (nextTokenIndex != -1);
        // append the leftover
        sbResult.append(characterOutput.substring(currentIndex));

        return sbResult.toString();
    }

    private String mapValue(final String token, final DisplayStats statsForMostRecent) {
        return switch (token) {
            case "name" -> statsForMostRecent.name();
            case "percentToNext" -> statsForMostRecent.percentToNext();
            case "hardcore" -> statsForMostRecent.isHardcore() ? "Hardcore" : "";
            case "level" -> String.valueOf(statsForMostRecent.level());
            case "gold" -> statsForMostRecent.gold();
            case "goldInStash" -> statsForMostRecent.goldInStash();
            case "mf" -> String.valueOf(statsForMostRecent.mf());
            case "gf" -> String.valueOf(statsForMostRecent.gf());
            case "attributes.strength" -> String.valueOf(statsForMostRecent.attributes().strength());
            case "attributes.dexterity" -> String.valueOf(statsForMostRecent.attributes().dexterity());
            case "attributes.vitality" -> String.valueOf(statsForMostRecent.attributes().vitality());
            case "attributes.energy" -> String.valueOf(statsForMostRecent.attributes().energy());
            case "resistances.fire" -> String.valueOf(statsForMostRecent.resistances().fire());
            case "resistances.lightning" -> String.valueOf(statsForMostRecent.resistances().lightning());
            case "resistances.cold" -> String.valueOf(statsForMostRecent.resistances().cold());
            case "resistances.poison" -> String.valueOf(statsForMostRecent.resistances().poison());
            case "resistances.physical" -> String.valueOf(statsForMostRecent.resistances().physical());
            case "breakpoints.fCR" -> String.valueOf(statsForMostRecent.breakpoints().fCR());
            case "breakpoints.nextFCR" -> String.valueOf(statsForMostRecent.breakpoints().nextFCR());
            case "breakpoints.fHR" -> String.valueOf(statsForMostRecent.breakpoints().fHR());
            case "breakpoints.nextFHR" -> String.valueOf(statsForMostRecent.breakpoints().nextFHR());
            case "breakpoints.fBR" -> String.valueOf(statsForMostRecent.breakpoints().fBR());
            case "breakpoints.nextFBR" -> String.valueOf(statsForMostRecent.breakpoints().nextFBR());
            case "fasterRunWalk" -> String.valueOf(statsForMostRecent.fasterRunWalk());
            case "runes" -> statsForMostRecent.runes();
            case "runewords" -> statsForMostRecent.runewords();
            case "lastUpdated" -> dtf.format(statsForMostRecent.lastUpdated());
            case "lastUpdatedAgo" -> getLastUpdatedAgo(statsForMostRecent);
            case "keys.terror" -> String.valueOf(statsForMostRecent.keys().terror());
            case "keys.hate" -> String.valueOf(statsForMostRecent.keys().hate());
            case "keys.destruction" -> String.valueOf(statsForMostRecent.keys().destruction());
            case "speedrun.fullRejuvs" -> String.valueOf(statsForMostRecent.speedRunItems().fullRejuvs());
            case "speedrun.smallRejuvs" -> String.valueOf(statsForMostRecent.speedRunItems().smallRejuvs());
            case "speedrun.chippedGems" -> String.valueOf(statsForMostRecent.speedRunItems().chippedGems());
            default -> "${" + token + "}";
        };
    }

    private String getLastUpdatedAgo(DisplayStats statsForMostRecent) {
        Duration duration = Duration.between(statsForMostRecent.lastUpdated(), Instant.now());
        if (duration.getSeconds() < 60) {
            return "%ds".formatted(duration.getSeconds());
        }
        return "%d:%02d".formatted(duration.toMinutesPart(), duration.toSecondsPart());
    }
}
