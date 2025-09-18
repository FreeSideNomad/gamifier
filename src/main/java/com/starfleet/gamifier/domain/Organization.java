package com.starfleet.gamifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Organization aggregate root containing all configuration data.
 * Single MongoDB document with embedded action types, mission types, and rank configurations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "organizations")
public class Organization {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String federationId;
    private String description;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    // Embedded configuration data
    @Builder.Default
    private List<ActionType> actionTypes = List.of();

    @Builder.Default
    private List<MissionType> missionTypes = List.of();

    @Builder.Default
    private List<RankConfiguration> rankConfigurations = List.of();

    /**
     * Embedded ActionType within Organization
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionType {
        private String id;
        private String name;
        private String description;
        private Integer points;
        private Set<CaptureMethod> captureMethods;
        private Set<ReporterType> allowedReporters;

        @Builder.Default
        private Boolean requiresManagerApproval = false;

        @Builder.Default
        private Boolean active = true;

        private String category; // e.g., "Exploration", "Diplomacy", "Engineering"

        @Builder.Default
        private Instant createdAt = Instant.now();

        @Builder.Default
        private Instant updatedAt = Instant.now();

        public boolean supportsUICapture() {
            return captureMethods != null && captureMethods.contains(CaptureMethod.UI);
        }

        public boolean supportsImportCapture() {
            return captureMethods != null && captureMethods.contains(CaptureMethod.IMPORT);
        }

        public boolean allowsReporter(ReporterType reporterType) {
            return allowedReporters != null && allowedReporters.contains(reporterType);
        }

        public boolean requiresApproval() {
            return Boolean.TRUE.equals(this.requiresManagerApproval);
        }
    }

    /**
     * Embedded MissionType within Organization
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissionType {
        private String id;
        private String name;
        private String description;
        private String badge; // Badge/insignia identifier or emoji
        private List<String> requiredActionTypeIds;

        @Builder.Default
        private Integer bonusPoints = 0;

        @Builder.Default
        private Boolean active = true;

        private String category; // e.g., "Exploration", "Command Training"

        @Builder.Default
        private Instant createdAt = Instant.now();

        @Builder.Default
        private Instant updatedAt = Instant.now();

        public boolean hasRequiredActions() {
            return requiredActionTypeIds != null && !requiredActionTypeIds.isEmpty();
        }

        public int getRequiredActionCount() {
            return requiredActionTypeIds != null ? requiredActionTypeIds.size() : 0;
        }

        public boolean requiresActionType(String actionTypeId) {
            return requiredActionTypeIds != null && requiredActionTypeIds.contains(actionTypeId);
        }
    }

    /**
     * Embedded RankConfiguration within Organization
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankConfiguration {
        private String id;
        private String name;
        private String description;
        private Integer pointsThreshold;
        private String insignia; // Visual representation (emoji, badge code)
        private Integer order; // Display order for ranking

        @Builder.Default
        private Boolean active = true;

        @Builder.Default
        private Instant createdAt = Instant.now();

        @Builder.Default
        private Instant updatedAt = Instant.now();

        public boolean isEligibleForPoints(Integer userPoints) {
            if (userPoints == null || pointsThreshold == null) {
                return false;
            }
            return userPoints >= pointsThreshold;
        }

        public boolean isHigherThan(RankConfiguration other) {
            if (other == null || other.pointsThreshold == null || this.pointsThreshold == null) {
                return false;
            }
            return this.pointsThreshold > other.pointsThreshold;
        }
    }

    // Business methods for Organization
    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }
}