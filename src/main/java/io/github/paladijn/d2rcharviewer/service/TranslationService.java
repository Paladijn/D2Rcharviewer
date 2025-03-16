package io.github.paladijn.d2rcharviewer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.paladijn.d2rcharviewer.model.translation.TranslationItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class TranslationService {
    private static final Logger log = getLogger(TranslationService.class);

    private final Map<String, String> translationsMappedByKey = new HashMap<>();

    @ConfigProperty(name = "translation.language", defaultValue = "enUS")
    String language = "enUS";

    public TranslationService(final ObjectMapper objectMapper) {
        try {
            storeTranslationItems(objectMapper, "translations/item-gems.json");
            storeTranslationItems(objectMapper, "translations/item-modifiers.json");
            storeTranslationItems(objectMapper, "translations/item-nameaffixes.json");
            storeTranslationItems(objectMapper, "translations/item-names.json");
            storeTranslationItems(objectMapper, "translations/item-runes.json");
            storeTranslationItems(objectMapper, "translations/mercenaries.json");
            storeTranslationItems(objectMapper, "translations/skills.json");
        } catch (IOException e) {
            log.error("Could not read one of the translation jsons, please ensure they are present in the translations/ folder", e);
            throw new RuntimeException(e);
        }
    }

    public String getTranslationByKey(final String key) {
        return translationsMappedByKey.getOrDefault(key, "NOT_FOUND");
    }

    private void storeTranslationItems(ObjectMapper objectMapper, String fileLocation) throws IOException {
        final InputStream resourceAsStream = new FileInputStream(fileLocation);
        final List<TranslationItem> translatedItems = objectMapper.readValue(resourceAsStream, new TypeReference<>() {});
        translatedItems.forEach(translatedItem -> translationsMappedByKey.put(translatedItem.key(), getTranslatedValue(translatedItem)));

        log.debug("found {} translated items in {}", translatedItems.size(), fileLocation);
    }

    private String getTranslatedValue(TranslationItem translatedItem) {

        final String value = switch (language) {
            case "enUS" -> translatedItem.enUS();
            case "zhTW" -> translatedItem.zhTW();
            case "deDE" -> translatedItem.deDE();
            case "esES" -> translatedItem.esES();
            case "frFR" -> translatedItem.frFR();
            case "itIT" -> translatedItem.itIT();
            case "koKR" -> translatedItem.koKR();
            case "plPL" -> translatedItem.plPL();
            case "esMX" -> translatedItem.esMX();
            case "jaJP" -> translatedItem.jaJP();
            case "ptBR" -> translatedItem.ptBR();
            case "ruRU" -> translatedItem.ruRU();
            case "zhCN" -> translatedItem.zhCN();

            default -> throw new RuntimeException("unsupported language: " + language);
        };

        return sanitiseResponse(value);
    }

    private String sanitiseResponse(String value) {
        if (!value.startsWith("[")) {
            return value;
        }
        int nextBlock = value.indexOf("[", 1);
        if (nextBlock == -1) {
            return value.substring(4);
        }
        return value.substring(4, nextBlock - 1);
    }
}
