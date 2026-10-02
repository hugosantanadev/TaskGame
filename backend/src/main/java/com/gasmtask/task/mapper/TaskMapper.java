package com.gasmtask.task.mapper;

import com.gasmtask.economy.domain.RewardPolicy;
import com.gasmtask.task.domain.Task;
import com.gasmtask.task.dto.ScheduleEntry;
import com.gasmtask.task.dto.TaskResponse;

import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final RewardPolicy rewards;

    public TaskMapper(RewardPolicy rewards) {
        this.rewards = rewards;
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getCategory(),
                task.getKind(),
                task.getPoints(),
                rewards.baseCoinsFor(task.getKind()),
                task.getDurationMinutes(),
                task.isRequiresProof(),
                task.slots().stream().map(slot -> new ScheduleEntry(slot.day(), slot.time())).toList(),
                task.isArchived(),
                task.getCreatedAt());
    }
}
