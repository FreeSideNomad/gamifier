package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.ActionCapture;
import com.starfleet.gamifier.domain.CaptureMethod;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ActionCapture aggregate root.
 */
@Repository
public interface ActionCaptureRepository extends MongoRepository<ActionCapture, String> {

    Optional<ActionCapture> findByOrganizationIdAndUserIdAndActionTypeIdAndDate(
            String organizationId, String userId, String actionTypeId, LocalDate date);

    List<ActionCapture> findByOrganizationIdAndUserId(String organizationId, String userId);

    Page<ActionCapture> findByOrganizationIdAndUserId(String organizationId, String userId, Pageable pageable);

    List<ActionCapture> findByOrganizationIdAndActionTypeId(String organizationId, String actionTypeId);

    List<ActionCapture> findByOrganizationIdAndStatus(String organizationId, ActionCapture.ActionStatus status);

    List<ActionCapture> findByOrganizationIdAndCaptureMethod(String organizationId, CaptureMethod captureMethod);

    List<ActionCapture> findByOrganizationIdAndDateBetween(String organizationId, LocalDate startDate, LocalDate endDate);

    // For manager approvals
    List<ActionCapture> findByOrganizationIdAndStatusAndActionTypeIdIn(
            String organizationId, ActionCapture.ActionStatus status, List<String> actionTypeIds);

    boolean existsByOrganizationIdAndUserIdAndActionTypeIdAndDate(
            String organizationId, String userId, String actionTypeId, LocalDate date);

    void deleteByOrganizationId(String organizationId);

    long countByOrganizationIdAndUserId(String organizationId, String userId);

    long countByOrganizationIdAndStatus(String organizationId, ActionCapture.ActionStatus status);
}