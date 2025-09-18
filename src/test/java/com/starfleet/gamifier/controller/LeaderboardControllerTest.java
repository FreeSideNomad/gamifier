package com.starfleet.gamifier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starfleet.gamifier.service.LeaderboardService;
import com.starfleet.gamifier.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    @MockBean
    private OrganizationService organizationService;

    @Autowired
    private ObjectMapper objectMapper;

    // Tests for ranking-based leaderboard methods moved from RankingController

    @Test
    void getRankingsLeaderboard_ShouldReturnLeaderboard() throws Exception {
        List<OrganizationService.UserRankSummary> leaderboard = List.of(
                OrganizationService.UserRankSummary.builder()
                        .userId("user-1")
                        .employeeId("EMP-001")
                        .name("John Doe")
                        .totalPoints(300)
                        .currentRankId("rank-2")
                        .currentRankName("Lieutenant")
                        .currentRankInsignia("⭐⭐")
                        .build(),
                OrganizationService.UserRankSummary.builder()
                        .userId("user-2")
                        .employeeId("EMP-002")
                        .name("Jane Smith")
                        .totalPoints(200)
                        .currentRankId("rank-1")
                        .currentRankName("Ensign")
                        .currentRankInsignia("⭐")
                        .build()
        );

        when(organizationService.getOrganizationRankings("org-1", 50)).thenReturn(leaderboard);

        mockMvc.perform(get("/api/leaderboards/rankings?organizationId=org-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].totalPoints").value(300))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[1].totalPoints").value(200));
    }

    @Test
    void getRankingsLeaderboard_WithCustomLimit_ShouldUseLimit() throws Exception {
        when(organizationService.getOrganizationRankings("org-1", 10)).thenReturn(List.of());

        mockMvc.perform(get("/api/leaderboards/rankings?organizationId=org-1&limit=10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRankingsLeaderboard_WithExcessiveLimit_ShouldCapAt100() throws Exception {
        when(organizationService.getOrganizationRankings("org-1", 100)).thenReturn(List.of());

        mockMvc.perform(get("/api/leaderboards/rankings?organizationId=org-1&limit=200"))
                .andExpect(status().isOk());
    }

    @Test
    void getRankStatistics_ShouldReturnStatistics() throws Exception {
        OrganizationService.RankStatistics statistics = OrganizationService.RankStatistics.builder()
                .organizationId("org-1")
                .totalUsers(100)
                .averagePoints(250)
                .rankDistribution(List.of(
                        OrganizationService.RankDistribution.builder()
                                .rankId("rank-1")
                                .rankName("Ensign")
                                .insignia("⭐")
                                .pointsThreshold(0)
                                .userCount(50)
                                .build(),
                        OrganizationService.RankDistribution.builder()
                                .rankId("rank-2")
                                .rankName("Lieutenant")
                                .insignia("⭐⭐")
                                .pointsThreshold(100)
                                .userCount(30)
                                .build()
                ))
                .build();

        when(organizationService.getRankStatistics("org-1")).thenReturn(statistics);

        mockMvc.perform(get("/api/leaderboards/rank-statistics?organizationId=org-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.organizationId").value("org-1"))
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.averagePoints").value(250))
                .andExpect(jsonPath("$.rankDistribution.length()").value(2))
                .andExpect(jsonPath("$.rankDistribution[0].rankName").value("Ensign"))
                .andExpect(jsonPath("$.rankDistribution[0].userCount").value(50));
    }
}