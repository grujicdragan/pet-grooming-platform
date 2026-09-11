-- =============================================================================
-- Seed: second tenant = Lapa Studio (slug: lapastudio)
-- Same structure as Sapelier, different copy / branding / media
-- =============================================================================

SET app.bypass_rls = 'true';

-- tenant: 11111111-2222-3333-4444-555555555555

INSERT INTO tenants (id, slug, name, is_active)
VALUES (
  '11111111-2222-3333-4444-555555555555',
  'lapastudio',
  'Lapa Studio',
  TRUE
);

INSERT INTO salon_profile (
  tenant_id, display_name, tagline, description,
  phone, phone_display, email,
  address_line1, city, country, timezone,
  maps_search_url, map_embed_url
) VALUES (
  '11111111-2222-3333-4444-555555555555',
  'Lapa Studio',
  'modern grooming bar',
  'Lapa Studio je urbani grooming bar za pse. Brzi termini, čist stil i loyalty koji se računa od prvog dolaska.',
  '+381641112233',
  '064 111 2233',
  'ciao@lapastudio.example',
  'Njegoševa 12',
  'Beograd',
  'RS',
  'Europe/Belgrade',
  'https://www.google.com/maps/search/?api=1&query=Njego%C5%A1eva+12%2C+Vra%C4%8Dar%2C+Beograd',
  'https://maps.google.com/maps?q=Njego%C5%A1eva+12,+Vra%C4%8Dar,+Beograd&hl=sr&z=16&output=embed'
);

INSERT INTO salon_branding (
  tenant_id, primary_color, secondary_color, accent_color,
  logo_url, logo_storage_key, cover_image_url, cover_storage_key, font_family
) VALUES (
  '11111111-2222-3333-4444-555555555555',
  '#1a365d',
  '#e85d4c',
  '#0f172a',
  'images/lapastudio/logo.svg',
  'images/lapastudio/logo.svg',
  'images/lapastudio/hero.jpg',
  'images/lapastudio/hero.jpg',
  'Syne, Manrope'
);

INSERT INTO locations (
  id, tenant_id, name, phone, address_line1, city, state, country, is_primary, is_active
) VALUES (
  '12121212-1212-1212-1212-121212121212',
  '11111111-2222-3333-4444-555555555555',
  'Vračar',
  '+381641112233',
  'Njegoševa 12',
  'Beograd',
  'Vračar',
  'RS',
  TRUE,
  TRUE
);

INSERT INTO location_working_hours (tenant_id, location_id, day, open_time, close_time, is_closed)
VALUES
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'MONDAY',    '10:00', '19:00', FALSE),
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'TUESDAY',   '10:00', '19:00', FALSE),
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'WEDNESDAY', '10:00', '19:00', FALSE),
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'THURSDAY',  '10:00', '19:00', FALSE),
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'FRIDAY',    '10:00', '20:00', FALSE),
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'SATURDAY',  '09:00', '14:00', FALSE),
  ('11111111-2222-3333-4444-555555555555', '12121212-1212-1212-1212-121212121212', 'SUNDAY',    NULL,    NULL,    TRUE);

INSERT INTO salon_social_links (tenant_id, platform, url, handle, display_order, is_active)
VALUES (
  '11111111-2222-3333-4444-555555555555',
  'instagram',
  'https://www.instagram.com/lapastudio.bg/',
  '@lapastudio.bg',
  1,
  TRUE
);

INSERT INTO pet_sizes (id, tenant_id, name, code, display_order, is_active)
VALUES (
  '13131313-1313-1313-1313-131313131313',
  '11111111-2222-3333-4444-555555555555',
  'Standard',
  'standard',
  1,
  TRUE
);

INSERT INTO service_categories (id, tenant_id, name, display_order, is_active)
VALUES (
  '14141414-1414-1414-1414-141414141414',
  '11111111-2222-3333-4444-555555555555',
  'Studio meni',
  1,
  TRUE
);

INSERT INTO services (
  id, tenant_id, category_id, code, name, description, duration_minutes, icon, display_order, is_active
) VALUES
  ('15151515-1515-1515-1515-151515151501', '11111111-2222-3333-4444-555555555555', '14141414-1414-1414-1414-141414141414',
   'full-groom', 'Signature cut', 'Kompletan look: kupanje, fen, šišanje i finish po rasi.', 100, '✨', 1, TRUE),
  ('15151515-1515-1515-1515-151515151502', '11111111-2222-3333-4444-555555555555', '14141414-1414-1414-1414-141414141414',
   'bath', 'Fresh wash', 'Premium šampon, kondicioner i puff feniranje.', 50, '💧', 2, TRUE),
  ('15151515-1515-1515-1515-151515151503', '11111111-2222-3333-4444-555555555555', '14141414-1414-1414-1414-141414141414',
   'trim', 'Shape refresh', 'Brza korekcija forme između velikih termina.', 35, '🪄', 3, TRUE),
  ('15151515-1515-1515-1515-151515151504', '11111111-2222-3333-4444-555555555555', '14141414-1414-1414-1414-141414141414',
   'nails', 'Paw care', 'Nokti, jastučići i lagani scrub šapa.', 25, '🦴', 4, TRUE),
  ('15151515-1515-1515-1515-151515151505', '11111111-2222-3333-4444-555555555555', '14141414-1414-1414-1414-141414141414',
   'spa', 'Glow spa', 'Maska, uši, mirisni finish i mini masaža.', 80, '🌟', 5, TRUE);

INSERT INTO service_prices (tenant_id, service_id, pet_size_id, price_cents, currency)
VALUES
  ('11111111-2222-3333-4444-555555555555', '15151515-1515-1515-1515-151515151501', '13131313-1313-1313-1313-131313131313', 5200, 'RSD'),
  ('11111111-2222-3333-4444-555555555555', '15151515-1515-1515-1515-151515151502', '13131313-1313-1313-1313-131313131313', 3100, 'RSD'),
  ('11111111-2222-3333-4444-555555555555', '15151515-1515-1515-1515-151515151503', '13131313-1313-1313-1313-131313131313', 2500, 'RSD'),
  ('11111111-2222-3333-4444-555555555555', '15151515-1515-1515-1515-151515151504', '13131313-1313-1313-1313-131313131313', 1100, 'RSD'),
  ('11111111-2222-3333-4444-555555555555', '15151515-1515-1515-1515-151515151505', '13131313-1313-1313-1313-131313131313', 4400, 'RSD');

INSERT INTO salon_gallery (
  tenant_id, title, caption, media_type, media_url, storage_key, alt_text, display_order, is_published
) VALUES
  ('11111111-2222-3333-4444-555555555555', 'Studio light', 'Studio light', 'IMAGE',
   'images/lapastudio/gallery-1.jpg', 'images/lapastudio/gallery-1.jpg', 'Pas pod studijskim svetlom', 1, TRUE),
  ('11111111-2222-3333-4444-555555555555', 'Fresh cut', 'Fresh cut', 'IMAGE',
   'images/lapastudio/gallery-2.jpg', 'images/lapastudio/gallery-2.jpg', 'Pas posle signature cut-a', 2, TRUE),
  ('11111111-2222-3333-4444-555555555555', 'City walk', 'City walk', 'IMAGE',
   'images/lapastudio/gallery-3.jpg', 'images/lapastudio/gallery-3.jpg', 'Šetnja posle tretmana', 3, TRUE),
  ('11111111-2222-3333-4444-555555555555', 'Happy client', 'Happy client', 'IMAGE',
   'images/lapastudio/gallery-4.jpg', 'images/lapastudio/gallery-4.jpg', 'Zadovoljan pas i vlasnik', 4, TRUE);

INSERT INTO testimonials (
  tenant_id, author_name, author_role, content, rating, avatar_url, avatar_storage_key, is_published, display_order
) VALUES
  ('11111111-2222-3333-4444-555555555555', 'Jelena M.', 'Coco, pudlica',
   'Signature cut je tačno ono što sam htela. Coco izgleda kao sa naslovnice.',
   5, 'images/lapastudio/gallery-2.jpg', 'images/lapastudio/gallery-2.jpg', TRUE, 1),
  ('11111111-2222-3333-4444-555555555555', 'Marko D.', 'Leo, francuski buldog',
   'Urban vibe, bez stresa. Glow spa je Leo-ov novi ritual.',
   5, 'images/lapastudio/gallery-1.jpg', 'images/lapastudio/gallery-1.jpg', TRUE, 2),
  ('11111111-2222-3333-4444-555555555555', 'Sara K.', 'Nala, samojed',
   'Zakazivanje online, termin tačan, komunikacija top. Vračar lokacija je super.',
   5, 'images/lapastudio/gallery-4.jpg', 'images/lapastudio/gallery-4.jpg', TRUE, 3);

INSERT INTO site_sections (
  tenant_id, section_key, title, subtitle, body, content_json, image_url, image_storage_key, cta_label, cta_url, display_order
) VALUES
  (
    '11111111-2222-3333-4444-555555555555',
    'hero',
    'Gradski grooming sa stavom.',
    'Gradski grooming sa <em>stavom</em>.',
    'Lapa Studio je mesto gde stil sreće negu. Brzo zakazivanje, jasan meni i loyalty sa 30% na svaki 6. tretman.',
    '{"badge":"Njegoševa 12, Vračar","floatTitle":"Clean studio energy","floatSubtitle":"bez gužve, bez stresa"}'::jsonb,
    'images/lapastudio/hero.jpg',
    'images/lapastudio/hero.jpg',
    'Rezerviši slot',
    '/book',
    1
  ),
  (
    '11111111-2222-3333-4444-555555555555',
    'stats',
    'Statistika',
    NULL,
    NULL,
    '{"items":[{"value":"1.2k+","label":"studio poseta"},{"value":"−30%","label":"na svaki 6. tretman"},{"value":"25–100","label":"min po tretmanu"}]}'::jsonb,
    NULL, NULL, NULL, NULL, 2
  ),
  (
    '11111111-2222-3333-4444-555555555555',
    'marquee',
    'Marquee',
    NULL,
    NULL,
    '{"items":["Signature cut","Fresh wash","Glow spa","Paw care","Vračar","Studio light","No rush"]}'::jsonb,
    NULL, NULL, NULL, NULL, 3
  ),
  (
    '11111111-2222-3333-4444-555555555555',
    'booking_steps',
    'Kako funkcioniše',
    NULL,
    NULL,
    '{"steps":[{"title":"Nalog","text":"Otvori demo nalog za minut — prava autentifikacija stiže kasnije."},{"title":"Slot","text":"Izaberi uslugu i termin. Meni je jasan, bez telefoniranja."},{"title":"Loyalty","text":"Šest pečata u ciklusu. Šesti tretman dolazi sa 30% popusta."}]}'::jsonb,
    NULL, NULL, NULL, NULL, 4
  );

INSERT INTO tenant_features (tenant_id, feature_key, is_enabled, config_json)
VALUES
  (
    '11111111-2222-3333-4444-555555555555',
    'online_booking',
    TRUE,
    '{"timeSlots":["10:00","11:30","13:00","14:30","16:00","17:30","19:00"]}'::jsonb
  ),
  (
    '11111111-2222-3333-4444-555555555555',
    'loyalty',
    TRUE,
    '{"treatmentsPerReward":6,"rewardDiscount":0.3}'::jsonb
  ),
  (
    '11111111-2222-3333-4444-555555555555',
    'gallery',
    TRUE,
    '{}'::jsonb
  ),
  (
    '11111111-2222-3333-4444-555555555555',
    'testimonials',
    TRUE,
    '{}'::jsonb
  );
