package com.starfleet.gamifier.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionCapture domain model.
 */
class ActionCaptureTest {

    private ActionCapture actionCapture;
    private Instant testTime;

    @BeforeEach
    void setUp() {
        testTime = Instant.now();
        actionCapture = ActionCapture.builder()
                .organizationId("org123")
                .userId("user123")
                .actionTypeId("action123")
                .date(LocalDate.now())
                .captureMethod(CaptureMethod.UI)
                .reporterType(ReporterType.SELF)
                .reporterId("user123")
                .build();
    }

    @Test
    void shouldCreateActionCaptureWithDefaults() {
        assertNotNull(actionCapture);
        assertEquals("org123", actionCapture.getOrganizationId());
        assertEquals("user123", actionCapture.getUserId());
        assertEquals("action123", actionCapture.getActionTypeId());
        assertEquals(LocalDate.now(), actionCapture.getDate());
        assertEquals(CaptureMethod.UI, actionCapture.getCaptureMethod());
        assertEquals(ReporterType.SELF, actionCapture.getReporterType());
        assertEquals("user123", actionCapture.getReporterId());
        assertEquals(ActionCapture.ActionStatus.PENDING, actionCapture.getStatus());
        assertNotNull(actionCapture.getCreatedAt());
        assertNotNull(actionCapture.getUpdatedAt());
    }

    @Test
    void shouldApproveAction() {
        String approverId = "manager123";
        String notes = "Well done!";

        actionCapture.approve(approverId, notes);

        assertEquals(ActionCapture.ActionStatus.APPROVED, actionCapture.getStatus());
        assertEquals(approverId, actionCapture.getApprovedBy());
        assertEquals(notes, actionCapture.getApprovalNotes());
        assertNotNull(actionCapture.getApprovedAt());
        assertTrue(actionCapture.getUpdatedAt().isAfter(testTime));
        assertTrue(actionCapture.isApproved());
        assertFalse(actionCapture.isPending());
        assertFalse(actionCapture.isRejected());
    }

    @Test
    void shouldRejectAction() {
        String rejectionReason = "Insufficient evidence";

        actionCapture.reject(rejectionReason);

        assertEquals(ActionCapture.ActionStatus.REJECTED, actionCapture.getStatus());
        assertEquals(rejectionReason, actionCapture.getRejectionReason());
        assertTrue(actionCapture.getUpdatedAt().isAfter(testTime));
        assertTrue(actionCapture.isRejected());
        assertFalse(actionCapture.isPending());
        assertFalse(actionCapture.isApproved());
    }

    @Test
    void shouldTestStatusMethods() {
        // Test pending status
        assertTrue(actionCapture.isPending());
        assertFalse(actionCapture.isApproved());
        assertFalse(actionCapture.isRejected());

        // Test approved status
        actionCapture.setStatus(ActionCapture.ActionStatus.APPROVED);
        assertFalse(actionCapture.isPending());
        assertTrue(actionCapture.isApproved());
        assertFalse(actionCapture.isRejected());

        // Test rejected status
        actionCapture.setStatus(ActionCapture.ActionStatus.REJECTED);
        assertFalse(actionCapture.isPending());
        assertFalse(actionCapture.isApproved());
        assertTrue(actionCapture.isRejected());
    }

    @Test
    void shouldTestCaptureMethodMethods() {
        // Test UI capture
        actionCapture.setCaptureMethod(CaptureMethod.UI);
        assertFalse(actionCapture.wasImported());

        // Test import capture
        actionCapture.setCaptureMethod(CaptureMethod.IMPORT);
        assertTrue(actionCapture.wasImported());
    }

    @Test
    void shouldTestReporterTypeMethods() {
        // Test self reporting
        actionCapture.setReporterType(ReporterType.SELF);
        assertTrue(actionCapture.isSelfReported());

        // Test peer reporting
        actionCapture.setReporterType(ReporterType.PEER);
        assertFalse(actionCapture.isSelfReported());

        // Test manager reporting
        actionCapture.setReporterType(ReporterType.MANAGER);
        assertFalse(actionCapture.isSelfReported());
    }

    @Test
    void shouldTestImportedActionCapture() {
        ActionCapture importedAction = ActionCapture.builder()
                .organizationId("org123")
                .userId("user123")
                .actionTypeId("action123")
                .date(LocalDate.now())
                .captureMethod(CaptureMethod.IMPORT)
                .reporterType(ReporterType.SELF)
                .reporterId("system")
                .status(ActionCapture.ActionStatus.APPROVED) // Import actions are auto-approved
                .build();

        assertTrue(importedAction.wasImported());
        assertTrue(importedAction.isApproved());
        assertFalse(importedAction.isPending());
    }

    @Test
    void shouldTestPeerReportedAction() {
        ActionCapture peerAction = ActionCapture.builder()
                .reporterType(ReporterType.PEER)
                .reporterId("peer123")
                .build();

        assertFalse(peerAction.isSelfReported());
        assertEquals("peer123", peerAction.getReporterId());
    }

    @Test
    void shouldTestManagerReportedAction() {
        ActionCapture managerAction = ActionCapture.builder()
                .reporterType(ReporterType.MANAGER)
                .reporterId("manager123")
                .build();

        assertFalse(managerAction.isSelfReported());
        assertEquals("manager123", managerAction.getReporterId());
    }
}