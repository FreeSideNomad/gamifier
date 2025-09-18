package com.starfleet.gamifier.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Event domain model and factory methods.
 */
class EventTest {

    @Test
    void shouldCreateEventWithDefaults() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(Event.EventType.ACTION_COMPLETED)
                .title("Test Event")
                .description("Test event description")
                .data(Map.of("key", "value"))
                .build();

        assertNotNull(event);
        assertEquals("org123", event.getOrganizationId());
        assertEquals("user123", event.getUserId());
        assertEquals(Event.EventType.ACTION_COMPLETED, event.getEventType());
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test event description", event.getDescription());
        assertEquals("value", event.getData().get("key"));
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateActionCompletedEvent() {
        String orgId = "org123";
        String userId = "user123";
        String actionName = "Away Mission";
        Integer points = 50;

        Event event = Event.actionCompleted(orgId, userId, actionName, points);

        assertEquals(orgId, event.getOrganizationId());
        assertEquals(userId, event.getUserId());
        assertEquals(Event.EventType.ACTION_COMPLETED, event.getEventType());
        assertEquals("Action Completed", event.getTitle());
        assertEquals("Completed action: Away Mission (+50 points)", event.getDescription());
        assertEquals(actionName, event.getData().get("actionName"));
        assertEquals(points, event.getData().get("points"));
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateMissionCompletedEvent() {
        String orgId = "org123";
        String userId = "user123";
        String missionName = "Explorer";
        String badge = "🌌";
        Integer bonusPoints = 200;

        Event event = Event.missionCompleted(orgId, userId, missionName, badge, bonusPoints);

        assertEquals(orgId, event.getOrganizationId());
        assertEquals(userId, event.getUserId());
        assertEquals(Event.EventType.MISSION_COMPLETED, event.getEventType());
        assertEquals("Mission Completed!", event.getTitle());
        assertEquals("Earned badge: 🌌 for completing Explorer (+200 bonus points)", event.getDescription());
        assertEquals(missionName, event.getData().get("missionName"));
        assertEquals(badge, event.getData().get("badge"));
        assertEquals(bonusPoints, event.getData().get("bonusPoints"));
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateRankAchievedEvent() {
        String orgId = "org123";
        String userId = "user123";
        String rankName = "Captain";
        String insignia = "🔴🔴";

        Event event = Event.rankAchieved(orgId, userId, rankName, insignia);

        assertEquals(orgId, event.getOrganizationId());
        assertEquals(userId, event.getUserId());
        assertEquals(Event.EventType.RANK_ACHIEVED, event.getEventType());
        assertEquals("Rank Promotion!", event.getTitle());
        assertEquals("Achieved rank: Captain 🔴🔴", event.getDescription());
        assertEquals(rankName, event.getData().get("rankName"));
        assertEquals(insignia, event.getData().get("insignia"));
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldTestAllEventTypes() {
        // Test all event types exist and can be used
        Event.EventType[] allTypes = Event.EventType.values();

        assertTrue(allTypes.length >= 9);

        // Verify specific event types exist
        assertNotNull(Event.EventType.ACTION_COMPLETED);
        assertNotNull(Event.EventType.ACTION_APPROVED);
        assertNotNull(Event.EventType.ACTION_REJECTED);
        assertNotNull(Event.EventType.MISSION_PROGRESS);
        assertNotNull(Event.EventType.MISSION_COMPLETED);
        assertNotNull(Event.EventType.RANK_ACHIEVED);
        assertNotNull(Event.EventType.POINTS_AWARDED);
        assertNotNull(Event.EventType.USER_REGISTERED);
        assertNotNull(Event.EventType.CONFIGURATION_CHANGED);
    }

    @Test
    void shouldCreateEventWithCustomTimestamp() {
        Instant customTime = Instant.parse("2024-01-01T12:00:00Z");

        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(Event.EventType.USER_REGISTERED)
                .title("User Registered")
                .description("New user joined Starfleet")
                .timestamp(customTime)
                .build();

        assertEquals(customTime, event.getTimestamp());
    }

    @Test
    void shouldCreateEventWithEmptyData() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(Event.EventType.CONFIGURATION_CHANGED)
                .title("Config Changed")
                .description("Admin updated configuration")
                .data(Map.of())
                .build();

        assertNotNull(event.getData());
        assertTrue(event.getData().isEmpty());
    }

    @Test
    void shouldCreateEventWithNullData() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(Event.EventType.POINTS_AWARDED)
                .title("Points Awarded")
                .description("Bonus points awarded")
                .build();

        // Data can be null
        assertNull(event.getData());
    }
}