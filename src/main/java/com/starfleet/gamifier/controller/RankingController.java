package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.service.OrganizationService;
import com.starfleet.gamifier.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final UserService userService;
    private final OrganizationService organizationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserService.UserRankInfo> getUserRank(@PathVariable String userId) {
        log.debug("Getting rank info for user: {}", userId);
        UserService.UserRankInfo rankInfo = userService.getCurrentRank(userId);
        return ResponseEntity.ok(rankInfo);
    }

    @GetMapping("/organization/{organizationId}/ranks")
    public ResponseEntity<List<OrganizationService.RankInfo>> getAvailableRanks(@PathVariable String organizationId) {
        log.debug("Getting available ranks for organization: {}", organizationId);
        List<OrganizationService.RankInfo> ranks = organizationService.getAvailableRanks(organizationId);
        return ResponseEntity.ok(ranks);
    }


    // Multi-user ranking methods moved to LeaderboardController

}