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
import io.github.paladijn.d2rcharviewer.model.Constants;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rcharviewer.model.diablorun.CompletedQuests;
import io.github.paladijn.d2rcharviewer.model.diablorun.D2ProcessInfo;
import io.github.paladijn.d2rcharviewer.model.diablorun.DIApplicationInfo;
import io.github.paladijn.d2rcharviewer.model.diablorun.DRUNItemQuality;
import io.github.paladijn.d2rcharviewer.model.diablorun.Hireling;
import io.github.paladijn.d2rcharviewer.model.diablorun.ItemPayload;
import io.github.paladijn.d2rcharviewer.model.diablorun.SyncRequest;
import io.github.paladijn.d2rsavegameparser.model.D2Character;
import io.github.paladijn.d2rsavegameparser.model.Difficulty;
import io.github.paladijn.d2rsavegameparser.model.Item;
import io.github.paladijn.d2rsavegameparser.model.ItemLocation;
import io.github.paladijn.d2rsavegameparser.model.Mercenary;
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
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class DiabloRunSyncService {
    private static final Logger log = getLogger(DiabloRunSyncService.class);

    private final DisplayStatsCalculator displayStatsCalculator;

    private final TranslationService translationService;

    private final HttpClient httpClient;

    private final CharacterParser characterParser;

    private final ObjectMapper objectMapper;

    @ConfigProperty(name = "diablo-run.enabled", defaultValue = "false")
    boolean isEnabled;

    @ConfigProperty(name = "diablo-run.url", defaultValue = "https://api.diablo.run/sync")
    String diabloRunURL;

    @ConfigProperty(name = "diablo-run.apikey", defaultValue = "NoKeySpecified")
    String apiKey;

    @ConfigProperty(name = "diablo-run.equipment-only", defaultValue = "true")
    boolean equipmentOnly;

    @ConfigProperty(name = "diablo-run.ignore-names-that-contain")
    List<String> ignoreNamesThatContain;

    public DiabloRunSyncService(DisplayStatsCalculator displayStatsCalculator,
                                TranslationService translationService,
                                ObjectMapper objectMapper) {
        this.displayStatsCalculator = displayStatsCalculator;
        this.translationService = translationService;
        this.httpClient = HttpClient.newHttpClient();
        this.characterParser = new CharacterParser(false);
        this.objectMapper = objectMapper;
    }

    public void sync(final Path characterFile) {
        if (!isEnabled) {
            log.info("Diablo.run sync disabled");
            return;
        }

        if(ignoreByName(characterFile)) {
            log.info("ignored sync of {} as it matched one of the ignore-names-that-contain filters", characterFile);
            return;
        }

        // TODO this should be run in a virtual thread (reading the file, collecting stats, sending sync request in one go)

        final byte[] allBytes;
        try {
            allBytes = Files.readAllBytes(characterFile);
        } catch (IOException e) {
            log.error("Failed to read characterFile", e);
            return;
        }
        final D2Character d2Character = characterParser.parse(ByteBuffer.wrap(allBytes));

        final SyncRequest syncRequest = createSyncRequest(d2Character);

        log.info("Diablo.run sync for {}", d2Character.name());

        final String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(syncRequest);
            log.info("syncrequest =\n{}", requestBody);
        } catch (JsonProcessingException e) {
            log.error("could not write syncRequest json", e);
            return;
        }

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(diabloRunURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Sync request status {}, with body {}", response.statusCode(), response.body());
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
                "API_KEY=%s".formatted(apiKey),
                new DIApplicationInfo("21.6.16"),
                new D2ProcessInfo("D2R", "d2s", List.of()),
                0,
                false,
                d2Character.attributes().experience() == 0,
                d2Character.name(),
                "", // this is a UUID, should we keep track of them? The API server seems to ignore the field
                d2Character.characterType().ordinal(),
                d2Character.expansion(),
                d2Character.hardcore(),
                d2Character.died(),
                0,
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
                d2Character.attributes().maxHP(),
                d2Character.attributes().mana(),
                d2Character.attributes().maxMana(),
                displayStats.breakpoints().fCR(),
                displayStats.breakpoints().fHR(),
                displayStats.fasterRunWalk(),
                displayStats.fasterAttackRate(),
                displayStats.mf(),
                new CompletedQuests(List.of(), List.of(), List.of()),
                null, // inventorytab?
                true, // always clear items
                convertItems(d2Character.items(), equipmentOnly),
                List.of(),
                convertHireling(d2Character.mercenary(), difficulty)
        );
    }

    private List<ItemPayload> convertItems(List<Item> items, boolean equippedOnly) {
        final List<ItemPayload> results = new ArrayList<>();
        for (Item item: items) {
            if (equippedOnly && item.location() != ItemLocation.EQUIPPED) {
                continue;
            }
            results.add(new ItemPayload(
                    item.guid() == null ? 0 : Integer.parseInt(item.guid()),
                    0, // check d2s what this value is, could be class restricted?
                    item.type(),
                    item.itemName(),
                    DRUNItemQuality.fromParsed(item.quality()),
                    List.of(),
                    new io.github.paladijn.d2rcharviewer.model.diablorun.ItemLocation(
                            item.x(),
                            item.y(),
                            1, // width
                            1, // height
                            item.position().ordinal(),
                            item.container().ordinal()
                    )));
        }
        return List.copyOf(results);
    }

    private Hireling convertHireling(Mercenary mercenary, Difficulty difficulty) {
        if (mercenary == null) {
            return null;
        }

        String name = getMercenaryName(mercenary.nameId(), mercenary.typeId());
        int level = getLevelByXP(mercenary.experience());
        int strength = calculateStrengthByType(mercenary.typeId(), level);
        int dexterity = calculateDexByType(mercenary.typeId(), level);
        int fireRes = mercenaryResistanceByLevel(level, difficulty, "fire", mercenary.items());
        int coldRes = mercenaryResistanceByLevel(level, difficulty, "cold", mercenary.items());
        int lightRes = mercenaryResistanceByLevel(level, difficulty, "light", mercenary.items());
        int poisRes = mercenaryResistanceByLevel(level, difficulty, "poison", mercenary.items());
        // note: items are currently added to the player character, so don't send those.
        return new Hireling(name, mercenary.typeId(), level, mercenary.experience(), strength, dexterity, fireRes, coldRes, lightRes, poisRes, List.of(), List.of());
    }

    private int calculateStrengthByType(short typeId, int level) {
        if (typeId < 6) {
            return 32 + level;
        }
        if (typeId < 15) {
            return 41 + (2 * level);
        }
        if (typeId < 24) {
            return 31 + level;
        }
        if (typeId < 30) {
            return 49 + (2 * level);
        }
        return 0;
    }

    private int calculateDexByType(short typeId, int level) {
        if (typeId < 6) {
            return 39 + (2 * level);
        }
        if (typeId < 15) {
            return 26 + (2 * level);
        }
        if (typeId < 24) {
            return 25 + level;
        }
        if (typeId < 30) {
            return 29 + level;
        }
        return 0;
    }

    private int mercenaryResistanceByLevel(int level, Difficulty difficulty, String type, List<Item> equippedItems) {
        int baseRes = (level - 3) * 2;
        if (level < 4) {
            baseRes = 0;
        }

        final int sum = baseRes + displayStatsCalculator.getTotalPointsInProperty(type + "resist", equippedItems, List.of());
        final int max = Math.min(95, 75 + displayStatsCalculator.getTotalPointsInProperty("max" + type + "resist", equippedItems, List.of()));

        return Math.min(max, switch (difficulty) {
                case NORMAL -> sum;
                case NIGHTMARE -> sum - 40;
                case HELL -> sum - 100;
            }
        );
    }

    private String getMercenaryName(short nameId, short typeId) {
        if (typeId < 6) {
            return translationService.getTranslationByKey("merc%02d".formatted(1 + nameId));
        }
        if (typeId < 15) {
            return translationService.getTranslationByKey("merca" + (201 + nameId));
        }
        if (typeId < 24) {
            return translationService.getTranslationByKey("merca" + (222 + nameId));
        }
        if (typeId < 30) {
            return translationService.getTranslationByKey("Merc" + (101 + nameId));
        }
        return "N.N.";
    }


    private int getLevelByXP(int experience) {
        // FIXME this is not fully correct as the merc has different XP levels, for example lvl 6 ends at 41.160
        int level = 1;
        while (Constants.xpLevels[level] < experience) {
            level++;
            if (level > 99) {
                break;
            }
        }
        return level - 1;
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
}
