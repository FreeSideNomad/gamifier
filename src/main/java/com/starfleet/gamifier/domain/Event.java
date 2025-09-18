package com.starfleet.gamifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.Instant;
import java.util.Map;

/**
 * Event aggregate root for tracking system activities and user feed.
 * Represents important activities in the gamification system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "events")
@CompoundIndex(def = "{'organizationId': 1, 'userId': 1, 'timestamp': -1}")
public class Event {

    @Id
    private String id;

    private String organizationId;
    private String userId;
    private EventType eventType;
    private String title;
    private String description;
    private Map<String, Object> data; // Additional event-specific data

    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Types of events in the system
     */
    public enum EventType {
        ACTION_COMPLETED,        // User completed an action
        ACTION_APPROVED,         // Action was approved by manager
        ACTION_REJECTED,         // Action was rejected
        MISSION_PROGRESS,        // Progress made on a mission
        MISSION_COMPLETED,       // Mission completed and badge earned
        RANK_ACHIEVED,          // New rank achieved
        POINTS_AWARDED,         // Points awarded to user
        USER_REGISTERED,        // New user joined the system
        CONFIGURATION_CHANGED   // Admin made configuration changes
    }

    // Factory methods for common events
    public static Event actionCompleted(String organizationId, String userId, String actionName, Integer points) {
        return Event.builder()
                .organizationId(organizationId)
                .userId(userId)
                .eventType(EventType.ACTION_COMPLETED)
                .title("Action Completed")
                .description("Completed action: " + actionName + " (+" + points + " points)")
                .data(Map.of("actionName", actionName, "points", points))
                .build();
    }

    public static Event missionCompleted(String organizationId, String userId, String missionName, String badge, Integer bonusPoints) {
        return Event.builder()
                .organizationId(organizationId)
                .userId(userId)
                .eventType(EventType.MISSION_COMPLETED)
                .title("Mission Completed!")
                .description("Earned badge: " + badge + " for completing " + missionName + " (+" + bonusPoints + " bonus points)")
                .data(Map.of("missionName", missionName, "badge", badge, "bonusPoints", bonusPoints))
                .build();
    }

    public static Event rankAchieved(String organizationId, String userId, String rankName, String insignia) {
        return Event.builder()
                .organizationId(organizationId)
                .userId(userId)
                .eventType(EventType.RANK_ACHIEVED)
                .title("Rank Promotion!")
                .description("Achieved rank: " + rankName + " " + insignia)
                .data(Map.of("rankName", rankName, "insignia", insignia))
                .build();
    }
}