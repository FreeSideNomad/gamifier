package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.service.UserService;
import com.starfleet.gamifier.controller.dto.UserRequests.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for User management (Gamification Service)
 * Handles user profiles, dashboards, and mission progress.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDashboardResponse> getCurrentUserDashboard() {
        // TODO: Get current user from security context
        String currentUserId = "current-user-id"; // Placeholder
        UserDashboardResponse dashboard = userService.getUserDashboard(currentUserId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardResponse> getUserDashboard(@RequestParam String userId) {
        UserDashboardResponse dashboard = userService.getUserDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
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
            @RequestParam String organizationId,
            Pageable pageable) {
        Page<User> users = userService.getAllUsers(organizationId, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<ImportResult> importUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam String organizationId) {
        ImportResult result = userService.importUsersFromCsv(file, organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/leaderboard/monthly")
    public ResponseEntity<List<LeaderboardEntry>> getMonthlyLeaderboard(
            @RequestParam String organizationId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntry> leaderboard = userService.getMonthlyLeaderboard(organizationId, limit);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/all-time")
    public ResponseEntity<List<LeaderboardEntry>> getAllTimeLeaderboard(
            @RequestParam String organizationId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntry> leaderboard = userService.getAllTimeLeaderboard(organizationId, limit);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/{userId}/missions/{missionId}")
    public ResponseEntity<MissionProgressResponse> getMissionProgress(
            @PathVariable String userId,
            @PathVariable String missionId) {
        MissionProgressResponse progress = userService.getMissionProgress(userId, missionId);
        return ResponseEntity.ok(progress);
    }
}