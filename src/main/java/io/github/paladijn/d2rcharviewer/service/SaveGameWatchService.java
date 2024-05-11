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
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

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

    @ConfigProperty(name = "savegame.location")
    private String savegameLocation;

    @Inject
    private StatisticsService statisticsService;

    @Inject
    private DisplayStatsCalculator displayStatsCalculator;

    private Thread pollThread;

    private DisplayStats lastDisplayStats;

    void onStart(@Observes StartupEvent ev) {
        Runnable task = this::pollSaveGameChanges;
        pollThread = Thread.startVirtualThread(task);
        pollThread.setName("SaveGame watcherThread");
    }

    public DisplayStats getLastDisplayStats() {
        return lastDisplayStats;
    }

    private void pollSaveGameChanges() {
        log.info("Starting SaveGameWatchService, loading initial savegame stats");
        lastDisplayStats = statisticsService.getStatsForMostRecent();

        final WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();

            final Path dir = Paths.get(savegameLocation);
            dir.register(watcher, ENTRY_MODIFY);
            log.info("started polling for savegame changes");

            WatchKey key;
            while ((key = watcher.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().endsWith(".d2s")) {
                        log.info("found savegame file updated {}", event.context());
                        lastDisplayStats = displayStatsCalculator.getDisplayStats(Path.of(savegameLocation, event.context().toString()));
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
