package com.gasmtask.ranking.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gasmtask.progression.domain.RankLadder;
import com.gasmtask.progression.dto.RankResponse;
import com.gasmtask.ranking.domain.RankingMetric;
import com.gasmtask.ranking.domain.RankingPeriod;
import com.gasmtask.ranking.domain.RankingScope;
import com.gasmtask.ranking.dto.RankingResponse;
import com.gasmtask.ranking.repository.RankingRepository;
import com.gasmtask.ranking.repository.RankingRepository.MyScore;
import com.gasmtask.shared.time.UserCalendar;
import com.gasmtask.shared.web.PageResponse;
import com.gasmtask.streak.service.DayClosingService;
import com.gasmtask.user.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ranking semanal e global (RN29). A semana é a de quem consulta, no fuso dela; como as datas das tarefas são
 * de calendário, cada pessoa entra com o que concluiu entre essa segunda e esse domingo no próprio fuso.
 */
@Service
public class RankingService {

    static final int MAX_PAGE_SIZE = 100;

    private final RankingRepository repository;
    private final DayClosingService closing;
    private final UserService users;
    private final UserCalendar calendar;

    public RankingService(RankingRepository repository, DayClosingService closing, UserService users,
                          UserCalendar calendar) {
        this.repository = repository;
        this.closing = closing;
        this.users = users;
        this.calendar = calendar;
    }

    @Transactional
    public RankingResponse ranking(UUID userId, RankingPeriod period, RankingMetric metric, RankingScope scope,
                                   int page, int size) {
        // A sequência de quem consulta precisa estar fechada até ontem; a dos outros, o job mantém em dia
        closing.closePendingDays(userId);
        LocalDate today = calendar.today(users.timeInfo(userId).zone());
        LocalDate from = UserCalendar.weekStartOf(today);
        LocalDate to = from.plusDays(6);
        Instant now = calendar.now();
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        List<RankingResponse.Entry> entries = repository
                .page(metric, from, to, now, userId, safeSize, (long) safePage * safeSize).stream()
                .map(row -> new RankingResponse.Entry(row.position(), row.displayName(), row.value(),
                        RankResponse.of(RankLadder.rankOf(row.xp())), row.you()))
                .toList();
        long total = repository.count(metric, from, to, now);
        MyScore mine = repository.mine(metric, from, to, now, userId).orElse(new MyScore(0, 0, false, 0));

        return new RankingResponse(period, metric, scope, from, to,
                new PageResponse<>(entries, safePage, safeSize, total, (int) Math.ceilDiv(total, safeSize)),
                new RankingResponse.Me(mine.value() > 0 ? mine.position() : null, mine.value(),
                        RankResponse.of(RankLadder.rankOf(mine.xp())), mine.visible()));
    }
}
