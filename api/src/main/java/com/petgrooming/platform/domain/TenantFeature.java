package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tenant_features")
public class TenantFeature {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "feature_key", nullable = false, length = 100)
  private String featureKey;

  @Column(name = "is_enabled", nullable = false)
  private boolean enabled;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "config_json", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> configJson;

  public UUID getTenantId() {
    return tenantId;
  }

  public String getFeatureKey() {
    return featureKey;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public Map<String, Object> getConfigJson() {
    return configJson;
  }
}
