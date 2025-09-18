package com.starfleet.gamifier.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

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
                .eventType(EventType.ACTION_CAPTURED)
                .data("Test event data")
                .build();

        assertNotNull(event);
        assertEquals("org123", event.getOrganizationId());
        assertEquals("user123", event.getUserId());
        assertEquals(EventType.ACTION_CAPTURED, event.getEventType());
        assertEquals("Test event data", event.getData());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateActionCapturedEvent() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(EventType.ACTION_CAPTURED)
                .data("Action 'Away Mission' captured by John Doe (50 points)")
                .build();

        assertEquals("org123", event.getOrganizationId());
        assertEquals("user123", event.getUserId());
        assertEquals(EventType.ACTION_CAPTURED, event.getEventType());
        assertEquals("Action 'Away Mission' captured by John Doe (50 points)", event.getData());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateMissionCompletedEvent() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(EventType.MISSION_COMPLETED)
                .data("Mission 'Explorer' completed! Earned badge: 🌌 (+200 bonus points)")
                .build();

        assertEquals("org123", event.getOrganizationId());
        assertEquals("user123", event.getUserId());
        assertEquals(EventType.MISSION_COMPLETED, event.getEventType());
        assertEquals("Mission 'Explorer' completed! Earned badge: 🌌 (+200 bonus points)", event.getData());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateRankPromotedEvent() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(EventType.RANK_PROMOTED)
                .data("Promoted to rank: Captain 🔴🔴")
                .build();

        assertEquals("org123", event.getOrganizationId());
        assertEquals("user123", event.getUserId());
        assertEquals(EventType.RANK_PROMOTED, event.getEventType());
        assertEquals("Promoted to rank: Captain 🔴🔴", event.getData());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldTestAllEventTypes() {
        // Test all event types exist and can be used
        EventType[] allTypes = EventType.values();

        assertTrue(allTypes.length >= 7);

        // Verify specific event types exist
        assertNotNull(EventType.USER_REGISTERED);
        assertNotNull(EventType.ACTION_CAPTURED);
        assertNotNull(EventType.ACTION_APPROVED);
        assertNotNull(EventType.ACTION_REJECTED);
        assertNotNull(EventType.MISSION_COMPLETED);
        assertNotNull(EventType.RANK_PROMOTED);
        assertNotNull(EventType.POINTS_AWARDED);
    }

    @Test
    void shouldCreateEventWithCustomTimestamp() {
        Instant customTime = Instant.parse("2024-01-01T12:00:00Z");

        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(EventType.USER_REGISTERED)
                .data("New user joined Starfleet")
                .timestamp(customTime)
                .build();

        assertEquals(customTime, event.getTimestamp());
    }

    @Test
    void shouldCreateEventWithEmptyData() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(EventType.POINTS_AWARDED)
                .data("")
                .build();

        assertNotNull(event.getData());
        assertTrue(event.getData().isEmpty());
    }

    @Test
    void shouldCreateEventWithNullData() {
        Event event = Event.builder()
                .organizationId("org123")
                .userId("user123")
                .eventType(EventType.POINTS_AWARDED)
                .build();

        // Data can be null
        assertNull(event.getData());
    }
}