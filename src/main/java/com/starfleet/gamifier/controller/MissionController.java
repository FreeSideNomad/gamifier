package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.controller.dto.UserRequests.MissionProgressSummary;
import com.starfleet.gamifier.service.AuthenticationService;
import com.starfleet.gamifier.service.UserService;
import com.starfleet.gamifier.service.UserService.BadgeInfo;
import com.starfleet.gamifier.service.UserService.MissionProgressDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for Mission Progress operations.
 * Handles mission tracking, badge display, and progress queries.
 */
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping("/progress")
    public ResponseEntity<List<MissionProgressSummary>> getCurrentUserMissionProgress() {
        String currentUserId = authenticationService.getCurrentUserId();
        List<MissionProgressSummary> progress = userService.getAllMissionProgress(currentUserId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/progress/{userId}")
    public ResponseEntity<List<MissionProgressSummary>> getUserMissionProgress(@PathVariable String userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new SecurityException("Access denied to user: " + userId);
        }
        List<MissionProgressSummary> progress = userService.getAllMissionProgress(userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{missionId}/progress")
    public ResponseEntity<MissionProgressDetails> getCurrentUserMissionDetails(@PathVariable String missionId) {
        String currentUserId = authenticationService.getCurrentUserId();
        MissionProgressDetails details = userService.getMissionProgressDetails(currentUserId, missionId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/{missionId}/progress/{userId}")
    public ResponseEntity<MissionProgressDetails> getUserMissionDetails(
            @PathVariable String missionId,
            @PathVariable String userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new SecurityException("Access denied to user: " + userId);
        }
        MissionProgressDetails details = userService.getMissionProgressDetails(userId, missionId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeInfo>> getCurrentUserBadges() {
        String currentUserId = authenticationService.getCurrentUserId();
        List<BadgeInfo> badges = userService.getEarnedBadges(currentUserId);
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/badges/{userId}")
    public ResponseEntity<List<BadgeInfo>> getUserBadges(@PathVariable String userId) {
        if (!authenticationService.canAccessUser(userId)) {
            throw new SecurityException("Access denied to user: " + userId);
        }
        List<BadgeInfo> badges = userService.getEarnedBadges(userId);
        return ResponseEntity.ok(badges);
    }
}