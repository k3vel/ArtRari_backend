INSERT INTO USERS(first_name,last_name,phone,email,password_hash,role,is_banned)
VALUES ('Ivan', 'Ivanov', '123', 'abc@gmail.com','$2a$10$N1m7kGCKNuXMehcfx1gQkOOWseeZRrtwJWER2GiKVE06GejG8eLgm', 'user',false),
       ('Petro','Petrenko','456','xyz@gmail.com','$2a$10$HvPRLsI/6l68VMgwGx9.u.fkOCDzvkByJAhPWOqawqOGURDZCgeS.','user',false),
       ('Mykola','Curator','789','curator@gmail.com','$2a$10$FPJpgU0ctv17xQfnY5K8tuGFuvM04BJ4xvCASSrIOr.loEv07.YRO','curator',false);

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