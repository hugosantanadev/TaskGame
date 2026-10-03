package com.gasmtask.streak.domain;

/** Regras do streak (RN22–RN24), puras. Extras nunca entram na conta. */
public final class StreakRules {

    private StreakRules() {
    }

    public static DayStatus statusOf(int mandatoryPlanned, int mandatoryDone) {
        if (mandatoryPlanned == 0) {
            return DayStatus.REST;
        }
        return mandatoryDone >= mandatoryPlanned ? DayStatus.FULFILLED : DayStatus.FAILED;
    }

    public static int next(int current, DayStatus closedDay) {
        return switch (closedDay) {
            case FULFILLED -> current + 1;
            case FAILED -> 0;
            case REST, FROZEN -> current;
        };
    }
}
