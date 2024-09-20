# Configuration options
In the config folder you'll find the application.properties which allows the following options. You can edit this with notepad, or any editor of your choice.

| option                      | default                  | explanation                                                                                                                                                  |
|-----------------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| quarkus.http.port           | 8080                     | Port on which the server will be reachable, should be > 1024.                                                                                                |
| quarkus.http.host           | 0.0.0.0                  | IP address on which the server is reachable, should be localhost, 0.0.0.0 (all allowed), or your external IP if you want to reach it from the outside world. |
| savegame.location           | -                        | This is where your DII:R savegames are installed, in my case C:\Users\Paladijn\Saved Games\Diablo II Resurrected.                                            |
| savegame.delay-in-ms        | 0                        | Optional delay in milliseconds in case your savegames are read too fast                                                                                      |
| sharedstash.include         | false                    | Include items, runes and gold from the shared stash.                                                                                                         |
| runewords.remove-duplicates | true                     | Remove duplicate runewords (if you already wear Stealth, it won't list it).                                                                                  |
| runes.withX                 | false                    | Displays rune amount as Tal x3 instead of Tal (3)                                                                                                            |
| template.character          | templates/character.html | Location of the character template file, if a file is found, the data is mapped in this file.                                                                |
| template.error              | templates/error.html     | Location of the error template - this will indicate there was an issue loading the savegame.                                                                 |
| template.nochars            | templates/noChars.html   | Location of the no characters found template, this will pop up when there are no savegames in the savegame.location                                          |
