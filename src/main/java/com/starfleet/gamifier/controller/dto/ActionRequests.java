package com.starfleet.gamifier.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTOs for Action operations
 */
public class ActionRequests {

    @Data
    public static class CaptureActionRequest {
        @NotBlank(message = "Action type ID is required")
        private String actionTypeId;

        @NotNull(message = "Action date is required")
        private LocalDate actionDate;

        private String targetUserId; // If capturing for someone else

        private String evidence;

        private String notes;
    }

    @Data
    public static class ApproveActionRequest {
        @NotBlank(message = "Approver ID is required")
        private String approverId;

        @Size(max = 200, message = "Approval notes must not exceed 200 characters")
        private String approvalNotes;
    }

    @Data
    public static class RejectActionRequest {
        @NotBlank(message = "Rejection reason is required")
        @Size(max = 200, message = "Rejection reason must not exceed 200 characters")
        private String rejectionReason;
    }

    @Data
    @Builder
    public static class ImportResult {
        private Integer totalRecords;
        private Integer successfulImports;
        private Integer failedImports;
        private List<String> errors;
    }

    @Data
    public static class ActionStatistics {
        private Long totalActions;
        private Long pendingApprovals;
        private Long approvedActions;
        private Long rejectedActions;
        private Long todayActions;
        private Long weekActions;
        private Long monthActions;
    }
}