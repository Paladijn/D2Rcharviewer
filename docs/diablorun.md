# Diablo.run gear upload

The viewer now also supports uploading your gear to https://diablo.run/users and offers more options and values than the original Diablo.run client (which seems better suited to LoD).

## Improvements
* DII:R resistance values
* DII:R fcr/fhr/etc.
* (Mostly) correct values for hp/mana
* Correct values for attributes (str/dex/etc. with bonuses)
* Name Runewords and include their stats (Stealth is named TalEth in the original client)
* Mercenary stats and items
* Cube/inventory/stash items as well (but skip the pots/tomes/scrolls/gems/runes, you can override this)
* Filter on character name (so we don't upload mule characters)

## Setup
Create a token signing in with your Twitch account at https://diablo.run/setup - APIKEY remains identical between sign-out/in, **so don't share it publicly!**  

Copy-paste this token to the application.properties field `diablo-run.apikey=` but perhaps add a lot of empty lines in front if you ever decide to open the file on stream.  
The `diablo-run.enabled=true` setting will start uploading the data each time the savegame is updated (every 5 minutes, death, save&exit, or identifying an item - whichever comes first).  

**Note** if you're also using the Diablo.run client at the same time, please remove the APIKEY there, so it doesn't get synced twice. If you do accidentally leak your APIKEY, please contact the [Diablo.run team](https://diablo.run/team) to have it reset.

## Configuration
The following options can be added to the application.properties

| option                                   | default                     | explanation                                                                                                                                                    |
|------------------------------------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| diablo-run.enabled                       | false                       | if set to false, the stats won't be uploaded to the diablo.run website                                                                                         |
| diablo-run.url                           | https://api.diablo.run/sync | the API endpoint of the diablo.run website, don't change unless you know what you're doing                                                                     |
| diablo-run.apikey                        | NoKeySpecified              | The key you retrieved from https://diablo.run/setup - you may want to hide this off-screen                                                                     |
| diablo-run.equipment-only                | true                        | only uploads equipped items, setting this to false will also upload the stash, cube and inventory (common items such as potions, gems, runes etc. are skipped) |
| diablo-run.ignore-names-that-contain     | []                          | Ignore any file/character names that contain word listed here, useful for skipping test or mule characters                                                     |
| diablo-run.always-share-these-item-codes | []                          | comma-separated list of item codes that you do want to upload, such as `r07` for Tal, and `r16,r14` for your favourite runes                                   |
| diablo-run.translate-base-names          | false                       | Will translate the basenames of items to the `translation.language` value, however this will break images as those require English names                       |
| translation.language                     | enUS                        | Translation of item properties and names, valid values are enUS, zhTW, deDE, esES, frFR, itIT, koKR, plPL, esMX, jaJP, ptBR, ruRU and zhCN                     |