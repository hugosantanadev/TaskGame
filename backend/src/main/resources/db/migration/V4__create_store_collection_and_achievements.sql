-- Loja, inventário, quarto, personagem e conquistas (Fase 3).
-- O catálogo da loja e o de conquistas são seed: o código é estável e as telas o usam como referência.

CREATE TABLE store_items (
    id          UUID         PRIMARY KEY,
    code        VARCHAR(40)  NOT NULL UNIQUE,
    name        VARCHAR(60)  NOT NULL,
    description VARCHAR(200) NOT NULL,
    category    VARCHAR(12)  NOT NULL,
    slot        VARCHAR(10),
    price       INTEGER      NOT NULL,
    available   BOOLEAN      NOT NULL,
    asset_key   VARCHAR(60),
    sort_order  INTEGER      NOT NULL,
    CONSTRAINT ck_store_items_category CHECK (category IN ('FURNITURE', 'DECORATION', 'CHARACTER')),
    CONSTRAINT ck_store_items_slot CHECK ((category = 'CHARACTER') = (slot IS NOT NULL)),
    CONSTRAINT ck_store_items_slot_values CHECK (slot IS NULL OR slot IN ('HEAD', 'OUTFIT', 'ACCESSORY')),
    CONSTRAINT ck_store_items_price CHECK (price > 0)
);

-- RN25: cada item é único na coleção do usuário; o preço pago fica registrado
CREATE TABLE inventory_items (
    id            UUID        PRIMARY KEY,
    user_id       UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    store_item_id UUID        NOT NULL REFERENCES store_items (id),
    price_paid    INTEGER     NOT NULL,
    acquired_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_inventory_items_user_item UNIQUE (user_id, store_item_id)
);

-- Quarto e personagem nascem finos: a futura camada visual acrescenta posição, camada e aparência
CREATE TABLE rooms (
    id         UUID        PRIMARY KEY,
    user_id    UUID        NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE room_items (
    id                UUID        PRIMARY KEY,
    room_id           UUID        NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
    inventory_item_id UUID        NOT NULL UNIQUE REFERENCES inventory_items (id) ON DELETE CASCADE,
    placed_at         TIMESTAMPTZ NOT NULL
);

CREATE TABLE characters (
    id         UUID        PRIMARY KEY,
    user_id    UUID        NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL
);

-- RN26: um item por slot
CREATE TABLE character_equipment (
    id                UUID        PRIMARY KEY,
    character_id      UUID        NOT NULL REFERENCES characters (id) ON DELETE CASCADE,
    slot              VARCHAR(10) NOT NULL,
    inventory_item_id UUID        NOT NULL UNIQUE REFERENCES inventory_items (id) ON DELETE CASCADE,
    equipped_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_character_equipment_slot UNIQUE (character_id, slot),
    CONSTRAINT ck_character_equipment_slot CHECK (slot IN ('HEAD', 'OUTFIT', 'ACCESSORY'))
);

CREATE TABLE achievements (
    id          UUID         PRIMARY KEY,
    code        VARCHAR(40)  NOT NULL UNIQUE,
    name        VARCHAR(60)  NOT NULL,
    description VARCHAR(200) NOT NULL,
    criterion   VARCHAR(30)  NOT NULL,
    threshold   INTEGER      NOT NULL,
    category    VARCHAR(20),
    asset_key   VARCHAR(60),
    sort_order  INTEGER      NOT NULL,
    CONSTRAINT ck_achievements_criterion CHECK (criterion IN
        ('TOTAL_COMPLETIONS', 'CATEGORY_COMPLETIONS', 'SAME_TASK_COMPLETIONS', 'STREAK_REACHED', 'WEEK_COMPLETE')),
    CONSTRAINT ck_achievements_category CHECK ((criterion = 'CATEGORY_COMPLETIONS') = (category IS NOT NULL)),
    CONSTRAINT ck_achievements_threshold CHECK (threshold > 0)
);

-- RN27: o desbloqueio é único e permanente
CREATE TABLE user_achievements (
    id             UUID        PRIMARY KEY,
    user_id        UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    achievement_id UUID        NOT NULL REFERENCES achievements (id),
    unlocked_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_achievements UNIQUE (user_id, achievement_id)
);

-- Compras entram no extrato com valor negativo e o item comprado
ALTER TABLE coin_transactions ADD COLUMN store_item_id UUID REFERENCES store_items (id);
ALTER TABLE coin_transactions ADD CONSTRAINT ck_coin_transactions_sign CHECK ((reason = 'PURCHASE') = (amount < 0));

INSERT INTO store_items (id, code, name, description, category, slot, price, available, asset_key, sort_order) VALUES
    (gen_random_uuid(), 'mug_coffee', 'Caneca de café', 'A primeira compra de quase todo mundo.', 'DECORATION', NULL, 5, TRUE, 'decoration.mug_coffee.v1', 10),
    (gen_random_uuid(), 'plant_small', 'Vasinho de suculenta', 'Pede pouca água e quase nenhuma atenção.', 'DECORATION', NULL, 8, TRUE, 'decoration.plant_small.v1', 20),
    (gen_random_uuid(), 'cap_red', 'Boné vermelho', 'Aba curva, da cor de uma sequência pegando fogo.', 'CHARACTER', 'HEAD', 12, TRUE, 'character.cap_red.v1', 30),
    (gen_random_uuid(), 'scarf_knit', 'Cachecol de tricô', 'Para as manhãs frias de estudo.', 'CHARACTER', 'ACCESSORY', 20, TRUE, 'character.scarf_knit.v1', 40),
    (gen_random_uuid(), 'poster_space', 'Pôster do espaço', 'Uma nebulosa na parede para lembrar que dá para ir longe.', 'DECORATION', NULL, 25, TRUE, 'decoration.poster_space.v1', 50),
    (gen_random_uuid(), 'tshirt_stripes', 'Camiseta listrada', 'Listras coloridas e confortável o dia inteiro.', 'CHARACTER', 'OUTFIT', 25, TRUE, 'character.tshirt_stripes.v1', 60),
    (gen_random_uuid(), 'lamp_desk', 'Luminária de mesa', 'Luz quente para as sessões da noite.', 'DECORATION', NULL, 30, TRUE, 'decoration.lamp_desk.v1', 70),
    (gen_random_uuid(), 'glasses_round', 'Óculos redondos', 'Aro fino, cara de quem leu o livro todo.', 'CHARACTER', 'ACCESSORY', 35, TRUE, 'character.glasses_round.v1', 80),
    (gen_random_uuid(), 'desk_simple', 'Escrivaninha simples', 'Um lugar fixo para estudar e trabalhar.', 'FURNITURE', NULL, 40, TRUE, 'furniture.desk_simple.v1', 90),
    (gen_random_uuid(), 'rug_round', 'Tapete redondo', 'Macio, colorido e bom para alongar.', 'DECORATION', NULL, 45, TRUE, 'decoration.rug_round.v1', 100),
    (gen_random_uuid(), 'headphones_basic', 'Fones de ouvido', 'Isolam o barulho na hora de focar.', 'CHARACTER', 'HEAD', 50, TRUE, 'character.headphones_basic.v1', 110),
    (gen_random_uuid(), 'beanbag_purple', 'Pufe roxo', 'Para ler largado no canto do quarto.', 'FURNITURE', NULL, 60, TRUE, 'furniture.beanbag_purple.v1', 120),
    (gen_random_uuid(), 'hoodie_purple', 'Moletom roxo', 'Capuz grande e bolso de canguru.', 'CHARACTER', 'OUTFIT', 70, TRUE, 'character.hoodie_purple.v1', 130),
    (gen_random_uuid(), 'bookshelf_wood', 'Estante de madeira', 'Cabe a coleção inteira, inclusive os livros que ainda vão ser lidos.', 'FURNITURE', NULL, 80, TRUE, 'furniture.bookshelf_wood.v1', 140),
    (gen_random_uuid(), 'neon_sign', 'Letreiro neon', 'Escreve "foco" em rosa na parede.', 'DECORATION', NULL, 100, TRUE, 'decoration.neon_sign.v1', 150),
    (gen_random_uuid(), 'bed_cozy', 'Cama aconchegante', 'Edredom grosso e travesseiro extra. Combina com a missão de dormir cedo.', 'FURNITURE', NULL, 120, TRUE, 'furniture.bed_cozy.v1', 160),
    (gen_random_uuid(), 'chair_gamer', 'Cadeira gamer', 'Encosto alto e apoio para os braços, para as maratonas de projeto.', 'FURNITURE', NULL, 150, TRUE, 'furniture.chair_gamer.v1', 170),
    (gen_random_uuid(), 'aquarium_small', 'Aquário pequeno', 'Dois peixinhos que nunca perdem a sequência.', 'DECORATION', NULL, 200, TRUE, 'decoration.aquarium_small.v1', 180);

-- RN28: "ler 10 livros" virou "50 sessões de leitura"; "estudar Java 20 vezes" virou "a mesma missão 20 vezes"
INSERT INTO achievements (id, code, name, description, criterion, threshold, category, asset_key, sort_order) VALUES
    (gen_random_uuid(), 'FIRST_TASK', 'Primeiro passo', 'Concluir a primeira tarefa.', 'TOTAL_COMPLETIONS', 1, NULL, 'achievement.first_task.v1', 10),
    (gen_random_uuid(), 'TOTAL_10', 'Pegando o ritmo', 'Concluir 10 tarefas.', 'TOTAL_COMPLETIONS', 10, NULL, 'achievement.total_10.v1', 20),
    (gen_random_uuid(), 'STREAK_3', 'Três seguidos', 'Chegar a uma sequência de 3 dias.', 'STREAK_REACHED', 3, NULL, 'achievement.streak_3.v1', 30),
    (gen_random_uuid(), 'STREAK_7', 'Uma semana firme', 'Chegar a uma sequência de 7 dias.', 'STREAK_REACHED', 7, NULL, 'achievement.streak_7.v1', 40),
    (gen_random_uuid(), 'WEEK_COMPLETE_1', 'Semana completa', 'Fechar uma semana com todas as obrigatórias feitas.', 'WEEK_COMPLETE', 1, NULL, 'achievement.week_complete_1.v1', 50),
    (gen_random_uuid(), 'TOTAL_50', 'Meia centena', 'Concluir 50 tarefas.', 'TOTAL_COMPLETIONS', 50, NULL, 'achievement.total_50.v1', 60),
    (gen_random_uuid(), 'STUDY_20', 'Estudante dedicado', 'Concluir 20 tarefas de estudo.', 'CATEGORY_COMPLETIONS', 20, 'STUDY', 'achievement.study_20.v1', 70),
    (gen_random_uuid(), 'EXERCISE_20', 'Corpo em movimento', 'Concluir 20 tarefas de exercício.', 'CATEGORY_COMPLETIONS', 20, 'EXERCISE', 'achievement.exercise_20.v1', 80),
    (gen_random_uuid(), 'PROJECT_20', 'Mão na massa', 'Concluir 20 tarefas de projeto.', 'CATEGORY_COMPLETIONS', 20, 'PROJECT', 'achievement.project_20.v1', 90),
    (gen_random_uuid(), 'SAME_TASK_20', 'Hábito formado', 'Concluir a mesma missão 20 vezes.', 'SAME_TASK_COMPLETIONS', 20, NULL, 'achievement.same_task_20.v1', 100),
    (gen_random_uuid(), 'SPIRITUALITY_30', 'Fé constante', 'Concluir 30 tarefas de espiritualidade.', 'CATEGORY_COMPLETIONS', 30, 'SPIRITUALITY', 'achievement.spirituality_30.v1', 110),
    (gen_random_uuid(), 'STREAK_30', 'Um mês de pé', 'Chegar a uma sequência de 30 dias.', 'STREAK_REACHED', 30, NULL, 'achievement.streak_30.v1', 120),
    (gen_random_uuid(), 'READING_50', '50 sessões de leitura', 'Concluir 50 tarefas de leitura.', 'CATEGORY_COMPLETIONS', 50, 'READING', 'achievement.reading_50.v1', 130),
    (gen_random_uuid(), 'WEEK_COMPLETE_4', 'Quatro semanas completas', 'Fechar quatro semanas com todas as obrigatórias feitas.', 'WEEK_COMPLETE', 4, NULL, 'achievement.week_complete_4.v1', 140),
    (gen_random_uuid(), 'TOTAL_100', 'Cem vezes feito', 'Concluir 100 tarefas.', 'TOTAL_COMPLETIONS', 100, NULL, 'achievement.total_100.v1', 150);

-- Contas existentes ganham quarto e personagem
INSERT INTO rooms (id, user_id, created_at) SELECT gen_random_uuid(), id, now() FROM users;
INSERT INTO characters (id, user_id, created_at) SELECT gen_random_uuid(), id, now() FROM users;
