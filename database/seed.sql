-- =============================================================================
-- Seed: first tenant = Sapelier (Šapelier)
-- Run after schema.sql. Requires bypass RLS (or DB superuser).
-- =============================================================================

SET app.bypass_rls = 'true';

-- Fixed IDs for stable references
-- tenant: aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa

INSERT INTO tenants (id, slug, name, is_active)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'sapelier',
  'Šapelier',
  TRUE
);

INSERT INTO salon_profile (
  tenant_id, display_name, tagline, description,
  phone, phone_display, email,
  address_line1, city, country, timezone,
  maps_search_url, map_embed_url
) VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'Šapelier',
  'atelje za pse',
  'Šapelier je tih, topao atelje za pse. Zakazivanje je kratko, loyalty program je jasan: svaki peti tretman dolazi sa 40% popusta.',
  '+381637128696',
  '063 712 8696',
  'hello@sapelier.example',
  'Zaplanjska 58',
  'Beograd',
  'RS',
  'Europe/Belgrade',
  'https://www.google.com/maps/search/?api=1&query=Zaplanjska+58%2C+Vo%C5%BEdovac%2C+Beograd',
  'https://maps.google.com/maps?q=Zaplanjska+58,+Vo%C5%BEdovac,+Beograd&hl=sr&z=16&output=embed'
);

INSERT INTO salon_branding (
  tenant_id, primary_color, secondary_color, accent_color,
  logo_url, logo_storage_key, cover_image_url, cover_storage_key, font_family
) VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  '#2f5d4a',
  '#c46a3a',
  '#1f2a24',
  'images/logo.png',
  'images/logo.png',
  'images/hero-dogs.jpg',
  'images/hero-dogs.jpg',
  'Fraunces, Outfit'
);

INSERT INTO locations (
  id, tenant_id, name, phone, address_line1, city, state, country, is_primary, is_active
) VALUES (
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'Voždovac',
  '+381637128696',
  'Zaplanjska 58',
  'Beograd',
  'Voždovac',
  'RS',
  TRUE,
  TRUE
);

INSERT INTO location_working_hours (tenant_id, location_id, day, open_time, close_time, is_closed)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'MONDAY',    '09:00', '18:00', FALSE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'TUESDAY',   '09:00', '18:00', FALSE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'WEDNESDAY', '09:00', '18:00', FALSE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'THURSDAY',  '09:00', '18:00', FALSE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'FRIDAY',    '09:00', '18:00', FALSE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SATURDAY',  '10:00', '15:00', FALSE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SUNDAY',    NULL,    NULL,    TRUE);

INSERT INTO salon_social_links (tenant_id, platform, url, handle, display_order, is_active)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'instagram',
  'https://www.instagram.com/sapeliergrooming/',
  '@sapeliergrooming',
  1,
  TRUE
);

-- Default pet size (flat pricing in current UI)
INSERT INTO pet_sizes (id, tenant_id, name, code, display_order, is_active)
VALUES (
  'cccccccc-cccc-cccc-cccc-cccccccccccc',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'Standard',
  'standard',
  1,
  TRUE
);

INSERT INTO service_categories (id, tenant_id, name, display_order, is_active)
VALUES (
  'dddddddd-dddd-dddd-dddd-dddddddddddd',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'Grooming',
  1,
  TRUE
);

INSERT INTO services (
  id, tenant_id, category_id, code, name, description, duration_minutes, icon, display_order, is_active
) VALUES
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd',
   'full-groom', 'Pun grooming', 'Kupanje, sušenje, šišanje po rasi i završni styling.', 90, '✂️', 1, TRUE),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd',
   'bath', 'Kupanje i feniranje', 'Nežni šampon, fen i rasčešljavanje bez pune korekcije.', 45, '🛁', 2, TRUE),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd',
   'trim', 'Korekcija šišanja', 'Održavanje forme između kompletnih tretmana.', 40, '💇', 3, TRUE),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd',
   'nails', 'Šišanje noktiju', 'Brzo, mirno skraćivanje noktiju i pregled šapa.', 20, '🐾', 4, TRUE),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'dddddddd-dddd-dddd-dddd-dddddddddddd',
   'spa', 'Spa paket', 'Kupanje, maska za dlaku, uši i lagana masaža.', 75, '🧖', 5, TRUE);

-- Prices stored in minor units (RSD has no decimals in practice → whole dinars as "cents"/units)
INSERT INTO service_prices (tenant_id, service_id, pet_size_id, price_cents, currency)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 4500, 'RSD'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 2800, 'RSD'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 2200, 'RSD'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 900, 'RSD'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 3900, 'RSD');

INSERT INTO salon_gallery (
  tenant_id, title, caption, media_type, media_url, storage_key, alt_text, display_order, is_published
) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Tretman u toku', 'Tretman u toku', 'IMAGE',
   'images/grooming.jpg', 'images/grooming.jpg', 'Pas tokom grooming tretmana', 1, TRUE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Prvi put u salonu', 'Prvi put u salonu', 'IMAGE',
   'images/puppy.jpg', 'images/puppy.jpg', 'Mladunac sa mekom dlakom', 2, TRUE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Posle šišanja', 'Posle šišanja', 'IMAGE',
   'images/walk.jpg', 'images/walk.jpg', 'Pas u šetnji posle šišanja', 3, TRUE),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Kući, sređen', 'Kući, sređen', 'IMAGE',
   'images/friends.jpg', 'images/friends.jpg', 'Vlasnik i pas zajedno', 4, TRUE);

INSERT INTO testimonials (
  tenant_id, author_name, author_role, content, rating, avatar_url, avatar_storage_key, is_published, display_order
) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Mila J.', 'Luna, koker',
   'Luna izlazi mirnija nego što je ušla. Termin je trajao tačno koliko su rekli.',
   5, 'images/puppy.jpg', 'images/puppy.jpg', TRUE, 1),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Nikola P.', 'Bobi, labrador',
   'Konačno salon gde Bobi ne paniči. Loyalty kartica je jasna — peti grooming nam je skoro pa poklon.',
   5, 'images/walk.jpg', 'images/walk.jpg', TRUE, 2),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Ivana S.', 'Maca, maine coon',
   'Zakazivanje za 30 sekundi, bez telefona. Spa paket je Maca oduševila.',
   5, 'images/grooming.jpg', 'images/grooming.jpg', TRUE, 3);

INSERT INTO site_sections (
  tenant_id, section_key, title, subtitle, body, content_json, image_url, image_storage_key, cta_label, cta_url, display_order
) VALUES
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'hero',
    'Šišanje koje ljubimci vole da ponove.',
    'Šišanje koje ljubimci <em>vole</em> da ponove.',
    'Šapelier je tih, topao atelje za pse. Zakazivanje je kratko, loyalty program je jasan: svaki peti tretman dolazi sa 40% popusta.',
    '{"badge":"Zaplanjska 58, Voždovac","floatTitle":"Mirno, bez žurbe","floatSubtitle":"sa puno poslastica"}'::jsonb,
    'images/hero-dogs.jpg',
    'images/hero-dogs.jpg',
    'Zakaži termin',
    '/book',
    1
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'stats',
    'Statistika',
    NULL,
    NULL,
    '{"items":[{"value":"850+","label":"zadovoljnih šapa"},{"value":"−40%","label":"na svaki 5. tretman"},{"value":"20–90","label":"min po tretmanu"}]}'::jsonb,
    NULL, NULL, NULL, NULL, 2
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'marquee',
    'Marquee',
    NULL,
    NULL,
    '{"items":["Kupanje","Šišanje po rasi","Stilizovanje","Spa paket","Nokti i šape","Bez žurbe","Voždovac"]}'::jsonb,
    NULL, NULL, NULL, NULL, 3
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'booking_steps',
    'Kako funkcioniše',
    NULL,
    NULL,
    '{"steps":[{"title":"Nalog","text":"Prijavi se ili se registruj za minut. Auth dolazi u sledećoj fazi."},{"title":"Datum i vreme","text":"Izaberi uslugu, ljubimca, slobodan dan i sat. Termini su vizuelni slotovi, bez telefoniranja."},{"title":"Loyalty","text":"Kartica sa 5 pečata. Peti pečat je nagrada: 40% manje na taj tretman."}]}'::jsonb,
    NULL, NULL, NULL, NULL, 4
  );

INSERT INTO tenant_features (tenant_id, feature_key, is_enabled, config_json)
VALUES
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'online_booking',
    TRUE,
    '{"timeSlots":["09:00","10:30","12:00","13:30","15:00","16:30"]}'::jsonb
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'loyalty',
    TRUE,
    '{"treatmentsPerReward":5,"rewardDiscount":0.4}'::jsonb
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'gallery',
    TRUE,
    '{}'::jsonb
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'testimonials',
    TRUE,
    '{}'::jsonb
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'dark_mode',
    TRUE,
    '{}'::jsonb
  );
