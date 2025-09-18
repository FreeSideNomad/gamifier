package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.CaptureMethod;
import com.starfleet.gamifier.domain.Organization;
import com.starfleet.gamifier.domain.ReporterType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrganizationRepository using Testcontainers.
 */
@DataMongoTest
@Testcontainers
class OrganizationRepositoryTest {

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();

        testOrganization = Organization.builder()
                .name("Test Federation")
                .federationId("TEST-001")
                .description("Test organization for unit tests")
                .build();
    }

    @Test
    void shouldSaveAndFindOrganization() {
        Organization saved = organizationRepository.save(testOrganization);

        assertNotNull(saved.getId());
        assertEquals("Test Federation", saved.getName());
        assertEquals("TEST-001", saved.getFederationId());

        Optional<Organization> found = organizationRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Federation", found.get().getName());
    }

    @Test
    void shouldFindByName() {
        organizationRepository.save(testOrganization);

        Optional<Organization> found = organizationRepository.findByName("Test Federation");
        assertTrue(found.isPresent());
        assertEquals("TEST-001", found.get().getFederationId());

        Optional<Organization> notFound = organizationRepository.findByName("Nonexistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void shouldFindByFederationId() {
        organizationRepository.save(testOrganization);

        Optional<Organization> found = organizationRepository.findByFederationId("TEST-001");
        assertTrue(found.isPresent());
        assertEquals("Test Federation", found.get().getName());

        Optional<Organization> notFound = organizationRepository.findByFederationId("NONEXISTENT");
        assertFalse(notFound.isPresent());
    }

    @Test
    void shouldFindActiveOrganizations() {
        // Save active organization
        organizationRepository.save(testOrganization);

        // Save inactive organization
        Organization inactive = Organization.builder()
                .name("Inactive Federation")
                .federationId("INACTIVE-001")
                .description("Inactive organization")
                .active(false)
                .build();
        organizationRepository.save(inactive);

        List<Organization> activeOrgs = organizationRepository.findByActiveTrue();
        assertEquals(1, activeOrgs.size());
        assertEquals("Test Federation", activeOrgs.get(0).getName());
    }

    @Test
    void shouldCheckExistenceByName() {
        assertFalse(organizationRepository.existsByName("Test Federation"));

        organizationRepository.save(testOrganization);

        assertTrue(organizationRepository.existsByName("Test Federation"));
        assertFalse(organizationRepository.existsByName("Nonexistent"));
    }

    @Test
    void shouldCheckExistenceByFederationId() {
        assertFalse(organizationRepository.existsByFederationId("TEST-001"));

        organizationRepository.save(testOrganization);

        assertTrue(organizationRepository.existsByFederationId("TEST-001"));
        assertFalse(organizationRepository.existsByFederationId("NONEXISTENT"));
    }

    @Test
    void shouldSaveOrganizationWithEmbeddedData() {
        // Create organization with embedded action types, mission types, and ranks
        Organization.ActionType actionType = Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Action")
                .description("Test action description")
                .points(50)
                .category("Testing")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.SELF))
                .requiresManagerApproval(false)
                .build();

        Organization.MissionType missionType = Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Mission")
                .description("Test mission description")
                .badge("🎯")
                .requiredActionTypeIds(List.of(actionType.getId()))
                .bonusPoints(100)
                .category("Testing")
                .build();

        Organization.RankConfiguration rank = Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Rank")
                .description("Test rank description")
                .pointsThreshold(100)
                .insignia("⭐")
                .order(1)
                .build();

        testOrganization.setActionTypes(List.of(actionType));
        testOrganization.setMissionTypes(List.of(missionType));
        testOrganization.setRankConfigurations(List.of(rank));

        Organization saved = organizationRepository.save(testOrganization);

        assertNotNull(saved.getId());
        assertEquals(1, saved.getActionTypes().size());
        assertEquals(1, saved.getMissionTypes().size());
        assertEquals(1, saved.getRankConfigurations().size());

        // Verify embedded data
        Organization.ActionType savedActionType = saved.getActionTypes().get(0);
        assertEquals("Test Action", savedActionType.getName());
        assertEquals(50, savedActionType.getPoints());
        assertEquals("Testing", savedActionType.getCategory());

        Organization.MissionType savedMissionType = saved.getMissionTypes().get(0);
        assertEquals("Test Mission", savedMissionType.getName());
        assertEquals("🎯", savedMissionType.getBadge());
        assertEquals(100, savedMissionType.getBonusPoints());

        Organization.RankConfiguration savedRank = saved.getRankConfigurations().get(0);
        assertEquals("Test Rank", savedRank.getName());
        assertEquals(100, savedRank.getPointsThreshold());
        assertEquals("⭐", savedRank.getInsignia());
    }

    @Test
    void shouldUpdateEmbeddedData() {
        // Save organization with initial data
        Organization.ActionType initialAction = Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Initial Action")
                .description("Initial description")
                .points(25)
                .build();

        testOrganization.setActionTypes(List.of(initialAction));
        Organization saved = organizationRepository.save(testOrganization);

        // Update embedded data
        Organization.ActionType updatedAction = Organization.ActionType.builder()
                .id(initialAction.getId())
                .name("Updated Action")
                .description("Updated description")
                .points(75)
                .build();

        saved.setActionTypes(List.of(updatedAction));
        Organization updated = organizationRepository.save(saved);

        assertEquals(1, updated.getActionTypes().size());
        assertEquals("Updated Action", updated.getActionTypes().get(0).getName());
        assertEquals(75, updated.getActionTypes().get(0).getPoints());
    }

    @Test
    void shouldDeleteOrganization() {
        Organization saved = organizationRepository.save(testOrganization);
        String organizationId = saved.getId();

        assertTrue(organizationRepository.findById(organizationId).isPresent());

        organizationRepository.deleteById(organizationId);

        assertFalse(organizationRepository.findById(organizationId).isPresent());
    }

    @Test
    void shouldCountOrganizations() {
        assertEquals(0, organizationRepository.count());

        organizationRepository.save(testOrganization);
        assertEquals(1, organizationRepository.count());

        Organization another = Organization.builder()
                .name("Another Federation")
                .federationId("ANOTHER-001")
                .description("Another test organization")
                .build();
        organizationRepository.save(another);

        assertEquals(2, organizationRepository.count());
    }
}