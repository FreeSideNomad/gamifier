package com.starfleet.gamifier.service;

import com.starfleet.gamifier.domain.CaptureMethod;
import com.starfleet.gamifier.domain.Organization;
import com.starfleet.gamifier.domain.ReporterType;
import com.starfleet.gamifier.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrganizationService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        testOrganization = Organization.builder()
                .id("org123")
                .name("Test Federation")
                .federationId("TEST-001")
                .description("Test organization")
                .actionTypes(new ArrayList<>())
                .missionTypes(new ArrayList<>())
                .rankConfigurations(new ArrayList<>())
                .build();
    }

    @Test
    void shouldCreateOrganization() {
        when(organizationRepository.existsByName("Test Federation")).thenReturn(false);
        when(organizationRepository.existsByFederationId("TEST-001")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization result = organizationService.createOrganization("Test Federation", "TEST-001", "Test description");

        assertNotNull(result);
        assertEquals("Test Federation", result.getName());
        assertEquals("TEST-001", result.getFederationId());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateName() {
        when(organizationRepository.existsByName("Test Federation")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createOrganization("Test Federation", "TEST-001", "Test description"));

        assertEquals("Organization with name 'Test Federation' already exists", exception.getMessage());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateFederationId() {
        when(organizationRepository.existsByName("Test Federation")).thenReturn(false);
        when(organizationRepository.existsByFederationId("TEST-001")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createOrganization("Test Federation", "TEST-001", "Test description"));

        assertEquals("Organization with federation ID 'TEST-001' already exists", exception.getMessage());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldGetOrganization() {
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));

        Organization result = organizationService.getOrganization("org123");

        assertNotNull(result);
        assertEquals("Test Federation", result.getName());
        verify(organizationRepository).findById("org123");
    }

    @Test
    void shouldThrowExceptionWhenOrganizationNotFound() {
        when(organizationRepository.findById("nonexistent")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                organizationService.getOrganization("nonexistent"));

        assertEquals("Organization not found: nonexistent", exception.getMessage());
    }

    @Test
    void shouldUpdateOrganization() {
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization result = organizationService.updateOrganization("org123", "Updated Name", "Updated Description");

        assertNotNull(result);
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void shouldDeleteOrganization() {
        organizationService.deleteOrganization("org123");

        verify(organizationRepository).deleteById("org123");
    }

    @Test
    void shouldGetAllActiveOrganizations() {
        List<Organization> activeOrgs = List.of(testOrganization);
        when(organizationRepository.findByActiveTrue()).thenReturn(activeOrgs);

        List<Organization> result = organizationService.getAllActiveOrganizations();

        assertEquals(1, result.size());
        assertEquals("Test Federation", result.get(0).getName());
        verify(organizationRepository).findByActiveTrue();
    }

    @Test
    void shouldCreateActionType() {
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization.ActionType result = organizationService.createActionType(
                "org123",
                "Test Action",
                "Test description",
                50,
                "Testing",
                Set.of(CaptureMethod.UI),
                Set.of(ReporterType.SELF),
                false
        );

        assertNotNull(result);
        assertEquals("Test Action", result.getName());
        assertEquals(50, result.getPoints());
        assertEquals("Testing", result.getCategory());
        assertFalse(result.getRequiresManagerApproval());
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateActionType() {
        Organization.ActionType existingAction = Organization.ActionType.builder()
                .name("Existing Action")
                .build();
        testOrganization.getActionTypes().add(existingAction);

        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createActionType(
                        "org123",
                        "Existing Action",
                        "Test description",
                        50,
                        "Testing",
                        Set.of(CaptureMethod.UI),
                        Set.of(ReporterType.SELF),
                        false
                ));

        assertEquals("Action type with name 'Existing Action' already exists in this organization", exception.getMessage());
    }

    @Test
    void shouldGetActionTypes() {
        Organization.ActionType activeAction = Organization.ActionType.builder()
                .name("Active Action")
                .active(true)
                .build();
        Organization.ActionType inactiveAction = Organization.ActionType.builder()
                .name("Inactive Action")
                .active(false)
                .build();

        testOrganization.getActionTypes().addAll(List.of(activeAction, inactiveAction));
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));

        List<Organization.ActionType> result = organizationService.getActionTypes("org123");

        assertEquals(1, result.size());
        assertEquals("Active Action", result.get(0).getName());
    }

    @Test
    void shouldUpdateActionType() {
        Organization.ActionType existingAction = Organization.ActionType.builder()
                .id("action123")
                .name("Original Action")
                .build();
        testOrganization.getActionTypes().add(existingAction);

        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization.ActionType result = organizationService.updateActionType(
                "org123",
                "action123",
                "Updated Action",
                "Updated description",
                75,
                "Updated Category",
                Set.of(CaptureMethod.UI, CaptureMethod.IMPORT),
                Set.of(ReporterType.SELF, ReporterType.PEER),
                true
        );

        assertNotNull(result);
        assertEquals("Updated Action", existingAction.getName());
        assertEquals("Updated description", existingAction.getDescription());
        assertEquals(75, existingAction.getPoints());
        assertTrue(existingAction.getRequiresManagerApproval());
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentActionType() {
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                organizationService.updateActionType(
                        "org123",
                        "nonexistent",
                        "Updated Action",
                        "Updated description",
                        75,
                        "Updated Category",
                        Set.of(CaptureMethod.UI),
                        Set.of(ReporterType.SELF),
                        false
                ));

        assertEquals("Action type not found: nonexistent", exception.getMessage());
    }

    @Test
    void shouldDeleteActionType() {
        Organization.ActionType actionToDelete = Organization.ActionType.builder()
                .id("action123")
                .name("Action to Delete")
                .build();
        testOrganization.getActionTypes().add(actionToDelete);

        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        organizationService.deleteActionType("org123", "action123");

        assertTrue(testOrganization.getActionTypes().isEmpty());
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void shouldCreateMissionType() {
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization.MissionType result = organizationService.createMissionType(
                "org123",
                "Test Mission",
                "Test mission description",
                "🎯",
                List.of("action1", "action2"),
                100,
                "Testing"
        );

        assertNotNull(result);
        assertEquals("Test Mission", result.getName());
        assertEquals("🎯", result.getBadge());
        assertEquals(100, result.getBonusPoints());
        assertEquals("Testing", result.getCategory());
        assertEquals(2, result.getRequiredActionTypeIds().size());
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void shouldCreateRank() {
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization.RankConfiguration result = organizationService.createRank(
                "org123",
                "Test Rank",
                "Test rank description",
                100,
                "⭐",
                1
        );

        assertNotNull(result);
        assertEquals("Test Rank", result.getName());
        assertEquals(100, result.getPointsThreshold());
        assertEquals("⭐", result.getInsignia());
        assertEquals(1, result.getOrder());
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateRankThreshold() {
        Organization.RankConfiguration existingRank = Organization.RankConfiguration.builder()
                .pointsThreshold(100)
                .build();
        testOrganization.getRankConfigurations().add(existingRank);

        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                organizationService.createRank("org123", "Test Rank", "Description", 100, "⭐", 1));

        assertEquals("Rank with points threshold 100 already exists in this organization", exception.getMessage());
    }

    @Test
    void shouldGetRanksOrderedByOrder() {
        Organization.RankConfiguration rank1 = Organization.RankConfiguration.builder()
                .name("Second Rank")
                .order(2)
                .active(true)
                .build();
        Organization.RankConfiguration rank2 = Organization.RankConfiguration.builder()
                .name("First Rank")
                .order(1)
                .active(true)
                .build();
        Organization.RankConfiguration inactiveRank = Organization.RankConfiguration.builder()
                .name("Inactive Rank")
                .order(3)
                .active(false)
                .build();

        testOrganization.getRankConfigurations().addAll(List.of(rank1, rank2, inactiveRank));
        when(organizationRepository.findById("org123")).thenReturn(Optional.of(testOrganization));

        List<Organization.RankConfiguration> result = organizationService.getRanks("org123");

        assertEquals(2, result.size());
        assertEquals("First Rank", result.get(0).getName());
        assertEquals("Second Rank", result.get(1).getName());
    }
}