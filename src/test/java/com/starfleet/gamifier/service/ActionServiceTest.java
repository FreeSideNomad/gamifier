package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.ActionRequests.CaptureActionRequest;
import com.starfleet.gamifier.domain.*;
import com.starfleet.gamifier.repository.ActionCaptureRepository;
import com.starfleet.gamifier.repository.EventRepository;
import com.starfleet.gamifier.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionServiceTest {

    @Mock
    private ActionCaptureRepository actionCaptureRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationService authenticationService;

    private ActionService actionService;

    private User currentUser;
    private Organization organization;
    private Organization.ActionType actionType;

    @BeforeEach
    void setUp() {
        actionService = new ActionService(
                actionCaptureRepository, eventRepository, organizationRepository,
                userService, authenticationService);

        currentUser = User.builder()
                .id("user-1")
                .organizationId("org-1")
                .employeeId("EMP-001")
                .name("Test")
                .surname("User")
                .role(UserRole.USER)
                .build();

        actionType = Organization.ActionType.builder()
                .id("action-1")
                .name("Test Action")
                .description("Test description")
                .points(50)
                .category("Test")
                .captureMethods(Set.of(CaptureMethod.UI))
                .requiresManagerApproval(false)
                .build();

        organization = Organization.builder()
                .id("org-1")
                .name("Test Organization")
                .actionTypes(List.of(actionType))
                .build();
    }

    @Test
    void captureAction_WithValidRequest_ShouldCreateActionCapture() {
        // Given
        CaptureActionRequest request = new CaptureActionRequest();
        request.setActionTypeId("action-1");
        request.setActionDate(LocalDate.now());
        request.setEvidence("Test evidence");
        request.setNotes("Test notes");

        when(authenticationService.getCurrentUserId()).thenReturn("user-1");
        when(userService.getUser("user-1")).thenReturn(currentUser);
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
        when(actionCaptureRepository.existsByUserIdAndActionTypeIdAndActionDate(
                anyString(), anyString(), any(LocalDate.class))).thenReturn(false);
        when(actionCaptureRepository.save(any(Action.class))).thenAnswer(invocation -> {
            Action ac = invocation.getArgument(0);
            ac.setId("capture-1");
            return ac;
        });

        // When
        Action result = actionService.captureAction(request);

        // Then
        assertNotNull(result);
        assertEquals("org-1", result.getOrganizationId());
        assertEquals("user-1", result.getUserId());
        assertEquals("action-1", result.getActionTypeId());
        assertEquals(CaptureMethod.UI, result.getCaptureMethod());
        assertEquals(CaptureStatus.APPROVED, result.getStatus());

        verify(actionCaptureRepository).save(any(Action.class));
        verify(eventRepository).save(any(Event.class));
        verify(userService).awardPoints("user-1", 50, "Action completed: Test Action");
        verify(userService).updateMissionProgress("user-1", "action-1");
    }

    @Test
    void captureAction_WithRequiresApproval_ShouldSetPendingStatus() {
        // Given
        actionType = actionType.toBuilder().requiresManagerApproval(true).build();
        organization = organization.toBuilder().actionTypes(List.of(actionType)).build();

        CaptureActionRequest request = new CaptureActionRequest();
        request.setActionTypeId("action-1");
        request.setActionDate(LocalDate.now());

        when(authenticationService.getCurrentUserId()).thenReturn("user-1");
        when(userService.getUser("user-1")).thenReturn(currentUser);
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
        when(actionCaptureRepository.existsByUserIdAndActionTypeIdAndActionDate(
                anyString(), anyString(), any(LocalDate.class))).thenReturn(false);
        when(actionCaptureRepository.save(any(Action.class))).thenAnswer(invocation -> {
            Action ac = invocation.getArgument(0);
            ac.setId("capture-1");
            return ac;
        });

        // When
        Action result = actionService.captureAction(request);

        // Then
        assertEquals(CaptureStatus.PENDING_APPROVAL, result.getStatus());
        verify(userService, never()).awardPoints(anyString(), any(Integer.class), anyString());
        verify(userService, never()).updateMissionProgress(anyString(), anyString());
    }

    @Test
    void captureAction_WithDuplicate_ShouldThrowException() {
        // Given
        CaptureActionRequest request = new CaptureActionRequest();
        request.setActionTypeId("action-1");
        request.setActionDate(LocalDate.now());

        when(authenticationService.getCurrentUserId()).thenReturn("user-1");
        when(userService.getUser("user-1")).thenReturn(currentUser);
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));
        when(actionCaptureRepository.existsByUserIdAndActionTypeIdAndActionDate(
                anyString(), anyString(), any(LocalDate.class))).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> actionService.captureAction(request));
        assertEquals("Action already captured for this user, action type, and date", exception.getMessage());
    }

    @Test
    void captureAction_WithNonUIActionType_ShouldThrowException() {
        // Given
        actionType = actionType.toBuilder().captureMethods(Set.of(CaptureMethod.IMPORT)).build();
        organization = organization.toBuilder().actionTypes(List.of(actionType)).build();

        CaptureActionRequest request = new CaptureActionRequest();
        request.setActionTypeId("action-1");
        request.setActionDate(LocalDate.now());

        when(authenticationService.getCurrentUserId()).thenReturn("user-1");
        when(userService.getUser("user-1")).thenReturn(currentUser);
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> actionService.captureAction(request));
        assertEquals("Action type does not support UI capture: action-1", exception.getMessage());
    }

    @Test
    void approveAction_WithDirectManager_ShouldApproveSuccessfully() {
        // Given
        Action pendingAction = Action.builder()
                .id("action-1")
                .organizationId("org-1")
                .userId("user-1")
                .actionTypeId("action-1")
                .status(CaptureStatus.PENDING_APPROVAL)
                .build();

        when(actionCaptureRepository.findById("action-1")).thenReturn(Optional.of(pendingAction));
        when(authenticationService.getCurrentUserId()).thenReturn("manager-1");
        when(userService.isDirectManager("manager-1", "user-1")).thenReturn(true);
        when(actionCaptureRepository.save(any(Action.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(organizationRepository.findById("org-1")).thenReturn(Optional.of(organization));

        // When
        Action result = actionService.approveAction("action-1");

        // Then
        assertEquals(CaptureStatus.APPROVED, result.getStatus());
        assertEquals("manager-1", result.getApprovedBy());
        verify(userService).awardPoints("user-1", 50, "Action approved: Test Action");
        verify(userService).updateMissionProgress("user-1", "action-1");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void approveAction_WithNonDirectManager_ShouldThrowSecurityException() {
        // Given
        Action pendingAction = Action.builder()
                .id("action-1")
                .organizationId("org-1")
                .userId("user-1")
                .actionTypeId("action-1")
                .status(CaptureStatus.PENDING_APPROVAL)
                .build();

        when(actionCaptureRepository.findById("action-1")).thenReturn(Optional.of(pendingAction));
        when(authenticationService.getCurrentUserId()).thenReturn("manager-2");
        when(userService.isDirectManager("manager-2", "user-1")).thenReturn(false);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class,
                () -> actionService.approveAction("action-1"));
        assertEquals("Only the direct manager can approve this action", exception.getMessage());
        verify(actionCaptureRepository, never()).save(any(Action.class));
        verify(userService, never()).awardPoints(anyString(), any(Integer.class), anyString());
    }

    @Test
    void rejectAction_WithDirectManager_ShouldRejectSuccessfully() {
        // Given
        Action pendingAction = Action.builder()
                .id("action-1")
                .organizationId("org-1")
                .userId("user-1")
                .actionTypeId("action-1")
                .status(CaptureStatus.PENDING_APPROVAL)
                .build();

        when(actionCaptureRepository.findById("action-1")).thenReturn(Optional.of(pendingAction));
        when(authenticationService.getCurrentUserId()).thenReturn("manager-1");
        when(userService.isDirectManager("manager-1", "user-1")).thenReturn(true);
        when(actionCaptureRepository.save(any(Action.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Action result = actionService.rejectAction("action-1", "Invalid evidence");

        // Then
        assertEquals(CaptureStatus.REJECTED, result.getStatus());
        assertEquals("manager-1", result.getApprovedBy());
        assertEquals("Invalid evidence", result.getRejectionReason());
        verify(eventRepository).save(any(Event.class));
        verify(userService, never()).awardPoints(anyString(), any(Integer.class), anyString());
    }

    @Test
    void rejectAction_WithNonDirectManager_ShouldThrowSecurityException() {
        // Given
        Action pendingAction = Action.builder()
                .id("action-1")
                .organizationId("org-1")
                .userId("user-1")
                .actionTypeId("action-1")
                .status(CaptureStatus.PENDING_APPROVAL)
                .build();

        when(actionCaptureRepository.findById("action-1")).thenReturn(Optional.of(pendingAction));
        when(authenticationService.getCurrentUserId()).thenReturn("manager-2");
        when(userService.isDirectManager("manager-2", "user-1")).thenReturn(false);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class,
                () -> actionService.rejectAction("action-1", "Invalid evidence"));
        assertEquals("Only the direct manager can reject this action", exception.getMessage());
        verify(actionCaptureRepository, never()).save(any(Action.class));
    }

    @Test
    void getPendingApprovals_WithDirectReports_ShouldReturnFilteredActions() {
        // Given
        User directReport1 = User.builder().id("user-1").build();
        User directReport2 = User.builder().id("user-2").build();
        List<User> directReports = List.of(directReport1, directReport2);

        Action action1 = Action.builder().id("action-1").userId("user-1").status(CaptureStatus.PENDING_APPROVAL).build();
        Action action2 = Action.builder().id("action-2").userId("user-2").status(CaptureStatus.PENDING_APPROVAL).build();
        Page<Action> expectedPage = new PageImpl<>(List.of(action1, action2));

        when(userService.getDirectReports("manager-1")).thenReturn(directReports);
        when(actionCaptureRepository.findByStatusAndUserIdIn(
                eq(CaptureStatus.PENDING_APPROVAL),
                eq(List.of("user-1", "user-2")),
                any(PageRequest.class))
        ).thenReturn(expectedPage);

        // When
        Page<Action> result = actionService.getPendingApprovals("manager-1", PageRequest.of(0, 10));

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals("action-1", result.getContent().get(0).getId());
        assertEquals("action-2", result.getContent().get(1).getId());
    }

    @Test
    void getPendingApprovals_WithNoDirectReports_ShouldReturnEmptyPage() {
        // Given
        when(userService.getDirectReports("manager-1")).thenReturn(Collections.emptyList());

        // When
        Page<Action> result = actionService.getPendingApprovals("manager-1", PageRequest.of(0, 10));

        // Then
        assertTrue(result.isEmpty());
        verify(actionCaptureRepository, never()).findByStatusAndUserIdIn(any(), any(), any());
    }
}