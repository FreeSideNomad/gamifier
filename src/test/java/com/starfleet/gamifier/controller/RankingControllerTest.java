package com.starfleet.gamifier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starfleet.gamifier.service.OrganizationService;
import com.starfleet.gamifier.service.UserService;
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

@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private OrganizationService organizationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUserRank_ShouldReturnUserRankInfo() throws Exception {
        UserService.UserRankInfo userRankInfo = UserService.UserRankInfo.builder()
                .userId("user-1")
                .currentRankId("rank-2")
                .currentRankName("Lieutenant")
                .currentRankInsignia("⭐⭐")
                .currentPoints(150)
                .currentRankThreshold(100)
                .nextRankId("rank-3")
                .nextRankName("Commander")
                .nextRankInsignia("⭐⭐⭐")
                .nextRankThreshold(500)
                .pointsToNextRank(350)
                .build();

        when(userService.getCurrentRank("user-1")).thenReturn(userRankInfo);

        mockMvc.perform(get("/api/rankings/user/user-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.currentRankName").value("Lieutenant"))
                .andExpect(jsonPath("$.currentPoints").value(150))
                .andExpect(jsonPath("$.pointsToNextRank").value(350));
    }

    @Test
    void getAvailableRanks_ShouldReturnRankList() throws Exception {
        List<OrganizationService.RankInfo> ranks = List.of(
                OrganizationService.RankInfo.builder()
                        .rankId("rank-1")
                        .name("Ensign")
                        .description("Entry level")
                        .pointsThreshold(0)
                        .insignia("⭐")
                        .order(1)
                        .build(),
                OrganizationService.RankInfo.builder()
                        .rankId("rank-2")
                        .name("Lieutenant")
                        .description("Mid level")
                        .pointsThreshold(100)
                        .insignia("⭐⭐")
                        .order(2)
                        .build()
        );

        when(organizationService.getAvailableRanks("org-1")).thenReturn(ranks);

        mockMvc.perform(get("/api/rankings/organization/org-1/ranks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ensign"))
                .andExpect(jsonPath("$[1].name").value("Lieutenant"));
    }


    // Multi-user ranking tests moved to LeaderboardControllerTest
}