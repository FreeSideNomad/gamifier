package com.starfleet.gamifier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Organization aggregate root containing all configuration data.
 * Single MongoDB document with embedded action types, mission types, and rank configurations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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

    public Optional<MissionType> getMissionType(String missionId) {
        return missionTypes.stream()
                .filter(mt -> mt.getId().equals(missionId))
                .findFirst();
    }

    public List<MissionType> getActiveMissionTypes() {
        return missionTypes.stream()
                .filter(MissionType::getActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<MissionType> getMissionTypesWithActionType(String actionTypeId) {
        return missionTypes.stream()
                .filter(MissionType::getActive)
                .filter(mission -> mission.getRequiredActionTypeIds().contains(actionTypeId))
                .collect(java.util.stream.Collectors.toList());
    }

    // Query methods for collections

    public Optional<ActionType> getActionType(String actionTypeId) {
        return actionTypes.stream()
                .filter(at -> at.getId().equals(actionTypeId))
                .findFirst();
    }

    public Optional<ActionType> getActionTypeByName(String name) {
        return actionTypes.stream()
                .filter(at -> at.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<ActionType> getActiveActionTypes() {
        return actionTypes.stream()
                .filter(ActionType::getActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<RankConfiguration> getRankConfiguration(String rankId) {
        return rankConfigurations.stream()
                .filter(rank -> rank.getId().equals(rankId))
                .findFirst();
    }

    public List<RankConfiguration> getActiveRankConfigurations() {
        return rankConfigurations.stream()
                .filter(RankConfiguration::getActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<RankConfiguration> getActiveRankConfigurationsSorted() {
        return rankConfigurations.stream()
                .filter(RankConfiguration::getActive)
                .sorted((r1, r2) -> Integer.compare(r1.getPointsThreshold(), r2.getPointsThreshold()))
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<RankConfiguration> getEligibleRank(Integer userPoints) {
        return rankConfigurations.stream()
                .filter(rank -> rank.isEligibleForPoints(userPoints) && rank.getActive())
                .max((r1, r2) -> Integer.compare(r1.getPointsThreshold(), r2.getPointsThreshold()));
    }

    public Optional<RankConfiguration> getNextRank(Integer currentPoints) {
        return rankConfigurations.stream()
                .filter(RankConfiguration::getActive)
                .filter(rank -> rank.getPointsThreshold() > currentPoints)
                .min((r1, r2) -> Integer.compare(r1.getPointsThreshold(), r2.getPointsThreshold()));
    }

    /**
     * Embedded ActionType within Organization
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
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
    @Builder(toBuilder = true)
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

        public boolean getActive() {
            return Boolean.TRUE.equals(this.active);
        }
    }

    /**
     * Embedded RankConfiguration within Organization
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
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

        public boolean getActive() {
            return Boolean.TRUE.equals(this.active);
        }
    }
}