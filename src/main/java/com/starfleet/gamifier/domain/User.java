package com.starfleet.gamifier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * User aggregate root for the gamification system.
 * Represents employees participating in the gamification program.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "users")
@CompoundIndex(def = "{'organizationId': 1, 'employeeId': 1}", unique = true)
@CompoundIndex(def = "{'organizationId': 1, 'totalPoints': -1}")
@CompoundIndex(def = "{'organizationId': 1, 'managerEmployeeId': 1}")
public class User {

    @Id
    private String id;

    private String organizationId;
    private String employeeId;
    private String name;
    private String surname;
    private String managerEmployeeId;

    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    private Integer totalPoints = 0;

    private String currentRankId;

    @Builder.Default
    private Instant lastLogin = Instant.now();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    // Embedded mission progress
    @Builder.Default
    private List<MissionProgress> missionProgress = new java.util.ArrayList<>();

    // Business methods
    public void updateProfile(String name, String surname, String managerEmployeeId) {
        this.name = name;
        this.surname = surname;
        this.managerEmployeeId = managerEmployeeId;
        this.updatedAt = Instant.now();
    }

    public void addPoints(Integer points) {
        this.totalPoints = (this.totalPoints != null ? this.totalPoints : 0) + points;
        this.updatedAt = Instant.now();
    }

    public void updateRank(String rankId) {
        this.currentRankId = rankId;
        this.updatedAt = Instant.now();
    }

    public void recordLogin() {
        this.lastLogin = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    public Optional<MissionProgress> getMissionProgress(String missionTypeId) {
        return missionProgress.stream()
                .filter(mp -> mp.getMissionTypeId().equals(missionTypeId))
                .findFirst();
    }

    // Query methods for mission progress

    public List<MissionProgress> getCompletedMissions() {
        return missionProgress.stream()
                .filter(MissionProgress::getCompleted)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Embedded MissionProgress within User
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissionProgress {
        private String missionTypeId;

        @Builder.Default
        private java.util.Set<String> completedActionTypeIds = new java.util.HashSet<>();

        @Builder.Default
        private Boolean completed = false;

        private java.time.LocalDateTime completionDate;

        @Builder.Default
        private Instant updatedAt = Instant.now();
    }
}