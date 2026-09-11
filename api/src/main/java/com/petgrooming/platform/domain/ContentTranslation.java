package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Locale override for a single field of a content row
 * (service, testimonial, gallery item, site section, salon profile).
 * Exactly one of {@code value} (text columns) / {@code valueJson} (jsonb columns) is set.
 */
@Entity
@Table(name = "content_translations")
public class ContentTranslation {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "locale", nullable = false, length = 10)
  private String locale;

  @Column(name = "entity_type", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private UUID entityId;

  @Column(name = "field", nullable = false, length = 50)
  private String field;

  @Column(name = "value")
  private String value;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "value_json", columnDefinition = "jsonb")
  private Map<String, Object> valueJson;

  public UUID getTenantId() {
    return tenantId;
  }

  public String getLocale() {
    return locale;
  }

  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public String getField() {
    return field;
  }

  public String getValue() {
    return value;
  }

  public Map<String, Object> getValueJson() {
    return valueJson;
  }
}
