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
package io.github.paladijn.d2rcharviewer.calculator;

import io.github.paladijn.d2rcharviewer.model.Breakpoints;
import io.github.paladijn.d2rcharviewer.model.Constants;
import io.github.paladijn.d2rcharviewer.model.DisplayAttributes;
import io.github.paladijn.d2rcharviewer.model.DisplayStats;
import io.github.paladijn.d2rcharviewer.model.Keys;
import io.github.paladijn.d2rcharviewer.model.Resistances;
import io.github.paladijn.d2rsavegameparser.model.D2Character;
import io.github.paladijn.d2rsavegameparser.model.Difficulty;
import io.github.paladijn.d2rsavegameparser.model.Item;
import io.github.paladijn.d2rsavegameparser.model.ItemLocation;
import io.github.paladijn.d2rsavegameparser.model.ItemPosition;
import io.github.paladijn.d2rsavegameparser.model.ItemProperty;
import io.github.paladijn.d2rsavegameparser.model.Location;
import io.github.paladijn.d2rsavegameparser.model.QuestData;
import io.github.paladijn.d2rsavegameparser.model.SharedStashTab;
import io.github.paladijn.d2rsavegameparser.model.Skill;
import io.github.paladijn.d2rsavegameparser.parser.CharacterParser;
import io.github.paladijn.d2rsavegameparser.parser.ParseException;
import io.github.paladijn.d2rsavegameparser.parser.SharedStashParser;
import io.github.paladijn.d2rsavegameparser.txt.MiscStats;
import io.github.paladijn.d2rsavegameparser.txt.Runeword;
import io.github.paladijn.d2rsavegameparser.txt.TXTProperties;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.paladijn.d2rsavegameparser.model.ItemContainer.INVENTORY;
import static io.github.paladijn.d2rsavegameparser.model.ItemContainer.STASH;
import static org.slf4j.LoggerFactory.getLogger;

public class DisplayStatsCalculator {
    private static final Logger log = getLogger(DisplayStatsCalculator.class);

    private String savegameLocation;

    private boolean removeDuplicateRuneword;

    private boolean includeSharedStash;

    private final BreakpointCalculator breakpointCalculator;

    private final CharacterParser characterParser = new CharacterParser(false);

    private final SharedStashParser sharedStashParser = new SharedStashParser(false);

    private final TXTProperties txtProperties = TXTProperties.getInstance();

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    public DisplayStatsCalculator(BreakpointCalculator breakpointCalculator,
                                  String savegameLocation,
                                  boolean removeDuplicateRuneword,
                                  boolean includeSharedStash) {
        this.savegameLocation = savegameLocation;
        this.breakpointCalculator = breakpointCalculator;
        this.removeDuplicateRuneword = removeDuplicateRuneword;
        this.includeSharedStash = includeSharedStash;
    }

    public DisplayStats getDisplayStats(final Path characterFile) {
        final String lastUpdateStr = dtf.format(Instant.now());

        ByteBuffer buffer;
        try {
            buffer = ByteBuffer.wrap(Files.readAllBytes(characterFile));
        } catch (IOException e) {
            throw new ParseException("Failed to read characterFile", e);
        }

        final D2Character character = characterParser.parse(buffer);

        long goldInStash = character.attributes().goldInStash();
        List<Item> allItems = character.items();
        if (includeSharedStash) {
            List<Item> fullItemList = new ArrayList<>(allItems);
            List<SharedStashTab> sharedStashTabs = getSharedStashTabs(character.hardcore());
            for (SharedStashTab tab : sharedStashTabs) {
                goldInStash += tab.gold();
                fullItemList.addAll(tab.items());
            }
            allItems = List.copyOf(fullItemList);
        }

        log.debug("total items: {}", character.items().size());
        List<Item> equippedItems = getEquippedItems(character);
        Map<String, Integer> availableRunes = getAvailableRunesByCode(allItems);
        List<ItemProperty> equippedSetBenefits = character.equippedSetBenefits();

        int frw = getTotalPointsInProperty("item_fastermovevelocity", equippedItems, equippedSetBenefits);
        int mf = getTotalPointsInProperty("item_magicbonus", equippedItems, equippedSetBenefits);
        int gf = getTotalPointsInProperty("item_goldbonus", equippedItems, equippedSetBenefits);
        int totalStrength = character.attributes().strength() + getTotalPointsInProperty("strength", equippedItems, equippedSetBenefits);
        int totalDexterity = character.attributes().dexterity() + getTotalPointsInProperty("dexterity", equippedItems, equippedSetBenefits);
        int totalVitality = character.attributes().vitality() + getTotalPointsInProperty("vitality", equippedItems, equippedSetBenefits);
        int totalEnergy = character.attributes().energy() + getTotalPointsInProperty("energy", equippedItems, equippedSetBenefits);
        final DisplayAttributes attributes = new DisplayAttributes(totalStrength, totalDexterity, totalVitality, totalEnergy);
        final List<Skill> skillsWithBenefits = character.skills().stream().filter(skill -> !skill.passiveBonuses().isEmpty()).toList();
        final List<ItemProperty> benefits = new ArrayList<>(equippedSetBenefits);
        skillsWithBenefits.forEach(skill -> benefits.addAll(skill.passiveBonuses()));

        Resistances resistances = new Resistances(getResistance("fire", character, equippedItems, benefits),
                getResistance("light", character, equippedItems, benefits),
                getResistance("poison", character, equippedItems, benefits),
                getResistance("cold", character, equippedItems, benefits),
                getResistance("damage", character, equippedItems, benefits));

        Breakpoints breakpoints = breakpointCalculator.calculateBreakpoints(character.characterType(), equippedItems, equippedSetBenefits);

        final String runes = getRunesByCount(availableRunes);
        final List<String> runewordsOnCharacter = getRuneWordsOnCharacter(character.items());
        final String runewords = getAvailableRuneWords(availableRunes, runewordsOnCharacter);

        Keys keys = getAvailableKeys(allItems);

        final String percentToNext = calculateLevelPercentage(character.level(), character.attributes().experience());

        return new DisplayStats(character.name(), character.characterType(), character.level(), character.hardcore(), percentToNext,
                attributes, resistances, breakpoints, frw, mf, gf, goldString(character.attributes().gold()), goldString(goldInStash),
                runes, runewords, keys, lastUpdateStr);
    }

    private String goldString(long goldValue) {
        if (goldValue < 1000) {
            return String.valueOf(goldValue);
        }
        return String.format("%dK", goldValue / 1000);
    }

    private String calculateLevelPercentage(byte level, long experience) {
        if (level == 99) {
            return "100";
        }
        long levelMin = Constants.xpLevels[level];
        long levelMax = Constants.xpLevels[level + 1];
        long total = levelMax - levelMin;
        long current = experience - levelMin;

        double percentage = ((double)current / (double)total) * 100.0f;
        return String.format("%2.1f", percentage);
    }

    private List<SharedStashTab> getSharedStashTabs(boolean isHardcore) {
        final String stashFilename = isHardcore ? "SharedStashHardCoreV2.d2i" : "SharedStashSoftCoreV2.d2i";
        final ByteBuffer buffer;
        try {
            buffer = ByteBuffer.wrap(Files.readAllBytes(Path.of(savegameLocation, stashFilename)));
        } catch (IOException e) {
            throw new ParseException("could not read shared stash file", e);
        }
        return sharedStashParser.parse(buffer);
    }

    private Difficulty getCurrentDifficulty(List<Location> locations) {
        return locations.stream()
                .filter(Location::isActive)
                .map(Location::difficulty)
                .findAny().orElse(Difficulty.NORMAL);
    }

    private List<String> getRuneWordsOnCharacter(List<Item> items) {
        return items.stream()
                .filter(Item::isRuneword)
                .map(Item::itemName)
                .toList();
    }

    private Keys getAvailableKeys(List<Item> allItems) {
        int terror = 0;
        int hate = 0;
        int destruction = 0;
        for (Item item: allItems) {
            if (item.level() >= 80) { // hate 80, terror 85, destruction 90
                switch (item.itemName()) {
                    case "Key of Terror" -> terror++;
                    case "Key of Hate" -> hate++;
                    case "Key of Destruction" -> destruction++;
                }
            }
        }
        return new Keys(terror, hate, destruction);
    }

    public static List<ItemProperty> getPropertiesByNames(List<Item> items, List<String> name) {
        List<ItemProperty> result = new ArrayList<>();
        items.forEach(item -> {
                    if (item.maxDurability() == 0 || item.durability() > 0) { // skip broken items, indestructible items have 0/0
                        List<ItemProperty> itemProperties = item.properties().stream()
                                .filter(itemProperty -> name.contains(itemProperty.name()))
                                .toList();
                        result.addAll(itemProperties);
                    }
                }
        );
        return result;
    }

    private String getAvailableRuneWords(Map<String, Integer> availableRunes, List<String> runewordsOnCharacter) {
        return txtProperties.getRunewords().stream()
                .filter(runeword -> runeword.isRunewordPossible(availableRunes)
                        && keepRuneword(runeword.getName(), runewordsOnCharacter))
                .map(Runeword::getName)
                .collect(Collectors.joining(", "));
    }

    private boolean keepRuneword(String name, List<String> runewordsOnCharacter) {
        if (removeDuplicateRuneword) {
            return !runewordsOnCharacter.contains(name);
        }
        return true;
    }

    private int getResistance(String type, D2Character character, List<Item> equippedItems, List<ItemProperty> benefits) {
        final int sum = getTotalPointsInProperty(type + "resist", equippedItems, benefits);
        final int max = Math.min(95, 75 + getTotalPointsInProperty("max" + type + "resist", equippedItems, benefits));
        return Math.min(max, adjustSumByLocation(character, sum));
    }

    private List<Item> getEquippedItems(D2Character character) {
        return character.items().stream()
                .filter(item -> requirementsMet(character, item)
                                && equippedOrCharm(item))
                .toList();
    }

    private Map<String, Integer> getAvailableRunesByCode(List<Item> items) {
        Map<String, Integer> result = new HashMap<>();
        for (Item item: items) {
            if (availableRune(item)) {
                final String key = item.code();
                int count = result.getOrDefault(key, 0);
                result.put(key, ++count);
            }
        }

        return result;
    }

    private String getRunesByCount(Map<String, Integer> availableRunes) {
        return availableRunes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(this::getRuneAmountString)
                .collect(Collectors.joining(", "));
    }

    private String getRuneAmountString(Map.Entry<String, Integer> entry) {
        MiscStats miscItem = txtProperties.getMiscItemsByCode(entry.getKey());
        final String rune = miscItem.getName().substring(0, miscItem.getName().indexOf(' '));
        if (entry.getValue() > 1) {
            return String.format("%s (%d)", rune, entry.getValue());
        }
        return rune;
    }

    private boolean availableRune(Item item) {
        return Item.isRune(item.type())
                && (item.container() == INVENTORY || item.container() == STASH);
    }

    private boolean requirementsMet(D2Character d2Character, Item item) {
        return d2Character.level() >= item.reqLvl()
                && d2Character.attributes().strength() >= item.reqStr()
                && d2Character.attributes().dexterity() >= item.reqDex();
    }

    private boolean equippedOrCharm(Item item) {
        return (item.location() == ItemLocation.EQUIPPED
                && item.position() != ItemPosition.LEFT_SWAP && item.position() != ItemPosition.RIGHT_SWAP)
                || (item.container() == INVENTORY && Item.isCharm(item.code()));
    }

    private int getTotalPointsInProperty(String propertyName, List<Item> equippedItems, List<ItemProperty> equippedSetBenefits) {
        List<ItemProperty> propsFound = getPropertiesByNames(equippedItems, List.of(propertyName));
        propsFound.addAll(equippedSetBenefits.stream().filter(ip -> ip.name().equals(propertyName)).toList());
        return propsFound.stream().mapToInt(itemProperty -> itemProperty.values()[0]).sum();
    }

    private int adjustSumByLocation(final D2Character character, final int current) {
        int sum = current;
        for (QuestData questData: character.questDataPerDifficulty()) {
            if (questData.resistanceScrollRead()) {
                log.debug("added 10 resistance for {}", questData.difficulty());
                sum += 10;
            }
        }

        final Difficulty difficulty = getCurrentDifficulty(character.locations());
        if (character.expansion()) { // most common use case, so first
            return switch (difficulty) {
                case NORMAL -> sum;
                case NIGHTMARE -> sum - 40;
                case HELL -> sum - 100;
            };
        }

        // Diablo II without LoD
        return switch (difficulty) {
            case NORMAL -> sum;
            case NIGHTMARE -> sum - 20;
            case HELL -> sum - 50;
        };
    }
}
