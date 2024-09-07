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
import io.github.paladijn.d2rcharviewer.service.StatisticsService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

    private final StatisticsService statisticsService;

    private final SaveGameWatchService saveGameWatchService;

    public StatisticsController(SaveGameWatchService saveGameWatchService, StatisticsService statisticsService) {
        this.saveGameWatchService = saveGameWatchService;
        this.statisticsService = statisticsService;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getInfo() {
        log.debug("attempting to load latest statistics");

        final DisplayStats statsForMostRecent = saveGameWatchService.getLastDisplayStats();
        if (statsForMostRecent == null) {
            return readFileContents(noCharsTemplate);
        }

        log.debug("character stats: {}", statsForMostRecent);

        return statisticsService.replaceValues(
                readFileContents(characterTemplate),
                statsForMostRecent);
    }

    @GET()
    @Path("json")
    @Produces(MediaType.APPLICATION_JSON)
    public DisplayStats getJSON() {

        final DisplayStats statsForMostRecent = saveGameWatchService.getLastDisplayStats();
        if (statsForMostRecent == null) {
            throw new NotFoundException();
        }
        return statsForMostRecent;
    }

    private String readFileContents(String location) {
        try {
            return Files.readString(java.nio.file.Path.of(location), StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            log.error("Issue reading file {}", location, ioe);
            throw new RuntimeException(ioe);
        }
    }
}
