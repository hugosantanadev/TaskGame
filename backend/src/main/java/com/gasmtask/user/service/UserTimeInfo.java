package com.gasmtask.user.service;

import java.time.LocalDate;
import java.time.ZoneId;

/** O que os outros módulos precisam saber do usuário para calcular datas. */
public record UserTimeInfo(ZoneId zone, LocalDate registrationDate) {
}
