package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.controller.dto.OrganizationRequests.*;
import com.starfleet.gamifier.domain.Organization;
import com.starfleet.gamifier.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Organization management (Configuration Service)
 * Handles organization CRUD operations and serves as the knowledge layer.
 */
@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<Organization> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        Organization organization = organizationService.createOrganization(
                request.getName(),
                request.getFederationId(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable String orgId) {
        Organization organization = organizationService.getOrganization(orgId);
        return ResponseEntity.ok(organization);
    }

    @PutMapping("/{orgId}")
    public ResponseEntity<Organization> updateOrganization(
            @PathVariable String orgId,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        Organization organization = organizationService.updateOrganization(
                orgId,
                request.getName(),
                request.getDescription()
        );
        return ResponseEntity.ok(organization);
    }

    @DeleteMapping("/{orgId}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable String orgId) {
        organizationService.deleteOrganization(orgId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllActiveOrganizations();
        return ResponseEntity.ok(organizations);
    }

    // Action Types Management
    @PostMapping("/{orgId}/action-types")
    public ResponseEntity<Organization.ActionType> createActionType(
            @PathVariable String orgId,
            @Valid @RequestBody CreateActionTypeRequest request) {
        Organization.ActionType actionType = organizationService.createActionType(
                orgId,
                request.getName(),
                request.getDescription(),
                request.getPoints(),
                request.getCategory(),
                request.getCaptureMethods(),
                request.getAllowedReporters(),
                request.getRequiresManagerApproval()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(actionType);
    }

    @GetMapping("/{orgId}/action-types")
    public ResponseEntity<List<Organization.ActionType>> getActionTypes(@PathVariable String orgId) {
        List<Organization.ActionType> actionTypes = organizationService.getActionTypes(orgId);
        return ResponseEntity.ok(actionTypes);
    }

    @PutMapping("/{orgId}/action-types/{actionTypeId}")
    public ResponseEntity<Organization.ActionType> updateActionType(
            @PathVariable String orgId,
            @PathVariable String actionTypeId,
            @Valid @RequestBody UpdateActionTypeRequest request) {
        Organization.ActionType actionType = organizationService.updateActionType(
                orgId,
                actionTypeId,
                request.getName(),
                request.getDescription(),
                request.getPoints(),
                request.getCategory(),
                request.getCaptureMethods(),
                request.getAllowedReporters(),
                request.getRequiresManagerApproval()
        );
        return ResponseEntity.ok(actionType);
    }

    @DeleteMapping("/{orgId}/action-types/{actionTypeId}")
    public ResponseEntity<Void> deleteActionType(@PathVariable String orgId, @PathVariable String actionTypeId) {
        organizationService.deleteActionType(orgId, actionTypeId);
        return ResponseEntity.noContent().build();
    }

    // Mission Types Management
    @PostMapping("/{orgId}/mission-types")
    public ResponseEntity<Organization.MissionType> createMissionType(
            @PathVariable String orgId,
            @Valid @RequestBody CreateMissionTypeRequest request) {
        Organization.MissionType missionType = organizationService.createMissionType(
                orgId,
                request.getName(),
                request.getDescription(),
                request.getBadge(),
                request.getRequiredActionTypeIds(),
                request.getBonusPoints(),
                request.getCategory()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(missionType);
    }

    @GetMapping("/{orgId}/mission-types")
    public ResponseEntity<List<Organization.MissionType>> getMissionTypes(@PathVariable String orgId) {
        List<Organization.MissionType> missionTypes = organizationService.getMissionTypes(orgId);
        return ResponseEntity.ok(missionTypes);
    }

    @PutMapping("/{orgId}/mission-types/{missionTypeId}")
    public ResponseEntity<Organization.MissionType> updateMissionType(
            @PathVariable String orgId,
            @PathVariable String missionTypeId,
            @Valid @RequestBody UpdateMissionTypeRequest request) {
        Organization.MissionType missionType = organizationService.updateMissionType(
                orgId,
                missionTypeId,
                request.getName(),
                request.getDescription(),
                request.getBadge(),
                request.getRequiredActionTypeIds(),
                request.getBonusPoints(),
                request.getCategory()
        );
        return ResponseEntity.ok(missionType);
    }

    @DeleteMapping("/{orgId}/mission-types/{missionTypeId}")
    public ResponseEntity<Void> deleteMissionType(@PathVariable String orgId, @PathVariable String missionTypeId) {
        organizationService.deleteMissionType(orgId, missionTypeId);
        return ResponseEntity.noContent().build();
    }

    // Rank Configurations Management
    @PostMapping("/{orgId}/ranks")
    public ResponseEntity<Organization.RankConfiguration> createRank(
            @PathVariable String orgId,
            @Valid @RequestBody CreateRankRequest request) {
        Organization.RankConfiguration rank = organizationService.createRank(
                orgId,
                request.getName(),
                request.getDescription(),
                request.getPointsThreshold(),
                request.getInsignia(),
                request.getOrder()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(rank);
    }

    @GetMapping("/{orgId}/ranks")
    public ResponseEntity<List<Organization.RankConfiguration>> getRanks(@PathVariable String orgId) {
        List<Organization.RankConfiguration> ranks = organizationService.getRanks(orgId);
        return ResponseEntity.ok(ranks);
    }

    @PutMapping("/{orgId}/ranks/{rankId}")
    public ResponseEntity<Organization.RankConfiguration> updateRank(
            @PathVariable String orgId,
            @PathVariable String rankId,
            @Valid @RequestBody UpdateRankRequest request) {
        Organization.RankConfiguration rank = organizationService.updateRank(
                orgId,
                rankId,
                request.getName(),
                request.getDescription(),
                request.getPointsThreshold(),
                request.getInsignia(),
                request.getOrder()
        );
        return ResponseEntity.ok(rank);
    }

    @DeleteMapping("/{orgId}/ranks/{rankId}")
    public ResponseEntity<Void> deleteRank(@PathVariable String orgId, @PathVariable String rankId) {
        organizationService.deleteRank(orgId, rankId);
        return ResponseEntity.noContent().build();
    }
}