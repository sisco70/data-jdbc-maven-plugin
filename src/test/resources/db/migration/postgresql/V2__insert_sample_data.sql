

INSERT INTO table_one (id, type, flag)
VALUES
    (1, 'SYSTEM_USER', true),
    (2, 'ADMIN_USER', true),
    (3, 'GUEST_USER', false),
    (4, 'SERVICE_ACCOUNT', true);


INSERT INTO table_two (id, code, json_data, json_txt, date_end)
VALUES
    (101, 200,
     '{"role": "admin", "permissions": ["read", "write", "execute"], "active": true}'::jsonb,
     '{"ui_theme": "dark", "language": "en-US"}'::json,
     '2028-12-31 23:59:59+01'),

    (102, 404,
     '{"role": "operator", "settings": {"notifications": true, "retry_count": 3}}'::jsonb,
     '{"ui_theme": "light", "language": "it-IT"}'::json,
     DEFAULT),

    (103, NULL, NULL, NULL, DEFAULT);


SELECT setval(pg_get_serial_sequence('table_two', 'id'), coalesce(max(id), 1)) FROM table_two;


INSERT INTO table_three (id, id_table_two, id_table_one, time_one, date_one, instant_one, instant_tz, doc)
VALUES
    (gen_random_uuid(), 101, 1,
     '08:30:00', '2026-08-16', '2026-08-16 08:30:00', '2026-08-16 08:30:00+02',
     '\x48656c6c6f20576f726c64'::bytea),

    (gen_random_uuid(), 102, 2,
     '14:15:30', '2026-08-15', '2026-08-15 14:15:30', '2026-08-15 14:15:30+02',
     NULL),

    (gen_random_uuid(), 103, 3,
     '18:45:00', '2026-08-10', '2026-08-10 18:45:00', '2026-08-10 18:45:00+02',
     '\x00010203040506070809'::bytea);



INSERT INTO table_four (id1, id2, number, number_larger, tag, scores, tst3)
VALUES
    (1, 101, 12.34, 123456789.012345,
     ARRAY['java', 'spring', 'pg']::varchar[],
     ARRAY[10, 20, 30, 40, 50]::int2[],
     ARRAY[1.1, 2.2, 3.3]::float4[]),

    (2, 102, 99.99, 999999999.999999,
     ARRAY['test', 'demo']::varchar[],
     ARRAY[100, 95]::int2[],
     ARRAY[0.5, 1.5, 2.5]::float4[]),

    (3, 103, NULL, NULL, NULL, NULL, NULL);



INSERT INTO table_to_be_ignored (id)
VALUES (10), (20), (30);