# Diablo II Armory gear upload

N.B. this used to describe syncing to https://diablo.run/, however that site will be decommissioned soon (summer '25). This has been replaced with the _d2armory_ website.

The viewer now also supports uploading your gear to https://d2armory.littlebluefrog.nl/ and offers more options and values than the original Diablo.run client (which seems better suited to LoD). For an overview of currently active profiles and their gear, check [this page](https://d2armory.littlebluefrog.nl/profiles/).

## Improvements
* DII:R resistance values
* DII:R fcr/fhr/etc.
* (Mostly) correct values for hp/mana
* Correct values for attributes (str/dex/etc. with bonuses)
* Name Runewords and include their stats (Stealth is named TalEth in the original client)
* Mercenary stats and items
* Cube/inventory/stash items as well (but skip the pots/tomes/scrolls/gems/runes, you can override this)
* Current Act the player is in
* Missing item images have been restored
* Filter on character name (so we don't upload mule characters)

## Setup
Request an API-key at https://d2armory.littlebluefrog.nl/signup/ - APIKEY allows others to upload on your profile, **so don't share it publicly!**  

Copy-paste this token to the application.properties field `gear-sync.apikey=` but perhaps add a lot of empty lines in front if you ever decide to open the file on stream.  
The `gear-sync.enabled=true` setting will start uploading the data each time the savegame is updated (every 5 minutes, death, save&exit, or identifying an item - whichever comes first).  

## Configuration
The following options can be added to the application.properties

| option                                  | default                                  | explanation                                                                                                                                              |
|-----------------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| gear-sync.enabled                       | false                                    | If set to false, the stats won't be uploaded to the diablo.run website                                                                                   |
| gear-sync.url                           | https://d2armory.littlebluefrog.nl/sync/ | The API endpoint of the diablo.run website, don't change unless you know what you're doing                                                               |
| gear-sync.apikey                        | NoKeySpecified                           | The key you requested from https://d2armory.littlebluefrog.nl or Diablo.run, whichever you use - you may want to hide this off-screen                    |
| gear-sync.equipment-only                | true                                     | Only uploads equipped items, setting this to false will also upload the stash, cube and inventory (common items such as potions, gems, etc. are skipped) |
| gear-sync.ignore-names-that-contain     | []                                       | Ignore any file/character names that contain word listed here, useful for skipping test or mule characters                                               |
| gear-sync.always-share-these-item-codes | []                                       | Comma-separated list of item codes that you do want to upload, such as `r07` for Tal, and `r16,r14` for your favourite runes                             |
| gear-sync.translate-base-names          | false                                    | Will translate the basenames of items to the `translation.language` value, however this will break images as those require English names                 |
| translation.language                    | enUS                                     | Translation of item properties and names, valid values are enUS, zhTW, deDE, esES, frFR, itIT, koKR, plPL, esMX, jaJP, ptBR, ruRU and zhCN               |
