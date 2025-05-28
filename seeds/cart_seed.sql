-- Seed data para la base de datos de carrito

-- Carrito activo del usuario Roberto Silva (TechStart Chile)
INSERT INTO Carrito (
    id_carrito, 
    id_usuario_ext, 
    estado_carrito, 
    total_estimado, 
    notas_cliente, 
    fecha_expiracion, 
    creado_el, 
    actualizado_el
) VALUES (
    '111e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440005',
    'ACTIVO',
    500000.00,
    'Necesitamos el sitio web corporativo con diseño moderno y responsivo',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);

INSERT INTO Carrito (
    id_carrito, 
    id_usuario_ext, 
    estado_carrito, 
    total_estimado, 
    notas_cliente, 
    fecha_expiracion, 
    creado_el, 
    actualizado_el
) VALUES (
    '111e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440006',
    'PROCESADO',
    575000.00,
    'Excelente trabajo en la tienda online. Muy satisfechos con el resultado.',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '14 days'
);

INSERT INTO Carrito (
    id_carrito, 
    id_usuario_ext, 
    estado_carrito, 
    total_estimado, 
    notas_cliente, 
    fecha_expiracion, 
    creado_el, 
    actualizado_el
) VALUES (
    '111e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440007',
    'ACTIVO',
    870000.00,
    'Portal web para consultoría legal con sección de documentos y blog',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes'
);

INSERT INTO Carrito (
    id_carrito, 
    id_usuario_ext, 
    estado_carrito, 
    total_estimado, 
    notas_cliente, 
    fecha_expiracion, 
    creado_el, 
    actualizado_el
) VALUES (
    '111e8400-e29b-41d4-a716-446655440004',
    '550e8400-e29b-41d4-a716-446655440008',
    'ABANDONADO',
    170000.00,
    'Evaluando presupuesto. Posible retoma en el futuro.',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
);

INSERT INTO Carrito (
    id_carrito, 
    id_usuario_ext, 
    estado_carrito, 
    total_estimado, 
    notas_cliente, 
    fecha_expiracion, 
    creado_el, 
    actualizado_el
) VALUES (
    '111e8400-e29b-41d4-a716-446655440005',
    '550e8400-e29b-41d4-a716-446655440009',
    'EXPIRADO',
    420000.00,
    NULL,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '40 days',
    CURRENT_TIMESTAMP - INTERVAL '35 days'
);

INSERT INTO Carrito (
    id_carrito, 
    id_usuario_ext, 
    estado_carrito, 
    total_estimado, 
    notas_cliente, 
    fecha_expiracion, 
    creado_el, 
    actualizado_el
) VALUES (
    '111e8400-e29b-41d4-a716-446655440006',
    '550e8400-e29b-41d4-a716-446655440013',
    'ACTIVO',
    175000.00,
    'Landing page para emprendimiento personal',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '6 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
);


INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440001',
    '111e8400-e29b-41d4-a716-446655440001',
    2,
    1,
    350000.00,
    350000.00,
    'Sitio web de 5 páginas con diseño tech moderno y CMS',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440002',
    '111e8400-e29b-41d4-a716-446655440001',
    9,
    1,
    120000.00,
    120000.00,
    'SEO orientado a tecnología y startups',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440003',
    '111e8400-e29b-41d4-a716-446655440002',
    17,
    1,
    450000.00,
    450000.00,
    'E-commerce para productos de retail con inventario',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '14 days'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440004',
    '111e8400-e29b-41d4-a716-446655440002',
    20,
    1,
    120000.00,
    120000.00,
    'Integración con WebPay y Mercado Pago',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '14 days'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440005',
    '111e8400-e29b-41d4-a716-446655440003',
    3,
    1,
    750000.00,
    750000.00,
    'Portal legal con sección de documentos, casos y blog jurídico',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440006',
    '111e8400-e29b-41d4-a716-446655440003',
    10,
    1,
    280000.00,
    280000.00,
    'SEO especializado en servicios legales',
    CURRENT_TIMESTAMP - INTERVAL '45 minutes',
    CURRENT_TIMESTAMP - INTERVAL '15 minutes'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440007',
    '111e8400-e29b-41d4-a716-446655440004',
    1,
    1,
    150000.00,
    150000.00,
    'Landing page para restaurant con menú y reservas',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '4 days'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440008',
    '111e8400-e29b-41d4-a716-446655440005',
    8,
    1,
    300000.00,
    300000.00,
    'Modernización del sitio web de la clínica dental',
    CURRENT_TIMESTAMP - INTERVAL '40 days',
    CURRENT_TIMESTAMP - INTERVAL '35 days'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440009',
    '111e8400-e29b-41d4-a716-446655440005',
    9,
    1,
    120000.00,
    120000.00,
    'SEO para servicios dentales',
    CURRENT_TIMESTAMP - INTERVAL '40 days',
    CURRENT_TIMESTAMP - INTERVAL '35 days'
);

INSERT INTO Carrito_Item (
    id_carrito_item,
    id_carrito,
    id_servicio_ext,
    cantidad,
    precio_unitario,
    subtotal,
    personalizaciones,
    creado_el,
    actualizado_el
) VALUES (
    '222e8400-e29b-41d4-a716-446655440010',
    '111e8400-e29b-41d4-a716-446655440006',
    1,
    1,
    150000.00,
    150000.00,
    'Landing page personal para coaching y consultoría',
    CURRENT_TIMESTAMP - INTERVAL '6 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
);


INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440001',
    '222e8400-e29b-41d4-a716-446655440001',
    1,
    45000.00,
    CURRENT_TIMESTAMP - INTERVAL '1 hour 30 minutes'
);

INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440002',
    '222e8400-e29b-41d4-a716-446655440001',
    3,
    25000.00,
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
);

INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440003',
    '222e8400-e29b-41d4-a716-446655440003',
    1,
    45000.00,
    CURRENT_TIMESTAMP - INTERVAL '15 days'
);

INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440004',
    '222e8400-e29b-41d4-a716-446655440005',
    2,
    35000.00,
    CURRENT_TIMESTAMP - INTERVAL '45 minutes'
);

INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440005',
    '222e8400-e29b-41d4-a716-446655440005',
    5,
    50000.00,
    CURRENT_TIMESTAMP - INTERVAL '30 minutes'
);

INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440006',
    '222e8400-e29b-41d4-a716-446655440007',
    10,
    30000.00,
    CURRENT_TIMESTAMP - INTERVAL '5 days'
);

INSERT INTO Carrito_Servicio_Adicional (
    id_carrito_servicio_adicional,
    id_carrito_item,
    id_servicio_adicional_ext,
    precio_adicional,
    creado_el
) VALUES (
    '333e8400-e29b-41d4-a716-446655440007',
    '222e8400-e29b-41d4-a716-446655440010',
    9,
    25000.00,
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
);