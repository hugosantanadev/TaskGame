package com.gasmtask.challenge.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Sorteia os desafios do dia entre os elegíveis. O sorteio é fixo por pessoa e por dia (a semente vem dos dois),
 * então pedir de novo dá o mesmo resultado, e cada pessoa tem uma combinação diferente.
 */
public final class ChallengePicker {

    private ChallengePicker() {
    }

    public static List<ChallengeType> pick(UUID userId, LocalDate date, List<ChallengeTask> tasks, LocalTime now,
                                           int count) {
        List<ChallengeType> eligible = new ArrayList<>(Arrays.stream(ChallengeType.values())
                .filter(type -> type.eligible(tasks, now))
                .toList());
        long seed = userId.getMostSignificantBits() ^ userId.getLeastSignificantBits() ^ (date.toEpochDay() * 7919);
        Collections.shuffle(eligible, new Random(seed));
        return eligible.stream()
                .limit(count)
                .sorted(Comparator.comparingInt(ChallengeType::ordinal))
                .toList();
    }
}
