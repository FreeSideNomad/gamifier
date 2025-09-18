package com.starfleet.gamifier.domain;

/**
 * Status of action capture in the approval workflow.
 */
public enum CaptureStatus {
    PENDING_APPROVAL,   // Waiting for manager approval
    APPROVED,           // Approved and points awarded
    REJECTED            // Rejected, no points awarded
}