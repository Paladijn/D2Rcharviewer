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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rcharviewer.model.diablorun.CompletedQuests;
import io.github.paladijn.d2rcharviewer.model.diablorun.D2ProcessInfo;
import io.github.paladijn.d2rcharviewer.model.diablorun.DIApplicationInfo;
import io.github.paladijn.d2rcharviewer.model.diablorun.SyncRequest;
import io.github.paladijn.d2rcharviewer.transformer.DiabloRunItemTransformer;
import io.github.paladijn.d2rcharviewer.transformer.DiabloRunMercenaryTransformer;
import io.github.paladijn.d2rsavegameparser.model.D2Character;
import io.github.paladijn.d2rsavegameparser.model.Difficulty;
import io.github.paladijn.d2rsavegameparser.model.Location;
import io.github.paladijn.d2rsavegameparser.parser.CharacterParser;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class DiabloRunSyncService {
    private static final Logger log = getLogger(DiabloRunSyncService.class);

    private final DisplayStatsCalculator displayStatsCalculator;

    private final DiabloRunMercenaryTransformer diabloRunMercenaryTransformer;

    private final DiabloRunItemTransformer diabloRunItemTransformer;

    private final HttpClient httpClient;

    private final CharacterParser characterParser;

    private final ObjectMapper objectMapper;

    @ConfigProperty(name = "gear-sync.enabled", defaultValue = "false")
    boolean isEnabled;

    @ConfigProperty(name = "gear-sync.url", defaultValue = "https://d2armory.littlebluefrog.nl/sync")
    String diabloRunURL;

    @ConfigProperty(name = "gear-sync.apikey", defaultValue = "NoKeySpecified")
    String apiKey;

    @ConfigProperty(name = "gear-sync.equipment-only", defaultValue = "true")
    boolean equipmentOnly;

    @ConfigProperty(name = "gear-sync.ignore-names-that-contain")
    List<String> ignoreNamesThatContain;

    public DiabloRunSyncService(DisplayStatsCalculator displayStatsCalculator,
                                DiabloRunMercenaryTransformer diabloRunMercenaryTransformer,
                                DiabloRunItemTransformer diabloRunItemTransformer,
                                ObjectMapper objectMapper) {
        this.displayStatsCalculator = displayStatsCalculator;
        this.diabloRunMercenaryTransformer = diabloRunMercenaryTransformer;
        this.diabloRunItemTransformer = diabloRunItemTransformer;
        this.httpClient = HttpClient.newHttpClient();
        this.characterParser = new CharacterParser(false);
        this.objectMapper = objectMapper;
    }

    public void sync(final Path characterFile) {
        if (!isEnabled) {
            log.info("Gear sync is disabled, check gear-sync.enabled property");
            return;
        }

        if(ignoreByName(characterFile)) {
            log.info("ignored sync of {} as it matched one of the ignore-names-that-contain filters", characterFile);
            return;
        }

        final SyncRunnable syncRunnable = new SyncRunnable(characterFile);
        final Thread syncThread = Thread.startVirtualThread(syncRunnable);
        syncThread.setName("D2Armory gear sync");
    }


    private boolean ignoreByName(Path characterFile) {
        for (String filter: ignoreNamesThatContain) {
            if (characterFile.getFileName().toString().toLowerCase().contains(filter)) {
                log.debug("matched on {}", filter);
                return true;
            }
        }
        return false;
    }

    private void parseAndSyncLatestCharacter(final Path characterFile) {
        final byte[] allBytes;
        try {
            allBytes = Files.readAllBytes(characterFile);
        } catch (IOException e) {
            log.error("Failed to read characterFile", e);
            return;
        }
        final D2Character d2Character = characterParser.parse(ByteBuffer.wrap(allBytes));

        final SyncRequest syncRequest = createSyncRequest(d2Character);

        log.info("D2Armory gear sync for {}", d2Character.name());

        final String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(syncRequest);
            log.debug("syncrequest =\n{}", requestBody);
        } catch (JsonProcessingException e) {
            log.error("could not write syncRequest json", e);
            return;
        }

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(diabloRunURL))
                .header("Content-Type", "application/json")
                .header("DiabloRun-APIKey", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (log.isDebugEnabled()) {
                log.debug("D2Armory gear sync request: status {}, with body {}", response.statusCode(), response.body());
            }
            if (response.statusCode() >= 300) {
                log.error("Problem connecting to sync [{}] -+> {}", response.statusCode(), response.body());
            }
        } catch (IOException e) {
            log.error("error calling sync", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected SyncRequest createSyncRequest(D2Character d2Character) {
        final DisplayStats displayStats = displayStatsCalculator.getDisplayStats(d2Character);
        final Difficulty difficulty = displayStatsCalculator.getCurrentDifficulty(d2Character.locations());

        // TODO we should hardcode some of these values to constants
        return new SyncRequest("DataRead",
                "API_KEY=",
                new DIApplicationInfo("21.6.16"),
                new D2ProcessInfo("D2R", "1.6.84219", List.of("D2RCharViewer", "1.1.0-SNAPSHOT")),
                0,
                false,
                d2Character.attributes().experience() == 0,
                d2Character.name(),
                "", // this is a UUID, should we keep track of them? The API server seems to ignore the field
                d2Character.characterType().ordinal(),
                d2Character.expansion(),
                d2Character.hardcore(),
                d2Character.attributes().hp() == 0,
                getCurrentAct(d2Character.locations()),
                difficulty.ordinal(),
                null,
                0,
                d2Character.level(),
                d2Character.attributes().experience(),
                displayStats.attributes().strength(),
                displayStats.attributes().dexterity(),
                displayStats.attributes().vitality(),
                displayStats.attributes().energy(),
                displayStats.resistances().fire(),
                displayStats.resistances().cold(),
                displayStats.resistances().lightning(),
                displayStats.resistances().poison(),
                d2Character.attributes().gold(),
                d2Character.attributes().goldInStash(),
                d2Character.attributes().hp(),
                Math.max(d2Character.attributes().hp(), displayStats.maxHP()), // sometimes these have rounding errors, so correct to the max value
                d2Character.attributes().mana(),
                Math.max(d2Character.attributes().mana(), displayStats.maxMana()), // sometimes these have rounding errors, so correct to the max value
                displayStats.breakpoints().fCR(),
                displayStats.breakpoints().fHR(),
                displayStats.fasterRunWalk(),
                displayStats.fasterAttackRate(),
                displayStats.mf(),
                new CompletedQuests(List.of(), List.of(), List.of()),
                null, // not sure what the inventory tab does. active stash tab?
                true, // always clear items
                diabloRunItemTransformer.convertItems(d2Character.items(), equipmentOnly, false, d2Character.level()),
                List.of(),
                diabloRunMercenaryTransformer.convertHireling(d2Character.mercenary(), difficulty)
        );
    }

    private int getCurrentAct(List<Location> locations) {
        for(Location current: locations) {
            if (current.isActive()) {
                return current.currentAct();
            }
        }
        return 0;
    }

    private class SyncRunnable implements Runnable {

        private final Path characterFile;

        public SyncRunnable(final Path characterFile) {
            this.characterFile = characterFile;
        }

        @Override
        public void run() {
            try {
                parseAndSyncLatestCharacter(characterFile);
            } catch (Exception e) {
                log.error("Exception caught while syncing", e);
            }
        }
    }
}
