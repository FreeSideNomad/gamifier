package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.UserRequests.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Placeholder service for User operations.
 * TODO: Implement in future stages.
 */
@Service
public class UserService {

    public UserDashboardResponse getUserDashboard(String userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public com.starfleet.gamifier.domain.User getUser(String userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public com.starfleet.gamifier.domain.User updateUser(String userId, String name, String surname, String managerEmployeeId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<com.starfleet.gamifier.domain.User> getAllUsers(String organizationId, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ImportResult importUsersFromCsv(MultipartFile file, String organizationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<LeaderboardEntry> getMonthlyLeaderboard(String organizationId, int limit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<LeaderboardEntry> getAllTimeLeaderboard(String organizationId, int limit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public MissionProgressResponse getMissionProgress(String userId, String missionId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}