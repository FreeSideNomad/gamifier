package com.starfleet.gamifier.domain;

/**
 * Types of events that can occur in the gamification system.
 */
public enum EventType {
    USER_REGISTERED,
    ACTION_CAPTURED,
    ACTION_APPROVED,
    ACTION_REJECTED,
    MISSION_COMPLETED,
    RANK_PROMOTED,
    POINTS_AWARDED
}