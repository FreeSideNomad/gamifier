package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.domain.ActionCapture;
import com.starfleet.gamifier.service.ActionService;
import com.starfleet.gamifier.controller.dto.ActionRequests.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<ActionCapture> captureAction(@Valid @RequestBody CaptureActionRequest request) {
        ActionCapture actionCapture = actionService.captureAction(
                request.getOrganizationId(),
                request.getUserId(),
                request.getActionTypeId(),
                request.getDate(),
                request.getReporterType(),
                request.getReporterId(),
                request.getEvidence()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(actionCapture);
    }

    @GetMapping
    public ResponseEntity<Page<ActionCapture>> getActionHistory(
            @RequestParam String userId,
            @RequestParam String organizationId,
            Pageable pageable) {
        Page<ActionCapture> actions = actionService.getActionHistory(userId, organizationId, pageable);
        return ResponseEntity.ok(actions);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResult> importActions(
            @RequestParam("file") MultipartFile file,
            @RequestParam String organizationId) {
        ImportResult result = actionService.importActionsFromCsv(file, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ActionCapture>> getPendingApprovals(
            @RequestParam String organizationId,
            @RequestParam String managerId) {
        List<ActionCapture> pendingActions = actionService.getPendingApprovals(organizationId, managerId);
        return ResponseEntity.ok(pendingActions);
    }

    @PutMapping("/{actionId}/approve")
    public ResponseEntity<ActionCapture> approveAction(
            @PathVariable String actionId,
            @Valid @RequestBody ApproveActionRequest request) {
        ActionCapture actionCapture = actionService.approveAction(
                actionId,
                request.getApproverId(),
                request.getApprovalNotes()
        );
        return ResponseEntity.ok(actionCapture);
    }

    @PutMapping("/{actionId}/reject")
    public ResponseEntity<ActionCapture> rejectAction(
            @PathVariable String actionId,
            @Valid @RequestBody RejectActionRequest request) {
        ActionCapture actionCapture = actionService.rejectAction(
                actionId,
                request.getRejectionReason()
        );
        return ResponseEntity.ok(actionCapture);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ActionStatistics> getActionStatistics(
            @RequestParam String organizationId,
            @RequestParam(required = false) String userId) {
        ActionStatistics statistics = actionService.getActionStatistics(organizationId, userId);
        return ResponseEntity.ok(statistics);
    }
}