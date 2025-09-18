package com.starfleet.gamifier.controller;

import com.starfleet.gamifier.domain.Event;
import com.starfleet.gamifier.domain.EventType;
import com.starfleet.gamifier.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * REST Controller for Event management (Gamification Service)
 * Handles user activity feeds and admin event monitoring.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<Event>> getUserEvents(
            @RequestParam String userId,
            @RequestParam String organizationId,
            @RequestParam(required = false) String since, // ISO timestamp
            Pageable pageable) {

        Instant sinceTimestamp = since != null ? Instant.parse(since) : null;
        Page<Event> events = eventService.getUserEvents(userId, organizationId, sinceTimestamp, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<Event>> getAdminEvents(
            @RequestParam String organizationId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            Pageable pageable) {

        EventType type = eventType != null ? EventType.valueOf(eventType) : null;
        Instant sinceTimestamp = since != null ? Instant.parse(since) : null;
        Instant untilTimestamp = until != null ? Instant.parse(until) : null;

        Page<Event> events = eventService.getAdminEvents(
                organizationId, type, userId, sinceTimestamp, untilTimestamp, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Event>> getUserFeedSinceLastLogin(
            @RequestParam String userId,
            @RequestParam String organizationId) {
        List<Event> feedEvents = eventService.getUserFeedSinceLastLogin(userId, organizationId);
        return ResponseEntity.ok(feedEvents);
    }

    @GetMapping("/statistics")
    public ResponseEntity<EventStatistics> getEventStatistics(
            @RequestParam String organizationId) {
        EventStatistics statistics = eventService.getEventStatistics(organizationId);
        return ResponseEntity.ok(statistics);
    }

    public static class EventStatistics {
        private final long totalEvents;
        private final long todayEvents;
        private final long weekEvents;
        private final long monthEvents;

        public EventStatistics(long totalEvents, long todayEvents, long weekEvents, long monthEvents) {
            this.totalEvents = totalEvents;
            this.todayEvents = todayEvents;
            this.weekEvents = weekEvents;
            this.monthEvents = monthEvents;
        }

        public long getTotalEvents() {
            return totalEvents;
        }

        public long getTodayEvents() {
            return todayEvents;
        }

        public long getWeekEvents() {
            return weekEvents;
        }

        public long getMonthEvents() {
            return monthEvents;
        }
    }
}