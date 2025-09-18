package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.UserRequests.*;
import com.starfleet.gamifier.domain.*;
import com.starfleet.gamifier.repository.EventRepository;
import com.starfleet.gamifier.repository.OrganizationRepository;
import com.starfleet.gamifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for User management operations.
 * Handles user profiles, dashboards, CSV imports, and leaderboards.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final EventRepository eventRepository;

    // User Management Operations
    public UserDashboardResponse getUserDashboard(String userId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        return UserDashboardResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .employeeId(user.getEmployeeId())
                .totalPoints(user.getTotalPoints())
                .currentRank(getCurrentRankName(user, organization))
                .currentRankInsignia(getCurrentRankInsignia(user, organization))
                .nextRank(getNextRankName(user, organization))
                .pointsToNextRank(getPointsToNextRank(user, organization))
                .missionProgress(getMissionProgressSummaries(user, organization))
                .availableActions(getAvailableActionSummaries(organization))
                .recentEvents(getRecentEvents(user))
                .build();
    }

    public User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    public User getUserByEmployeeId(String organizationId, String employeeId) {
        return userRepository.findByOrganizationIdAndEmployeeId(organizationId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with employee ID: " + employeeId + " in organization: " + organizationId));
    }

    public User updateUser(String userId, String name, String surname, String managerEmployeeId) {
        User user = getUser(userId);
        user.updateProfile(name, surname, managerEmployeeId);
        return userRepository.save(user);
    }

    public Page<User> getAllUsers(String organizationId, Pageable pageable) {
        return userRepository.findByOrganizationId(organizationId, pageable);
    }

    // CSV Import Functionality
    public ImportResult importUsersFromCsv(MultipartFile file, String organizationId) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        // Verify organization exists
        getOrganization(organizationId);

        List<String> errors = new ArrayList<>();
        int totalRecords = 0;
        int successfulImports = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    validateCsvHeader(line);
                    continue;
                }

                totalRecords++;
                try {
                    User user = parseCsvLineToUser(line, organizationId);
                    userRepository.save(user);
                    successfulImports++;
                } catch (Exception e) {
                    errors.add("Line " + totalRecords + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }

        return ImportResult.builder()
                .totalRecords(totalRecords)
                .successfulImports(successfulImports)
                .failedImports(totalRecords - successfulImports)
                .errors(errors)
                .build();
    }

    // Leaderboard Operations
    public List<LeaderboardEntry> getMonthlyLeaderboard(String organizationId, int limit) {
        // For now, return all-time leaderboard as monthly calculation needs action capture dates
        return getAllTimeLeaderboard(organizationId, limit);
    }

    public List<LeaderboardEntry> getAllTimeLeaderboard(String organizationId, int limit) {
        List<User> topUsers = userRepository.findTop10ByOrganizationIdOrderByTotalPointsDesc(organizationId);
        Organization organization = getOrganization(organizationId);

        return topUsers.stream()
                .limit(limit)
                .map(user -> LeaderboardEntry.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .surname(user.getSurname())
                        .employeeId(user.getEmployeeId())
                        .totalPoints(user.getTotalPoints())
                        .currentRank(getCurrentRankName(user, organization))
                        .insignia(getCurrentRankInsignia(user, organization))
                        .position(topUsers.indexOf(user) + 1)
                        .build())
                .collect(Collectors.toList());
    }

    // Mission Progress Operations

    /**
     * Update mission progress for a user after an action is completed.
     * Checks all active missions to see if any are now complete.
     */
    public void updateMissionProgress(String userId, String actionTypeId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        // Find all missions that include this action type
        List<Organization.MissionType> relevantMissions = organization.getMissionTypesWithActionType(actionTypeId);

        for (Organization.MissionType missionType : relevantMissions) {
            updateSingleMissionProgress(user, missionType, actionTypeId);
        }

        // Save updated user
        userRepository.save(user);
    }

    /**
     * Check if a mission is completed and award badge/points if so.
     */
    private void updateSingleMissionProgress(User user, Organization.MissionType missionType, String completedActionTypeId) {
        // Find or create mission progress for this user
        Optional<User.MissionProgress> existingProgress = user.getMissionProgress(missionType.getId());

        User.MissionProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();

            // Skip if mission already completed
            if (progress.getCompleted()) {
                return;
            }
        } else {
            // Create new mission progress
            progress = User.MissionProgress.builder()
                    .missionTypeId(missionType.getId())
                    .completedActionTypeIds(new java.util.HashSet<>())
                    .completed(false)
                    .build();
            user.getMissionProgress().add(progress);
        }

        // Add the completed action type if not already present
        progress.getCompletedActionTypeIds().add(completedActionTypeId);

        // Check if mission is now complete
        java.util.Set<String> requiredActionTypes = new java.util.HashSet<>(missionType.getRequiredActionTypeIds());
        boolean missionComplete = progress.getCompletedActionTypeIds().containsAll(requiredActionTypes);

        if (missionComplete && !progress.getCompleted()) {
            completeMission(user, missionType, progress);
        }
    }

    /**
     * Complete a mission - award badge, bonus points, and generate events.
     */
    private void completeMission(User user, Organization.MissionType missionType, User.MissionProgress progress) {
        // Mark mission as completed
        progress.setCompleted(true);
        progress.setCompletionDate(java.time.LocalDateTime.now());

        // Award bonus points via awardPoints (this handles rank promotion automatically)
        awardPoints(user.getId(), missionType.getBonusPoints(),
                String.format("Mission completed: %s", missionType.getName()));

        // Generate mission completion event
        Event missionEvent = Event.builder()
                .organizationId(user.getOrganizationId())
                .userId(user.getId())
                .eventType(EventType.MISSION_COMPLETED)
                .data(String.format("Mission '%s' completed! Earned badge: %s (+%d bonus points)",
                        missionType.getName(), missionType.getBadge(), missionType.getBonusPoints()))
                .build();

        eventRepository.save(missionEvent);

        log.info("Mission completed: {} for user {} - awarded {} bonus points",
                missionType.getName(), user.getEmployeeId(), missionType.getBonusPoints());
    }

    /**
     * Get detailed mission progress for a specific user and mission.
     */
    public MissionProgressDetails getMissionProgressDetails(String userId, String missionId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        Organization.MissionType missionType = organization.getMissionType(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        Optional<User.MissionProgress> userProgress = user.getMissionProgress(missionId);

        // Build action progress details
        List<ActionProgressDetail> actionDetails = missionType.getRequiredActionTypeIds().stream()
                .map(actionTypeId -> {
                    Organization.ActionType actionType = getActionType(organization, actionTypeId);
                    boolean completed = userProgress.map(mp ->
                                    mp.getCompletedActionTypeIds().contains(actionTypeId))
                            .orElse(false);

                    return ActionProgressDetail.builder()
                            .actionTypeId(actionTypeId)
                            .actionName(actionType.getName())
                            .actionDescription(actionType.getDescription())
                            .actionCategory(actionType.getCategory())
                            .points(actionType.getPoints())
                            .completed(completed)
                            .build();
                })
                .collect(Collectors.toList());

        int completedActionsCount = actionDetails.stream()
                .mapToInt(detail -> detail.getCompleted() ? 1 : 0)
                .sum();

        return MissionProgressDetails.builder()
                .missionId(missionId)
                .missionName(missionType.getName())
                .description(missionType.getDescription())
                .badge(missionType.getBadge())
                .category(missionType.getCategory())
                .bonusPoints(missionType.getBonusPoints())
                .totalRequiredActions(missionType.getRequiredActionCount())
                .completedActionsCount(completedActionsCount)
                .completed(userProgress.map(User.MissionProgress::getCompleted).orElse(false))
                .completionDate(userProgress.map(User.MissionProgress::getCompletionDate).orElse(null))
                .actionProgress(actionDetails)
                .build();
    }

    /**
     * Get summary of all mission progress for a user.
     */
    public List<com.starfleet.gamifier.controller.dto.UserRequests.MissionProgressSummary> getAllMissionProgress(String userId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        return organization.getActiveMissionTypes().stream()
                .map(missionType -> {
                    Optional<User.MissionProgress> progress = user.getMissionProgress(missionType.getId());

                    int completedActions = progress.map(mp -> mp.getCompletedActionTypeIds().size()).orElse(0);
                    boolean completed = progress.map(User.MissionProgress::getCompleted).orElse(false);

                    return com.starfleet.gamifier.controller.dto.UserRequests.MissionProgressSummary.builder()
                            .missionId(missionType.getId())
                            .missionName(missionType.getName())
                            .badge(missionType.getBadge())
                            .completedActions(completedActions)
                            .totalActions(missionType.getRequiredActionCount())
                            .completed(completed)
                            .bonusPoints(missionType.getBonusPoints())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get earned badges for a user.
     */
    public List<BadgeInfo> getEarnedBadges(String userId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        return user.getCompletedMissions().stream()
                .map(progress -> {
                    Organization.MissionType mission = organization.getMissionType(progress.getMissionTypeId())
                            .orElse(null);

                    if (mission == null) return null;

                    return BadgeInfo.builder()
                            .missionId(mission.getId())
                            .missionName(mission.getName())
                            .badge(mission.getBadge())
                            .category(mission.getCategory())
                            .description(mission.getDescription())
                            .bonusPoints(mission.getBonusPoints())
                            .earnedDate(progress.getCompletionDate())
                            .build();
                })
                .filter(badge -> badge != null)
                .collect(Collectors.toList());
    }

    public MissionProgressResponse getMissionProgress(String userId, String missionId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        Optional<Organization.MissionType> missionType = organization.getMissionType(missionId);

        if (missionType.isEmpty()) {
            throw new IllegalArgumentException("Mission not found: " + missionId);
        }

        Organization.MissionType mission = missionType.get();
        Optional<User.MissionProgress> userProgress = user.getMissionProgress(missionId);

        List<ActionProgress> actionProgress = mission.getRequiredActionTypeIds().stream()
                .map(actionTypeId -> {
                    String actionName = getActionTypeName(organization, actionTypeId);
                    boolean completed = userProgress.map(mp ->
                                    mp.getCompletedActionTypeIds().contains(actionTypeId))
                            .orElse(false);

                    return ActionProgress.builder()
                            .actionTypeId(actionTypeId)
                            .actionName(actionName)
                            .completed(completed)
                            .completionDate(completed ? LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                            .build();
                })
                .collect(Collectors.toList());

        return MissionProgressResponse.builder()
                .missionId(missionId)
                .missionName(mission.getName())
                .description(mission.getDescription())
                .badge(mission.getBadge())
                .category(mission.getCategory())
                .completed(userProgress.map(User.MissionProgress::getCompleted).orElse(false))
                .actionProgress(actionProgress)
                .bonusPoints(mission.getBonusPoints())
                .build();
    }

    // User Points and Rank Operations

    /**
     * Get current rank information for a user.
     */
    public UserRankInfo getCurrentRank(String userId) {
        User user = getUser(userId);
        Organization organization = getOrganization(user.getOrganizationId());

        Optional<Organization.RankConfiguration> currentRank = getCurrentRankConfiguration(user, organization);
        Optional<Organization.RankConfiguration> nextRank = getNextRankConfiguration(user, organization);

        return UserRankInfo.builder()
                .userId(userId)
                .currentRankId(user.getCurrentRankId())
                .currentRankName(currentRank.map(Organization.RankConfiguration::getName).orElse("No Rank"))
                .currentRankInsignia(currentRank.map(Organization.RankConfiguration::getInsignia).orElse(""))
                .currentPoints(user.getTotalPoints())
                .currentRankThreshold(currentRank.map(Organization.RankConfiguration::getPointsThreshold).orElse(0))
                .nextRankId(nextRank.map(Organization.RankConfiguration::getId).orElse(null))
                .nextRankName(nextRank.map(Organization.RankConfiguration::getName).orElse(null))
                .nextRankInsignia(nextRank.map(Organization.RankConfiguration::getInsignia).orElse(null))
                .nextRankThreshold(nextRank.map(Organization.RankConfiguration::getPointsThreshold).orElse(null))
                .pointsToNextRank(nextRank.map(r -> r.getPointsThreshold() - user.getTotalPoints()).orElse(null))
                .build();
    }

    private Optional<Organization.RankConfiguration> getCurrentRankConfiguration(User user, Organization organization) {
        if (user.getCurrentRankId() == null) {
            return Optional.empty();
        }

        return organization.getRankConfiguration(user.getCurrentRankId());
    }

    private Optional<Organization.RankConfiguration> getNextRankConfiguration(User user, Organization organization) {
        return organization.getNextRank(user.getTotalPoints());
    }

    /**
     * Award points to a user and check for rank promotion.
     * This is the single method that should be used for all point awarding.
     */
    @CacheEvict(value = {"allTimeLeaderboard", "monthlyLeaderboard", "leaderboardStats"},
                key = "#root.target.getUser(#userId).organizationId")
    public void awardPoints(String userId, Integer points, String reason) {
        User user = getUser(userId);
        user.addPoints(points);

        // Check for rank promotion
        Organization organization = getOrganization(user.getOrganizationId());
        boolean promoted = checkAndPromoteUserInternal(user, organization);

        userRepository.save(user);

        // Generate points awarded event
        generatePointsAwardedEvent(user, points, reason);

        log.info("Awarded {} points to user {} - {}", points, user.getEmployeeId(), reason);
    }

    /**
     * Check if user is eligible for rank promotion and promote if necessary.
     * Generates rank promotion event if promoted.
     *
     * @return true if user was promoted, false otherwise
     */
    private boolean checkAndPromoteUserInternal(User user, Organization organization) {
        Optional<Organization.RankConfiguration> newRank = organization.getEligibleRank(user.getTotalPoints());

        if (newRank.isPresent() && !newRank.get().getId().equals(user.getCurrentRankId())) {
            String oldRankId = user.getCurrentRankId();
            user.updateRank(newRank.get().getId());

            // Generate rank promotion event
            generateRankPromotionEvent(user, oldRankId, newRank.get());

            log.info("User {} promoted from rank {} to rank: {}",
                    user.getEmployeeId(), oldRankId, newRank.get().getName());
            return true;
        }
        return false;
    }

    /**
     * Generate event for points being awarded to user
     */
    private void generatePointsAwardedEvent(User user, Integer points, String reason) {
        Event pointsEvent = Event.builder()
                .organizationId(user.getOrganizationId())
                .userId(user.getId())
                .eventType(EventType.POINTS_AWARDED)
                .data(String.format("Awarded %d points - %s", points, reason))
                .build();

        eventRepository.save(pointsEvent);
    }

    /**
     * Generate event for rank promotion
     */
    private void generateRankPromotionEvent(User user, String oldRankId, Organization.RankConfiguration newRank) {
        Event rankEvent = Event.builder()
                .organizationId(user.getOrganizationId())
                .userId(user.getId())
                .eventType(EventType.RANK_PROMOTED)
                .data(String.format("Promoted to rank: %s %s", newRank.getName(), newRank.getInsignia()))
                .build();

        eventRepository.save(rankEvent);
    }


    // Legacy method - deprecated, use awardPoints instead
    @Deprecated
    public void addPointsToUser(String userId, Integer points) {
        awardPoints(userId, points, "Legacy point award");
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Helper Methods
    private Organization getOrganization(String organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
    }

    private String getCurrentRankName(User user, Organization organization) {
        if (user.getCurrentRankId() == null) return "Unranked";

        return organization.getRankConfiguration(user.getCurrentRankId())
                .map(Organization.RankConfiguration::getName)
                .orElse("Unknown Rank");
    }

    private String getCurrentRankInsignia(User user, Organization organization) {
        if (user.getCurrentRankId() == null) return "🔸";

        return organization.getRankConfiguration(user.getCurrentRankId())
                .map(Organization.RankConfiguration::getInsignia)
                .orElse("🔸");
    }

    private String getNextRankName(User user, Organization organization) {
        return organization.getNextRank(user.getTotalPoints())
                .map(Organization.RankConfiguration::getName)
                .orElse("Admiral"); // Highest rank achieved
    }

    private Integer getPointsToNextRank(User user, Organization organization) {
        return organization.getNextRank(user.getTotalPoints())
                .map(rank -> rank.getPointsThreshold() - user.getTotalPoints())
                .orElse(0); // No next rank
    }

    private List<com.starfleet.gamifier.controller.dto.UserRequests.MissionProgressSummary> getMissionProgressSummaries(User user, Organization organization) {
        return organization.getActiveMissionTypes().stream()
                .map(missionType -> {
                    Optional<User.MissionProgress> progress = user.getMissionProgress(missionType.getId());

                    int completedActions = progress.map(mp -> mp.getCompletedActionTypeIds().size()).orElse(0);
                    boolean completed = progress.map(User.MissionProgress::getCompleted).orElse(false);

                    return com.starfleet.gamifier.controller.dto.UserRequests.MissionProgressSummary.builder()
                            .missionId(missionType.getId())
                            .missionName(missionType.getName())
                            .badge(missionType.getBadge())
                            .completedActions(completedActions)
                            .totalActions(missionType.getRequiredActionCount())
                            .completed(completed)
                            .bonusPoints(missionType.getBonusPoints())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ActionTypeSummary> getAvailableActionSummaries(Organization organization) {
        return organization.getActiveActionTypes().stream()
                .map(actionType -> ActionTypeSummary.builder()
                        .actionTypeId(actionType.getId())
                        .name(actionType.getName())
                        .description(actionType.getDescription())
                        .points(actionType.getPoints())
                        .category(actionType.getCategory())
                        .canCapture(actionType.supportsUICapture())
                        .build())
                .collect(Collectors.toList());
    }

    private List<RecentEvent> getRecentEvents(User user) {
        // Placeholder - will be implemented when EventService is complete
        return List.of(
                RecentEvent.builder()
                        .eventType("USER_REGISTERED")
                        .title("Welcome to Starfleet!")
                        .description("Your journey begins now, " + user.getName())
                        .timestamp(user.getCreatedAt().toString())
                        .build()
        );
    }

    private String getActionTypeName(Organization organization, String actionTypeId) {
        return organization.getActionType(actionTypeId)
                .map(Organization.ActionType::getName)
                .orElse("Unknown Action");
    }

    private void validateCsvHeader(String headerLine) {
        String[] expectedHeaders = {"employee_id", "name", "surname", "manager_employee_id", "role"};
        String[] actualHeaders = headerLine.toLowerCase().split(",");

        if (actualHeaders.length < 4) {
            throw new IllegalArgumentException("CSV must have at least 4 columns: employee_id, name, surname, manager_employee_id");
        }
    }

    private User parseCsvLineToUser(String line, String organizationId) {
        String[] values = line.split(",");

        if (values.length < 4) {
            throw new IllegalArgumentException("Invalid CSV line format");
        }

        String employeeId = values[0].trim();
        String name = values[1].trim();
        String surname = values[2].trim();
        String managerEmployeeId = values[3].trim().isEmpty() ? null : values[3].trim();
        UserRole role = values.length > 4 && "admin".equalsIgnoreCase(values[4].trim()) ? UserRole.ADMIN : UserRole.USER;

        // Check for duplicate employee ID
        if (userRepository.existsByOrganizationIdAndEmployeeId(organizationId, employeeId)) {
            throw new IllegalArgumentException("Employee ID already exists: " + employeeId);
        }

        return User.builder()
                .organizationId(organizationId)
                .employeeId(employeeId)
                .name(name)
                .surname(surname)
                .managerEmployeeId(managerEmployeeId)
                .role(role)
                .totalPoints(0)
                .missionProgress(new ArrayList<>())
                .build();
    }

    public List<User> getUsersByOrganization(String organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    /**
     * Check if a user is the direct manager of another user.
     */
    public boolean isDirectManager(String managerId, String subordinateId) {
        User manager = getUser(managerId);
        User subordinate = getUser(subordinateId);

        // Check if they're in the same organization
        if (!manager.getOrganizationId().equals(subordinate.getOrganizationId())) {
            return false;
        }

        // Check if the subordinate's manager employee ID matches the manager's employee ID
        return subordinate.getManagerEmployeeId() != null &&
                subordinate.getManagerEmployeeId().equals(manager.getEmployeeId());
    }

    /**
     * Get all users who report directly to a manager.
     */
    public List<User> getDirectReports(String managerId) {
        User manager = getUser(managerId);
        return userRepository.findByOrganizationIdAndManagerEmployeeId(
                manager.getOrganizationId(),
                manager.getEmployeeId());
    }

    private Organization.ActionType getActionType(Organization organization, String actionTypeId) {
        return organization.getActionType(actionTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Action type not found: " + actionTypeId));
    }

    // DTOs for mission progress

    @lombok.Data
    @lombok.Builder
    public static class MissionProgressDetails {
        private String missionId;
        private String missionName;
        private String description;
        private String badge;
        private String category;
        private Integer bonusPoints;
        private Integer totalRequiredActions;
        private Integer completedActionsCount;
        private Boolean completed;
        private java.time.LocalDateTime completionDate;
        private List<ActionProgressDetail> actionProgress;
    }

    @lombok.Data
    @lombok.Builder
    public static class ActionProgressDetail {
        private String actionTypeId;
        private String actionName;
        private String actionDescription;
        private String actionCategory;
        private Integer points;
        private Boolean completed;
    }

    @lombok.Data
    @lombok.Builder
    public static class BadgeInfo {
        private String missionId;
        private String missionName;
        private String badge;
        private String category;
        private String description;
        private Integer bonusPoints;
        private java.time.LocalDateTime earnedDate;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserRankInfo {
        private String userId;
        private String currentRankId;
        private String currentRankName;
        private String currentRankInsignia;
        private Integer currentPoints;
        private Integer currentRankThreshold;
        private String nextRankId;
        private String nextRankName;
        private String nextRankInsignia;
        private Integer nextRankThreshold;
        private Integer pointsToNextRank;
    }
}