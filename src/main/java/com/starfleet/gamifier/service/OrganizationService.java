package com.starfleet.gamifier.service;

import com.starfleet.gamifier.domain.CaptureMethod;
import com.starfleet.gamifier.domain.Organization;
import com.starfleet.gamifier.domain.ReporterType;
import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.repository.OrganizationRepository;
import com.starfleet.gamifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Organization management and configuration operations.
 * Handles all knowledge layer operations within the organization aggregate.
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    // Organization CRUD operations
    public Organization createOrganization(String name, String federationId, String description) {
        if (organizationRepository.existsByName(name)) {
            throw new IllegalArgumentException("Organization with name '" + name + "' already exists");
        }
        if (organizationRepository.existsByFederationId(federationId)) {
            throw new IllegalArgumentException("Organization with federation ID '" + federationId + "' already exists");
        }

        Organization organization = Organization.builder()
                .name(name)
                .federationId(federationId)
                .description(description)
                .build();

        return organizationRepository.save(organization);
    }

    public Organization getOrganization(String orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + orgId));
    }

    public Organization updateOrganization(String orgId, String name, String description) {
        Organization organization = getOrganization(orgId);
        organization.updateDetails(name, description);
        return organizationRepository.save(organization);
    }

    public void deleteOrganization(String orgId) {
        organizationRepository.deleteById(orgId);
    }

    public List<Organization> getAllActiveOrganizations() {
        return organizationRepository.findByActiveTrue();
    }

    // Action Type operations
    public Organization.ActionType createActionType(String orgId, String name, String description, Integer points,
                                                    String category, Set<CaptureMethod> captureMethods,
                                                    Set<ReporterType> allowedReporters, Boolean requiresManagerApproval) {
        Organization organization = getOrganization(orgId);

        // Check for duplicate name
        boolean nameExists = organization.getActionTypes().stream()
                .anyMatch(at -> at.getName().equals(name));
        if (nameExists) {
            throw new IllegalArgumentException("Action type with name '" + name + "' already exists in this organization");
        }

        Organization.ActionType actionType = Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .points(points)
                .category(category)
                .captureMethods(captureMethods)
                .allowedReporters(allowedReporters)
                .requiresManagerApproval(requiresManagerApproval != null ? requiresManagerApproval : false)
                .build();

        organization.getActionTypes().add(actionType);
        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);

        return actionType;
    }

    public List<Organization.ActionType> getActionTypes(String orgId) {
        Organization organization = getOrganization(orgId);
        return organization.getActionTypes().stream()
                .filter(at -> Boolean.TRUE.equals(at.getActive()))
                .toList();
    }

    public Organization.ActionType updateActionType(String orgId, String actionTypeId, String name, String description,
                                                    Integer points, String category, Set<CaptureMethod> captureMethods,
                                                    Set<ReporterType> allowedReporters, Boolean requiresManagerApproval) {
        Organization organization = getOrganization(orgId);

        Organization.ActionType actionType = organization.getActionTypes().stream()
                .filter(at -> at.getId().equals(actionTypeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action type not found: " + actionTypeId));

        actionType.setName(name);
        actionType.setDescription(description);
        actionType.setPoints(points);
        actionType.setCategory(category);
        actionType.setCaptureMethods(captureMethods);
        actionType.setAllowedReporters(allowedReporters);
        actionType.setRequiresManagerApproval(requiresManagerApproval != null ? requiresManagerApproval : false);
        actionType.setUpdatedAt(Instant.now());

        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);

        return actionType;
    }

    public void deleteActionType(String orgId, String actionTypeId) {
        Organization organization = getOrganization(orgId);

        organization.getActionTypes().removeIf(at -> at.getId().equals(actionTypeId));
        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);
    }

    // Mission Type operations
    public Organization.MissionType createMissionType(String orgId, String name, String description, String badge,
                                                      List<String> requiredActionTypeIds, Integer bonusPoints, String category) {
        Organization organization = getOrganization(orgId);

        // Check for duplicate name
        boolean nameExists = organization.getMissionTypes().stream()
                .anyMatch(mt -> mt.getName().equals(name));
        if (nameExists) {
            throw new IllegalArgumentException("Mission type with name '" + name + "' already exists in this organization");
        }

        Organization.MissionType missionType = Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .badge(badge)
                .requiredActionTypeIds(requiredActionTypeIds)
                .bonusPoints(bonusPoints != null ? bonusPoints : 0)
                .category(category)
                .build();

        organization.getMissionTypes().add(missionType);
        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);

        return missionType;
    }

    public List<Organization.MissionType> getMissionTypes(String orgId) {
        Organization organization = getOrganization(orgId);
        return organization.getMissionTypes().stream()
                .filter(mt -> Boolean.TRUE.equals(mt.getActive()))
                .toList();
    }

    public Organization.MissionType updateMissionType(String orgId, String missionTypeId, String name, String description,
                                                      String badge, List<String> requiredActionTypeIds, Integer bonusPoints, String category) {
        Organization organization = getOrganization(orgId);

        Organization.MissionType missionType = organization.getMissionTypes().stream()
                .filter(mt -> mt.getId().equals(missionTypeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mission type not found: " + missionTypeId));

        missionType.setName(name);
        missionType.setDescription(description);
        missionType.setBadge(badge);
        missionType.setRequiredActionTypeIds(requiredActionTypeIds);
        missionType.setBonusPoints(bonusPoints != null ? bonusPoints : 0);
        missionType.setCategory(category);
        missionType.setUpdatedAt(Instant.now());

        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);

        return missionType;
    }

    public void deleteMissionType(String orgId, String missionTypeId) {
        Organization organization = getOrganization(orgId);

        organization.getMissionTypes().removeIf(mt -> mt.getId().equals(missionTypeId));
        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);
    }

    // Rank Configuration operations
    public Organization.RankConfiguration createRank(String orgId, String name, String description,
                                                     Integer pointsThreshold, String insignia, Integer order) {
        Organization organization = getOrganization(orgId);

        // Check for duplicate points threshold
        boolean thresholdExists = organization.getRankConfigurations().stream()
                .anyMatch(rc -> rc.getPointsThreshold().equals(pointsThreshold));
        if (thresholdExists) {
            throw new IllegalArgumentException("Rank with points threshold " + pointsThreshold + " already exists in this organization");
        }

        Organization.RankConfiguration rank = Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .pointsThreshold(pointsThreshold)
                .insignia(insignia)
                .order(order)
                .build();

        organization.getRankConfigurations().add(rank);
        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);

        return rank;
    }

    public List<Organization.RankConfiguration> getRanks(String orgId) {
        Organization organization = getOrganization(orgId);
        return organization.getRankConfigurations().stream()
                .filter(rc -> Boolean.TRUE.equals(rc.getActive()))
                .sorted((r1, r2) -> Integer.compare(r1.getOrder(), r2.getOrder()))
                .toList();
    }

    public Organization.RankConfiguration updateRank(String orgId, String rankId, String name, String description,
                                                     Integer pointsThreshold, String insignia, Integer order) {
        Organization organization = getOrganization(orgId);

        Organization.RankConfiguration rank = organization.getRankConfigurations().stream()
                .filter(rc -> rc.getId().equals(rankId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rank not found: " + rankId));

        rank.setName(name);
        rank.setDescription(description);
        rank.setPointsThreshold(pointsThreshold);
        rank.setInsignia(insignia);
        rank.setOrder(order);
        rank.setUpdatedAt(Instant.now());

        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);

        return rank;
    }

    public void deleteRank(String orgId, String rankId) {
        Organization organization = getOrganization(orgId);

        organization.getRankConfigurations().removeIf(rc -> rc.getId().equals(rankId));
        organization.setUpdatedAt(Instant.now());
        organizationRepository.save(organization);
    }

    // Ranking operations

    /**
     * Get available ranks for an organization.
     */
    public List<RankInfo> getAvailableRanks(String organizationId) {
        Organization organization = getOrganization(organizationId);

        return organization.getActiveRankConfigurationsSorted().stream()
                .map(rank -> RankInfo.builder()
                        .rankId(rank.getId())
                        .name(rank.getName())
                        .description(rank.getDescription())
                        .pointsThreshold(rank.getPointsThreshold())
                        .insignia(rank.getInsignia())
                        .order(rank.getOrder())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get organization rankings (leaderboard).
     */
    public List<UserRankSummary> getOrganizationRankings(String organizationId, int limit) {
        return userRepository.findByOrganizationId(organizationId).stream()
                .sorted((u1, u2) -> Integer.compare(u2.getTotalPoints(), u1.getTotalPoints()))
                .limit(limit)
                .map(user -> {
                    Organization organization = getOrganization(user.getOrganizationId());
                    Optional<Organization.RankConfiguration> rank = getCurrentRankConfiguration(user, organization);

                    return UserRankSummary.builder()
                            .userId(user.getId())
                            .employeeId(user.getEmployeeId())
                            .name(user.getName() + " " + user.getSurname())
                            .totalPoints(user.getTotalPoints())
                            .currentRankId(user.getCurrentRankId())
                            .currentRankName(rank.map(Organization.RankConfiguration::getName).orElse("No Rank"))
                            .currentRankInsignia(rank.map(Organization.RankConfiguration::getInsignia).orElse(""))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get rank statistics for an organization.
     */
    public RankStatistics getRankStatistics(String organizationId) {
        List<User> users = userRepository.findByOrganizationId(organizationId);
        Organization organization = getOrganization(organizationId);

        // Calculate statistics per rank
        List<RankDistribution> distribution = organization.getActiveRankConfigurations().stream()
                .map(rank -> {
                    long userCount = users.stream()
                            .filter(user -> rank.getId().equals(user.getCurrentRankId()))
                            .count();

                    return RankDistribution.builder()
                            .rankId(rank.getId())
                            .rankName(rank.getName())
                            .insignia(rank.getInsignia())
                            .pointsThreshold(rank.getPointsThreshold())
                            .userCount((int) userCount)
                            .build();
                })
                .collect(Collectors.toList());

        // Calculate average points
        double averagePoints = users.stream()
                .mapToInt(User::getTotalPoints)
                .average()
                .orElse(0.0);

        return RankStatistics.builder()
                .organizationId(organizationId)
                .totalUsers(users.size())
                .averagePoints((int) Math.round(averagePoints))
                .rankDistribution(distribution)
                .build();
    }

    private Optional<Organization.RankConfiguration> getCurrentRankConfiguration(User user, Organization organization) {
        if (user.getCurrentRankId() == null) {
            return Optional.empty();
        }

        return organization.getRankConfiguration(user.getCurrentRankId());
    }

    // DTOs for ranking system

    @lombok.Data
    @lombok.Builder
    public static class RankInfo {
        private String rankId;
        private String name;
        private String description;
        private Integer pointsThreshold;
        private String insignia;
        private Integer order;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserRankSummary {
        private String userId;
        private String employeeId;
        private String name;
        private Integer totalPoints;
        private String currentRankId;
        private String currentRankName;
        private String currentRankInsignia;
    }

    @lombok.Data
    @lombok.Builder
    public static class RankStatistics {
        private String organizationId;
        private Integer totalUsers;
        private Integer averagePoints;
        private List<RankDistribution> rankDistribution;
    }

    @lombok.Data
    @lombok.Builder
    public static class RankDistribution {
        private String rankId;
        private String rankName;
        private String insignia;
        private Integer pointsThreshold;
        private Integer userCount;
    }
}