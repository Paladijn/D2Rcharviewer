package io.github.paladijn.d2rcharviewer.utils;

import org.slf4j.Logger;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

public interface SaveGameFolder {
    static String getSavegameFolder(String location) {
        if (".".equals(location)) {
            String newLocation = System.getenv("USERPROFILE")
                    + File.separator + "Saved Games"
                    + File.separator + "Diablo II Resurrected";
            final Logger log = getLogger(SaveGameFolder.class);
            log.warn("savegame.location property not configured, assuming {} is the location", newLocation);
            return newLocation;
        }
        return location;
    }
}
