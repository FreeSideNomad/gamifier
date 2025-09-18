package com.starfleet.gamifier.controller.dto;

import com.starfleet.gamifier.domain.CaptureMethod;
import com.starfleet.gamifier.domain.ReporterType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * Request DTOs for Organization operations
 */
public class OrganizationRequests {

    @Data
    public static class CreateOrganizationRequest {
        @NotBlank(message = "Organization name is required")
        @Size(max = 100, message = "Organization name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Federation ID is required")
        @Size(max = 50, message = "Federation ID must not exceed 50 characters")
        private String federationId;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }

    @Data
    public static class UpdateOrganizationRequest {
        @NotBlank(message = "Organization name is required")
        @Size(max = 100, message = "Organization name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }

    @Data
    public static class CreateActionTypeRequest {
        @NotBlank(message = "Action type name is required")
        @Size(max = 100, message = "Action type name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @NotNull(message = "Points value is required")
        @Min(value = 1, message = "Points must be at least 1")
        @Max(value = 1000, message = "Points must not exceed 1000")
        private Integer points;

        @Size(max = 50, message = "Category must not exceed 50 characters")
        private String category;

        @NotEmpty(message = "At least one capture method is required")
        private Set<CaptureMethod> captureMethods;

        @NotEmpty(message = "At least one reporter type is required")
        private Set<ReporterType> allowedReporters;

        private Boolean requiresManagerApproval = false;
    }

    @Data
    public static class UpdateActionTypeRequest {
        @NotBlank(message = "Action type name is required")
        @Size(max = 100, message = "Action type name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @NotNull(message = "Points value is required")
        @Min(value = 1, message = "Points must be at least 1")
        @Max(value = 1000, message = "Points must not exceed 1000")
        private Integer points;

        @Size(max = 50, message = "Category must not exceed 50 characters")
        private String category;

        @NotEmpty(message = "At least one capture method is required")
        private Set<CaptureMethod> captureMethods;

        @NotEmpty(message = "At least one reporter type is required")
        private Set<ReporterType> allowedReporters;

        private Boolean requiresManagerApproval = false;
    }

    @Data
    public static class CreateMissionTypeRequest {
        @NotBlank(message = "Mission type name is required")
        @Size(max = 100, message = "Mission type name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @NotBlank(message = "Badge is required")
        @Size(max = 10, message = "Badge must not exceed 10 characters")
        private String badge;

        @NotEmpty(message = "At least one required action type is needed")
        private List<String> requiredActionTypeIds;

        @Min(value = 0, message = "Bonus points cannot be negative")
        @Max(value = 1000, message = "Bonus points must not exceed 1000")
        private Integer bonusPoints = 0;

        @Size(max = 50, message = "Category must not exceed 50 characters")
        private String category;
    }

    @Data
    public static class UpdateMissionTypeRequest {
        @NotBlank(message = "Mission type name is required")
        @Size(max = 100, message = "Mission type name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @NotBlank(message = "Badge is required")
        @Size(max = 10, message = "Badge must not exceed 10 characters")
        private String badge;

        @NotEmpty(message = "At least one required action type is needed")
        private List<String> requiredActionTypeIds;

        @Min(value = 0, message = "Bonus points cannot be negative")
        @Max(value = 1000, message = "Bonus points must not exceed 1000")
        private Integer bonusPoints = 0;

        @Size(max = 50, message = "Category must not exceed 50 characters")
        private String category;
    }

    @Data
    public static class CreateRankRequest {
        @NotBlank(message = "Rank name is required")
        @Size(max = 50, message = "Rank name must not exceed 50 characters")
        private String name;

        @Size(max = 200, message = "Description must not exceed 200 characters")
        private String description;

        @NotNull(message = "Points threshold is required")
        @Min(value = 0, message = "Points threshold cannot be negative")
        private Integer pointsThreshold;

        @NotBlank(message = "Insignia is required")
        @Size(max = 10, message = "Insignia must not exceed 10 characters")
        private String insignia;

        @NotNull(message = "Order is required")
        @Min(value = 1, message = "Order must be at least 1")
        private Integer order;
    }

    @Data
    public static class UpdateRankRequest {
        @NotBlank(message = "Rank name is required")
        @Size(max = 50, message = "Rank name must not exceed 50 characters")
        private String name;

        @Size(max = 200, message = "Description must not exceed 200 characters")
        private String description;

        @NotNull(message = "Points threshold is required")
        @Min(value = 0, message = "Points threshold cannot be negative")
        private Integer pointsThreshold;

        @NotBlank(message = "Insignia is required")
        @Size(max = 10, message = "Insignia must not exceed 10 characters")
        private String insignia;

        @NotNull(message = "Order is required")
        @Min(value = 1, message = "Order must be at least 1")
        private Integer order;
    }
}