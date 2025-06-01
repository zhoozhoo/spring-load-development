BEGIN;

    INSERT INTO rifles (owner_id, name, description, measurement_units, caliber, barrel_length)
    SELECT '394ef5b9-819c-4e4e-9630-905d611cbbca', 'Tikka T3x CTR', '6.5mm Creedmoor Tikka T3x CTR', 'Imperial', '6.5 Creedmoor', 20
    WHERE NOT EXISTS (
        SELECT 1 FROM rifles 
        WHERE owner_id = '394ef5b9-819c-4e4e-9630-905d611cbbca' 
        AND name = 'Tikka T3x CTR'
    );

    INSERT INTO loads (owner_id, name, description, measurement_units, powder_manufacturer, powder_type, 
        bullet_manufacturer, bullet_type, bullet_weight,
        primer_manufacturer, primer_type, case_overall_length, rifle_id)
    SELECT '394ef5b9-819c-4e4e-9630-905d611cbbca', 'H4350 Load Development', 'Initial load development', 'Imperial',
        'Hodgdon', 'H4350', 'Berger', 'Hybrid Target', 140,
        'CCI', 'BR2', 2.303,
        (SELECT id FROM rifles WHERE name = 'Tikka T3x CTR' AND owner_id = '394ef5b9-819c-4e4e-9630-905d611cbbca')
    WHERE NOT EXISTS (
        SELECT 1 FROM loads 
        WHERE owner_id = '394ef5b9-819c-4e4e-9630-905d611cbbca' 
        AND name = 'H4350 Load Development'
    );

    WITH new_groups AS (
        INSERT INTO groups (owner_id, load_id, date, powder_charge, target_range, group_size)
        SELECT v.owner_id, 
               (SELECT id FROM loads WHERE name = 'H4350 Load Development' AND owner_id = '394ef5b9-819c-4e4e-9630-905d611cbbca'),
               v.date, v.powder_charge, v.target_range, v.group_size
        FROM (VALUES 
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 37.8, 100, 0.834),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 38.1, 100, 0.514),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 38.4, 100, 0.259),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 38.7, 100, 0.444),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 39.0, 100, 1.054),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 39.3, 100, 0.628),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 39.6, 100, 0.173),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 39.9, 100, 0.630),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 40.2, 100, 0.785),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 40.5, 100, 0.738),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 40.8, 100, 0.819),
            ('394ef5b9-819c-4e4e-9630-905d611cbbca', CURRENT_DATE, 41.1, 100, 0.895)
        ) AS v(owner_id, date, powder_charge, target_range, group_size)
        WHERE NOT EXISTS (
            SELECT 1 FROM groups 
            WHERE owner_id = v.owner_id 
            AND date = v.date 
            AND powder_charge = v.powder_charge
        )
        RETURNING id, powder_charge
    )
    INSERT INTO shots (owner_id, group_id, velocity)
    SELECT '394ef5b9-819c-4e4e-9630-905d611cbbca', g.id, v.velocity
    FROM new_groups g
    CROSS JOIN LATERAL (
        VALUES 
            (37.8, 2364), (37.8, 2335), (37.8, 2328),
            (38.1, 2377), (38.1, 2339), (38.1, 2360),
            (38.4, 2397), (38.4, 2415), (38.4, 2411),
            (38.7, 2415), (38.7, 2418),
            (39.0, 2446), (39.0, 2476), (39.0, 2460),
            (39.3, 2466), (39.3, 2460), (39.3, 2458),
            (39.6, 2484), (39.6, 2465), (39.6, 2462),
            (39.9, 2490), (39.9, 2485), (39.9, 2492),
            (40.2, 2528), (40.2, 2513), (40.2, 2499),
            (40.5, 2507), (40.5, 2501), (40.5, 2548),
            (40.8, 2569), (40.8, 2559), (40.8, 2545),
            (41.1, 2556), (41.1, 2574)
        ) AS v(powder_charge, velocity)
    WHERE g.powder_charge = v.powder_charge
    ON CONFLICT DO NOTHING;

COMMIT;