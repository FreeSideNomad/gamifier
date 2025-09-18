package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Organization aggregate root.
 * Single repository handling all configuration operations within the organization.
 */
@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByName(String name);

    Optional<Organization> findByFederationId(String federationId);

    List<Organization> findByActiveTrue();

    boolean existsByName(String name);

    boolean existsByFederationId(String federationId);
}