package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.EventController.EventStatistics;
import com.starfleet.gamifier.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Placeholder service for Event operations.
 * TODO: Implement in future stages.
 */
@Service
public class EventService {

    public Page<Event> getUserEvents(String userId, String organizationId, Instant sinceTimestamp, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<Event> getAdminEvents(String organizationId, Event.EventType type, String userId,
                                    Instant sinceTimestamp, Instant untilTimestamp, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<Event> getUserFeedSinceLastLogin(String userId, String organizationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public EventStatistics getEventStatistics(String organizationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}