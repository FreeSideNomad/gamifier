package com.starfleet.gamifier.controller.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTOs for Leaderboard operations
 */
public class LeaderboardResponses {

    @Data
    public static class LeaderboardEntry {
        private String userId;
        private String name;
        private String surname;
        private String employeeId;
        private Integer totalPoints;
        private Integer monthlyPoints;
        private String currentRank;
        private String insignia;
        private Integer position;
        private String department;
    }

    @Data
    public static class UserLeaderboardPosition {
        private String userId;
        private Integer position;
        private Integer totalUsers;
        private Integer totalPoints;
        private String currentRank;
        private List<LeaderboardEntry> nearbyUsers; // Users ranked around this user
    }

    @Data
    public static class LeaderboardStatistics {
        private Integer totalUsers;
        private Integer activeUsers;
        private Double averagePoints;
        private Integer topUserPoints;
        private String topUserName;
        private List<DepartmentStats> departmentStats;
    }

    @Data
    public static class DepartmentStats {
        private String department;
        private Integer userCount;
        private Double averagePoints;
        private Integer totalPoints;
    }
}