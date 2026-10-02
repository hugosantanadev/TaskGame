package com.gasmtask.planning.mapper;

import java.time.LocalDate;

import com.gasmtask.planning.domain.DayLockPolicy;
import com.gasmtask.planning.domain.TaskOccurrence;
import com.gasmtask.planning.dto.OccurrenceResponse;

import org.springframework.stereotype.Component;

@Component
public class OccurrenceMapper {

    public OccurrenceResponse toResponse(TaskOccurrence occurrence, LocalDate today, boolean onboardingOpen) {
        boolean removable = occurrence.isPending()
                && DayLockPolicy.canTakeOut(occurrence.getOccurrenceDate(), today, onboardingOpen);
        return new OccurrenceResponse(
                occurrence.getId(),
                occurrence.getTaskId(),
                occurrence.getOccurrenceDate(),
                occurrence.getPlannedTime(),
                occurrence.getTitle(),
                occurrence.getCategory(),
                occurrence.getKind(),
                occurrence.getPoints(),
                occurrence.getBaseCoins(),
                occurrence.getDurationMinutes(),
                occurrence.isRequiresProof(),
                occurrence.getStatus(),
                occurrence.getCompletedAt(),
                occurrence.getOnTime(),
                occurrence.getEarnedPoints(),
                occurrence.getEarnedCoins(),
                occurrence.isProofAttached(),
                removable);
    }
}
