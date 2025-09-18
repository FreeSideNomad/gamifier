package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.User;
import com.starfleet.gamifier.domain.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User aggregate root.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByOrganizationIdAndEmployeeId(String organizationId, String employeeId);

    List<User> findByOrganizationId(String organizationId);

    Page<User> findByOrganizationId(String organizationId, Pageable pageable);

    List<User> findByOrganizationIdAndRole(String organizationId, UserRole role);

    List<User> findByOrganizationIdAndManagerEmployeeId(String organizationId, String managerEmployeeId);

    boolean existsByOrganizationIdAndEmployeeId(String organizationId, String employeeId);

    void deleteByOrganizationId(String organizationId);

    long countByOrganizationId(String organizationId);

    // Leaderboard queries
    List<User> findTop10ByOrganizationIdOrderByTotalPointsDesc(String organizationId);

    Page<User> findByOrganizationIdOrderByTotalPointsDesc(String organizationId, Pageable pageable);
}