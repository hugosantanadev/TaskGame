package com.gasmtask.streak.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.gasmtask.planning.service.PlanningService;
import com.gasmtask.user.service.UserService;

import org.junit.jupiter.api.Test;

class DayClosingJobTest {

    private final UserService users = mock(UserService.class);
    private final DayClosingService closing = mock(DayClosingService.class);
    private final PlanningService planning = mock(PlanningService.class);
    private final DayClosingJob job = new DayClosingJob(users, closing, planning);

    @Test
    void fechaOsDiasEGaranteAsSemanasDeCadaUsuario() {
        UUID ana = UUID.randomUUID();
        UUID bia = UUID.randomUUID();
        when(users.allIds()).thenReturn(List.of(ana, bia));

        job.run();

        verify(closing).closePendingDays(ana);
        verify(planning).ensureUpcomingWeeks(ana);
        verify(closing).closePendingDays(bia);
        verify(planning).ensureUpcomingWeeks(bia);
    }

    @Test
    void falhaDeUmUsuarioNaoParaOsOutros() {
        UUID broken = UUID.randomUUID();
        UUID fine = UUID.randomUUID();
        when(users.allIds()).thenReturn(List.of(broken, fine));
        doThrow(new IllegalStateException("dados inconsistentes")).when(closing).closePendingDays(broken);

        job.run();

        verify(planning, never()).ensureUpcomingWeeks(broken);
        verify(closing).closePendingDays(fine);
        verify(planning).ensureUpcomingWeeks(fine);
    }
}
