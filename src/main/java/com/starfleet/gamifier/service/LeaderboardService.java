package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.LeaderboardResponses.LeaderboardEntry;
import com.starfleet.gamifier.controller.dto.LeaderboardResponses.LeaderboardStatistics;
import com.starfleet.gamifier.controller.dto.LeaderboardResponses.UserLeaderboardPosition;
import com.starfleet.gamifier.controller.dto.LeaderboardResponses.DepartmentStats;
import com.starfleet.gamifier.domain.Organization;
import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Leaderboard operations and ranking calculations.
 * Provides monthly, all-time, and department-based leaderboards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OrganizationService organizationService;

    /**
     * Get monthly leaderboard based on points earned in specific month.
     * Note: Currently uses total points as monthly points aren't tracked separately.
     * TODO: Implement monthly point tracking for accurate monthly leaderboards.
     */
    @Cacheable(value = "monthlyLeaderboard", key = "#organizationId + '-' + #month + '-' + #pageable.pageNumber")
    public Page<LeaderboardEntry> getMonthlyLeaderboard(String organizationId, YearMonth month, Pageable pageable) {
        log.debug("Getting monthly leaderboard for organization {} and month {}", organizationId, month);

        // For now, return all-time leaderboard as monthly points aren't tracked
        // TODO: Implement monthly point tracking
        return getAllTimeLeaderboard(organizationId, pageable);
    }

    /**
     * Get all-time leaderboard based on total points.
     */
    @Cacheable(value = "allTimeLeaderboard", key = "#organizationId + '-' + #pageable.pageNumber")
    public Page<LeaderboardEntry> getAllTimeLeaderboard(String organizationId, Pageable pageable) {
        log.debug("Getting all-time leaderboard for organization {}", organizationId);

        // Create pageable with total points sorting
        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "totalPoints")
        );

        Page<User> users = userRepository.findByOrganizationId(organizationId, sortedPageable);
        Organization organization = organizationService.getOrganization(organizationId);

        List<LeaderboardEntry> entries = users.getContent().stream()
            .map(user -> createLeaderboardEntry(user, organization, users.getContent().indexOf(user) + 1 + (pageable.getPageNumber() * pageable.getPageSize())))
            .collect(Collectors.toList());

        return new PageImpl<>(entries, pageable, users.getTotalElements());
    }

    /**
     * Get department-based monthly leaderboard.
     * Note: Department filtering not implemented as User doesn't have department field.
     */
    public Page<LeaderboardEntry> getDepartmentMonthlyLeaderboard(String organizationId, String department,
                                                                  YearMonth month, Pageable pageable) {
        log.warn("Department filtering not implemented - User entity lacks department field. Returning all users.");
        return getMonthlyLeaderboard(organizationId, month, pageable);
    }

    /**
     * Get department-based all-time leaderboard.
     * Note: Department filtering not implemented as User doesn't have department field.
     */
    public Page<LeaderboardEntry> getDepartmentAllTimeLeaderboard(String organizationId, String department,
                                                                  Pageable pageable) {
        log.warn("Department filtering not implemented - User entity lacks department field. Returning all users.");
        return getAllTimeLeaderboard(organizationId, pageable);
    }

    /**
     * Get user's position in monthly leaderboard.
     */
    public UserLeaderboardPosition getUserMonthlyPosition(String organizationId, String userId, YearMonth month) {
        log.debug("Getting monthly position for user {} in organization {}", userId, organizationId);

        // For now, return all-time position as monthly points aren't tracked
        return getUserAllTimePosition(organizationId, userId);
    }

    /**
     * Get user's position in all-time leaderboard.
     */
    public UserLeaderboardPosition getUserAllTimePosition(String organizationId, String userId) {
        log.debug("Getting all-time position for user {} in organization {}", userId, organizationId);

        User targetUser = userService.getUser(userId);
        Integer userPoints = targetUser.getTotalPoints();

        // Count users with higher points
        long higherRankedUsers = userRepository.countByOrganizationIdAndTotalPointsGreaterThan(organizationId, userPoints);
        int position = (int) higherRankedUsers + 1;

        // Get total users in organization
        long totalUsers = userRepository.countByOrganizationId(organizationId);

        // Get nearby users (5 above and 5 below)
        List<LeaderboardEntry> nearbyUsers = getNearbyUsers(organizationId, position, 5);

        Organization organization = organizationService.getOrganization(organizationId);
        String currentRank = getCurrentRankName(targetUser, organization);

        UserLeaderboardPosition result = new UserLeaderboardPosition();
        result.setUserId(userId);
        result.setPosition(position);
        result.setTotalUsers((int) totalUsers);
        result.setTotalPoints(userPoints);
        result.setCurrentRank(currentRank);
        result.setNearbyUsers(nearbyUsers);

        return result;
    }

    /**
     * Get comprehensive leaderboard statistics for an organization.
     */
    @Cacheable(value = "leaderboardStats", key = "#organizationId")
    public LeaderboardStatistics getLeaderboardStatistics(String organizationId) {
        log.debug("Calculating leaderboard statistics for organization {}", organizationId);

        List<User> allUsers = userRepository.findByOrganizationId(organizationId);

        if (allUsers.isEmpty()) {
            return createEmptyStatistics();
        }

        // Calculate basic statistics
        int totalUsers = allUsers.size();
        int activeUsers = (int) allUsers.stream()
            .filter(user -> user.getTotalPoints() > 0)
            .count();

        double averagePoints = allUsers.stream()
            .mapToInt(User::getTotalPoints)
            .average()
            .orElse(0.0);

        User topUser = allUsers.stream()
            .max(Comparator.comparing(User::getTotalPoints))
            .orElse(null);

        // Create department stats (placeholder - all users in "General" department)
        DepartmentStats generalDept = new DepartmentStats();
        generalDept.setDepartment("General");
        generalDept.setUserCount(totalUsers);
        generalDept.setAveragePoints(averagePoints);
        generalDept.setTotalPoints(allUsers.stream().mapToInt(User::getTotalPoints).sum());

        LeaderboardStatistics stats = new LeaderboardStatistics();
        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setAveragePoints(averagePoints);

        if (topUser != null) {
            stats.setTopUserPoints(topUser.getTotalPoints());
            stats.setTopUserName(topUser.getName() + " " + topUser.getSurname());
        }

        stats.setDepartmentStats(Collections.singletonList(generalDept));

        return stats;
    }

    /**
     * Helper method to create LeaderboardEntry from User.
     */
    private LeaderboardEntry createLeaderboardEntry(User user, Organization organization, int position) {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setUserId(user.getId());
        entry.setName(user.getName());
        entry.setSurname(user.getSurname());
        entry.setEmployeeId(user.getEmployeeId());
        entry.setTotalPoints(user.getTotalPoints());
        entry.setMonthlyPoints(user.getTotalPoints()); // TODO: Implement monthly points tracking
        entry.setPosition(position);
        entry.setDepartment("General"); // TODO: Add department field to User

        String currentRank = getCurrentRankName(user, organization);
        entry.setCurrentRank(currentRank);

        // Get rank insignia
        String insignia = organization.getRankConfigurations().stream()
            .filter(rank -> rank.getId().equals(user.getCurrentRankId()))
            .map(Organization.RankConfiguration::getInsignia)
            .findFirst()
            .orElse("");
        entry.setInsignia(insignia);

        return entry;
    }

    /**
     * Helper method to get current rank name.
     */
    private String getCurrentRankName(User user, Organization organization) {
        if (user.getCurrentRankId() == null) {
            return "Unranked";
        }

        return organization.getRankConfigurations().stream()
            .filter(rank -> rank.getId().equals(user.getCurrentRankId()))
            .map(Organization.RankConfiguration::getName)
            .findFirst()
            .orElse("Unknown Rank");
    }

    /**
     * Helper method to get nearby users around a specific position.
     */
    private List<LeaderboardEntry> getNearbyUsers(String organizationId, int position, int range) {
        int startPosition = Math.max(1, position - range);
        int pageSize = range * 2 + 1;
        int pageNumber = Math.max(0, (startPosition - 1) / pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "totalPoints"));
        Page<LeaderboardEntry> leaderboard = getAllTimeLeaderboard(organizationId, pageable);

        return leaderboard.getContent();
    }

    /**
     * Helper method to create empty statistics.
     */
    private LeaderboardStatistics createEmptyStatistics() {
        LeaderboardStatistics stats = new LeaderboardStatistics();
        stats.setTotalUsers(0);
        stats.setActiveUsers(0);
        stats.setAveragePoints(0.0);
        stats.setTopUserPoints(0);
        stats.setTopUserName("No users");
        stats.setDepartmentStats(Collections.emptyList());
        return stats;
    }
}