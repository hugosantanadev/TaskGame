package com.gasmtask.task.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.gasmtask.shared.security.AuthenticatedUser;
import com.gasmtask.task.dto.ScheduleSuggestionResponse;
import com.gasmtask.task.dto.TaskRequest;
import com.gasmtask.task.dto.TaskResponse;
import com.gasmtask.task.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Missões")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Lista as missões ativas (ou as arquivadas, com archived=true)")
    public List<TaskResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                   @RequestParam(defaultValue = "false") boolean archived) {
        return taskService.list(user.id(), archived);
    }

    @PostMapping
    @Operation(summary = "Cria uma missão e a coloca no plano a partir de amanhã (ou hoje, com startToday)")
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
                                               @Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(user.id(), request);
        return ResponseEntity.created(URI.create("/api/v1/tasks/" + created.id())).body(created);
    }

    @GetMapping("/schedule-suggestion")
    @Operation(summary = "Sugere dias espaçados para \"N vezes por semana\"")
    public ScheduleSuggestionResponse suggestion(@RequestParam int timesPerWeek) {
        return taskService.suggest(timesPerWeek);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma missão")
    public TaskResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return taskService.get(user.id(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita uma missão; o plano muda a partir de amanhã")
    public TaskResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id,
                               @Valid @RequestBody TaskRequest request) {
        return taskService.update(user.id(), id, request);
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Arquiva a missão e tira do plano as ocorrências a partir de amanhã")
    public TaskResponse archive(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return taskService.archive(user.id(), id);
    }
}
