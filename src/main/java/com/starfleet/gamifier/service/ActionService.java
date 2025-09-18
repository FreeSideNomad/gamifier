package com.starfleet.gamifier.service;

import com.starfleet.gamifier.controller.dto.ActionRequests.*;
import com.starfleet.gamifier.domain.ActionCapture;
import com.starfleet.gamifier.domain.ReporterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Placeholder service for Action operations.
 * TODO: Implement in future stages.
 */
@Service
public class ActionService {

    public ActionCapture captureAction(String organizationId, String userId, String actionTypeId,
                                     LocalDate date, ReporterType reporterType, String reporterId, String evidence) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Page<ActionCapture> getActionHistory(String userId, String organizationId, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ImportResult importActionsFromCsv(MultipartFile file, String organizationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<ActionCapture> getPendingApprovals(String organizationId, String managerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ActionCapture approveAction(String actionId, String approverId, String approvalNotes) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ActionCapture rejectAction(String actionId, String rejectionReason) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ActionStatistics getActionStatistics(String organizationId, String userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}