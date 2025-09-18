package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.controller.dto.ActionRequests.CaptureActionRequest;
import com.starfleet.gamifier.controller.dto.ActionRequests.ImportResult;
import com.starfleet.gamifier.controller.dto.ActionRequests.RejectActionRequest;
import com.starfleet.gamifier.domain.Action;
import com.starfleet.gamifier.service.ActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Action management (Gamification Service)
 * Handles action capture, history, and approval workflows.
 */
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @PostMapping
    public ResponseEntity<Action> captureAction(@Valid @RequestBody CaptureActionRequest request) {
        Action action = actionService.captureAction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(action);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<Page<Action>> getUserActionHistory(
            @PathVariable String userId,
            Pageable pageable) {
        Page<Action> history = actionService.getUserActionHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResult> importActions(
            @RequestParam("file") MultipartFile file,
            @RequestParam String organizationId) {
        ImportResult result = actionService.importActionsFromCsv(file, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/pending/{managerId}")
    public ResponseEntity<Page<Action>> getPendingApprovals(
            @PathVariable String managerId,
            Pageable pageable) {
        Page<Action> pending = actionService.getPendingApprovals(managerId, pageable);
        return ResponseEntity.ok(pending);
    }

    @PutMapping("/{actionCaptureId}/approve")
    public ResponseEntity<Action> approveAction(@PathVariable String actionCaptureId) {
        Action action = actionService.approveAction(actionCaptureId);
        return ResponseEntity.ok(action);
    }

    @PutMapping("/{actionCaptureId}/reject")
    public ResponseEntity<Action> rejectAction(
            @PathVariable String actionCaptureId,
            @Valid @RequestBody RejectActionRequest request) {
        Action action = actionService.rejectAction(actionCaptureId, request.getRejectionReason());
        return ResponseEntity.ok(action);
    }
}