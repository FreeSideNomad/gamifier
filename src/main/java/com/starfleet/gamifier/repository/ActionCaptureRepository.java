package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.Action;
import com.starfleet.gamifier.domain.CaptureMethod;
import com.starfleet.gamifier.domain.CaptureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for ActionCapture aggregate root.
 */
@Repository
public interface ActionCaptureRepository extends MongoRepository<Action, String> {

    // Core finder methods with updated field names
    Page<Action> findByUserIdOrderByActionDateDesc(String userId, Pageable pageable);

    Page<Action> findByStatusAndOrganizationId(CaptureStatus status, String organizationId, Pageable pageable);

    Page<Action> findByStatusAndUserIdIn(CaptureStatus status, List<String> userIds, Pageable pageable);

    boolean existsByUserIdAndActionTypeIdAndActionDate(String userId, String actionTypeId, LocalDate actionDate);

    List<Action> findByOrganizationIdAndUserId(String organizationId, String userId);

    List<Action> findByOrganizationIdAndActionTypeId(String organizationId, String actionTypeId);

    List<Action> findByOrganizationIdAndStatus(String organizationId, CaptureStatus status);

    List<Action> findByOrganizationIdAndCaptureMethod(String organizationId, CaptureMethod captureMethod);

    List<Action> findByOrganizationIdAndActionDateBetween(String organizationId, LocalDate startDate, LocalDate endDate);

    void deleteByOrganizationId(String organizationId);

    long countByOrganizationIdAndUserId(String organizationId, String userId);

    long countByOrganizationIdAndStatus(String organizationId, CaptureStatus status);
}