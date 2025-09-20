package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.ActionRequests.CaptureActionRequest;
import com.starfleet.gamifier.controller.dto.ActionRequests.ImportResult;
import com.starfleet.gamifier.domain.*;
import com.starfleet.gamifier.repository.ActionCaptureRepository;
import com.starfleet.gamifier.repository.EventRepository;
import com.starfleet.gamifier.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling action capture operations.
 * Supports both UI-based manual capture and CSV import.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionService {

    private final ActionCaptureRepository actionCaptureRepository;
    private final EventRepository eventRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    /**
     * Capture an action manually through the UI.
     */
    public Action captureAction(CaptureActionRequest request) {
        String currentUserId = authenticationService.getCurrentUserId();
        User currentUser = userService.getUser(currentUserId);

        // Validate action type exists and supports UI capture
        Organization organization = getOrganization(currentUser.getOrganizationId());
        Organization.ActionType actionType = getActionType(organization, request.getActionTypeId());

        if (!actionType.supportsUICapture()) {
            throw new IllegalArgumentException("Action type does not support UI capture: " + request.getActionTypeId());
        }

        // Check for duplicate action on same date (idempotency rule)
        String targetUserId = request.getTargetUserId() != null ? request.getTargetUserId() : currentUserId;
        if (isDuplicateAction(targetUserId, request.getActionTypeId(), request.getActionDate())) {
            throw new IllegalArgumentException("Action already captured for this user, action type, and date");
        }

        // Determine capture status based on approval requirements
        CaptureStatus status = actionType.getRequiresManagerApproval() ? CaptureStatus.PENDING_APPROVAL : CaptureStatus.APPROVED;

        Action action = Action.builder()
                .organizationId(currentUser.getOrganizationId())
                .userId(targetUserId)
                .actionTypeId(request.getActionTypeId())
                .actionDate(request.getActionDate())
                .captureMethod(CaptureMethod.UI)
                .status(status)
                .reporterUserId(currentUserId)
                .evidence(request.getEvidence())
                .notes(request.getNotes())
                .build();

        action = actionCaptureRepository.save(action);

        // Generate event
        generateActionCaptureEvent(action, actionType, currentUser);

        // If auto-approved, award points immediately and update mission progress
        if (status == CaptureStatus.APPROVED) {
            userService.awardPoints(targetUserId, actionType.getPoints(),
                    String.format("Action completed: %s", actionType.getName()));
            userService.updateMissionProgress(targetUserId, request.getActionTypeId());
        }

        log.info("Action captured: {} for user {} by user {}",
                request.getActionTypeId(), targetUserId, currentUserId);

        return action;
    }

    /**
     * Import actions from CSV file.
     */
    public ImportResult importActionsFromCsv(MultipartFile file, String organizationId) {
        authenticationService.requireAdminAccess(organizationId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        Organization organization = getOrganization(organizationId);
        List<String> errors = new ArrayList<>();
        int totalRecords = 0;
        int successfulImports = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    validateCsvHeader(line);
                    continue;
                }

                totalRecords++;
                try {
                    Action action = parseCsvLineToActionCapture(line, organizationId, organization);
                    actionCaptureRepository.save(action);

                    // Auto-approve imported actions, award points, and update mission progress
                    Organization.ActionType actionType = getActionType(organization, action.getActionTypeId());
                    userService.awardPoints(action.getUserId(), actionType.getPoints(),
                            String.format("CSV import: %s", actionType.getName()));
                    userService.updateMissionProgress(action.getUserId(), action.getActionTypeId());

                    successfulImports++;
                } catch (Exception e) {
                    errors.add("Line " + totalRecords + ": " + e.getMessage());
                    log.warn("Failed to import action on line {}: {}", totalRecords, e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }

        log.info("Imported {} out of {} actions from CSV for organization {}",
                successfulImports, totalRecords, organizationId);

        return ImportResult.builder()
                .totalRecords(totalRecords)
                .successfulImports(successfulImports)
                .failedImports(totalRecords - successfulImports)
                .errors(errors)
                .build();
    }

    /**
     * Get all actions for an organization.
     */
    public Page<Action> getOrganizationActions(String organizationId, Pageable pageable) {
        authenticationService.requireOrganizationAccess(organizationId);
        return actionCaptureRepository.findByOrganizationIdOrderByActionDateDesc(organizationId, pageable);
    }

    /**
     * Get action history for a user.
     */
    public Page<Action> getUserActionHistory(String userId, Pageable pageable) {
        authenticationService.requireOrganizationAccess(authenticationService.getCurrentOrganizationId());
        return actionCaptureRepository.findByUserIdOrderByActionDateDesc(userId, pageable);
    }

    /**
     * Get pending approvals for a manager.
     * Only returns actions from users who report directly to this manager.
     */
    public Page<Action> getPendingApprovals(String managerId, Pageable pageable) {
        // Get all users who report directly to this manager
        List<User> directReports = userService.getDirectReports(managerId);

        if (directReports.isEmpty()) {
            // Return empty page if manager has no direct reports
            return Page.empty(pageable);
        }

        // Extract user IDs
        List<String> directReportIds = directReports.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // Find pending actions only for direct reports
        return actionCaptureRepository.findByStatusAndUserIdIn(
                CaptureStatus.PENDING_APPROVAL, directReportIds, pageable);
    }

    /**
     * Approve an action capture.
     * Only the direct manager of the user can approve their actions.
     */
    public Action approveAction(String actionCaptureId) {
        Action action = getActionCapture(actionCaptureId);

        if (action.getStatus() != CaptureStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Action is not pending approval");
        }

        String currentUserId = authenticationService.getCurrentUserId();

        // Verify that the current user is the direct manager of the action's user
        if (!userService.isDirectManager(currentUserId, action.getUserId())) {
            throw new SecurityException("Only the direct manager can approve this action");
        }

        action.approve(currentUserId);
        action = actionCaptureRepository.save(action);

        // Award points for approved action and update mission progress
        Organization organization = getOrganization(action.getOrganizationId());
        Organization.ActionType actionType = getActionType(organization, action.getActionTypeId());
        userService.awardPoints(action.getUserId(), actionType.getPoints(),
                String.format("Action approved: %s", actionType.getName()));
        userService.updateMissionProgress(action.getUserId(), action.getActionTypeId());

        // Generate approval event
        generateActionApprovalEvent(action, actionType);

        log.info("Action approved: {} for user {}", actionCaptureId, action.getUserId());

        return action;
    }

    /**
     * Reject an action capture.
     * Only the direct manager of the user can reject their actions.
     */
    public Action rejectAction(String actionCaptureId, String rejectionReason) {
        Action action = getActionCapture(actionCaptureId);

        if (action.getStatus() != CaptureStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Action is not pending approval");
        }

        String currentUserId = authenticationService.getCurrentUserId();

        // Verify that the current user is the direct manager of the action's user
        if (!userService.isDirectManager(currentUserId, action.getUserId())) {
            throw new SecurityException("Only the direct manager can reject this action");
        }

        action.reject(currentUserId, rejectionReason);
        action = actionCaptureRepository.save(action);

        // Generate rejection event
        generateActionRejectionEvent(action);

        log.info("Action rejected: {} for user {} - {}",
                actionCaptureId, action.getUserId(), rejectionReason);

        return action;
    }

    // Helper Methods

    private Organization getOrganization(String organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
    }

    private Organization.ActionType getActionType(Organization organization, String actionTypeId) {
        return organization.getActionType(actionTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Action type not found: " + actionTypeId));
    }

    private Action getActionCapture(String actionCaptureId) {
        return actionCaptureRepository.findById(actionCaptureId)
                .orElseThrow(() -> new IllegalArgumentException("Action capture not found: " + actionCaptureId));
    }

    private boolean isDuplicateAction(String userId, String actionTypeId, LocalDate actionDate) {
        return actionCaptureRepository.existsByUserIdAndActionTypeIdAndActionDate(userId, actionTypeId, actionDate);
    }


    private void generateActionCaptureEvent(Action action, Organization.ActionType actionType, User reporter) {
        Event event = Event.builder()
                .organizationId(action.getOrganizationId())
                .userId(action.getUserId())
                .eventType(EventType.ACTION_CAPTURED)
                .data(String.format("Action '%s' captured by %s %s (%d points)",
                        actionType.getName(), reporter.getName(), reporter.getSurname(), actionType.getPoints()))
                .build();

        eventRepository.save(event);
    }

    private void generateActionApprovalEvent(Action action, Organization.ActionType actionType) {
        Event event = Event.builder()
                .organizationId(action.getOrganizationId())
                .userId(action.getUserId())
                .eventType(EventType.ACTION_APPROVED)
                .data(String.format("Action '%s' approved (%d points awarded)",
                        actionType.getName(), actionType.getPoints()))
                .build();

        eventRepository.save(event);
    }

    private void generateActionRejectionEvent(Action action) {
        Event event = Event.builder()
                .organizationId(action.getOrganizationId())
                .userId(action.getUserId())
                .eventType(EventType.ACTION_REJECTED)
                .data(String.format("Action rejected: %s", action.getRejectionReason()))
                .build();

        eventRepository.save(event);
    }

    private void validateCsvHeader(String headerLine) {
        String[] expectedHeaders = {"employee_id", "action_type", "date", "evidence", "notes"};
        String[] actualHeaders = headerLine.toLowerCase().split(",");

        if (actualHeaders.length < 3) {
            throw new IllegalArgumentException("CSV must have at least 3 columns: employee_id, action_type, date");
        }
    }

    private Action parseCsvLineToActionCapture(String line, String organizationId, Organization organization) {
        String[] values = line.split(",");

        if (values.length < 3) {
            throw new IllegalArgumentException("Invalid CSV line format");
        }

        String employeeId = values[0].trim();
        String actionTypeName = values[1].trim();
        String dateStr = values[2].trim();
        String evidence = values.length > 3 ? values[3].trim() : null;
        String notes = values.length > 4 ? values[4].trim() : null;

        // Find user by employee ID
        User user = userService.getUserByEmployeeId(organizationId, employeeId);

        // Find action type by name
        Optional<Organization.ActionType> actionTypeOpt = organization.getActionTypeByName(actionTypeName);

        if (actionTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("Action type not found: " + actionTypeName);
        }

        Organization.ActionType actionType = actionTypeOpt.get();

        if (!actionType.supportsImportCapture()) {
            throw new IllegalArgumentException("Action type does not support import: " + actionTypeName);
        }

        // Parse date
        LocalDate actionDate;
        try {
            actionDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD: " + dateStr);
        }

        // Check for duplicates
        if (isDuplicateAction(user.getId(), actionType.getId(), actionDate)) {
            throw new IllegalArgumentException("Duplicate action for employee " + employeeId + " on " + dateStr);
        }

        return Action.builder()
                .organizationId(organizationId)
                .userId(user.getId())
                .actionTypeId(actionType.getId())
                .actionDate(actionDate)
                .captureMethod(CaptureMethod.IMPORT)
                .status(CaptureStatus.APPROVED) // Auto-approve imports
                .reporterUserId("SYSTEM")
                .evidence(evidence)
                .notes(notes)
                .build();
    }
}