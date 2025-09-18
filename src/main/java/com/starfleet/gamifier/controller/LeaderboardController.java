package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.controller.dto.LeaderboardResponses.LeaderboardEntry;
import com.starfleet.gamifier.controller.dto.LeaderboardResponses.LeaderboardStatistics;
import com.starfleet.gamifier.controller.dto.LeaderboardResponses.UserLeaderboardPosition;
import com.starfleet.gamifier.service.LeaderboardService;
import com.starfleet.gamifier.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

/**
 * REST Controller for Leaderboard functionality (Gamification Service)
 * Handles monthly and all-time leaderboards with various filtering options.
 */
@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final OrganizationService organizationService;

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

    // Ranking-based leaderboard methods (moved from RankingController)

    @GetMapping("/rankings")
    public ResponseEntity<List<OrganizationService.UserRankSummary>> getRankingsLeaderboard(
            @RequestParam String organizationId,
            @RequestParam(defaultValue = "50") int limit) {

        if (limit > 100) {
            limit = 100; // Cap the limit to prevent performance issues
        }

        List<OrganizationService.UserRankSummary> leaderboard = organizationService.getOrganizationRankings(organizationId, limit);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/rank-statistics")
    public ResponseEntity<OrganizationService.RankStatistics> getRankStatistics(
            @RequestParam String organizationId) {
        OrganizationService.RankStatistics statistics = organizationService.getRankStatistics(organizationId);
        return ResponseEntity.ok(statistics);
    }
}