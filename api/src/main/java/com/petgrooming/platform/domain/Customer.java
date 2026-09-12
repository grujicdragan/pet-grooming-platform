package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  private String email;

  private String phone;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }

  public static Customer forUser(UUID tenantId, UserAccount user) {
    Customer customer = new Customer();
    customer.tenantId = tenantId;
    customer.userId = user.getId();
    customer.firstName = user.getFirstName() == null || user.getFirstName().isBlank() ? "—" : user.getFirstName();
    customer.lastName = user.getLastName() == null ? "" : user.getLastName();
    customer.email = user.getEmail();
    customer.active = true;
    return customer;
  }

  public UUID getId() {
    return id;
  }
}
