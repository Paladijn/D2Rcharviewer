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
package io.github.paladijn.d2rcharviewer.resource;

import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rcharviewer.service.SaveGameWatchService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Path("/stats")
public class StatisticsController {
    private static final Logger log = getLogger(StatisticsController.class);

    @ConfigProperty(name = "template.character", defaultValue = "templates/character.html")
    private String characterTemplate;

    @ConfigProperty(name = "template.error", defaultValue = "templates/error.html")
    private String errorTemplate;

    @ConfigProperty(name = "template.nochars", defaultValue = "templates/noChars.html")
    private String noCharsTemplate;

    private final SaveGameWatchService saveGameWatchService;

    public StatisticsController(SaveGameWatchService saveGameWatchService) {
        this.saveGameWatchService = saveGameWatchService;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getInfo() {
        log.info("attempting to load latest savegame");

        final DisplayStats statsForMostRecent = saveGameWatchService.getLastDisplayStats();
        if (statsForMostRecent == null) {
            return readFileContents(noCharsTemplate);
        }

        log.debug("character stats: {}", statsForMostRecent);

        try (InputStream resource = new FileInputStream(characterTemplate)) {
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines()
                    .map(line -> mapValues(line, statsForMostRecent))
                    .collect(Collectors.joining());
        } catch (IOException ioe) {
            log.error("issue reading the stats", ioe);
            return readFileContents(errorTemplate);
        }
    }

    private String readFileContents(String location) {
        try (InputStream resource = new FileInputStream(location)) {
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String mapValues(String line, DisplayStats statsForMostRecent) {
        line = line.replace("${name}", String.format("%s", statsForMostRecent.name()));
        line = line.replace("${percentToNext}", String.format("%s", statsForMostRecent.percentToNext()));
        line = line.replace("${hardcore}", String.format("%s", statsForMostRecent.isHardcore() ? "Hardcore" : ""));

        line = line.replace("${level}", String.format("%d", statsForMostRecent.level()));
        line = line.replace("${gold}", String.format("%s", statsForMostRecent.gold()));
        line = line.replace("${goldInStash}", String.format("%s", statsForMostRecent.goldInStash()));
        line = line.replace("${mf}", String.format("%d", statsForMostRecent.mf()));
        line = line.replace("${gf}", String.format("%d", statsForMostRecent.gf()));

        line = line.replace("${attributes.strength}", String.format("%d", statsForMostRecent.attributes().strength()));
        line = line.replace("${attributes.dexterity}", String.format("%d", statsForMostRecent.attributes().dexterity()));
        line = line.replace("${attributes.vitality}", String.format("%d", statsForMostRecent.attributes().vitality()));
        line = line.replace("${attributes.energy}", String.format("%d", statsForMostRecent.attributes().energy()));

        line = line.replace("${resistances.fire}", String.format("%d", statsForMostRecent.resistances().fire()));
        line = line.replace("${resistances.lightning}", String.format("%d", statsForMostRecent.resistances().lightning()));
        line = line.replace("${resistances.cold}", String.format("%d", statsForMostRecent.resistances().cold()));
        line = line.replace("${resistances.poison}", String.format("%d", statsForMostRecent.resistances().poison()));

        line = line.replace("${breakpoints.fCR}", String.format("%d", statsForMostRecent.breakpoints().fCR()));
        line = line.replace("${breakpoints.nextFCR}", String.format("%d", statsForMostRecent.breakpoints().nextFCR()));
        line = line.replace("${breakpoints.fBR}", String.format("%d", statsForMostRecent.breakpoints().fBR()));
        line = line.replace("${breakpoints.nextFBR}", String.format("%d", statsForMostRecent.breakpoints().nextFBR()));
        line = line.replace("${breakpoints.fHR}", String.format("%d", statsForMostRecent.breakpoints().fHR()));
        line = line.replace("${breakpoints.nextFHR}", String.format("%d", statsForMostRecent.breakpoints().nextFHR()));

        line = line.replace("${fasterRunWalk}", String.format("%d", statsForMostRecent.fasterRunWalk()));

        line = line.replace("${runes}", String.format("%s", statsForMostRecent.runes()));
        line = line.replace("${runewords}", String.format("%s", statsForMostRecent.runewords()));
        line = line.replace("${lastUpdated}", String.format("%s", statsForMostRecent.lastUpdated()));

        line = line.replace("${keys-of-terror}", String.format("%s", statsForMostRecent.keys().terror()));
        line = line.replace("${keys-of-hate}", String.format("%s", statsForMostRecent.keys().hate()));
        line = line.replace("${keys-of-destruction}", String.format("%s", statsForMostRecent.keys().destruction()));

        return line;
    }
}
