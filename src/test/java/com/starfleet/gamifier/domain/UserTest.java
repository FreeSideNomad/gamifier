package com.starfleet.gamifier.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User domain model.
 */
class UserTest {

    private User user;
    private Instant testTime;

    @BeforeEach
    void setUp() {
        testTime = Instant.now();
        user = User.builder()
                .organizationId("org123")
                .employeeId("EMP001")
                .name("James T.")
                .surname("Kirk")
                .managerEmployeeId("EMP002")
                .build();
    }

    @Test
    void shouldCreateUserWithDefaults() {
        assertNotNull(user);
        assertEquals("org123", user.getOrganizationId());
        assertEquals("EMP001", user.getEmployeeId());
        assertEquals("James T.", user.getName());
        assertEquals("Kirk", user.getSurname());
        assertEquals("EMP002", user.getManagerEmployeeId());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(0, user.getTotalPoints());
        assertNotNull(user.getLastLogin());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getMissionProgress());
    }

    @Test
    void shouldUpdateUserProfile() {
        String newName = "Jean-Luc";
        String newSurname = "Picard";
        String newManagerId = "EMP003";

        user.updateProfile(newName, newSurname, newManagerId);

        assertEquals(newName, user.getName());
        assertEquals(newSurname, user.getSurname());
        assertEquals(newManagerId, user.getManagerEmployeeId());
        assertTrue(user.getUpdatedAt().isAfter(testTime));
    }

    @Test
    void shouldAddPointsToUser() {
        user.addPoints(50);
        assertEquals(50, user.getTotalPoints());
        assertTrue(user.getUpdatedAt().isAfter(testTime));

        user.addPoints(25);
        assertEquals(75, user.getTotalPoints());
    }

    @Test
    void shouldHandleNullTotalPoints() {
        user.setTotalPoints(null);
        user.addPoints(50);
        assertEquals(50, user.getTotalPoints());
    }

    @Test
    void shouldUpdateUserRank() {
        String newRankId = "rank123";

        user.updateRank(newRankId);

        assertEquals(newRankId, user.getCurrentRankId());
        assertTrue(user.getUpdatedAt().isAfter(testTime));
    }

    @Test
    void shouldRecordUserLogin() {
        Instant beforeLogin = user.getLastLogin();

        user.recordLogin();

        assertTrue(user.getLastLogin().isAfter(beforeLogin));
        assertTrue(user.getUpdatedAt().isAfter(testTime));
    }

    @Test
    void shouldGetFullName() {
        assertEquals("James T. Kirk", user.getFullName());
    }

    @Test
    void shouldCheckAdminRole() {
        assertFalse(user.isAdmin());

        user.setRole(UserRole.ADMIN);
        assertTrue(user.isAdmin());
    }

    @Test
    void shouldCreateMissionProgress() {
        User.MissionProgress progress = User.MissionProgress.builder()
                .missionTypeId("mission123")
                .completedActionTypeIds(List.of("action1", "action2"))
                .completed(false)
                .build();

        assertNotNull(progress);
        assertEquals("mission123", progress.getMissionTypeId());
        assertEquals(2, progress.getCompletedActionTypeIds().size());
        assertFalse(progress.getCompleted());
        assertNull(progress.getCompletionDate());
        assertNotNull(progress.getUpdatedAt());
    }

    @Test
    void shouldCreateCompletedMissionProgress() {
        Instant completionTime = Instant.now();
        User.MissionProgress progress = User.MissionProgress.builder()
                .missionTypeId("mission123")
                .completedActionTypeIds(List.of("action1", "action2", "action3"))
                .completed(true)
                .completionDate(completionTime)
                .build();

        assertTrue(progress.getCompleted());
        assertEquals(completionTime, progress.getCompletionDate());
    }
}