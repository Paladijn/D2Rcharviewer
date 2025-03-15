package io.github.paladijn.d2rcharviewer.service;

import io.github.paladijn.d2rcharviewer.calculator.DisplayStatsCalculator;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rcharviewer.resource.DiabloRunRestClient;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class DiabloRunSyncService {
    private static final Logger log = getLogger(DiabloRunSyncService.class);

    private final DisplayStatsCalculator displayStatsCalculator;

    private final DiabloRunRestClient diabloRunRestClient;

    @ConfigProperty(name = "diablo-run.enabled", defaultValue = "false")
    boolean isEnabled;

    @ConfigProperty(name = "diablo-run.apikey", defaultValue = "NoKeySpecified")
    String apiKey;

    @ConfigProperty(name = "diablo-run.ignore-names-that-contain")
    List<String> ignoreNamesThatContain;

    public DiabloRunSyncService(DisplayStatsCalculator displayStatsCalculator,
                                @RestClient DiabloRunRestClient diabloRunRestClient) {
        this.displayStatsCalculator = displayStatsCalculator;
        this.diabloRunRestClient = diabloRunRestClient;
    }

    public void sync(final Path characterFile) {
        // TODO skip when enabled = false
        if(ignoreByName(characterFile)) {
            log.info("ignored sync of {} as it matched one of the ignore-names-that-contain filters", characterFile);
            return;
        }

        final DisplayStats displayStats = displayStatsCalculator.getDisplayStats(characterFile);

        log.info("Diablo.run sync for {}, enabled: {}", displayStats.name(), isEnabled);

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
