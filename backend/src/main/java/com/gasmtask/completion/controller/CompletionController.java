package com.gasmtask.completion.controller;

import java.time.Duration;
import java.util.UUID;

import com.gasmtask.completion.dto.CompletionResponse;
import com.gasmtask.completion.dto.ProofAttachedResponse;
import com.gasmtask.completion.service.ProofContent;
import com.gasmtask.completion.service.TaskCompletionService;
import com.gasmtask.shared.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/occurrences")
@Tag(name = "Conclusão")
public class CompletionController {

    private final TaskCompletionService completionService;

    public CompletionController(TaskCompletionService completionService) {
        this.completionService = completionService;
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Conclui a tarefa de hoje; a foto (campo proof, multipart) é opcional, salvo se a missão exigir")
    public CompletionResponse complete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id,
                                       @RequestParam(name = "proof", required = false) MultipartFile proof) {
        return completionService.complete(user.id(), id, proof);
    }

    @PostMapping(path = "/{id}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Anexa a foto depois de concluir, no mesmo dia, e paga o bônus de prova")
    public ProofAttachedResponse attachProof(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id,
                                             @RequestParam(name = "proof", required = false) MultipartFile proof) {
        return completionService.attachProof(user.id(), id, proof);
    }

    @GetMapping("/{id}/proof")
    @Operation(summary = "Imagem da prova (só para o dono)")
    public ResponseEntity<byte[]> proof(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        ProofContent content = completionService.loadProof(user.id(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(content.bytes());
    }
}
