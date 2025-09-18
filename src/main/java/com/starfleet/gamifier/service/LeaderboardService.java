package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.LeaderboardResponses.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

/**
 * Placeholder service for Leaderboard operations.
 * TODO: Implement in future stages.
 */
@Service
public class LeaderboardService {

    public Page<LeaderboardEntry> getMonthlyLeaderboard(String organizationId, YearMonth month, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<LeaderboardEntry> getAllTimeLeaderboard(String organizationId, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<LeaderboardEntry> getDepartmentMonthlyLeaderboard(String organizationId, String department,
                                                                YearMonth month, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<LeaderboardEntry> getDepartmentAllTimeLeaderboard(String organizationId, String department,
                                                                Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UserLeaderboardPosition getUserMonthlyPosition(String organizationId, String userId, YearMonth month) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UserLeaderboardPosition getUserAllTimePosition(String organizationId, String userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public LeaderboardStatistics getLeaderboardStatistics(String organizationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}