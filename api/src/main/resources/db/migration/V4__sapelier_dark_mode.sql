-- Enable the dark theme toggle for the Sapelier tenant.
-- The web app shows the switch only when this feature is enabled.

SET app.bypass_rls = 'true';

INSERT INTO tenant_features (tenant_id, feature_key, is_enabled, config_json)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'dark_mode',
  TRUE,
  '{}'::jsonb
)
ON CONFLICT (tenant_id, feature_key) DO UPDATE
  SET is_enabled = EXCLUDED.is_enabled;
