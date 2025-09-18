package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for Event aggregate root.
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByOrganizationIdAndUserId(String organizationId, String userId);

    Page<Event> findByOrganizationIdAndUserId(String organizationId, String userId, Pageable pageable);

    List<Event> findByOrganizationIdAndUserIdAndTimestampAfter(String organizationId, String userId, Instant timestamp);

    List<Event> findByOrganizationId(String organizationId);

    Page<Event> findByOrganizationId(String organizationId, Pageable pageable);

    List<Event> findByOrganizationIdAndEventType(String organizationId, Event.EventType eventType);

    List<Event> findByOrganizationIdAndTimestampBetween(String organizationId, Instant start, Instant end);

    void deleteByOrganizationId(String organizationId);

    long countByOrganizationIdAndUserId(String organizationId, String userId);

    long countByOrganizationIdAndEventType(String organizationId, Event.EventType eventType);
}