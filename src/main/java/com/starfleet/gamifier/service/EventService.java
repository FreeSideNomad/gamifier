package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.EventController.EventStatistics;
import com.starfleet.gamifier.domain.Event;
import com.starfleet.gamifier.domain.EventType;
import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for Event feed and monitoring operations.
 * Handles user activity feeds and admin event monitoring.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    /**
     * Get paginated events for a specific user.
     */
    public Page<Event> getUserEvents(String userId, String organizationId, Instant sinceTimestamp, Pageable pageable) {
        log.debug("Getting events for user {} in organization {} since {}", userId, organizationId, sinceTimestamp);

        if (sinceTimestamp != null) {
            List<Event> events = eventRepository.findByOrganizationIdAndUserIdAndTimestampAfter(
                organizationId, userId, sinceTimestamp);
            return new PageImpl<>(events, pageable, events.size());
        } else {
            return eventRepository.findByOrganizationIdAndUserId(organizationId, userId, pageable);
        }
    }

    /**
     * Get admin events with comprehensive filtering options.
     */
    public Page<Event> getAdminEvents(String organizationId, EventType type, String userId,
                                      Instant sinceTimestamp, Instant untilTimestamp, Pageable pageable) {
        log.debug("Getting admin events for organization {} with filters: type={}, userId={}, since={}, until={}",
                 organizationId, type, userId, sinceTimestamp, untilTimestamp);

        Query query = new Query();
        query.addCriteria(Criteria.where("organizationId").is(organizationId));

        if (type != null) {
            query.addCriteria(Criteria.where("eventType").is(type));
        }

        if (userId != null) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }

        if (sinceTimestamp != null || untilTimestamp != null) {
            Criteria timeCriteria = Criteria.where("timestamp");
            if (sinceTimestamp != null) {
                timeCriteria = timeCriteria.gte(sinceTimestamp);
            }
            if (untilTimestamp != null) {
                timeCriteria = timeCriteria.lte(untilTimestamp);
            }
            query.addCriteria(timeCriteria);
        }

        query.with(pageable);

        List<Event> events = mongoTemplate.find(query, Event.class);
        long totalCount = mongoTemplate.count(query.skip(0).limit(0), Event.class);

        return new PageImpl<>(events, pageable, totalCount);
    }

    /**
     * Get user feed events since their last login.
     */
    public List<Event> getUserFeedSinceLastLogin(String userId, String organizationId) {
        log.debug("Getting feed events for user {} since last login", userId);

        User user = userService.getUser(userId);
        Instant lastLogin = user.getLastLogin();

        if (lastLogin == null) {
            // If no last login, return events from last 7 days
            lastLogin = Instant.now().minus(7, ChronoUnit.DAYS);
        }

        return eventRepository.findByOrganizationIdAndUserIdAndTimestampAfter(
            organizationId, userId, lastLogin);
    }

    /**
     * Get comprehensive event statistics for an organization.
     */
    public EventStatistics getEventStatistics(String organizationId) {
        log.debug("Calculating event statistics for organization {}", organizationId);

        Instant now = Instant.now();
        Instant startOfToday = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant startOfWeek = now.minus(7, ChronoUnit.DAYS);
        Instant startOfMonth = now.minus(30, ChronoUnit.DAYS);

        // Count total events
        long totalEvents = countEventsByOrganizationAndTimeRange(organizationId, null, null);

        // Count today's events
        long todayEvents = countEventsByOrganizationAndTimeRange(organizationId, startOfToday, null);

        // Count week events
        long weekEvents = countEventsByOrganizationAndTimeRange(organizationId, startOfWeek, null);

        // Count month events
        long monthEvents = countEventsByOrganizationAndTimeRange(organizationId, startOfMonth, null);

        return new EventStatistics(totalEvents, todayEvents, weekEvents, monthEvents);
    }

    /**
     * Helper method to count events by time range.
     */
    private long countEventsByOrganizationAndTimeRange(String organizationId, Instant since, Instant until) {
        Query query = new Query();
        query.addCriteria(Criteria.where("organizationId").is(organizationId));

        if (since != null || until != null) {
            Criteria timeCriteria = Criteria.where("timestamp");
            if (since != null) {
                timeCriteria = timeCriteria.gte(since);
            }
            if (until != null) {
                timeCriteria = timeCriteria.lte(until);
            }
            query.addCriteria(timeCriteria);
        }

        return mongoTemplate.count(query, Event.class);
    }

    /**
     * Get events by type for analytics.
     */
    public List<Event> getEventsByType(String organizationId, EventType eventType) {
        return eventRepository.findByOrganizationIdAndEventType(organizationId, eventType);
    }

    /**
     * Get events within time range for reporting.
     */
    public List<Event> getEventsByTimeRange(String organizationId, Instant start, Instant end) {
        return eventRepository.findByOrganizationIdAndTimestampBetween(organizationId, start, end);
    }
}