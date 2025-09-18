package com.starfleet.gamifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.Instant;
import java.util.List;

/**
 * User aggregate root for the gamification system.
 * Represents employees participating in the gamification program.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
@CompoundIndex(def = "{'organizationId': 1, 'employeeId': 1}", unique = true)
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
    private List<MissionProgress> missionProgress = List.of();

    /**
     * Embedded MissionProgress within User
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MissionProgress {
        private String missionTypeId;
        private List<String> completedActionTypeIds;
        private Boolean completed;
        private Instant completionDate;

        @Builder.Default
        private Instant updatedAt = Instant.now();
    }

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
}