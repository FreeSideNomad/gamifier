package com.starfleet.gamifier.service;

import com.starfleet.gamifier.domain.CaptureMethod;
import com.starfleet.gamifier.domain.Organization;
import com.starfleet.gamifier.domain.ReporterType;
import com.starfleet.gamifier.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for Organization management and configuration operations.
 * Handles all knowledge layer operations within the organization aggregate.
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

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
}