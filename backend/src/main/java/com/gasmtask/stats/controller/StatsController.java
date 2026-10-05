package com.gasmtask.stats.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.stats.domain.StatsGranularity;
import com.gasmtask.stats.dto.MissionEvolutionResponse;
import com.gasmtask.stats.dto.MissionSummaryResponse;
import com.gasmtask.stats.dto.StatsHistoryResponse;
import com.gasmtask.stats.dto.StatsOverviewResponse;
import com.gasmtask.stats.dto.WeekSummaryResponse;
import com.gasmtask.stats.service.MissionStatsService;
import com.gasmtask.stats.service.StatsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Estatísticas")
public class StatsController {

    private final StatsService statsService;
    private final MissionStatsService missionStats;

    public StatsController(StatsService statsService, MissionStatsService missionStats) {
        this.statsService = statsService;
        this.missionStats = missionStats;
    }

    @GetMapping("/stats/missions")
    @Operation(summary = "Cada missão (ativas e arquivadas): concluídas, perdidas, taxa e sequência")
    public List<MissionSummaryResponse> missions(@AuthenticationPrincipal AuthenticatedUser user) {
        return missionStats.missions(user.id());
    }

    @GetMapping("/stats/missions/{taskId}")
    @Operation(summary = "Evolução de uma missão semana a semana (até 26 semanas), com melhor mês e média semanal")
    public MissionEvolutionResponse missionEvolution(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @PathVariable UUID taskId,
                                                     @RequestParam(defaultValue = "12") int weeks) {
        return missionStats.evolution(user.id(), taskId, weeks);
    }

    @GetMapping("/stats/overview")
    @Operation(summary = "Totais desde o cadastro: concluídas, perdidas, pontos, moedas, streaks e dias")
    public StatsOverviewResponse overview(@AuthenticationPrincipal AuthenticatedUser user) {
        return statsService.overview(user.id());
    }

    @GetMapping("/stats/history")
    @Operation(summary = "Planejado × concluído nos últimos períodos (semanas ou meses, até 26), do mais antigo ao atual")
    public StatsHistoryResponse history(@AuthenticationPrincipal AuthenticatedUser user,
                                        @RequestParam(defaultValue = "WEEK") StatsGranularity granularity,
                                        @RequestParam(defaultValue = "8") int periods) {
        return statsService.history(user.id(), granularity, periods);
    }

    @GetMapping("/weeks/{weekStart}/summary")
    @Operation(summary = "Resumo da semana (weekStart é uma segunda-feira): parcial enquanto ela corre, final depois")
    public WeekSummaryResponse weekSummary(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                           LocalDate weekStart) {
        return statsService.weekSummary(user.id(), weekStart);
    }
}
