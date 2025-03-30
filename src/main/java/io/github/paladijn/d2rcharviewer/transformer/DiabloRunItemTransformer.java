/*
   Copyright 2024-2025 Paladijn (paladijn2960+d2rsavegameparser@gmail.com)

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
package io.github.paladijn.d2rcharviewer.transformer;

import io.github.paladijn.d2rcharviewer.model.diablorun.DRUNItemQuality;
import io.github.paladijn.d2rcharviewer.model.diablorun.ItemPayload;
import io.github.paladijn.d2rcharviewer.model.translation.DisplayProperty;
import io.github.paladijn.d2rcharviewer.service.TranslationService;
import io.github.paladijn.d2rsavegameparser.model.CharacterType;
import io.github.paladijn.d2rsavegameparser.model.Item;
import io.github.paladijn.d2rsavegameparser.model.ItemContainer;
import io.github.paladijn.d2rsavegameparser.model.ItemLocation;
import io.github.paladijn.d2rsavegameparser.model.ItemProperty;
import io.github.paladijn.d2rsavegameparser.model.ItemQuality;
import io.github.paladijn.d2rsavegameparser.model.SkillTree;
import io.github.paladijn.d2rsavegameparser.model.SkillType;
import io.github.paladijn.d2rsavegameparser.txt.TXTProperties;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class DiabloRunItemTransformer {
    private static final Logger log = getLogger(DiabloRunItemTransformer.class);

    private final TranslationService translationService;

    private final TXTProperties txtProperties;

    public DiabloRunItemTransformer(TranslationService translationService) {
        this.translationService = translationService;
        this.txtProperties = TXTProperties.getInstance();
    }

    public List<ItemPayload> convertItems(List<Item> items, boolean equippedOnly, boolean isMercenaryItem, int level) {
        final List<ItemPayload> results = new ArrayList<>();
        for (Item item: items) {
            if (equippedOnly && item.location() != ItemLocation.EQUIPPED) {
                continue;
            }
            // skip pots, scrolls, tomes, gems, keys, runes and the Horadric Cube
            if (item.itemName().contains("Potion")
                    || Item.isScroll(item.code())
                    || Item.isTome(item.code())
                    || Item.isGem(item.type(), item.type2())
                    || item.itemName().equals("Key")
                    || Item.isRune(item.type())
                    || item.itemName().equals("Horadric Cube")) {
                continue;
            }
            final String itemName = item.quality() == ItemQuality.UNIQUE || item.quality() == ItemQuality.SET ? translationService.getTranslationByKey(item.itemName()) : item.itemName();
            results.add(new ItemPayload(
                    item.guid() == null ? 0 : Integer.parseInt(item.guid()),
                    0, // check d2s what this value is, could be class restricted?
                    item.type(),
                    itemName,
                    item.isRuneword() ? DRUNItemQuality.GOLD : DRUNItemQuality.fromParsed(item.quality()),
                    getItemProperties(item.properties(), item.cntSockets(), level),
                    new io.github.paladijn.d2rcharviewer.model.diablorun.ItemLocation(
                            item.x(),
                            item.y(),
                            1, // width, perhaps retrieve this from the property files
                            1, // height, perhaps retrieve this from the property files
                            item.position().ordinal(),
                            convertItemContainer(item.container(), item.location(), isMercenaryItem)
                    )));
        }
        return List.copyOf(results);
    }

    /**
     * List from https://github.com/DiabloRun/diablorun-api-server/blob/master/src/sync/item-updates.ts
     *
     * @param container {@link ItemContainer} of the item
     * @param location {@link ItemLocation} of the item
     * @param isMercenaryItem true if this item is equipped on the mercenary
     * @return the Diablo.run id for the container
     */
    private int convertItemContainer(ItemContainer container, ItemLocation location, boolean isMercenaryItem) {
        if (isMercenaryItem) {
            return 10;
        }

        if (location == ItemLocation.EQUIPPED) {
            return 0;
        }
        if (location == ItemLocation.BELT) {
            return 1;
        }
        if (container == ItemContainer.INVENTORY) {
            return 2;
        }
        if (container == ItemContainer.HORADRIC_CUBE) {
            return 5;
        }
        if (container == ItemContainer.STASH) {
            return 6;
        }

        // if unknown or stash, it's displayed as a stash item, so:
        log.warn("unknown location for item at container {}, location {}", container, location);
        return 6;
    }

    List<String> getItemProperties(List<ItemProperty> properties, short cntSockets, int level) {
        List<String> allProperties = new ArrayList<>();
        List<DisplayProperty> displayProperties = getDisplayProperties(properties, level);
        for(DisplayProperty displayProperty: displayProperties) {
            final String translatedLabel = translationService.getTranslationByKey(displayProperty.label());
            if (displayProperty.values().isEmpty()) {
                allProperties.add(translatedLabel);
            } else {
                String extended = replacePropertyFields(translatedLabel, displayProperty.values());
                if (displayProperty.extend()) {
                    extended += " " + displayProperty.values().getLast();
                }
                allProperties.add(extended);
            }
        }
        if (cntSockets > 0) {
            allProperties.add(replacePropertyFields(translationService.getTranslationByKey("Socketable"), List.of(String.valueOf(cntSockets)))); // this is a bit silly, but even when the sockets are empty we just list the total number
        }
        return List.copyOf(allProperties);
    }

    protected List<DisplayProperty> getDisplayProperties(final List<ItemProperty> properties, int level) {
        final List<DisplayProperty> displayProperties = new ArrayList<>();
        for(int i = 0; i < properties.size(); i++) {
            final ItemProperty property = properties.get(i);
            if (property.index() >= 39 && property.index() <= 45 && i < properties.size() - 1 && properties.get(i + 1).index() == property.index()) {
                // combine and skip the next one
                log.debug("Combining {} values", property.name());
                for (int valueIndex = 0; valueIndex < property.values().length; valueIndex++) {
                    property.values()[valueIndex] += properties.get(i + 1).values()[valueIndex];
                }
                i++;
            }

            switch (property.index()) {
                case 23: continue; // secondary_mindamage
                case 24: continue; // secondary_maxdamage
                case 48: if (addedFireDamage(property, i, properties, displayProperties)) {
                    i++;
                    continue;
                }
                    break;
                case 50: if(addedLightningDamage(property, i, properties, displayProperties)) {
                    i++;
                    continue;
                }
                    break;
                case 52: if(addedMagicDamage(property, i, properties, displayProperties)) {
                    i++;
                    continue;
                }
                    break;
                case 54: if(addedColdDamage(property, i, properties, displayProperties)) {
                    i++;
                    continue;
                }
                    break;
                case 56: continue; // cold_length is not displayed
                case 57: if (addedPoisonDamage(property, i, properties, displayProperties)) {
                    i++;
                    continue;
                }
                    break;
                case 59: continue; // poison length is picked up in the display below
                case 83:
                    addClassSpecificSkills(property, displayProperties);
                    continue;
                case 107:
                    addSingleSkill(property, displayProperties);
                    continue;
                case 159: continue; // item_throw_mindamage
                case 160: continue; // item_throw_maxdamage
                case 188:
                    addSkillTab(property, displayProperties);
                    continue;
                case 195, 196, 197, 198, 199, 201:
                    addSkillOn(property, displayProperties);
                    continue;
                case 204:
                    addChargedItem(property, displayProperties);
                    continue;
                case 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 256, 247, 248, 249, 250:
                    addPropertyPerLevel(property, level, displayProperties);
                    continue;
                case 356: continue; // questitemdifficulty
            }

            final String label = txtProperties.getItemStatCostsByID(property.index()).getDescStrPos();
            final List<String> values = getValues(property.values());
            displayProperties.add(new DisplayProperty(label, values, false));
        }
        return displayProperties;
    }

    private void addClassSpecificSkills(ItemProperty property, List<DisplayProperty> displayProperties) {
        final int index = property.values()[0];
        if (index >= CharacterType.values().length) {
            log.error("can't find class for property {}", property);
            return;
        }
        final String label = switch (CharacterType.values()[index]) {
            case CharacterType.AMAZON -> "ModStr3a";
            case SORCERESS -> "ModStr3d";
            case NECROMANCER -> "ModStr3c";
            case PALADIN -> "ModStr3b";
            case BARBARIAN -> "ModStr3e";
            case DRUID -> "ModStre8a";
            case ASSASSIN -> "ModStre8b";
            case NONE -> "NO CLASS SELECTED";
        };
        displayProperties.add(new DisplayProperty(label, List.of(String.valueOf(property.values()[1])), false));
    }

    private void addPropertyPerLevel(ItemProperty property, int level, List<DisplayProperty> displayProperties) {
        final int value = (int) Math.floor(property.values()[0] / 8.0 * level);
        final String label = txtProperties.getItemStatCostsByID(property.index()).getDescStrPos();
        final String perLevel = translationService.getTranslationByKey("increaseswithplaylevelX");
        displayProperties.add(new DisplayProperty(label, List.of(String.valueOf(value), perLevel), true));
    }

    private void addChargedItem(ItemProperty property, List<DisplayProperty> displayProperties) {
        final int skillID = getSkillID(property.values()[1]);
        final String skillName = translationService.getTranslationByKey(getSkillLabel(skillID));
        displayProperties.add(new DisplayProperty("ModStre10d",
                List.of(String.valueOf(property.values()[0]), skillName, String.valueOf(property.values()[2]), String.valueOf(property.values()[3])), false));
    }

    private void addSkillOn(ItemProperty property, List<DisplayProperty> displayProperties) {
        final int skillID = getSkillID(property.values()[1]);
        final String skillName = translationService.getTranslationByKey(getSkillLabel(skillID));
        displayProperties.add(new DisplayProperty(txtProperties.getItemStatCostsByID(property.index()).getDescStrPos(),
                List.of(String.valueOf(property.values()[2]), String.valueOf(property.values()[0]), skillName), false));
    }

    private void addSkillTab(ItemProperty property, List<DisplayProperty> displayProperties) {
        final SkillTree skillTree = SkillTree.findSkillTreeById(property.values()[0]);
        final String classOnly = translationService.getTranslationByKey(skillTree.getCharacterType().getDisplayName().substring(0, 3) + "Only");
        displayProperties.add(new DisplayProperty("StrSklTabItem" + (1 + skillTree.ordinal()), List.of(String.valueOf(property.values()[1]), classOnly), true));
    }

    private void addSingleSkill(ItemProperty property, List<DisplayProperty> displayProperties) {
        final int skillID = getSkillID(property.values()[0]);
        final String skillLabel = getSkillLabel(skillID);
        final String skillName = translationService.getTranslationByKey(skillLabel);
        final SkillType skillType = SkillType.findSkillById(property.values()[0]);
        final String classOnly = translationService.getTranslationByKey(skillType.getCharacterType().getDisplayName().substring(0, 3) + "Only");
        displayProperties.add(new DisplayProperty("ItemModifierClassSkill", List.of(String.valueOf(property.values()[1]), skillName, classOnly), false));
    }

    private String getSkillLabel(int skillID) {
        return skillID < 221 ? "skillname" + skillID : "Skillname" + skillID;
    }

    private int getSkillID(int value) {
        return value < 221 ? value : 1 + value;
    }

    private boolean addedPoisonDamage(ItemProperty property, int index, List<ItemProperty> properties, List<DisplayProperty> displayProperties) {
        if (index < properties.size() - 2) {
            // the min + max + duration is present in some item properties, but not always, so we'll parse them separately regardless.
            final ItemProperty maxPoisonDamage = properties.get(index + 1);
            final Optional<ItemProperty> poisonLength = properties.stream().filter(p -> p.index() == 59).findFirst();
            if (poisonLength.isPresent() && maxPoisonDamage.index() == 58) {
                final int length = poisonLength.get().values()[0];
                final int minDamage = property.values()[0] * length / 256;
                final int maxDamage = maxPoisonDamage.values()[0] * length / 256;
                final String duration = String.valueOf(length / 25);
                if (minDamage == maxDamage) {
                    displayProperties.add(new DisplayProperty("strModPoisonDamage",
                            List.of(String.valueOf(minDamage), duration), false));
                } else {
                    displayProperties.add(new DisplayProperty("strModPoisonDamageRange",
                            List.of(String.valueOf(minDamage), String.valueOf(maxDamage), duration), false));
                }
                return true;
            }
        }
        return false;
    }

    private boolean addedColdDamage(ItemProperty property, int index, List<ItemProperty> properties, List<DisplayProperty> displayProperties) {
        if (index < properties.size() - 1) {
            final ItemProperty maxColdDamage = properties.get(index + 1);
            if (maxColdDamage.index() == 55) {
                displayProperties.add(new DisplayProperty("strModColdDamageRange",
                        List.of(String.valueOf(property.values()[0]), String.valueOf(maxColdDamage.values()[0])), false));
                return true;
            }
        }
        return false;
    }

    private boolean addedMagicDamage(ItemProperty property, int index, List<ItemProperty> properties, List<DisplayProperty> displayProperties) {
        if (index < properties.size() - 1) {
            final ItemProperty maxColdDamage = properties.get(index + 1);
            if (maxColdDamage.index() == 53) {
                displayProperties.add(new DisplayProperty("strModMagicDamageRange",
                        List.of(String.valueOf(property.values()[0]), String.valueOf(maxColdDamage.values()[0])), false));
                return true;
            }
        }
        return false;
    }

    private boolean addedLightningDamage(ItemProperty property, int index, List<ItemProperty> properties, List<DisplayProperty> displayProperties) {
        if (index < properties.size() - 1) {
            final ItemProperty maxLgthDamage = properties.get(index + 1);
            if (maxLgthDamage.index() == 51) {
                displayProperties.add(new DisplayProperty("strModLightningDamageRange",
                        List.of(String.valueOf(property.values()[0]), String.valueOf(maxLgthDamage.values()[0])), false));
                return true;
            }
        }
        return false;
    }

    private boolean addedFireDamage(ItemProperty property, int index, List<ItemProperty> properties, List<DisplayProperty> displayProperties) {
        if (index < properties.size() - 1) {
            final ItemProperty maxFireDamage = properties.get(index + 1);
            if (maxFireDamage.index() == 49) {
                displayProperties.add(new DisplayProperty("strModFireDamageRange",
                        List.of(String.valueOf(property.values()[0]), String.valueOf(maxFireDamage.values()[0])), false));
                return true;
            }
        }
        return false;
    }

    private List<String> getValues(int[] values) {
        List<String> result = new ArrayList<>();
        switch (values.length) {
            case 1 -> result.add(String.valueOf(values[0]));
            case 3 -> {
                if (values[0] == values[1]) {
                    result.add(String.valueOf(values[0]));
                } else {
                    result.add(String.valueOf(values[0]));
                    result.add(String.valueOf(values[1]));
                    result.add(String.valueOf(values[2]));
                }
            }
            default -> log.warn("we ended up here with an unsupported length of {}", values.length);
        }
        return result;
    }

    String replacePropertyFields(final String input, final List<String> values) {
        // some translations cover %0, %1, %2, %3, so also map these
        if (input.contains("%0") || input.contains("%+0")) {
            return replacedSequencedPropertyFields(input, values);
        }

        // We'll need to pull a little trick with the previous replaced outputs in order to map the other %d, %s fields.
        String previousOutput = input;
        String output = previousOutput;
        for(String value: values) {
            output = previousOutput.replaceFirst("%d%%", value + "%")
                    .replaceFirst("%\\+d%%", "+" + value + "%")
                    .replaceFirst("%\\+d", "+" + value);
            if (output.equals(previousOutput)) {
                output = previousOutput.replaceFirst("%[dis]", value);
            }
            previousOutput = output;
        }
        return output;
    }

    private String replacedSequencedPropertyFields(String input, List<String> values) {
        String output = input;
        if (values.size() > 0) {
            output = input.replace("%+0", "+" + values.getFirst())
                    .replace("%0", values.getFirst());
        }
        if (values.size() > 1) {
            output = output.replace("%+1", "+" + values.get(1))
                    .replace("%1", values.get(1));
        }
        if (values.size() > 2) {
            output = output.replace("%+2", "+" + values.get(2))
                    .replace("%2", values.get(2));
        }
        if (values.size() > 3) {
            output = output.replace("%+3", "+" + values.get(3))
                    .replace("%3", values.get(3));
        }

        return output;
    }
}
