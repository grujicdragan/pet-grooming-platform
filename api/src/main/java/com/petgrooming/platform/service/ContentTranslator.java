package com.petgrooming.platform.service;

import com.petgrooming.platform.domain.ContentTranslation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory lookup of translations for one tenant + locale.
 * Falls back to the base value when no translation exists.
 */
public final class ContentTranslator {

  public static final String SALON_PROFILE = "salon_profile";
  public static final String SERVICE = "service";
  public static final String TESTIMONIAL = "testimonial";
  public static final String GALLERY = "gallery";
  public static final String SITE_SECTION = "site_section";

  private static final ContentTranslator EMPTY =
      new ContentTranslator(Collections.emptyMap(), Collections.emptyMap());

  private final Map<String, String> texts;
  private final Map<String, Map<String, Object>> jsons;

  private ContentTranslator(Map<String, String> texts, Map<String, Map<String, Object>> jsons) {
    this.texts = texts;
    this.jsons = jsons;
  }

  public static ContentTranslator none() {
    return EMPTY;
  }

  public static ContentTranslator of(List<ContentTranslation> rows) {
    if (rows.isEmpty()) {
      return EMPTY;
    }
    Map<String, String> texts = new HashMap<>();
    Map<String, Map<String, Object>> jsons = new HashMap<>();
    for (ContentTranslation row : rows) {
      String key = key(row.getEntityType(), row.getEntityId(), row.getField());
      if (row.getValueJson() != null) {
        jsons.put(key, row.getValueJson());
      } else if (row.getValue() != null && !row.getValue().isBlank()) {
        texts.put(key, row.getValue());
      }
    }
    return new ContentTranslator(texts, jsons);
  }

  public String text(String entityType, UUID entityId, String field, String fallback) {
    if (texts.isEmpty() || entityId == null) {
      return fallback;
    }
    return texts.getOrDefault(key(entityType, entityId, field), fallback);
  }

  public Map<String, Object> json(String entityType, UUID entityId, String field, Map<String, Object> fallback) {
    if (jsons.isEmpty() || entityId == null) {
      return fallback;
    }
    return jsons.getOrDefault(key(entityType, entityId, field), fallback);
  }

  private static String key(String entityType, UUID entityId, String field) {
    return entityType + '|' + entityId + '|' + field;
  }
}
