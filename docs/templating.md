# Templating
You can template the output in html to fit your needs. It is preferred to make a copy of an existing template rather than editing one as they may be overwritten during an update.

You'll need to configure at least three templates (defaults are supplied):

| option                      | default                  | explanation                                                                                                                                                  |
|-----------------------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| template.character          | templates/character.html | Location of the character template file, if a file is found, the data is mapped in this file.                                                                |
| template.error              | templates/error.html     | Location of the error template - this will indicate there was an issue loading the savegame.                                                                 |
| template.nochars            | templates/noChars.html   | Location of the no characters found template, this will pop up when there are no savegames in the savegame.location                                          |

You can simply copy an existing template and remove any unwanted fields, or create your own using your favourite editor (that includes Notepad.exe, Llama). Any token between ${...} will be replaced. If you made a typo, or used the wrong CapS, it also won't be replaced.
> [!CAUTION]  
> Editing the original files may result in your changes being overwritten when you update the application.

Keep in mind that to refresh the html you'll need to leave this in (or use an alternative):
Currently, the pages refresh every 5 seconds, if you want it slower (or faster), adjust the following value in the top of each html files:
```html
<meta http-equiv="refresh" content="5">
```

# Images
You can add images to the output using the html `<img src="">` tag either linking to one hosted elsewhere, or by placing them in the templates/images/ folder. Subfolders are supported, so for example `templates/images/subfolder/2.jpg` can be displayed using `<img src="images/subfolder/2.jpg">`. For security reasons, only templates/images and its subfolders are supported to host image files directly.  
If you're looking for an example, both the default `noChars.html` and `error.html` in the templates folder have an image.

# CSS styling
Separate .css files can be hosted in templates/css/ and its subfolders. To use them link to `css/`.

# JavaScript files
Separate javascript files can be hosted in templates/js/ and its subfolders. To use them link to `js/`, just make sure they work well in the OBS browser window.

# Available tokens
| token                 | type   | notes                                                                                         |
|-----------------------|--------|-----------------------------------------------------------------------------------------------|
| name                  | string |                                                                                               |
| percentToNext         | string | percentage to next level, formatted as a string, without the %                                |
| hardcore              | string | hardcore if hardcore character, empty string otherwise                                        |
| level                 | number |                                                                                               |
| gold                  | string | number is rounded > 1000 to 1K etc.                                                           |
| goldInStash           | string | number is rounded > 1000 to 1K etc.                                                           |
| mf                    | number | magic find %                                                                                  |
| gf                    | number | gold find %                                                                                   |
| attributes.strength   | number |                                                                                               |
| attributes.dexterity  | number |                                                                                               |
| attributes.vitality   | number |                                                                                               |
| attributes.energy     | number |                                                                                               |
| resistances.fire      | number |                                                                                               |
| resistances.lightning | number |                                                                                               |
| resistances.cold      | number |                                                                                               |
| resistances.poison    | number |                                                                                               |
| resistances.physical  | number |                                                                                               |
| breakpoints.fCR       | number | current Faster Cast Rate breakpoint                                                           |
| breakpoints.nextFCR   | number | next Faster Cast Rate breakpoint                                                              |
| breakpoints.fHR       | number | current Faster Hit Recovery breakpoint                                                        |
| breakpoints.nextFHR   | number | next Faster Hit Recovery breakpoint                                                           |
| breakpoints.fBR       | number | current Faster Block Rate breakpoint                                                          |
| breakpoints.nextFBR   | number | next Faster Block Rate breakpoint                                                             |
| fasterRunWalk         | number | faster run walk %                                                                             |
| runes                 | string | list of runes in inventory and stash (including shared stash if property enabled)             |
| runewords             | string | list of runewords you can create with your runes                                              |
| lastUpdated           | string | hh:mm timestamp of you the last time your character data was updated (can be up to 5 minutes) |
| keys.terror           | number | number of Key of Terror in your inventory/stash                                               |
| keys.hate             | number | number of Key of Hate in your inventory/stash                                                 |
| keys.destruction      | number | number of Key of Destruction in your inventory/stash                                          |
| speedrun.fullRejuvs   | number | number of full rejuvination potions in your inventory/stash                                   |
| speedrun.smallRejuvs  | number | number of (normal/35%) rejuvination potions in your inventory/stash                           |
| speedrun.chippedGems  | number | number of chipped gems in your inventory                                                      |

If you think any other data should be added please request so in an issue on GitHub. Not all fields can be added unfortunately (such as amount of times a character has died).
