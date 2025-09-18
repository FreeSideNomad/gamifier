package com.starfleet.gamifier.service;

import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.domain.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Authentication service for managing user context and Azure AD integration.
 * Placeholder implementation for Stage 2 - will be enhanced with actual Azure AD integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;

    /**
     * Get the current authenticated user from security context.
     * Placeholder implementation - returns mock user for development.
     */
    public User getCurrentUser() {
        // TODO: Replace with actual Azure AD token parsing
        String currentUserId = getCurrentUserId();
        return userService.getUser(currentUserId);
    }

    /**
     * Get the current user ID from security context.
     * Placeholder implementation - returns hardcoded ID for development.
     */
    public String getCurrentUserId() {
        // TODO: Extract user ID from Azure AD JWT token
        return "dev-user-001"; // Development placeholder
    }

    /**
     * Get the current user's organization ID.
     */
    public String getCurrentOrganizationId() {
        User currentUser = getCurrentUser();
        return currentUser.getOrganizationId();
    }

    /**
     * Check if the current user has admin privileges.
     */
    public boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRole() == UserRole.ADMIN;
    }

    /**
     * Check if the current user has access to the specified organization.
     */
    public boolean hasAccessToOrganization(String organizationId) {
        User currentUser = getCurrentUser();
        return currentUser.getOrganizationId().equals(organizationId);
    }

    /**
     * Check if the current user can access another user's data.
     * Users can access their own data, admins can access any user in their organization.
     */
    public boolean canAccessUser(String targetUserId) {
        User currentUser = getCurrentUser();

        // Users can always access their own data
        if (currentUser.getId().equals(targetUserId)) {
            return true;
        }

        // Admins can access any user in their organization
        if (currentUser.getRole() == UserRole.ADMIN) {
            try {
                User targetUser = userService.getUser(targetUserId);
                return currentUser.getOrganizationId().equals(targetUser.getOrganizationId());
            } catch (Exception e) {
                log.warn("Failed to check access for user {}: {}", targetUserId, e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Validate that the current user has admin privileges in the specified organization.
     * Throws exception if user doesn't have access.
     */
    public void requireAdminAccess(String organizationId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin privileges required");
        }

        if (!currentUser.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Access denied to organization: " + organizationId);
        }
    }

    /**
     * Validate that the current user has access to the specified organization.
     * Throws exception if user doesn't have access.
     */
    public void requireOrganizationAccess(String organizationId) {
        if (!hasAccessToOrganization(organizationId)) {
            throw new SecurityException("Access denied to organization: " + organizationId);
        }
    }
}