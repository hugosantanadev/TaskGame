-- Melhorias do quarto: trilhas de equipamento com três degraus. Cada degrau substitui o anterior e rende moedas
-- a mais nas tarefas da categoria ligada à trilha. O degrau zero (colchão no chão, celular com teclado e mouse)
-- não é item: é o quarto de quem está começando.

ALTER TABLE store_items
    ADD COLUMN track VARCHAR(12),
    ADD COLUMN tier  INTEGER;

ALTER TABLE store_items DROP CONSTRAINT ck_store_items_category;
ALTER TABLE store_items ADD CONSTRAINT ck_store_items_category
    CHECK (category IN ('FURNITURE', 'DECORATION', 'CHARACTER', 'EQUIPMENT'));
ALTER TABLE store_items ADD CONSTRAINT ck_store_items_equipment
    CHECK ((category = 'EQUIPMENT') = (track IS NOT NULL AND tier IS NOT NULL));
ALTER TABLE store_items ADD CONSTRAINT ck_store_items_track
    CHECK (track IS NULL OR track IN ('COMPUTER', 'DESK', 'BED', 'BOOKSHELF', 'GYM', 'PEACE', 'ORGANIZER'));
ALTER TABLE store_items ADD CONSTRAINT ck_store_items_tier CHECK (tier IS NULL OR tier BETWEEN 1 AND 3);
CREATE UNIQUE INDEX uq_store_items_track_tier ON store_items (track, tier) WHERE track IS NOT NULL;

-- Escrivaninha, cama e estante já existiam: viram o segundo degrau das trilhas delas
UPDATE store_items SET category = 'EQUIPMENT', track = 'DESK', tier = 2, price = 70, sort_order = 2022,
    description = 'Gaveta, espaço para o caderno e um lugar fixo para estudar.'
WHERE code = 'desk_simple';
UPDATE store_items SET category = 'EQUIPMENT', track = 'BED', tier = 2, price = 120, sort_order = 2032
WHERE code = 'bed_cozy';
UPDATE store_items SET category = 'EQUIPMENT', track = 'BOOKSHELF', tier = 2, price = 80, sort_order = 2042
WHERE code = 'bookshelf_wood';

-- Melhoria aparece sozinha no quarto: o que estava colocado à mão sai da lista do quarto
DELETE FROM room_items
WHERE inventory_item_id IN (
    SELECT i.id FROM inventory_items i JOIN store_items s ON s.id = i.store_item_id WHERE s.category = 'EQUIPMENT'
);

INSERT INTO store_items (id, code, name, description, category, slot, price, available, asset_key, sort_order, track, tier) VALUES
    (gen_random_uuid(), 'computer_old_laptop', 'Notebook velho', 'Esquenta, faz barulho e ainda assim roda tudo o que você precisa.', 'EQUIPMENT', NULL, 60, TRUE, 'equipment.computer_old_laptop.v1', 2011, 'COMPUTER', 1),
    (gen_random_uuid(), 'computer_new_laptop', 'Notebook novo', 'Leve, rápido e com bateria para o dia inteiro.', 'EQUIPMENT', NULL, 180, TRUE, 'equipment.computer_new_laptop.v1', 2012, 'COMPUTER', 2),
    (gen_random_uuid(), 'computer_desktop', 'PC completo', 'Monitor grande, gabinete com luz e teclado mecânico.', 'EQUIPMENT', NULL, 450, TRUE, 'equipment.computer_desktop.v1', 2013, 'COMPUTER', 3),
    (gen_random_uuid(), 'desk_folding', 'Mesa dobrável', 'Abre em qualquer canto e já vira lugar de estudo.', 'EQUIPMENT', NULL, 25, TRUE, 'equipment.desk_folding.v1', 2021, 'DESK', 1),
    (gen_random_uuid(), 'desk_l_shaped', 'Escrivaninha em L', 'Espaço para o computador, os livros e o caderno ao mesmo tempo.', 'EQUIPMENT', NULL, 260, TRUE, 'equipment.desk_l_shaped.v1', 2023, 'DESK', 3),
    (gen_random_uuid(), 'bed_single', 'Cama de solteiro', 'Sair do chão já melhora o sono.', 'EQUIPMENT', NULL, 40, TRUE, 'equipment.bed_single.v1', 2031, 'BED', 1),
    (gen_random_uuid(), 'bed_headboard', 'Cama com cabeceira', 'Colchão firme, cabeceira estofada e criado-mudo.', 'EQUIPMENT', NULL, 300, TRUE, 'equipment.bed_headboard.v1', 2033, 'BED', 3),
    (gen_random_uuid(), 'shelf_wall', 'Prateleira de parede', 'Tira os livros do chão.', 'EQUIPMENT', NULL, 25, TRUE, 'equipment.shelf_wall.v1', 2041, 'BOOKSHELF', 1),
    (gen_random_uuid(), 'bookshelf_double', 'Estante dupla', 'Duas colunas de prateleiras para a coleção crescer.', 'EQUIPMENT', NULL, 260, TRUE, 'equipment.bookshelf_double.v1', 2043, 'BOOKSHELF', 3),
    (gen_random_uuid(), 'gym_dumbbells', 'Par de halteres', 'O começo de toda academia em casa.', 'EQUIPMENT', NULL, 35, TRUE, 'equipment.gym_dumbbells.v1', 2051, 'GYM', 1),
    (gen_random_uuid(), 'gym_bench', 'Banco com halteres', 'Supino, remada e abdominal sem sair do quarto.', 'EQUIPMENT', NULL, 140, TRUE, 'equipment.gym_bench.v1', 2052, 'GYM', 2),
    (gen_random_uuid(), 'gym_rack', 'Rack com barra', 'Barra, anilhas e suporte: a academia chegou em casa.', 'EQUIPMENT', NULL, 360, TRUE, 'equipment.gym_rack.v1', 2053, 'GYM', 3),
    (gen_random_uuid(), 'peace_cushion', 'Almofada de meditação', 'Um lugar para sentar, respirar e agradecer.', 'EQUIPMENT', NULL, 20, TRUE, 'equipment.peace_cushion.v1', 2061, 'PEACE', 1),
    (gen_random_uuid(), 'peace_mat', 'Tapete com vela', 'Luz baixa e silêncio para os minutos de paz.', 'EQUIPMENT', NULL, 70, TRUE, 'equipment.peace_mat.v1', 2062, 'PEACE', 2),
    (gen_random_uuid(), 'peace_corner', 'Cantinho de paz', 'Plantas, luz quente e um banquinho: o canto mais calmo da casa.', 'EQUIPMENT', NULL, 220, TRUE, 'equipment.peace_corner.v1', 2063, 'PEACE', 3),
    (gen_random_uuid(), 'organizer_boxes', 'Caixas organizadoras', 'Cada coisa no seu lugar, pelo menos dentro das caixas.', 'EQUIPMENT', NULL, 20, TRUE, 'equipment.organizer_boxes.v1', 2071, 'ORGANIZER', 1),
    (gen_random_uuid(), 'organizer_dresser', 'Cômoda', 'Gavetas para a roupa sair de cima da cadeira.', 'EQUIPMENT', NULL, 80, TRUE, 'equipment.organizer_dresser.v1', 2072, 'ORGANIZER', 2),
    (gen_random_uuid(), 'organizer_wardrobe', 'Guarda-roupa', 'Portas, cabides e espelho: tudo arrumado.', 'EQUIPMENT', NULL, 240, TRUE, 'equipment.organizer_wardrobe.v1', 2073, 'ORGANIZER', 3);

-- O bônus de melhoria entra no extrato como as outras partes da recompensa, uma vez por tarefa
ALTER TABLE coin_transactions DROP CONSTRAINT ck_coin_transactions_reason;
ALTER TABLE coin_transactions ADD CONSTRAINT ck_coin_transactions_reason CHECK (reason IN
    ('TASK_REWARD', 'ON_TIME_BONUS', 'PROOF_BONUS', 'PURCHASE', 'CHALLENGE_REWARD', 'STREAK_FREEZE', 'CHEST_REWARD',
     'EQUIPMENT_BONUS'));
