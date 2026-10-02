package com.gasmtask.ranking.controller;

import com.gasmtask.ranking.domain.RankingMetric;
import com.gasmtask.ranking.domain.RankingPeriod;
import com.gasmtask.ranking.domain.RankingScope;
import com.gasmtask.ranking.dto.RankingResponse;
import com.gasmtask.ranking.service.RankingService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@Tag(name = "Ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    @Operation(summary = "Ranking da semana por pontos, tarefas concluídas, moedas ganhas ou sequência, com a sua posição")
    public RankingResponse ranking(@AuthenticationPrincipal AuthenticatedUser user,
                                   @RequestParam(defaultValue = "WEEK") RankingPeriod period,
                                   @RequestParam(defaultValue = "POINTS") RankingMetric metric,
                                   @RequestParam(defaultValue = "GLOBAL") RankingScope scope,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return rankingService.ranking(user.id(), period, metric, scope, page, size);
    }
}
