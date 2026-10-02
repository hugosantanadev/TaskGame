package com.gasmtask.user.dto;

import java.time.LocalTime;

import com.gasmtask.user.domain.ReminderSettings;

public record ReminderSettingsResponse(boolean tasksEnabled, int leadMinutes, LocalTime bedtime, LocalTime wakeTime) {

    public static ReminderSettingsResponse of(ReminderSettings settings) {
        return new ReminderSettingsResponse(settings.isTasksEnabled(), settings.getLeadMinutes(),
                settings.getBedtime(), settings.getWakeTime());
    }
}
