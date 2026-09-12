-- One customer profile per user inside a tenant (walk-in customers still have user_id NULL).
CREATE UNIQUE INDEX uq_customers_tenant_user
  ON customers (tenant_id, user_id)
  WHERE user_id IS NOT NULL;
