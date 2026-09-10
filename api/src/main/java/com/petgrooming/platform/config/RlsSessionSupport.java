package com.petgrooming.platform.config;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Ensures app queries can read tenant rows under FORCE ROW LEVEL SECURITY
 * when the DB role is not a superuser. Must run inside an open transaction.
 */
@Component
public class RlsSessionSupport {

  private final EntityManager entityManager;

  public RlsSessionSupport(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void enableBypass() {
    Session session = entityManager.unwrap(Session.class);
    session.doWork(connection -> {
      try (var statement = connection.createStatement()) {
        statement.execute("SELECT set_config('app.bypass_rls', 'true', true)");
      }
    });
  }
}
