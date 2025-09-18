package com.starfleet.gamifier.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionCapture domain model.
 */
class ActionTest {

    private Action action;
    private Instant testTime;

    @BeforeEach
    void setUp() {
        testTime = Instant.now();
        action = Action.builder()
                .organizationId("org123")
                .userId("user123")
                .actionTypeId("action123")
                .actionDate(LocalDate.now())
                .captureMethod(CaptureMethod.UI)
                .reporterUserId("user123")
                .evidence("test evidence")
                .notes("test notes")
                .build();
    }

    @Test
    void shouldCreateActionCaptureWithDefaults() {
        assertNotNull(action);
        assertEquals("org123", action.getOrganizationId());
        assertEquals("user123", action.getUserId());
        assertEquals("action123", action.getActionTypeId());
        assertEquals(LocalDate.now(), action.getActionDate());
        assertEquals(CaptureMethod.UI, action.getCaptureMethod());
        assertEquals("user123", action.getReporterUserId());
        assertEquals(CaptureStatus.PENDING_APPROVAL, action.getStatus());
        assertEquals("test evidence", action.getEvidence());
        assertEquals("test notes", action.getNotes());
        assertNotNull(action.getCreatedAt());
        assertNotNull(action.getUpdatedAt());
    }

    @Test
    void shouldApproveAction() {
        String approverId = "manager123";

        action.approve(approverId);

        assertEquals(CaptureStatus.APPROVED, action.getStatus());
        assertEquals(approverId, action.getApprovedBy());
        assertNotNull(action.getApprovedAt());
        assertTrue(action.getUpdatedAt().isAfter(testTime));
        assertTrue(action.isApproved());
        assertFalse(action.isPending());
        assertFalse(action.isRejected());
    }

    @Test
    void shouldRejectAction() {
        String approverId = "manager123";
        String rejectionReason = "Insufficient evidence";

        action.reject(approverId, rejectionReason);

        assertEquals(CaptureStatus.REJECTED, action.getStatus());
        assertEquals(rejectionReason, action.getRejectionReason());
        assertEquals(approverId, action.getApprovedBy());
        assertTrue(action.getUpdatedAt().isAfter(testTime));
        assertTrue(action.isRejected());
        assertFalse(action.isPending());
        assertFalse(action.isApproved());
    }

    @Test
    void shouldTestStatusMethods() {
        // Test pending status
        assertTrue(action.isPending());
        assertFalse(action.isApproved());
        assertFalse(action.isRejected());

        // Test approved status
        action.setStatus(CaptureStatus.APPROVED);
        assertFalse(action.isPending());
        assertTrue(action.isApproved());
        assertFalse(action.isRejected());

        // Test rejected status
        action.setStatus(CaptureStatus.REJECTED);
        assertFalse(action.isPending());
        assertFalse(action.isApproved());
        assertTrue(action.isRejected());
    }

    @Test
    void shouldTestCaptureMethodMethods() {
        // Test UI capture
        action.setCaptureMethod(CaptureMethod.UI);
        assertFalse(action.wasImported());

        // Test import capture
        action.setCaptureMethod(CaptureMethod.IMPORT);
        assertTrue(action.wasImported());
    }

    @Test
    void shouldTestReporterUserId() {
        // Test reporter user ID is set correctly
        assertEquals("user123", action.getReporterUserId());

        // Test setting different reporter
        action.setReporterUserId("manager123");
        assertEquals("manager123", action.getReporterUserId());
    }

    @Test
    void shouldTestImportedActionCapture() {
        Action importedAction = Action.builder()
                .organizationId("org123")
                .userId("user123")
                .actionTypeId("action123")
                .actionDate(LocalDate.now())
                .captureMethod(CaptureMethod.IMPORT)
                .reporterUserId("SYSTEM")
                .status(CaptureStatus.APPROVED) // Import actions are auto-approved
                .build();

        assertTrue(importedAction.wasImported());
        assertTrue(importedAction.isApproved());
        assertFalse(importedAction.isPending());
    }

    @Test
    void shouldTestPeerReportedAction() {
        Action peerAction = Action.builder()
                .reporterUserId("peer123")
                .build();

        assertEquals("peer123", peerAction.getReporterUserId());
    }

    @Test
    void shouldTestManagerReportedAction() {
        Action managerAction = Action.builder()
                .reporterUserId("manager123")
                .build();

        assertEquals("manager123", managerAction.getReporterUserId());
    }
}