package com.gasmtask.planning.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import com.gasmtask.planning.dto.AddOccurrenceRequest;
import com.gasmtask.planning.dto.CreateExtraRequest;
import com.gasmtask.planning.dto.MoveOccurrenceRequest;
import com.gasmtask.planning.dto.OccurrenceResponse;
import com.gasmtask.planning.dto.WeekResponse;
import com.gasmtask.planning.service.PlanningService;
import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.streak.service.DayClosingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Plano semanal")
public class PlanningController {

    private final PlanningService planning;
    private final DayClosingService closing;

    public PlanningController(PlanningService planning, DayClosingService closing) {
        this.planning = planning;
        this.closing = closing;
    }

    @GetMapping("/weeks/current")
    @Operation(summary = "Plano da semana atual, dia a dia")
    public WeekResponse currentWeek(@AuthenticationPrincipal AuthenticatedUser user) {
        closing.closePendingDays(user.id());
        return planning.currentWeek(user.id(), 0);
    }

    @GetMapping("/weeks/next")
    @Operation(summary = "Plano da próxima semana, dia a dia")
    public WeekResponse nextWeek(@AuthenticationPrincipal AuthenticatedUser user) {
        closing.closePendingDays(user.id());
        return planning.currentWeek(user.id(), 1);
    }

    @GetMapping("/weeks/{weekStart}")
    @Operation(summary = "Plano de uma semana (weekStart é uma segunda-feira, AAAA-MM-DD)")
    public WeekResponse week(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        closing.closePendingDays(user.id());
        return planning.week(user.id(), weekStart);
    }

    @PostMapping("/weeks/{weekStart}/occurrences")
    @Operation(summary = "Inclui uma missão num dia (hoje ou futuro)")
    public ResponseEntity<OccurrenceResponse> addOccurrence(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @Valid @RequestBody AddOccurrenceRequest request) {
        OccurrenceResponse created = planning.addOccurrence(user.id(), weekStart, request);
        return ResponseEntity.created(URI.create("/api/v1/occurrences/" + created.id())).body(created);
    }

    @PostMapping("/extras")
    @Operation(summary = "Cria uma extra avulsa para um dia a partir de amanhã")
    public ResponseEntity<OccurrenceResponse> createExtra(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @Valid @RequestBody CreateExtraRequest request) {
        OccurrenceResponse created = planning.createExtra(user.id(), request);
        return ResponseEntity.created(URI.create("/api/v1/occurrences/" + created.id())).body(created);
    }

    @PatchMapping("/occurrences/{id}")
    @Operation(summary = "Muda o dia ou o horário de uma tarefa pendente (a partir de amanhã)")
    public OccurrenceResponse move(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id,
                                   @Valid @RequestBody MoveOccurrenceRequest request) {
        return planning.move(user.id(), id, request);
    }

    @DeleteMapping("/occurrences/{id}")
    @Operation(summary = "Remove uma tarefa pendente do plano (a partir de amanhã)")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        planning.remove(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
