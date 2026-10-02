package com.gasmtask.notification.controller;

import java.util.List;

import com.gasmtask.notification.dto.UpcomingReminderResponse;
import com.gasmtask.notification.service.ReminderService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reminders")
@Tag(name = "Lembretes")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Lembretes que saem nas próximas horas (até 48): tarefas, hora de dormir e de acordar")
    public List<UpcomingReminderResponse> upcoming(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @RequestParam(defaultValue = "24") int hours) {
        return reminderService.upcoming(user.id(), hours);
    }
}
