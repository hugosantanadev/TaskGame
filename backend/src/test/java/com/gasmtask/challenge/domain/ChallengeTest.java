package com.gasmtask.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.gasmtask.task.domain.TaskCategory;
import com.gasmtask.task.domain.TaskKind;

import org.junit.jupiter.api.Test;

class ChallengeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 5);
    private static final LocalTime MORNING = LocalTime.of(8, 0);

    private static ChallengeTask pending(TaskKind kind, TaskCategory category) {
        return new ChallengeTask(kind, category, true, null, false, false, true);
    }

    private static ChallengeTask done(TaskCategory category, LocalTime at, boolean onTime, boolean photo) {
        return new ChallengeTask(TaskKind.MANDATORY, category, false, at, onTime, photo, true);
    }

    @Test
    void progressoContaSoOQueOsDesafiosPedem() {
        List<ChallengeTask> tasks = List.of(
                done(TaskCategory.STUDY, LocalTime.of(9, 0), true, true),
                done(TaskCategory.EXERCISE, LocalTime.of(13, 0), true, false),
                done(TaskCategory.STUDY, LocalTime.of(11, 59), false, false),
                pending(TaskKind.EXTRA, TaskCategory.HOME));

        assertThat(ChallengeType.EARLY_BIRD.progress(tasks)).isEqualTo(2);
        assertThat(ChallengeType.ON_TIME.progress(tasks)).isEqualTo(2);
        assertThat(ChallengeType.PHOTO.progress(tasks)).isEqualTo(1);
        assertThat(ChallengeType.VARIETY.progress(tasks)).isEqualTo(2);
        assertThat(ChallengeType.MARATHON.progress(tasks)).isEqualTo(3);
        assertThat(ChallengeType.EXTRA_MILE.progress(tasks)).isZero();
        // As três obrigatórias estão feitas: a extra pendente não impede o dia cumprido
        assertThat(ChallengeType.FULL_DAY.progress(tasks)).isEqualTo(1);
    }

    @Test
    void soEntraNoSorteioOQueDaParaCumprirEAindaNaoFoiCumprido() {
        List<ChallengeTask> twoStudies = List.of(pending(TaskKind.MANDATORY, TaskCategory.STUDY),
                pending(TaskKind.MANDATORY, TaskCategory.STUDY));

        assertThat(ChallengeType.EARLY_BIRD.eligible(twoStudies, MORNING)).isTrue();
        assertThat(ChallengeType.EARLY_BIRD.eligible(twoStudies, LocalTime.of(15, 0))).isFalse(); // tarde demais
        assertThat(ChallengeType.EXTRA_MILE.eligible(twoStudies, MORNING)).isFalse();             // sem extra
        assertThat(ChallengeType.VARIETY.eligible(twoStudies, MORNING)).isFalse();                // uma categoria
        assertThat(ChallengeType.MARATHON.eligible(twoStudies, MORNING)).isFalse();               // só duas tarefas
        assertThat(ChallengeType.PHOTO.eligible(
                List.of(done(TaskCategory.STUDY, MORNING, false, true)), MORNING)).isFalse();     // já cumprido
    }

    @Test
    void sorteioEFixoPorPessoaEDiaETrazNoMaximoTres() {
        List<ChallengeTask> rich = List.of(
                pending(TaskKind.MANDATORY, TaskCategory.STUDY),
                pending(TaskKind.MANDATORY, TaskCategory.EXERCISE),
                pending(TaskKind.MANDATORY, TaskCategory.READING),
                pending(TaskKind.EXTRA, TaskCategory.HOME));
        UUID user = UUID.randomUUID();

        List<ChallengeType> first = ChallengePicker.pick(user, DAY, rich, MORNING, 3);
        List<ChallengeType> again = ChallengePicker.pick(user, DAY, rich, MORNING, 3);

        assertThat(first).hasSize(3).isEqualTo(again).isSorted();
        assertThat(first).allMatch(type -> type.eligible(rich, MORNING));
        assertThat(ChallengePicker.pick(user, DAY, List.of(), MORNING, 3)).isEmpty();
    }
}
