package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "service_prices")
public class ServicePrice {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "service_id", nullable = false)
  private UUID serviceId;

  @Column(name = "pet_size_id", nullable = false)
  private UUID petSizeId;

  @Column(name = "price_cents", nullable = false)
  private int priceCents;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(nullable = false, length = 3)
  private String currency;

  public UUID getServiceId() {
    return serviceId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public int getPriceCents() {
    return priceCents;
  }

  public String getCurrency() {
    return currency;
  }
}
