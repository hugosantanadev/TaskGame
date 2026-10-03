package com.gasmtask.progression.domain;

import java.util.List;
import java.util.Optional;

/**
 * A escada de elos, pura. Os degraus ficam mais longos conforme se sobe: com umas quatro obrigatórias por dia
 * (cerca de 900 XP por mês), o Bronze chega na primeira semana, o Ouro em pouco mais de um mês, o Diamante em
 * uns três meses e a Lenda em quase um ano de constância.
 */
public final class RankLadder {

    public static final List<Rank> STEPS = List.of(
            new Rank(RankTier.IRON, 1, 0),
            new Rank(RankTier.IRON, 2, 50),
            new Rank(RankTier.IRON, 3, 100),
            new Rank(RankTier.BRONZE, 1, 175),
            new Rank(RankTier.BRONZE, 2, 250),
            new Rank(RankTier.BRONZE, 3, 350),
            new Rank(RankTier.SILVER, 1, 475),
            new Rank(RankTier.SILVER, 2, 600),
            new Rank(RankTier.SILVER, 3, 750),
            new Rank(RankTier.GOLD, 1, 950),
            new Rank(RankTier.GOLD, 2, 1150),
            new Rank(RankTier.GOLD, 3, 1400),
            new Rank(RankTier.PLATINUM, 1, 1700),
            new Rank(RankTier.PLATINUM, 2, 2000),
            new Rank(RankTier.PLATINUM, 3, 2350),
            new Rank(RankTier.DIAMOND, 1, 2750),
            new Rank(RankTier.DIAMOND, 2, 3200),
            new Rank(RankTier.DIAMOND, 3, 3700),
            new Rank(RankTier.MASTER, 1, 4300),
            new Rank(RankTier.MASTER, 2, 5000),
            new Rank(RankTier.MASTER, 3, 5800),
            new Rank(RankTier.LEGEND, null, 7000));

    private RankLadder() {
    }

    /** O degrau mais alto cujo mínimo o XP já alcançou. */
    public static Rank rankOf(int xp) {
        Rank current = STEPS.getFirst();
        for (Rank step : STEPS) {
            if (xp >= step.minXp()) {
                current = step;
            }
        }
        return current;
    }

    /** O próximo degrau, ou vazio no topo. */
    public static Optional<Rank> nextAfter(Rank rank) {
        int index = STEPS.indexOf(rank);
        return index + 1 < STEPS.size() ? Optional.of(STEPS.get(index + 1)) : Optional.empty();
    }

    /** Elos alcançados até esse XP, em ordem: são as roupas que a pessoa já ganhou. */
    public static List<RankTier> tiersReachedBy(int xp) {
        RankTier top = rankOf(xp).tier();
        return List.of(RankTier.values()).subList(0, top.ordinal() + 1);
    }
}
