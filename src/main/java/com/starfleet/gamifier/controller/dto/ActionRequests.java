package com.starfleet.gamifier.controller.dto;

import com.starfleet.gamifier.domain.ReporterType;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTOs for Action operations
 */
public class ActionRequests {

    @Data
    public static class CaptureActionRequest {
        @NotBlank(message = "Organization ID is required")
        private String organizationId;

        @NotBlank(message = "User ID is required")
        private String userId;

        @NotBlank(message = "Action type ID is required")
        private String actionTypeId;

        @NotNull(message = "Date is required")
        private LocalDate date;

        @NotNull(message = "Reporter type is required")
        private ReporterType reporterType;

        @NotBlank(message = "Reporter ID is required")
        private String reporterId;

        @Size(max = 500, message = "Evidence must not exceed 500 characters")
        private String evidence;
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