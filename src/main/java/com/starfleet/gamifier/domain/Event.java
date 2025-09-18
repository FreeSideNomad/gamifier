package com.starfleet.gamifier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

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
@CompoundIndex(def = "{'organizationId': 1, 'eventType': 1, 'timestamp': -1}")
@CompoundIndex(def = "{'organizationId': 1, 'timestamp': -1}")
public class Event {

    @Id
    private String id;

    private String organizationId;
    private String userId;
    private EventType eventType;
    private String data; // String representation of event data

    @Builder.Default
    private Instant timestamp = Instant.now();

}