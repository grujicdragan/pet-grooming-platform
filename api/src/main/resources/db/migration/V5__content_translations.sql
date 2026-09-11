-- =============================================================================
-- Content localization
--  * content_translations: per-tenant, per-locale overrides for text fields of
--    content entities (services, testimonials, gallery, site sections, profile).
--    The base row keeps the tenant's default language; API overlays a locale.
--  * localization feature: which locales a tenant offers (drives the dropdown).
--  * en + ru translations for the Sapelier tenant.
-- =============================================================================

SET app.bypass_rls = 'true';

CREATE TABLE content_translations (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id    UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  locale       VARCHAR(10)  NOT NULL,           -- BCP 47 language tag: 'en', 'ru', 'sr'
  entity_type  VARCHAR(50)  NOT NULL,           -- salon_profile | service | testimonial | gallery | site_section
  entity_id    UUID         NOT NULL,           -- id of the translated row
  field        VARCHAR(50)  NOT NULL,           -- column name on the translated row
  value        TEXT,                            -- for text columns
  value_json   JSONB,                           -- for jsonb columns (e.g. site_sections.content_json)
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_content_translation UNIQUE (tenant_id, locale, entity_type, entity_id, field),
  CONSTRAINT ck_content_translation_value CHECK (value IS NOT NULL OR value_json IS NOT NULL)
);

CREATE INDEX idx_content_translations_lookup
  ON content_translations (tenant_id, locale);

CREATE TRIGGER trg_content_translations_updated_at
  BEFORE UPDATE ON content_translations
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

ALTER TABLE content_translations ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_translations FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_select ON content_translations
  FOR SELECT USING (bypass_rls() OR tenant_id = current_tenant_id());
CREATE POLICY tenant_isolation_insert ON content_translations
  FOR INSERT WITH CHECK (bypass_rls() OR tenant_id = current_tenant_id());
CREATE POLICY tenant_isolation_update ON content_translations
  FOR UPDATE USING (bypass_rls() OR tenant_id = current_tenant_id())
  WITH CHECK (bypass_rls() OR tenant_id = current_tenant_id());
CREATE POLICY tenant_isolation_delete ON content_translations
  FOR DELETE USING (bypass_rls() OR tenant_id = current_tenant_id());

-- -----------------------------------------------------------------------------
-- Sapelier: offered locales
-- -----------------------------------------------------------------------------

INSERT INTO tenant_features (tenant_id, feature_key, is_enabled, config_json)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'localization',
  TRUE,
  '{"default":"sr","locales":["sr","en","ru"]}'::jsonb
)
ON CONFLICT (tenant_id, feature_key) DO UPDATE
  SET is_enabled = EXCLUDED.is_enabled, config_json = EXCLUDED.config_json;

-- -----------------------------------------------------------------------------
-- Sapelier translations
-- -----------------------------------------------------------------------------

DO $$
DECLARE
  t   UUID := 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
  prof UUID;
  sec_hero UUID; sec_stats UUID; sec_marquee UUID; sec_steps UUID;
  tst1 UUID; tst2 UUID; tst3 UUID;
  gal1 UUID; gal2 UUID; gal3 UUID; gal4 UUID;
BEGIN
  SELECT id INTO prof FROM salon_profile WHERE tenant_id = t;
  SELECT id INTO sec_hero    FROM site_sections WHERE tenant_id = t AND section_key = 'hero';
  SELECT id INTO sec_stats   FROM site_sections WHERE tenant_id = t AND section_key = 'stats';
  SELECT id INTO sec_marquee FROM site_sections WHERE tenant_id = t AND section_key = 'marquee';
  SELECT id INTO sec_steps   FROM site_sections WHERE tenant_id = t AND section_key = 'booking_steps';
  SELECT id INTO tst1 FROM testimonials WHERE tenant_id = t AND display_order = 1;
  SELECT id INTO tst2 FROM testimonials WHERE tenant_id = t AND display_order = 2;
  SELECT id INTO tst3 FROM testimonials WHERE tenant_id = t AND display_order = 3;
  SELECT id INTO gal1 FROM salon_gallery WHERE tenant_id = t AND display_order = 1;
  SELECT id INTO gal2 FROM salon_gallery WHERE tenant_id = t AND display_order = 2;
  SELECT id INTO gal3 FROM salon_gallery WHERE tenant_id = t AND display_order = 3;
  SELECT id INTO gal4 FROM salon_gallery WHERE tenant_id = t AND display_order = 4;

  INSERT INTO content_translations (tenant_id, locale, entity_type, entity_id, field, value) VALUES
  -- ========================== ENGLISH ==========================
  (t, 'en', 'salon_profile', prof, 'tagline',     'a grooming atelier for dogs'),
  (t, 'en', 'salon_profile', prof, 'description', 'Šapelier is a quiet, warm grooming atelier for dogs. Booking takes seconds and the loyalty program is simple: every fifth treatment comes with 40% off.'),

  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'name',        'Full groom'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'description', 'Bath, blow-dry, breed-specific haircut and finishing styling.'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'name',        'Bath & blow-dry'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'description', 'Gentle shampoo, blow-dry and brush-out without a full trim.'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'name',        'Trim touch-up'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'description', 'Keeps the shape between full grooms.'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'name',        'Nail trim'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'description', 'Quick, calm nail shortening and a paw check.'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'name',        'Spa package'),
  (t, 'en', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'description', 'Bath, coat mask, ear care and a light massage.'),

  (t, 'en', 'testimonial', tst1, 'content',     'Luna leaves calmer than she came in. The appointment took exactly as long as they said.'),
  (t, 'en', 'testimonial', tst1, 'author_role', 'Luna, cocker spaniel'),
  (t, 'en', 'testimonial', tst2, 'content',     'Finally a salon where Bobi doesn''t panic. The loyalty card is clear — the fifth groom is almost a gift.'),
  (t, 'en', 'testimonial', tst2, 'author_role', 'Bobi, labrador'),
  (t, 'en', 'testimonial', tst3, 'content',     'Booked in 30 seconds, no phone call. Maca loved the spa package.'),
  (t, 'en', 'testimonial', tst3, 'author_role', 'Maca, maine coon'),

  (t, 'en', 'gallery', gal1, 'caption',  'Treatment in progress'),
  (t, 'en', 'gallery', gal1, 'alt_text', 'Dog during a grooming treatment'),
  (t, 'en', 'gallery', gal2, 'caption',  'First visit to the salon'),
  (t, 'en', 'gallery', gal2, 'alt_text', 'Puppy with a soft coat'),
  (t, 'en', 'gallery', gal3, 'caption',  'After the haircut'),
  (t, 'en', 'gallery', gal3, 'alt_text', 'Dog on a walk after a haircut'),
  (t, 'en', 'gallery', gal4, 'caption',  'Home, all tidy'),
  (t, 'en', 'gallery', gal4, 'alt_text', 'Owner and dog together'),

  (t, 'en', 'site_section', sec_hero, 'title',     'Haircuts pets love to repeat.'),
  (t, 'en', 'site_section', sec_hero, 'subtitle',  'Haircuts pets <em>love</em> to repeat.'),
  (t, 'en', 'site_section', sec_hero, 'body',      'Šapelier is a quiet, warm grooming atelier for dogs. Booking takes seconds and the loyalty program is simple: every fifth treatment comes with 40% off.'),
  (t, 'en', 'site_section', sec_hero, 'cta_label', 'Book an appointment'),

  -- ========================== RUSSIAN ==========================
  (t, 'ru', 'salon_profile', prof, 'tagline',     'ателье груминга для собак'),
  (t, 'ru', 'salon_profile', prof, 'description', 'Šapelier — тихое, тёплое ателье груминга для собак. Запись занимает секунды, а программа лояльности проста: каждая пятая процедура со скидкой 40%.'),

  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'name',        'Полный груминг'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'description', 'Мытьё, сушка, стрижка по породе и финальный стайлинг.'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'name',        'Мытьё и сушка'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'description', 'Мягкий шампунь, сушка феном и расчёсывание без полной коррекции.'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'name',        'Коррекция стрижки'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'description', 'Поддержание формы между полными процедурами.'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'name',        'Стрижка когтей'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee4', 'description', 'Быстрое, спокойное укорачивание когтей и осмотр лап.'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'name',        'Spa-пакет'),
  (t, 'ru', 'service', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee5', 'description', 'Мытьё, маска для шерсти, уход за ушами и лёгкий массаж.'),

  (t, 'ru', 'testimonial', tst1, 'content',     'Луна выходит спокойнее, чем пришла. Приём длился ровно столько, сколько обещали.'),
  (t, 'ru', 'testimonial', tst1, 'author_role', 'Луна, кокер-спаниель'),
  (t, 'ru', 'testimonial', tst2, 'content',     'Наконец-то салон, где Боби не паникует. Карта лояльности понятна — пятый груминг почти подарок.'),
  (t, 'ru', 'testimonial', tst2, 'author_role', 'Боби, лабрадор'),
  (t, 'ru', 'testimonial', tst3, 'content',     'Запись за 30 секунд, без звонков. Spa-пакет Мацу привёл в восторг.'),
  (t, 'ru', 'testimonial', tst3, 'author_role', 'Маца, мейн-кун'),

  (t, 'ru', 'gallery', gal1, 'caption',  'Процедура в процессе'),
  (t, 'ru', 'gallery', gal1, 'alt_text', 'Собака во время груминга'),
  (t, 'ru', 'gallery', gal2, 'caption',  'Первый раз в салоне'),
  (t, 'ru', 'gallery', gal2, 'alt_text', 'Щенок с мягкой шерстью'),
  (t, 'ru', 'gallery', gal3, 'caption',  'После стрижки'),
  (t, 'ru', 'gallery', gal3, 'alt_text', 'Собака на прогулке после стрижки'),
  (t, 'ru', 'gallery', gal4, 'caption',  'Дома, ухоженный'),
  (t, 'ru', 'gallery', gal4, 'alt_text', 'Хозяин и собака вместе'),

  (t, 'ru', 'site_section', sec_hero, 'title',     'Стрижка, которую питомцы хотят повторить.'),
  (t, 'ru', 'site_section', sec_hero, 'subtitle',  'Стрижка, которую питомцы <em>хотят</em> повторить.'),
  (t, 'ru', 'site_section', sec_hero, 'body',      'Šapelier — тихое, тёплое ателье груминга для собак. Запись занимает секунды, а программа лояльности проста: каждая пятая процедура со скидкой 40%.'),
  (t, 'ru', 'site_section', sec_hero, 'cta_label', 'Записаться');

  -- JSON content of site sections
  INSERT INTO content_translations (tenant_id, locale, entity_type, entity_id, field, value_json) VALUES
  (t, 'en', 'site_section', sec_hero, 'content_json',
     '{"badge":"Zaplanjska 58, Voždovac","floatTitle":"Calm, no rush","floatSubtitle":"with plenty of treats"}'::jsonb),
  (t, 'en', 'site_section', sec_stats, 'content_json',
     '{"items":[{"value":"850+","label":"happy paws"},{"value":"−40%","label":"on every 5th treatment"},{"value":"20–90","label":"min per treatment"}]}'::jsonb),
  (t, 'en', 'site_section', sec_marquee, 'content_json',
     '{"items":["Bathing","Breed haircuts","Styling","Spa package","Nails & paws","No rush","Voždovac"]}'::jsonb),
  (t, 'en', 'site_section', sec_steps, 'content_json',
     '{"steps":[{"title":"Account","text":"Sign in or register in a minute. Auth arrives in the next phase."},{"title":"Date & time","text":"Pick a service, your pet, a free day and hour. Slots are visual — no phone calls."},{"title":"Loyalty","text":"A card with 5 stamps. The fifth stamp is the reward: 40% off that treatment."}]}'::jsonb),

  (t, 'ru', 'site_section', sec_hero, 'content_json',
     '{"badge":"Zaplanjska 58, Вождовац","floatTitle":"Спокойно, без спешки","floatSubtitle":"и с кучей лакомств"}'::jsonb),
  (t, 'ru', 'site_section', sec_stats, 'content_json',
     '{"items":[{"value":"850+","label":"довольных лап"},{"value":"−40%","label":"на каждую 5-ю процедуру"},{"value":"20–90","label":"мин на процедуру"}]}'::jsonb),
  (t, 'ru', 'site_section', sec_marquee, 'content_json',
     '{"items":["Мытьё","Стрижка по породе","Стайлинг","Spa-пакет","Когти и лапы","Без спешки","Вождовац"]}'::jsonb),
  (t, 'ru', 'site_section', sec_steps, 'content_json',
     '{"steps":[{"title":"Аккаунт","text":"Войдите или зарегистрируйтесь за минуту. Авторизация появится на следующем этапе."},{"title":"Дата и время","text":"Выберите услугу, питомца, свободный день и час. Слоты наглядны — без звонков."},{"title":"Лояльность","text":"Карта с 5 печатями. Пятая печать — награда: скидка 40% на эту процедуру."}]}'::jsonb);
END $$;
