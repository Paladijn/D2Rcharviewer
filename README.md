# character viewer for Diablo II: Resurrected

This character viewer will generate a html representation of your character at http://localhost:8080/stats (you can change the port and host with the configuration options below).  
The output is templated and matches ${fields} which are replaced with the character data. You're free to alter the markup and remove fields you don't want.  

By default the page is refreshed every 5 seconds, but the savegame file is only updates every 5 minutes, when you save&exit, or when you identify an item.

To install the application, unzip the file to a directory of your choice, configure any options to your preference in the config/application.properties file (you can use notepad, or any editor of your choice) and doubleclick the d2charviewer-0.0.1-SNAPSHOT-runner.exe file. The application has no other dependencies.
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

If an error occurs, check if your configuration is correct with the descriptions below and verify the following folders are present in the same folder as the .exe:
- config (contains the application.properties)
- templates (contains the three template files)
- txt (contains the Diablo resources)

Currently, the pages refresh every 5 seconds, if you want it slower (or faster), adjust the following value in the top of each html files:
```html
<meta http-equiv="refresh" content="5">
```

## Configuration
In the config folder you'll find the application.properties which allows the following options:

| option                      | default                  | explanation                                                                                                                                                  |
|-----------------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| quarkus.http.port           | 8080                     | Port on which the server will be reachable, should be > 1024.                                                                                                |
| quarkus.http.host           | 0.0.0.0                  | IP address on which the server is reachable, should be localhost, 0.0.0.0 (all allowed), or your external IP if you want to reach it from the outside world. |
| savegame.location           | -                        | This is where your DII:R savegames are installed, in my case C:\Users\Paladijn\Saved Games\Diablo II Resurrected.                                            |
| sharedstash.include         | false                    | Include items, runes and gold from the shared stash.                                                                                                         |
| runewords.remove-duplicates | true                     | Remove duplicate runewords (if you already wear Stealth, it won't list it).                                                                                  |
| template.character          | templates/character.html | Location of the character template file, if a file is found, the data is mapped in this file.                                                                |
| template.error              | templates/error.html     | Location of the error template - this will indicate there was an issue loading the savegame.                                                                 |
| template.nochars            | templates/noChars.html   | Location of the no characters found template, this will pop up when there are no savegames in the savegame.location                                          |

## Build on your own
While a zip with an executable is offered, you can also build the software on your own. To run it non-native, grab any JVM (Adoptium, Azul, GraalVM are great) and run the application using
```shell
.\mvnw.cmd package quarkus:run
```
You can also run in developer mode with quarkus:dev, but this will impact performance.

If you wish to build the .exe by yourself, follow the instructions [here](https://www.graalvm.org/latest/docs/getting-started/windows/) and install both GraalVM and Visual Studion build tools/Windows SDK if you don't have them.
You can then build the .exe with:
```shell
.\mvnw.cmd package -Dnative
```
This will take about 1-3 minutes depending on your cpu and memory.  
The resulting executable will be available in the target folder.
