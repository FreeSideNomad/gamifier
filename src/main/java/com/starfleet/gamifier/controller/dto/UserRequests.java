package com.starfleet.gamifier.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Request and Response DTOs for User operations
 */
public class UserRequests {

    @Data
    public static class UpdateUserRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must not exceed 50 characters")
        private String name;

        @NotBlank(message = "Surname is required")
        @Size(max = 50, message = "Surname must not exceed 50 characters")
        private String surname;

        @Size(max = 50, message = "Manager employee ID must not exceed 50 characters")
        private String managerEmployeeId;
    }

    @Data
    @Builder
    public static class UserDashboardResponse {
        private String userId;
        private String name;
        private String surname;
        private String employeeId;
        private Integer totalPoints;
        private String currentRank;
        private String currentRankInsignia;
        private String nextRank;
        private Integer pointsToNextRank;
        private List<MissionProgressSummary> missionProgress;
        private List<ActionTypeSummary> availableActions;
        private List<RecentEvent> recentEvents;
    }

    @Data
    @Builder
    public static class MissionProgressSummary {
        private String missionId;
        private String missionName;
        private String badge;
        private Integer completedActions;
        private Integer totalActions;
        private Boolean completed;
        private Integer bonusPoints;
    }

    @Data
    @Builder
    public static class ActionTypeSummary {
        private String actionTypeId;
        private String name;
        private String description;
        private Integer points;
        private String category;
        private Boolean canCapture;
    }

    @Data
    @Builder
    public static class RecentEvent {
        private String eventType;
        private String title;
        private String description;
        private String timestamp;
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
    @Builder
    public static class LeaderboardEntry {
        private String userId;
        private String name;
        private String surname;
        private String employeeId;
        private Integer totalPoints;
        private String currentRank;
        private String insignia;
        private Integer position;
    }

    @Data
    @Builder
    public static class MissionProgressResponse {
        private String missionId;
        private String missionName;
        private String description;
        private String badge;
        private String category;
        private Boolean completed;
        private List<ActionProgress> actionProgress;
        private Integer bonusPoints;
    }

    @Data
    @Builder
    public static class ActionProgress {
        private String actionTypeId;
        private String actionName;
        private Boolean completed;
        private String completionDate;
    }
}