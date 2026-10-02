package com.gasmtask.task.dto;

import java.time.DayOfWeek;
import java.util.List;

public record ScheduleSuggestionResponse(int timesPerWeek, List<DayOfWeek> days) {
}
