package com.gasmtask.character.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Títulos ganhos ao treinar um atributo: três por atributo, nos níveis 3, 6 e 10 (ex.: Força 6 vira
 * "Rato de academia"; Inteligência 10, "Mestre nos estudos"). O nome exibido é do app, a partir do código.
 * Um título ganho não se perde, porque atributo só sobe.
 */
public enum Title {
    STUDENT(Attribute.INTELLIGENCE, 3),
    SHARP_MIND(Attribute.INTELLIGENCE, 6),
    STUDY_MASTER(Attribute.INTELLIGENCE, 10),
    GYM_REGULAR(Attribute.STRENGTH, 3),
    GYM_RAT(Attribute.STRENGTH, 6),
    STRENGTH_MASTER(Attribute.STRENGTH, 10),
    CURIOUS_READER(Attribute.WISDOM, 3),
    BOOKWORM(Attribute.WISDOM, 6),
    WISDOM_MASTER(Attribute.WISDOM, 10),
    SERENE_SOUL(Attribute.SPIRIT, 3),
    STEADY_HEART(Attribute.SPIRIT, 6),
    SPIRIT_MASTER(Attribute.SPIRIT, 10),
    WELL_RESTED(Attribute.VITALITY, 3),
    FULL_ENERGY(Attribute.VITALITY, 6),
    REST_MASTER(Attribute.VITALITY, 10),
    HANDS_ON(Attribute.CREATIVITY, 3),
    BUILDER(Attribute.CREATIVITY, 6),
    CREATOR_MASTER(Attribute.CREATIVITY, 10),
    TIDY_HOME(Attribute.DISCIPLINE, 3),
    ORGANIZED(Attribute.DISCIPLINE, 6),
    DISCIPLINE_MASTER(Attribute.DISCIPLINE, 10);

    private final Attribute attribute;
    private final int level;

    Title(Attribute attribute, int level) {
        this.attribute = attribute;
        this.level = level;
    }

    public Attribute attribute() {
        return attribute;
    }

    public int level() {
        return level;
    }

    public boolean unlockedBy(Map<Attribute, Integer> levels) {
        return levels.getOrDefault(attribute, 1) >= level;
    }

    /** Títulos do atributo ganhos ao passar do nível {@code before} para {@code after}. */
    public static List<Title> reachedBetween(Attribute attribute, int before, int after) {
        return Arrays.stream(values())
                .filter(title -> title.attribute == attribute && title.level > before && title.level <= after)
                .toList();
    }
}
