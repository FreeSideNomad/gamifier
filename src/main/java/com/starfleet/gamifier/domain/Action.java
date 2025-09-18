package com.starfleet.gamifier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document(collection = "actions")
@CompoundIndex(def = "{'organizationId': 1, 'userId': 1, 'actionTypeId': 1, 'actionDate': 1}", unique = true)
public class Action {

    @Id
    private String id;

    private String organizationId;
    private String userId;
    private String actionTypeId;
    private LocalDate actionDate;
    private CaptureMethod captureMethod;
    private String reporterUserId; // ID of the person who reported the action

    @Builder.Default
    private CaptureStatus status = CaptureStatus.PENDING_APPROVAL;

    private String evidence;
    private String notes;
    private String rejectionReason;
    private Instant approvedAt;
    private String approvedBy;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    // Business methods
    public void approve(String approverId) {
        this.status = CaptureStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void reject(String approverId, String rejectionReason) {
        this.status = CaptureStatus.REJECTED;
        this.approvedBy = approverId;
        this.rejectionReason = rejectionReason;
        this.updatedAt = Instant.now();
    }

    public boolean isApproved() {
        return CaptureStatus.APPROVED.equals(this.status);
    }

    public boolean isPending() {
        return CaptureStatus.PENDING_APPROVAL.equals(this.status);
    }

    public boolean isRejected() {
        return CaptureStatus.REJECTED.equals(this.status);
    }

    public boolean wasImported() {
        return CaptureMethod.IMPORT.equals(this.captureMethod);
    }
}