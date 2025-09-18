package com.starfleet.gamifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.Instant;
import java.time.LocalDate;

/**
 * ActionCapture aggregate root for tracking completed actions.
 * Represents instances when users perform actions that earn points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "action_captures")
@CompoundIndex(def = "{'organizationId': 1, 'userId': 1, 'actionTypeId': 1, 'date': 1}", unique = true)
public class ActionCapture {

    @Id
    private String id;

    private String organizationId;
    private String userId;
    private String actionTypeId;
    private LocalDate date;
    private CaptureMethod captureMethod;
    private ReporterType reporterType;
    private String reporterId; // ID of the person who reported the action

    @Builder.Default
    private ActionStatus status = ActionStatus.PENDING;

    private String approvalNotes;
    private String rejectionReason;
    private Instant approvedAt;
    private String approvedBy;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    /**
     * Status of the action capture
     */
    public enum ActionStatus {
        PENDING,    // Waiting for approval
        APPROVED,   // Approved and points awarded
        REJECTED    // Rejected, no points awarded
    }

    // Business methods
    public void approve(String approverId, String notes) {
        this.status = ActionStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvalNotes = notes;
        this.approvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void reject(String rejectionReason) {
        this.status = ActionStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.updatedAt = Instant.now();
    }

    public boolean isApproved() {
        return ActionStatus.APPROVED.equals(this.status);
    }

    public boolean isPending() {
        return ActionStatus.PENDING.equals(this.status);
    }

    public boolean isRejected() {
        return ActionStatus.REJECTED.equals(this.status);
    }

    public boolean wasImported() {
        return CaptureMethod.IMPORT.equals(this.captureMethod);
    }

    public boolean isSelfReported() {
        return ReporterType.SELF.equals(this.reporterType);
    }
}