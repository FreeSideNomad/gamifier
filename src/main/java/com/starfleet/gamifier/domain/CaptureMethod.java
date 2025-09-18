package com.starfleet.gamifier.domain;

/**
 * Enumeration of methods by which actions can be captured in the Starfleet system.
 */
public enum CaptureMethod {
    /**
     * Actions captured through user interface (manual reporting)
     */
    UI,

    /**
     * Actions captured through automated import (CSV, API)
     */
    IMPORT
}