package com.starfleet.gamifier.config;

import com.starfleet.gamifier.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultDataInitializer.
 */
@ExtendWith(MockitoExtension.class)
class DefaultDataInitializerTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private DefaultDataInitializer defaultDataInitializer;

    @BeforeEach
    void setUp() {
        // Reset mock interactions before each test
        reset(organizationRepository);
    }

    @Test
    void shouldInitializeDefaultDataWhenOrganizationDoesNotExist() throws Exception {
        when(organizationRepository.existsByFederationId("UFP-001")).thenReturn(false);

        defaultDataInitializer.run(applicationArguments);

        verify(organizationRepository).existsByFederationId("UFP-001");
        verify(organizationRepository).save(any());
    }

    @Test
    void shouldSkipInitializationWhenOrganizationExists() throws Exception {
        when(organizationRepository.existsByFederationId("UFP-001")).thenReturn(true);

        defaultDataInitializer.run(applicationArguments);

        verify(organizationRepository).existsByFederationId("UFP-001");
        verify(organizationRepository, never()).save(any());
    }
}