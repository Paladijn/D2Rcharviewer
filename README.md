# Character viewer for Diablo II: Resurrected

This character viewer will generate a html representation of your character at http://localhost:8080/stats (you can change the port and host with the configuration options below) with very low cpu and memory usage.

The output is templated and matches ${fields} which are replaced with the character data. You're free to alter the markup and remove fields you don't want. See [templates](docs/templating.md) for more info.  

By default, the page is refreshed every 5 seconds, but the current savegame file only updates:
* every 5 minutes, 
* when you save&exit, 
* when you die,
* or when you identify an item.

To install the application, unzip the file to a directory of your choice, [configure](docs/configuration.md) any options to your preference and doubleclick the d2charviewer.exe file.  
The application has no other dependencies, but needs the config, txt and templates folder in the same location.

## Microsoft Defender Anti-virus warning
Microsoft's anti-virus tool sometimes considers the application to be a potential virus referring it to Program:Win32/Wacapew.C!ml -> [more details about this on microsoft.com](https://www.microsoft.com/en-us/wdsi/threats/malware-encyclopedia-description?Name=Program:Win32/Wacapew.C!ml) which seems to be caused by the application containing code to connect to the Diablo II Armory / Diablo.run to update the players gear, which obviously is intended behaviour (you can switch this feature off with `gear-sync.enabled=false` in the config/application.properties file, but that won't prevent the virus popup). It can also be caused by the application looking up the user folder as that's where the savegames are stored - this again is legitimate behaviour.   

Should this occur, then either allow it directly in the popup, or go to `virus and threat protection` in the settings. The `Current threats` has a link to `protection history` where you can select the most recent `threat` which should display `D2RCharViewer.exe` and allow it on your PC. 

## In case the savegame folder is not auto-detected
The app should pick the right savegame folder, but if you receive an error about this edit the config/application.properties and add ```savegame.location=C:/Users/Paladijn/Saved Games/Diablo II Resurrected``` where you replace ```Paladijn``` with your Windows user name.

## Detailed documentation  
* [Configuration options](docs/configuration.md)  
* [Templating](docs/templating.md)  
* [Gear-sync configuration](docs/gear-sync.md) to sync your gear to D2Armory.LittleBlueFrog.nl
* [Build on your own](docs/build.md) in case you don't want to use the supplied zip

If all is configured well, you'll see the following:
```logcatfilter
[io.git.pal.d2c.ser.SaveGameWatchService] (SaveGame watcherThread) Starting SaveGameWatchService, loading initial savegame stats
[io.git.pal.d2c.ser.StatisticsService] (SaveGame watcherThread) found C:/Users/Paladijn/Saved Games/Diablo II Resurrected/Hamers.d2s
[io.quarkus] (main) d2charviewer 0.0.1-SNAPSHOT native (powered by Quarkus 3.10.0) started in 0.074s. Listening on: http://0.0.0.0:8081
[io.quarkus] (main) Profile prod activated. 
[io.quarkus] (main) Installed features: [cdi, rest, smallrye-context-propagation, vertx]
[io.git.pal.d2c.ser.SaveGameWatchService] (SaveGame watcherThread) started polling for savegame changes
```
As you can see it is quite fast starting up, it also uses almost no cpu% and between 15-50mb of RAM.

In case your savegame folder is not auto-detected, add it to config/application.properties by hand and make sure to use / (or \\) instead of \. For example, mine is:
```savegame.location=C:/Users/Paladijn/Saved Games/Diablo II Resurrected```

If an error occurs, check if your configuration is correct with the descriptions in [Configuration options](docs/configuration.md) and verify the following folders are present in the same folder as the .exe:
- config (contains the application.properties)
- templates (contains the three template files)
- txt (contains the Diablo resources)
