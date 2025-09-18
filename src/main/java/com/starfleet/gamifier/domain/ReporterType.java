package com.starfleet.gamifier.domain;

/**
 * Enumeration of who can report actions in the Starfleet gamification system.
 */
public enum ReporterType {
    /**
     * User reports their own actions
     */
    SELF,

    /**
     * Peer (colleague) reports the action
     */
    PEER,

    /**
     * Manager reports the action for their team member
     */
    MANAGER
}