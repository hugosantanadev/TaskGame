package com.gasmtask.task.domain;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

/**
 * Sugere dias para "N vezes por semana" (RN07): dias espaçados, priorizando segunda a sexta.
 * A sugestão é só o ponto de partida; o usuário ajusta antes de salvar.
 */
public final class ScheduleDistributor {

    private static final Map<Integer, List<DayOfWeek>> SUGGESTIONS = Map.of(
            1, List.of(WEDNESDAY),
            2, List.of(TUESDAY, THURSDAY),
            3, List.of(MONDAY, WEDNESDAY, FRIDAY),
            4, List.of(MONDAY, TUESDAY, THURSDAY, FRIDAY),
            5, List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY),
            6, List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY),
            7, List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY));

    private ScheduleDistributor() {
    }

    public static List<DayOfWeek> suggest(int timesPerWeek) {
        List<DayOfWeek> days = SUGGESTIONS.get(timesPerWeek);
        if (days == null) {
            throw new IllegalArgumentException("Vezes por semana deve ficar entre 1 e 7: " + timesPerWeek);
        }
        return days;
    }
}
