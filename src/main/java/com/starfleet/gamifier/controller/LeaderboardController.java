package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.service.LeaderboardService;
import com.starfleet.gamifier.controller.dto.LeaderboardResponses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

/**
 * REST Controller for Leaderboard functionality (Gamification Service)
 * Handles monthly and all-time leaderboards with various filtering options.
 */
@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/monthly")
    public ResponseEntity<Page<LeaderboardEntry>> getMonthlyLeaderboard(
            @RequestParam String organizationId,
            @RequestParam(required = false) String yearMonth, // YYYY-MM format
            Pageable pageable) {

        YearMonth month = yearMonth != null ? YearMonth.parse(yearMonth) : YearMonth.now();
        Page<LeaderboardEntry> leaderboard = leaderboardService.getMonthlyLeaderboard(organizationId, month, pageable);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/all-time")
    public ResponseEntity<Page<LeaderboardEntry>> getAllTimeLeaderboard(
            @RequestParam String organizationId,
            Pageable pageable) {
        Page<LeaderboardEntry> leaderboard = leaderboardService.getAllTimeLeaderboard(organizationId, pageable);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/department")
    public ResponseEntity<Page<LeaderboardEntry>> getDepartmentLeaderboard(
            @RequestParam String organizationId,
            @RequestParam String department,
            @RequestParam(defaultValue = "all-time") String period, // "monthly" or "all-time"
            @RequestParam(required = false) String yearMonth,
            Pageable pageable) {

        YearMonth month = yearMonth != null ? YearMonth.parse(yearMonth) : YearMonth.now();
        Page<LeaderboardEntry> leaderboard;

        if ("monthly".equals(period)) {
            leaderboard = leaderboardService.getDepartmentMonthlyLeaderboard(organizationId, department, month, pageable);
        } else {
            leaderboard = leaderboardService.getDepartmentAllTimeLeaderboard(organizationId, department, pageable);
        }

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/user-position")
    public ResponseEntity<UserLeaderboardPosition> getUserPosition(
            @RequestParam String organizationId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "all-time") String period,
            @RequestParam(required = false) String yearMonth) {

        YearMonth month = yearMonth != null ? YearMonth.parse(yearMonth) : YearMonth.now();
        UserLeaderboardPosition position;

        if ("monthly".equals(period)) {
            position = leaderboardService.getUserMonthlyPosition(organizationId, userId, month);
        } else {
            position = leaderboardService.getUserAllTimePosition(organizationId, userId);
        }

        return ResponseEntity.ok(position);
    }

    @GetMapping("/statistics")
    public ResponseEntity<LeaderboardStatistics> getLeaderboardStatistics(
            @RequestParam String organizationId) {
        LeaderboardStatistics statistics = leaderboardService.getLeaderboardStatistics(organizationId);
        return ResponseEntity.ok(statistics);
    }
}