INSERT INTO USERS(first_name,last_name,phone,email,password_hash,role,is_banned)
VALUES ('Ivan', 'Ivanov', '123', 'abc@gmail.com','$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', 'user',false),
       ('Petro','Petrenko','456','xyz@gmail.com','$2a$10$HvPRLsI/6l68VMgwGx9.u.fkOCDzvkByJAhPWOqawqOGURDZCgeS.','user',false),
       ('Mykola','Curator','789','curator@gmail.com','$2a$10$FPJpgU0ctv17xQfnY5K8tuGFuvM04BJ4xvCASSrIOr.loEv07.YRO','curator',false),
       ('Ivan','Curator','228','curator1@gmail.com','$2a$10$FPJpgU0ctv17xQfnY5K8tuGFuvM04BJ4xvCASSrIOr.loEv07.YRO','curator',false);

-- Теги
INSERT INTO tag (name) VALUES
                           ('Oil Painting'), ('Digital Art'), ('Abstract'), ('Portrait'), ('Landscape');

-- Выставки (3 штуки для покрытия всех сценариев)
INSERT INTO exhibition (curator_id, title, theme, description, background_url, start_date, status, thumbnail_url) VALUES
                                                                                                                      (3, 'Spring Awakening', 'Nature & Rebirth', 'A beautiful collection of spring-themed art.', 'https://picsum.photos/seed/bg1/1920/1080', CURRENT_TIMESTAMP - INTERVAL '10 days', 'running', 'https://picsum.photos/seed/thumb1/400/300'),
                                                                                                                      (3, 'Digital Frontiers', 'Modern Digital Art', 'Exploring the boundaries of digital mediums.', 'https://picsum.photos/seed/bg2/1920/1080', CURRENT_TIMESTAMP - INTERVAL '30 days', 'converted_into_auction', 'https://picsum.photos/seed/thumb2/400/300'),
                                                                                                                      (3, 'Future Visions', 'Sci-Fi Concepts', 'Visions of tomorrow.', 'https://picsum.photos/seed/bg3/1920/1080', CURRENT_TIMESTAMP - INTERVAL '15 days', 'converted_into_auction', 'https://picsum.photos/seed/thumb3/400/300');

-- Аукционы (Строго 1-к-1: каждый на свою уникальную выставку)
INSERT INTO auction (exhibition_id, start_date, auction_step, status, end_date) VALUES
                                                                                    (2, CURRENT_TIMESTAMP - INTERVAL '15 days', 50.00, 'finished', CURRENT_TIMESTAMP - INTERVAL '5 days'), -- Завершенный
                                                                                    (3, CURRENT_TIMESTAMP - INTERVAL '2 days', 100.00, 'active', CURRENT_TIMESTAMP + INTERVAL '5 days');   -- Активный

-- Картины (Распределены между User 1 и User 2)
INSERT INTO work (owner_id, exhibition_id, title, author, description, technique, creation_date, photo_url, start_price, status) VALUES
                                                                                                                                     (1, 1, 'Morning Dew', 'John Doe', 'A calm morning.', 'Oil on Canvas', '2023-05-10', 'https://picsum.photos/seed/work1/800/800', 500.00, 'available'), -- Выставка 1
                                                                                                                                     (2, 1, 'Neon City', 'Jane Smith', 'Cyberpunk vibes.', 'Digital', '2024-01-15', 'https://picsum.photos/seed/work2/800/800', 300.00, 'available'),    -- Выставка 1
                                                                                                                                     (1, 2, 'The Algorithm', 'John Doe', 'Visualizing algorithms.', 'Digital', '2022-11-20', 'https://picsum.photos/seed/work3/800/800', 150.00, 'sold'),   -- Завершенный Аукцион (Продана)
                                                                                                                                     (2, 2, 'Pixel Sunrise', 'Jane Smith', '8-bit style sunrise.', 'Pixel Art', '2024-02-10', 'https://picsum.photos/seed/work4/800/800', 100.00, 'available'), -- Завершенный Аукцион (Не продана)
                                                                                                                                     (1, 3, 'Cyber Portrait', 'John Doe', 'A futuristic portrait.', 'Digital', '2023-08-05', 'https://picsum.photos/seed/work5/800/800', 250.00, 'available'), -- Активный Аукцион
                                                                                                                                     (2, NULL, 'Hidden Gem', 'Jane Smith', 'A sketch from my notebook.', 'Pencil', '2021-06-12', 'https://picsum.photos/seed/work6/800/800', 50.00, 'available'); -- Просто в профиле

-- Связи картин и тегов
INSERT INTO tag_work (tag_id, work_id) VALUES
                                           (1, 1), (5, 1),
                                           (2, 2),
                                           (2, 3), (3, 3),
                                           (2, 4), (5, 4),
                                           (2, 5), (4, 5),
                                           (4, 6);

-- Лоты (Для картин, участвующих в аукционах)
INSERT INTO lot (work_id, auction_id, current_price, end_date, status) VALUES
                                                                           (3, 1, 250.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'sold'),   -- Проданный
                                                                           (4, 1, 100.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'unsold'), -- Без ставок
                                                                           (5, 2, 450.00, CURRENT_TIMESTAMP + INTERVAL '5 days', 'available'); -- Активный торгующийся

-- Ставки (Bids)
INSERT INTO bid (user_id, lot_id, amount, created_at, is_win) VALUES
                                                                  (2, 1, 200.00, CURRENT_TIMESTAMP - INTERVAL '6 days', FALSE),
                                                                  (2, 1, 250.00, CURRENT_TIMESTAMP - INTERVAL '5 days', TRUE), -- User 2 выиграл лот 1
                                                                  (2, 3, 350.00, CURRENT_TIMESTAMP - INTERVAL '1 days', FALSE),
                                                                  (2, 3, 450.00, CURRENT_TIMESTAMP - INTERVAL '2 hours', FALSE);

-- История покупок (Ожидание оплаты)
INSERT INTO purchase_history (user_id, lot_id, final_price, win_date, status) VALUES
    (2, 1, 250.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'pending_payment'); -- User 2 должен оплатить

-- =============================================================================
-- Дополнительные демо-данные (разные сценарии UI и бизнес-логики)
-- Пароль у всех тестовых аккаунтов тот же, что у Ivan (см. README / .env.example)
-- =============================================================================

-- Пользователи: активные покупатели, админ, забаненный
INSERT INTO users (first_name, last_name, phone, email, password_hash, role, is_banned) VALUES
                                                                                            ('Olena', 'Buyer', '111', 'olena@gmail.com', '$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', 'user', false),
                                                                                            ('Andriy', 'Collector', '222', 'andriy@gmail.com', '$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', 'user', false),
                                                                                            ('Admin', 'System', '333', 'admin@gmail.com', '$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', 'admin', false),
                                                                                            ('Viktor', 'Banned', '444', 'banned@gmail.com', '$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', 'user', true);

INSERT INTO tag (name) VALUES
                           ('Sculpture'),
                           ('Watercolor'),
                           ('Still Life'),
                           ('Minimalism');

-- Выставка без аукциона (только каталог)
INSERT INTO exhibition (curator_id, title, theme, description, background_url, start_date, status, thumbnail_url) VALUES
                                                                                                                      (3, 'Urban Shadows', 'Street & City', 'Street art and urban photography from local artists.',
                                                                                                                       'https://picsum.photos/seed/bg4/1920/1080', CURRENT_TIMESTAMP - INTERVAL '3 days', 'running',
                                                                                                                       'https://picsum.photos/seed/thumb4/400/300'),
                                                                                                                      (3, 'Silent Gallery', 'Minimal Forms', 'Quiet compositions and negative space.',
                                                                                                                       'https://picsum.photos/seed/bg5/1920/1080', CURRENT_TIMESTAMP - INTERVAL '20 days', 'converted_into_auction',
                                                                                                                       'https://picsum.photos/seed/thumb5/400/300'),
                                                                                                                      (3, 'Last Light', 'Golden Hour', 'Works capturing the last rays of the day — live bidding.',
                                                                                                                       'https://picsum.photos/seed/bg6/1920/1080', CURRENT_TIMESTAMP - INTERVAL '7 days', 'converted_into_auction',
                                                                                                                       'https://picsum.photos/seed/thumb6/400/300'),
                                                                                                                      (3, 'Heritage Room', 'Classics Revisited', 'Finished auction with mixed lot outcomes.',
                                                                                                                       'https://picsum.photos/seed/bg7/1920/1080', CURRENT_TIMESTAMP - INTERVAL '45 days', 'converted_into_auction',
                                                                                                                       'https://picsum.photos/seed/thumb7/400/300');

-- Аукционы: запланированный, «горячий» активный, завершённый
INSERT INTO auction (exhibition_id, start_date, auction_step, status, end_date) VALUES
                                                                                    (5, CURRENT_TIMESTAMP + INTERVAL '3 days', 75.00, 'scheduled', CURRENT_TIMESTAMP + INTERVAL '10 days'),
                                                                                    (6, CURRENT_TIMESTAMP - INTERVAL '1 day', 50.00, 'active', CURRENT_TIMESTAMP + INTERVAL '2 hours'),
                                                                                    (7, CURRENT_TIMESTAMP - INTERVAL '20 days', 25.00, 'finished', CURRENT_TIMESTAMP - INTERVAL '10 days');

-- Картины
INSERT INTO work (owner_id, exhibition_id, title, author, description, technique, creation_date, photo_url, start_price, status) VALUES
                                                                                                                                     (4, 4, 'Rain on Asphalt', 'Olena K.', 'Wet reflections after summer rain.', 'Photography', '2024-06-01', 'https://picsum.photos/seed/work7/800/800', 180.00, 'available'),
                                                                                                                                     (5, 4, 'Subway Mosaic', 'Andriy P.', 'Underground station patterns.', 'Mixed Media', '2023-11-12', 'https://picsum.photos/seed/work8/800/800', 220.00, 'available'),
                                                                                                                                     (1, 4, 'Night Tram', 'John Doe', 'Yellow tram in blue hour.', 'Oil on Canvas', '2022-09-03', 'https://picsum.photos/seed/work9/800/800', 400.00, 'available'),
                                                                                                                                     (2, 5, 'White Cube', 'Jane Smith', 'Almost empty canvas — tension in silence.', 'Acrylic', '2024-03-20', 'https://picsum.photos/seed/work10/800/800', 120.00, 'available'),
                                                                                                                                     (4, 5, 'Single Line', 'Olena K.', 'One continuous stroke portrait.', 'Ink', '2023-12-01', 'https://picsum.photos/seed/work11/800/800', 90.00, 'available'),
                                                                                                                                     (5, 6, 'Dusk Harbor', 'Andriy P.', 'Boats and orange sky.', 'Watercolor', '2021-07-19', 'https://picsum.photos/seed/work12/800/800', 300.00, 'available'),
                                                                                                                                     (1, 6, 'Sunset Ridge', 'John Doe', 'Mountain silhouette at sunset.', 'Oil on Canvas', '2020-04-15', 'https://picsum.photos/seed/work13/800/800', 350.00, 'available'),
                                                                                                                                     (2, 6, 'Afterglow', 'Jane Smith', 'Abstract warm gradients.', 'Digital', '2024-08-30', 'https://picsum.photos/seed/work14/800/800', 200.00, 'available'),
                                                                                                                                     (4, 7, 'Old Masters Study', 'Olena K.', 'Copy exercise in classical style.', 'Oil on Canvas', '2019-01-10', 'https://picsum.photos/seed/work15/800/800', 80.00, 'sold'),
                                                                                                                                     (5, 7, 'Porcelain Vase', 'Andriy P.', 'Still life with chipped vase.', 'Watercolor', '2022-05-22', 'https://picsum.photos/seed/work16/800/800', 60.00, 'sold'),
                                                                                                                                     (1, 7, 'Withdrawn Frame', 'John Doe', 'Lot removed by curator before end.', 'Mixed Media', '2018-11-05', 'https://picsum.photos/seed/work17/800/800', 110.00, 'available'),
                                                                                                                                     (2, 1, 'Blossom Field', 'Jane Smith', 'Extra work on running exhibition Spring Awakening.', 'Oil on Canvas', '2024-04-01', 'https://picsum.photos/seed/work18/800/800', 275.00, 'available'),
                                                                                                                                     (4, NULL, 'Studio Sketch', 'Olena K.', 'Personal portfolio only, not on exhibition.', 'Charcoal', '2025-01-08', 'https://picsum.photos/seed/work19/800/800', 40.00, 'available');

INSERT INTO tag_work (tag_id, work_id) VALUES
                                           (5, 7), (6, 7),
                                           (3, 8), (5, 8),
                                           (5, 9),
                                           (9, 10), (3, 10),
                                           (9, 11), (4, 11),
                                           (6, 12), (5, 12),
                                           (1, 13), (5, 13),
                                           (2, 14), (3, 14),
                                           (1, 15), (7, 15),
                                           (7, 16), (5, 16),
                                           (3, 17),
                                           (1, 18), (5, 18),
                                           (4, 19);

-- Лоты
INSERT INTO lot (work_id, auction_id, current_price, end_date, status) VALUES
                                                                           (10, 3, 120.00, CURRENT_TIMESTAMP + INTERVAL '10 days', 'scheduled'),
                                                                           (11, 3, 90.00, CURRENT_TIMESTAMP + INTERVAL '10 days', 'scheduled'),
                                                                           (12, 4, 550.00, CURRENT_TIMESTAMP + INTERVAL '2 hours', 'available'),
                                                                           (13, 4, 400.00, CURRENT_TIMESTAMP + INTERVAL '2 hours', 'available'),
                                                                           (14, 4, 200.00, CURRENT_TIMESTAMP + INTERVAL '2 hours', 'available'),
                                                                           (15, 5, 205.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'sold'),
                                                                           (16, 5, 95.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'sold'),
                                                                           (17, 5, 110.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'cancelled');

-- Ставки: завершённый лот, «война» на Last Light, ставки на активный лот 3
INSERT INTO bid (user_id, lot_id, amount, created_at, is_win) VALUES
                                                                  (1, 6, 300.00, CURRENT_TIMESTAMP - INTERVAL '20 hours', false),
                                                                  (2, 6, 350.00, CURRENT_TIMESTAMP - INTERVAL '18 hours', false),
                                                                  (4, 6, 400.00, CURRENT_TIMESTAMP - INTERVAL '12 hours', false),
                                                                  (5, 6, 450.00, CURRENT_TIMESTAMP - INTERVAL '8 hours', false),
                                                                  (1, 6, 500.00, CURRENT_TIMESTAMP - INTERVAL '4 hours', false),
                                                                  (4, 6, 550.00, CURRENT_TIMESTAMP - INTERVAL '1 hour', false),
                                                                  (2, 7, 350.00, CURRENT_TIMESTAMP - INTERVAL '6 hours', false),
                                                                  (4, 7, 400.00, CURRENT_TIMESTAMP - INTERVAL '3 hours', false),
                                                                  (1, 9, 130.00, CURRENT_TIMESTAMP - INTERVAL '12 days', false),
                                                                  (4, 9, 180.00, CURRENT_TIMESTAMP - INTERVAL '11 days', false),
                                                                  (5, 9, 205.00, CURRENT_TIMESTAMP - INTERVAL '10 days', true),
                                                                  (1, 10, 70.00, CURRENT_TIMESTAMP - INTERVAL '11 days', false),
                                                                  (4, 10, 95.00, CURRENT_TIMESTAMP - INTERVAL '10 days', true),
                                                                  (1, 3, 300.00, CURRENT_TIMESTAMP - INTERVAL '3 hours', false),
                                                                  (4, 3, 400.00, CURRENT_TIMESTAMP - INTERVAL '1 hour', false);

-- История покупок: completed, pending_shipment, cancelled (+ pending_payment выше на lot 1)
INSERT INTO purchase_history (user_id, lot_id, final_price, win_date, status) VALUES
                                                                                  (5, 9, 205.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'completed'),
                                                                                  (4, 10, 95.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'pending_shipment'),
                                                                                  (1, 10, 95.00, CURRENT_TIMESTAMP - INTERVAL '9 days', 'cancelled');

-- =============================================================================
-- МЕГА-ГЕНЕРАТОР ДАНИХ: 30 виставок, 20 аукціонів, сотні робіт та ставок
-- =============================================================================
DO $$
    DECLARE
        v_curator_id BIGINT;
        v_exhibition_id BIGINT;
        v_auction_id BIGINT;
        v_work_id BIGINT;
        v_lot_id BIGINT;
        v_user_id BIGINT;

        i INT;
        j INT;
        b INT;

        -- Назви для 30 виставок
        titles TEXT[] := ARRAY[
            'Whispers of Nature', 'Urban Geometry', 'Faces of the World', 'Abstract Dimensions', 'Silent Ocean',
            'Neon Dreams', 'Classic Revival', 'Surreal Mind', 'Industrial Era', 'Future Tech',
            'Minimalist Approach', 'Vibrant Colors', 'Dark Fantasy', 'Golden Age', 'Modern Sculptures',
            'Digital Canvas', 'Monochrome Dreams', 'Street Art Vibes', 'Renaissance 2.0', 'Pop Culture',
            'Mythology Today', 'Space Exploration', 'Deep Sea', 'Forest Magic', 'Desert Heat',
            'Frozen Time', 'Liquid Metal', 'Ethereal Light', 'Shadow Play', 'Cosmic Dust'
            ];

        -- Частини для генерації назв картин
        work_prefixes TEXT[] := ARRAY['Morning', 'Dark', 'Golden', 'Fading', 'Hidden', 'Silent', 'Neon', 'Forgotten', 'Abstract', 'Vivid'];
        work_suffixes TEXT[] := ARRAY['Light', 'Shadows', 'City', 'Portrait', 'Landscape', 'Dream', 'Illusion', 'Concept', 'Figure', 'Space'];

        -- Техніки
        techs TEXT[] := ARRAY['Oil on Canvas', 'Digital', 'Watercolor', 'Acrylic', 'Mixed Media', 'Photography', 'Sculpture', 'Charcoal', 'Pencil', 'Ink'];

        v_status exhibition_status;
        v_auction_status auction_status;
        v_lot_status lot_status;

        v_base_date TIMESTAMPTZ;
        v_auction_start TIMESTAMPTZ;
        v_auction_end TIMESTAMPTZ;

        v_price NUMERIC;
        v_auction_step NUMERIC;

    BEGIN
        -- Отримуємо ID будь-якого куратора (наприклад, Mykola Curator з вашого скрипта)
        SELECT id INTO v_curator_id FROM users WHERE role = 'curator' LIMIT 1;

        -- 1. СТВОРЮЄМО 15 НОВИХ АКТИВНИХ КОЛЕКЦІОНЕРІВ (ЮЗЕРІВ)
        FOR i IN 1..15 LOOP
                INSERT INTO users (first_name, last_name, phone, email, password_hash, role, is_banned)
                VALUES (
                           'User' || i,
                           'Collector' || i,
                           '090' || LPAD(i::text, 7, '0'),
                           'mega_collector' || i || '@gmail.com',
                           '$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', -- Стандартний пароль
                           'user',
                           false
                       );
            END LOOP;

        -- 2. СТВОРЮЄМО 30 ВИСТАВОК
        FOR i IN 1..30 LOOP
                -- Перші 20 виставок будуть аукціонами, інші 10 - просто виставками
                IF i <= 20 THEN v_status := 'converted_into_auction';
                ELSE v_status := 'running';
                END IF;

                v_base_date := CURRENT_TIMESTAMP - (random() * 60 || ' days')::interval;

                INSERT INTO exhibition (curator_id, title, theme, description, background_url, start_date, status, thumbnail_url)
                VALUES (
                           v_curator_id,
                           titles[i],
                           'Epic Theme ' || i,
                           'A massive collection of incredible artworks featuring ' || titles[i],
                           'https://picsum.photos/seed/mega_bg' || i || '/1920/1080',
                           v_base_date,
                           v_status,
                           'https://picsum.photos/seed/mega_thumb' || i || '/400/300'
                       ) RETURNING id INTO v_exhibition_id;

                -- 3. СТВОРЮЄМО АУКЦІОНИ (ДЛЯ 20 ВИСТАВОК)
                IF v_status = 'converted_into_auction' THEN
                    -- Розподіл: 8 завершених, 7 активних, 5 запланованих
                    IF i <= 8 THEN
                        v_auction_status := 'finished';
                        v_auction_start := CURRENT_TIMESTAMP - (random() * 20 + 10 || ' days')::interval;
                        v_auction_end := v_auction_start + INTERVAL '7 days';
                    ELSIF i <= 15 THEN
                        v_auction_status := 'active';
                        v_auction_start := CURRENT_TIMESTAMP - (random() * 5 || ' days')::interval;
                        v_auction_end := CURRENT_TIMESTAMP + (random() * 5 + 1 || ' days')::interval;
                    ELSE
                        v_auction_status := 'scheduled';
                        v_auction_start := CURRENT_TIMESTAMP + (random() * 5 + 1 || ' days')::interval;
                        v_auction_end := v_auction_start + INTERVAL '7 days';
                    END IF;

                    v_auction_step := (floor(random() * 10) + 1) * 25.00; -- Крок від 25 до 250

                    INSERT INTO auction (exhibition_id, start_date, auction_step, status, end_date)
                    VALUES (v_exhibition_id, v_auction_start, v_auction_step, v_auction_status, v_auction_end)
                    RETURNING id INTO v_auction_id;
                ELSE
                    v_auction_id := NULL;
                END IF;

                -- 4. ГЕНЕРУЄМО КАРТИНИ (від 4 до 8 на КОЖНУ виставку)
                FOR j IN 1..(floor(random() * 5) + 4) LOOP
                        -- Вибираємо випадкового власника картини
                        SELECT id INTO v_user_id FROM users WHERE role = 'user' ORDER BY random() LIMIT 1;
                        v_price := (floor(random() * 40) + 10) * 50.00; -- Стартова ціна від 500 до 2500

                        INSERT INTO work (owner_id, exhibition_id, title, author, description, technique, creation_date, photo_url, start_price, status)
                        VALUES (
                                   v_user_id,
                                   v_exhibition_id,
                                   work_prefixes[floor(random() * 10) + 1] || ' ' || work_suffixes[floor(random() * 10) + 1] || ' ' || j,
                                   'Artist ' || (floor(random() * 500) + 1),
                                   'A magnificent piece of art created with passion.',
                                   techs[floor(random() * 10) + 1],
                                   CURRENT_DATE - (random() * 2000 || ' days')::interval,
                                   'https://picsum.photos/seed/mega_work_' || v_exhibition_id || '_' || j || '/800/800',
                                   v_price,
                                   'available'
                               ) RETURNING id INTO v_work_id;

                        -- Додаємо до роботи 1–3 випадкові теги (для фільтрів/виводу в UI)
                        INSERT INTO tag_work (tag_id, work_id)
                        SELECT t.id, v_work_id
                        FROM tag t
                        ORDER BY random()
                        LIMIT (floor(random() * 3) + 1)::int;

                        -- 5. ЯКЩО ЦЕ АУКЦІОН — СТВОРЮЄМО ЛОТИ ТА СИМУЛЮЄМО ТОРГИ (BIDDING WARS)
                        IF v_auction_id IS NOT NULL THEN
                            IF v_auction_status = 'scheduled' THEN v_lot_status := 'scheduled';
                            ELSIF v_auction_status = 'active' THEN v_lot_status := 'available';
                            ELSE
                                -- Для завершених аукціонів: 80% лотів продано, 20% не продано
                                IF random() > 0.2 THEN v_lot_status := 'sold'; ELSE v_lot_status := 'unsold'; END IF;
                            END IF;

                            INSERT INTO lot (work_id, auction_id, current_price, end_date, status)
                            VALUES (v_work_id, v_auction_id, v_price, v_auction_end, v_lot_status)
                            RETURNING id INTO v_lot_id;

                            -- Симуляція ставок (для активних та проданих лотів)
                            IF (v_auction_status = 'active') OR (v_auction_status = 'finished' AND v_lot_status = 'sold') THEN
                                -- Робимо від 1 до 7 ставок на кожен лот (імітація активності)
                                FOR b IN 1..(floor(random() * 7) + 1) LOOP
                                        SELECT id INTO v_user_id FROM users WHERE role = 'user' ORDER BY random() LIMIT 1;

                                        -- Підіймаємо ціну (крок аукціону * 1 або 2)
                                        v_price := v_price + (v_auction_step * (floor(random() * 2) + 1));

                                        INSERT INTO bid (user_id, lot_id, amount, created_at, is_win)
                                        VALUES (
                                                   v_user_id,
                                                   v_lot_id,
                                                   v_price,
                                                   v_auction_start + (b * INTERVAL '4 hours'), -- Хронологія ставок
                                                   FALSE
                                               );
                                    END LOOP;

                                -- Оновлюємо фінальну/поточну ціну лота після всіх ставок
                                UPDATE lot SET current_price = v_price WHERE id = v_lot_id;

                                -- Якщо лот проданий (аукціон закінчився) — фіксуємо переможця
                                IF v_lot_status = 'sold' THEN
                                    UPDATE bid SET is_win = TRUE WHERE lot_id = v_lot_id AND amount = v_price;
                                    UPDATE work SET status = 'sold' WHERE id = v_work_id;

                                    -- Додаємо випадковий статус в історію покупок
                                    INSERT INTO purchase_history (user_id, lot_id, final_price, win_date, status)
                                    VALUES (
                                               v_user_id,
                                               v_lot_id,
                                               v_price,
                                               v_auction_end,
                                               (ARRAY['pending_payment', 'pending_shipment', 'completed']::purchase_status[])[floor(random() * 3) + 1]
                                           );
                                END IF;
                            END IF;
                        END IF;
                    END LOOP;
            END LOOP;
    END $$;
