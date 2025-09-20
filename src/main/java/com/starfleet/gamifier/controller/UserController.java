package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.controller.dto.UserRequests.ImportResult;
import com.starfleet.gamifier.controller.dto.UserRequests.MissionProgressResponse;
import com.starfleet.gamifier.controller.dto.UserRequests.UpdateUserRequest;
import com.starfleet.gamifier.controller.dto.UserRequests.UserDashboardResponse;
import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.service.AuthenticationService;
import com.starfleet.gamifier.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for User management (Gamification Service)
 * Handles user profiles, dashboards, and mission progress.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<UserDashboardResponse> getCurrentUserDashboard() {
        String currentUserId = authenticationService.getCurrentUserId();
        UserDashboardResponse dashboard = userService.getUserDashboard(currentUserId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardResponse> getUserDashboard(@RequestParam String userId) {
        authenticationService.requireAdminAccess(authenticationService.getCurrentOrganizationId());
        UserDashboardResponse dashboard = userService.getUserDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new SecurityException("Access denied to user: " + userId);
        }
        User user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(
                userId,
                request.getName(),
                request.getSurname(),
                request.getManagerEmployeeId()
        );
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(required = false) String organizationId,
            Pageable pageable) {
        // If no organizationId provided, use current user's organization
        String orgId = organizationId != null ? organizationId : authenticationService.getCurrentOrganizationId();
        authenticationService.requireAdminAccess(orgId);
        Page<User> users = userService.getAllUsers(orgId, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/current")
    public ResponseEntity<UserDashboardResponse> getCurrentUser() {
        String currentUserId = authenticationService.getCurrentUserId();
        UserDashboardResponse dashboard = userService.getUserDashboard(currentUserId);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping
    public ResponseEntity<ImportResult> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam String organizationId) {
        authenticationService.requireAdminAccess(organizationId);
        ImportResult result = userService.importUsersFromCsv(file, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // Leaderboard methods moved to LeaderboardController

    @GetMapping("/{userId}/missions/{missionId}")
    public ResponseEntity<MissionProgressResponse> getMissionProgress(
            @PathVariable String userId,
            @PathVariable String missionId) {
        MissionProgressResponse progress = userService.getMissionProgress(userId, missionId);
        return ResponseEntity.ok(progress);
    }
}