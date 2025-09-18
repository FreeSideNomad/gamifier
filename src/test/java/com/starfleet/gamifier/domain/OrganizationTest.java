package com.starfleet.gamifier.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Organization domain model and embedded entities.
 */
class OrganizationTest {

    private Organization organization;
    private Instant testTime;

    @BeforeEach
    void setUp() {
        testTime = Instant.now();
        organization = Organization.builder()
                .name("Test Federation")
                .federationId("TEST-001")
                .description("Test organization")
                .build();
    }

    @Test
    void shouldCreateOrganizationWithDefaults() {
        assertNotNull(organization);
        assertEquals("Test Federation", organization.getName());
        assertEquals("TEST-001", organization.getFederationId());
        assertEquals("Test organization", organization.getDescription());
        assertTrue(organization.getActive());
        assertNotNull(organization.getCreatedAt());
        assertNotNull(organization.getUpdatedAt());
        assertNotNull(organization.getActionTypes());
        assertNotNull(organization.getMissionTypes());
        assertNotNull(organization.getRankConfigurations());
    }

    @Test
    void shouldUpdateOrganizationDetails() {
        String newName = "Updated Federation";
        String newDescription = "Updated description";

        organization.updateDetails(newName, newDescription);

        assertEquals(newName, organization.getName());
        assertEquals(newDescription, organization.getDescription());
        assertTrue(organization.getUpdatedAt().isAfter(testTime));
    }

    @Test
    void shouldActivateOrganization() {
        organization.setActive(false);

        organization.activate();

        assertTrue(organization.getActive());
        assertTrue(organization.isActive());
        assertTrue(organization.getUpdatedAt().isAfter(testTime));
    }

    @Test
    void shouldDeactivateOrganization() {
        organization.deactivate();

        assertFalse(organization.getActive());
        assertFalse(organization.isActive());
        assertTrue(organization.getUpdatedAt().isAfter(testTime));
    }

    @Test
    void shouldHandleNullActiveStatus() {
        organization.setActive(null);
        assertFalse(organization.isActive());
    }

    @Test
    void shouldCreateActionTypeWithDefaults() {
        Organization.ActionType actionType = Organization.ActionType.builder()
                .name("Test Action")
                .description("Test description")
                .points(50)
                .build();

        assertNotNull(actionType);
        assertEquals("Test Action", actionType.getName());
        assertEquals("Test description", actionType.getDescription());
        assertEquals(50, actionType.getPoints());
        assertFalse(actionType.getRequiresManagerApproval());
        assertTrue(actionType.getActive());
        assertNotNull(actionType.getCreatedAt());
        assertNotNull(actionType.getUpdatedAt());
    }

    @Test
    void shouldTestActionTypeBusinessMethods() {
        Organization.ActionType actionType = Organization.ActionType.builder()
                .captureMethods(Set.of(CaptureMethod.UI, CaptureMethod.IMPORT))
                .allowedReporters(Set.of(ReporterType.SELF, ReporterType.PEER))
                .requiresManagerApproval(true)
                .build();

        assertTrue(actionType.supportsUICapture());
        assertTrue(actionType.supportsImportCapture());
        assertTrue(actionType.allowsReporter(ReporterType.SELF));
        assertTrue(actionType.allowsReporter(ReporterType.PEER));
        assertFalse(actionType.allowsReporter(ReporterType.MANAGER));
        assertTrue(actionType.requiresApproval());
    }

    @Test
    void shouldTestActionTypeWithNullValues() {
        Organization.ActionType actionType = Organization.ActionType.builder().build();

        assertFalse(actionType.supportsUICapture());
        assertFalse(actionType.supportsImportCapture());
        assertFalse(actionType.allowsReporter(ReporterType.SELF));
        assertFalse(actionType.requiresApproval());
    }

    @Test
    void shouldCreateMissionTypeWithDefaults() {
        Organization.MissionType missionType = Organization.MissionType.builder()
                .name("Test Mission")
                .description("Test mission description")
                .badge("🎯")
                .requiredActionTypeIds(List.of("action1", "action2"))
                .build();

        assertNotNull(missionType);
        assertEquals("Test Mission", missionType.getName());
        assertEquals("Test mission description", missionType.getDescription());
        assertEquals("🎯", missionType.getBadge());
        assertEquals(2, missionType.getRequiredActionTypeIds().size());
        assertEquals(0, missionType.getBonusPoints());
        assertTrue(missionType.getActive());
        assertNotNull(missionType.getCreatedAt());
        assertNotNull(missionType.getUpdatedAt());
    }

    @Test
    void shouldTestMissionTypeBusinessMethods() {
        Organization.MissionType missionType = Organization.MissionType.builder()
                .requiredActionTypeIds(List.of("action1", "action2", "action3"))
                .build();

        assertTrue(missionType.hasRequiredActions());
        assertEquals(3, missionType.getRequiredActionCount());
        assertTrue(missionType.requiresActionType("action1"));
        assertTrue(missionType.requiresActionType("action2"));
        assertFalse(missionType.requiresActionType("nonexistent"));
    }

    @Test
    void shouldTestMissionTypeWithEmptyActions() {
        Organization.MissionType missionType = Organization.MissionType.builder()
                .requiredActionTypeIds(List.of())
                .build();

        assertFalse(missionType.hasRequiredActions());
        assertEquals(0, missionType.getRequiredActionCount());
        assertFalse(missionType.requiresActionType("action1"));
    }

    @Test
    void shouldTestMissionTypeWithNullActions() {
        Organization.MissionType missionType = Organization.MissionType.builder().build();

        assertFalse(missionType.hasRequiredActions());
        assertEquals(0, missionType.getRequiredActionCount());
        assertFalse(missionType.requiresActionType("action1"));
    }

    @Test
    void shouldCreateRankConfigurationWithDefaults() {
        Organization.RankConfiguration rank = Organization.RankConfiguration.builder()
                .name("Test Rank")
                .pointsThreshold(100)
                .insignia("⭐")
                .order(1)
                .build();

        assertNotNull(rank);
        assertEquals("Test Rank", rank.getName());
        assertEquals(100, rank.getPointsThreshold());
        assertEquals("⭐", rank.getInsignia());
        assertEquals(1, rank.getOrder());
        assertTrue(rank.getActive());
        assertNotNull(rank.getCreatedAt());
        assertNotNull(rank.getUpdatedAt());
    }

    @Test
    void shouldTestRankEligibilityMethods() {
        Organization.RankConfiguration rank = Organization.RankConfiguration.builder()
                .pointsThreshold(100)
                .build();

        assertTrue(rank.isEligibleForPoints(100));
        assertTrue(rank.isEligibleForPoints(150));
        assertFalse(rank.isEligibleForPoints(50));
        assertFalse(rank.isEligibleForPoints(null));
    }

    @Test
    void shouldTestRankComparisonMethods() {
        Organization.RankConfiguration lowerRank = Organization.RankConfiguration.builder()
                .pointsThreshold(100)
                .build();

        Organization.RankConfiguration higherRank = Organization.RankConfiguration.builder()
                .pointsThreshold(200)
                .build();

        assertTrue(higherRank.isHigherThan(lowerRank));
        assertFalse(lowerRank.isHigherThan(higherRank));
        assertFalse(lowerRank.isHigherThan(null));
        assertFalse(lowerRank.isHigherThan(Organization.RankConfiguration.builder().build()));
    }

    @Test
    void shouldTestRankWithNullValues() {
        Organization.RankConfiguration rank = Organization.RankConfiguration.builder().build();

        assertFalse(rank.isEligibleForPoints(100));
        assertFalse(rank.isHigherThan(Organization.RankConfiguration.builder().pointsThreshold(50).build()));
    }
}