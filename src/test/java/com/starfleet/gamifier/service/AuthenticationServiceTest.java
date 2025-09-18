package com.starfleet.gamifier.service;

import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    private AuthenticationService authenticationService;

    private User currentUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userService);

        currentUser = User.builder()
                .id("dev-user-001")
                .organizationId("org-1")
                .employeeId("EMP-001")
                .name("Test")
                .surname("User")
                .role(UserRole.ADMIN)
                .totalPoints(1000)
                .build();

        targetUser = User.builder()
                .id("target-user")
                .organizationId("org-1")
                .employeeId("EMP-002")
                .name("Target")
                .surname("User")
                .role(UserRole.USER)
                .totalPoints(500)
                .build();
    }

    @Test
    void getCurrentUser_ReturnsExpectedUser() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When
        User result = authenticationService.getCurrentUser();

        // Then
        assertEquals(currentUser, result);
        verify(userService).getUser("dev-user-001");
    }

    @Test
    void getCurrentUserId_ReturnsHardcodedDevUserId() {
        // When
        String result = authenticationService.getCurrentUserId();

        // Then
        assertEquals("dev-user-001", result);
    }

    @Test
    void getCurrentOrganizationId_ReturnsCurrentUserOrganization() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When
        String result = authenticationService.getCurrentOrganizationId();

        // Then
        assertEquals("org-1", result);
    }

    @Test
    void isCurrentUserAdmin_WithAdminUser_ReturnsTrue() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When
        boolean result = authenticationService.isCurrentUserAdmin();

        // Then
        assertTrue(result);
    }

    @Test
    void isCurrentUserAdmin_WithRegularUser_ReturnsFalse() {
        // Given
        User regularUser = currentUser.toBuilder().role(UserRole.USER).build();
        when(userService.getUser("dev-user-001")).thenReturn(regularUser);

        // When
        boolean result = authenticationService.isCurrentUserAdmin();

        // Then
        assertFalse(result);
    }

    @Test
    void hasAccessToOrganization_WithSameOrganization_ReturnsTrue() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When
        boolean result = authenticationService.hasAccessToOrganization("org-1");

        // Then
        assertTrue(result);
    }

    @Test
    void hasAccessToOrganization_WithDifferentOrganization_ReturnsFalse() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When
        boolean result = authenticationService.hasAccessToOrganization("org-2");

        // Then
        assertFalse(result);
    }

    @Test
    void canAccessUser_WithOwnUserId_ReturnsTrue() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When
        boolean result = authenticationService.canAccessUser("dev-user-001");

        // Then
        assertTrue(result);
    }

    @Test
    void canAccessUser_AdminAccessingUserInSameOrg_ReturnsTrue() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);
        when(userService.getUser("target-user")).thenReturn(targetUser);

        // When
        boolean result = authenticationService.canAccessUser("target-user");

        // Then
        assertTrue(result);
    }

    @Test
    void canAccessUser_AdminAccessingUserInDifferentOrg_ReturnsFalse() {
        // Given
        User differentOrgUser = targetUser.toBuilder().organizationId("org-2").build();
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);
        when(userService.getUser("target-user")).thenReturn(differentOrgUser);

        // When
        boolean result = authenticationService.canAccessUser("target-user");

        // Then
        assertFalse(result);
    }

    @Test
    void canAccessUser_RegularUserAccessingOtherUser_ReturnsFalse() {
        // Given
        User regularUser = currentUser.toBuilder().role(UserRole.USER).build();
        when(userService.getUser("dev-user-001")).thenReturn(regularUser);

        // When
        boolean result = authenticationService.canAccessUser("target-user");

        // Then
        assertFalse(result);
    }

    @Test
    void requireAdminAccess_WithAdminInCorrectOrg_DoesNotThrow() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When & Then
        assertDoesNotThrow(() -> authenticationService.requireAdminAccess("org-1"));
    }

    @Test
    void requireAdminAccess_WithNonAdmin_ThrowsSecurityException() {
        // Given
        User regularUser = currentUser.toBuilder().role(UserRole.USER).build();
        when(userService.getUser("dev-user-001")).thenReturn(regularUser);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class,
                () -> authenticationService.requireAdminAccess("org-1"));
        assertEquals("Admin privileges required", exception.getMessage());
    }

    @Test
    void requireAdminAccess_WithAdminInWrongOrg_ThrowsSecurityException() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class,
                () -> authenticationService.requireAdminAccess("org-2"));
        assertEquals("Access denied to organization: org-2", exception.getMessage());
    }

    @Test
    void requireOrganizationAccess_WithAccessToOrg_DoesNotThrow() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When & Then
        assertDoesNotThrow(() -> authenticationService.requireOrganizationAccess("org-1"));
    }

    @Test
    void requireOrganizationAccess_WithoutAccessToOrg_ThrowsSecurityException() {
        // Given
        when(userService.getUser("dev-user-001")).thenReturn(currentUser);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class,
                () -> authenticationService.requireOrganizationAccess("org-2"));
        assertEquals("Access denied to organization: org-2", exception.getMessage());
    }
}