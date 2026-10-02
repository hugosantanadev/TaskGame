package com.gasmtask.today.dto;

import com.gasmtask.planning.domain.DayProgress;

public record ProgressResponse(int mandatoryPlanned, int mandatoryDone, int extrasPlanned, int extrasDone,
                               int points, int coins, boolean fulfilled) {

    public static ProgressResponse of(DayProgress progress) {
        return new ProgressResponse(progress.mandatoryPlanned(), progress.mandatoryDone(), progress.extrasPlanned(),
                progress.extrasDone(), progress.points(), progress.coins(), progress.fulfilled());
    }
}
