package com.gasmtask.character.domain;

/**
 * Níveis de atributo, puros. Cada nível pede 25 XP a mais que o anterior: o nível 2 vem com 25 XP
 * (cinco obrigatórias), o 5 com 250 e o 10 com 1.125. Sem teto, mas cada nível fica mais longo.
 */
public final class AttributeLevels {

    static final int STEP = 25;

    private AttributeLevels() {
    }

    /** XP mínimo para estar no nível (nível 1 começa em 0). */
    public static int startOf(int level) {
        return STEP * (level - 1) * level / 2;
    }

    public static int levelOf(int xp) {
        int level = 1;
        while (startOf(level + 1) <= xp) {
            level++;
        }
        return level;
    }
}
