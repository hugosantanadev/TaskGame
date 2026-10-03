package com.gasmtask.streak.domain;

/** Como um dia fechou (RN22). FROZEN é uma falha salva pelo protetor de sequência: não soma nem quebra. */
public enum DayStatus {
    FULFILLED, FAILED, REST, FROZEN
}
