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

import io.github.paladijn.d2rcharviewer.calculator.BreakpointCalculator;
import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.ConfigOptions;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rsavegameparser.parser.ParseException;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class SaveGameWatchService {
    private static final Logger log = getLogger(SaveGameWatchService.class);

    private final StatisticsService statisticsService;

    private final DisplayStatsCalculator displayStatsCalculator;

    private Thread pollThread;

    private DisplayStats lastDisplayStats;

    private String savegameLocation;

    private long savegameReadDelayMS;

    public SaveGameWatchService(@ConfigProperty(name = "savegame.location", defaultValue = ".") String savegameLocation,
                                @ConfigProperty(name = "savegame.delay-in-ms", defaultValue = "0") long savegameReadDelayMS,
                                @ConfigProperty(name = "runewords.remove-duplicates", defaultValue = "true") boolean removeDuplicateRuneword,
                                @ConfigProperty(name = "sharedstash.include", defaultValue = "false") boolean includeSharedStash,
                                @ConfigProperty(name = "runes.withX", defaultValue = "false") boolean runesWithX) {
        this.savegameLocation = getSavegameLocation(savegameLocation);
        this.savegameReadDelayMS = savegameReadDelayMS;
        this.displayStatsCalculator = new DisplayStatsCalculator(new BreakpointCalculator(), savegameLocation, new ConfigOptions(removeDuplicateRuneword, includeSharedStash, runesWithX));
        this.statisticsService = new StatisticsService(displayStatsCalculator, savegameLocation);
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
        lastDisplayStats = statisticsService.getStatsForMostRecent();

        final WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();

            final Path dir = Paths.get(savegameLocation);
            dir.register(watcher, ENTRY_MODIFY);
            pollSaveGameFolder(watcher);
        } catch (IOException e) {
            log.error("Problem creating watcher on {}", savegameLocation, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void pollSaveGameFolder(WatchService watcher) throws InterruptedException {
        log.info("started polling for savegame changes");

        WatchKey key;
        while ((key = watcher.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.context().toString().endsWith(".d2s")) {
                    log.info("found savegame file updated {} (delayed: {} ms)", event.context(), savegameReadDelayMS);
                    if (savegameReadDelayMS > 0) {
                        Thread.sleep(savegameReadDelayMS);
                    }
                    try {
                        lastDisplayStats = displayStatsCalculator.getDisplayStats(Path.of(savegameLocation, event.context().toString()));
                    } catch (ParseException pe) {
                        log.error("Could not parse savegame", pe);
                        log.info("awaiting next modification..., set the savegame.delay-in-ms property to fine tune a delay in reading the file.");
                    }
                }
            }
            key.reset();
        }
    }

    private String getSavegameLocation(String location) {
        if (".".equals(location)) {
            String newLocation = System.getenv("USERPROFILE")
                    + File.separator + "Saved Games"
                    + File.separator + "Diablo II Resurrected";
            log.warn("savegame.location property not configured, assuming {} is the location", newLocation);
            return newLocation;
        }
        return location;
    }
}
