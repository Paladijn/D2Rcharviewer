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
import io.github.paladijn.d2rcharviewer.utils.SaveGameFolder;
import io.github.paladijn.d2rsavegameparser.parser.ParseException;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class SaveGameWatchService {
    private static final Logger log = getLogger(SaveGameWatchService.class);

    private final StatisticsService statisticsService;

    private final DisplayStatsCalculator displayStatsCalculator;

    private final GearSyncService gearSyncService;

    private Thread pollThread;

    private DisplayStats lastDisplayStats;

    private String savegameFolder;

    private long savegameReadDelayMS;

    @ConfigProperty(name = "parser.store-on-failure.enabled", defaultValue = "true")
    boolean storeParseFailures;

    public SaveGameWatchService(@ConfigProperty(name = "savegame.location", defaultValue = ".") String savegameLocation,
                                @ConfigProperty(name = "savegame.delay-in-ms", defaultValue = "20") long savegameReadDelayMS,
                                DisplayStatsCalculator displayStatsCalculator,
                                StatisticsService statisticsService,
                                GearSyncService gearSyncService) {
        this.savegameFolder = SaveGameFolder.getSavegameFolder(savegameLocation);
        this.savegameReadDelayMS = savegameReadDelayMS;
        this.displayStatsCalculator = displayStatsCalculator;
        this.statisticsService = statisticsService;
        this.gearSyncService = gearSyncService;
    }

    void onStart(@Observes StartupEvent ev) {
        Runnable task = this::startPolling;
        pollThread = Thread.startVirtualThread(task);
        pollThread.setName("SaveGame watcherThread");
    }

    public DisplayStats getLastDisplayStats() {
        return lastDisplayStats;
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    private void startPolling() {
        log.info("Starting SaveGameWatchService, loading initial savegame stats");
        lastDisplayStats = statisticsService.getStatsForMostRecent(savegameFolder);

        final WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();

            final Path dir = Path.of(savegameFolder);
            dir.register(watcher, ENTRY_MODIFY);
            pollSaveGameFolder(watcher);
        } catch (IOException e) {
            log.error("Problem creating watcher/polling on {}", savegameFolder, e);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    private void pollSaveGameFolder(WatchService watcher) throws InterruptedException, IOException {
        log.info("started polling for savegame changes");

        WatchKey key;
        while ((key = watcher.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.context().toString().endsWith(".d2s")) {
                    log.info("found savegame file updated {} (delayed: {} ms)", event.context(), savegameReadDelayMS);
                    if (savegameReadDelayMS > 0) {
                        Thread.sleep(savegameReadDelayMS);
                    }
                    final Path characterFile = Path.of(savegameFolder, event.context().toString());
                    try {

                        lastDisplayStats = displayStatsCalculator.getDisplayStats(characterFile);
                        log.debug("updated stats for {}, checking if we need to sync", characterFile);
                        gearSyncService.sync(characterFile);
                    } catch (ParseException | NullPointerException | IndexOutOfBoundsException e) {
                        log.error("Could not parse savegame", e);
                        log.info("awaiting next modification..., set the savegame.delay-in-ms property to fine tune a delay in reading the file.");
                        storeSavegamesIfNeeded(e, characterFile);
                    }
                }
            }
            key.reset();
        }
    }

    private void storeSavegamesIfNeeded(RuntimeException e, Path characterFile) throws IOException {
        if (storeParseFailures) {
            // As we had an oopsie, we are going to copy the original file to the broken/ directory, and also store the stacktrace there
            Files.createDirectories(Path.of("broken"));
            final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            final Path destination = Path.of("broken", "%s_%s".formatted(timestamp, characterFile.getFileName()));
            Files.copy(characterFile, destination, StandardCopyOption.REPLACE_EXISTING);
            // Java magix... would be nice to have a helper method for this tbh.
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                Files.writeString(Path.of(
                        "%s_stacktrace.log".formatted(timestamp)),
                        sw.toString(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
            log.warn("Stored broken d2s savegame file at {}, alongside the stacktrace. Please report this to the author, so the issue can be fixed.", destination);
        } else {
            log.info("skipped storing the broken d2s file");
        }
    }
}
