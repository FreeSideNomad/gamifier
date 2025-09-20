package com.starfleet.gamifier.config;

import com.starfleet.gamifier.domain.*;
import com.starfleet.gamifier.repository.OrganizationRepository;
import com.starfleet.gamifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Initializes default Star Trek themed data for the Starfleet Gamifier system.
 * Creates the default Federation organization with ranks, action types, and missions.
 */
@Component
@ConditionalOnProperty(name = "starfleet.gamifier.data-initialization.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DefaultDataInitializer implements ApplicationRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        // Check if default organization already exists
        if (organizationRepository.existsByFederationId("UFP-001")) {
            log.info("Default Starfleet organization already exists, skipping initialization");
            return;
        }

        log.info("Initializing default Star Trek themed data...");

        Organization organization = createDefaultOrganization();
        organizationRepository.save(organization);

        // Create default development users
        createDefaultUsers(organization.getId());

        log.info("Default Starfleet organization '{}' created with ID: {}",
                organization.getName(), organization.getId());
    }

    private Organization createDefaultOrganization() {
        Organization organization = Organization.builder()
                .name("United Federation of Planets")
                .federationId("UFP-001")
                .description("Default Starfleet organization for gamification - boldly going where no one has gone before!")
                .actionTypes(createDefaultActionTypes())
                .missionTypes(createDefaultMissionTypes())
                .rankConfigurations(createDefaultRanks())
                .build();

        return organization;
    }

    private List<Organization.ActionType> createDefaultActionTypes() {
        List<Organization.ActionType> actionTypes = new ArrayList<>();

        // Exploration Category
        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Away Mission Participation")
                .description("Participated in away mission or field work assignment")
                .points(50)
                .category("Exploration")
                .captureMethods(Set.of(CaptureMethod.UI, CaptureMethod.IMPORT))
                .allowedReporters(Set.of(ReporterType.SELF, ReporterType.MANAGER))
                .requiresManagerApproval(true)
                .build());

        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("First Contact Protocol")
                .description("Successfully executed first contact procedures with new species")
                .points(100)
                .category("Exploration")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.MANAGER))
                .requiresManagerApproval(false)
                .build());

        // Professional Development Category
        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Starfleet Academy Course")
                .description("Completed mandatory or advanced Starfleet Academy training module")
                .points(25)
                .category("Professional Development")
                .captureMethods(Set.of(CaptureMethod.IMPORT))
                .allowedReporters(Set.of(ReporterType.SELF))
                .requiresManagerApproval(false)
                .build());

        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Cross-Training Certification")
                .description("Earned certification in secondary department specialty")
                .points(75)
                .category("Professional Development")
                .captureMethods(Set.of(CaptureMethod.UI, CaptureMethod.IMPORT))
                .allowedReporters(Set.of(ReporterType.SELF, ReporterType.MANAGER))
                .requiresManagerApproval(true)
                .build());

        // Innovation Category
        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Technological Innovation")
                .description("Proposed or implemented technological improvement or invention")
                .points(100)
                .category("Innovation")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.SELF, ReporterType.PEER, ReporterType.MANAGER))
                .requiresManagerApproval(true)
                .build());

        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Scientific Discovery")
                .description("Made significant scientific discovery or research breakthrough")
                .points(150)
                .category("Innovation")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.PEER, ReporterType.MANAGER))
                .requiresManagerApproval(true)
                .build());

        // Collaboration Category
        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Inter-Department Collaboration")
                .description("Demonstrated exceptional teamwork across department boundaries")
                .points(40)
                .category("Collaboration")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.PEER, ReporterType.MANAGER))
                .requiresManagerApproval(false)
                .build());

        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Diplomatic Success")
                .description("Successfully resolved conflict through diplomatic means")
                .points(80)
                .category("Collaboration")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.MANAGER))
                .requiresManagerApproval(false)
                .build());

        // Engineering Category
        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Critical System Repair")
                .description("Solved critical operational or technical problem under pressure")
                .points(75)
                .category("Engineering")
                .captureMethods(Set.of(CaptureMethod.UI, CaptureMethod.IMPORT))
                .allowedReporters(Set.of(ReporterType.SELF, ReporterType.PEER, ReporterType.MANAGER))
                .requiresManagerApproval(true)
                .build());

        actionTypes.add(Organization.ActionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Emergency Response")
                .description("Responded effectively to ship-wide or station emergency")
                .points(60)
                .category("Engineering")
                .captureMethods(Set.of(CaptureMethod.UI))
                .allowedReporters(Set.of(ReporterType.MANAGER))
                .requiresManagerApproval(false)
                .build());

        return actionTypes;
    }

    private List<Organization.MissionType> createDefaultMissionTypes() {
        List<Organization.MissionType> missionTypes = new ArrayList<>();

        // Get action type IDs (in real implementation, these would be retrieved from the saved action types)
        List<Organization.ActionType> actionTypes = createDefaultActionTypes();

        missionTypes.add(Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Academy Graduate")
                .description("Complete all basic Starfleet Academy requirements and demonstrate core competencies")
                .badge("🎓")
                .bonusPoints(100)
                .category("Training")
                .requiredActionTypeIds(List.of(
                        getActionTypeIdByName(actionTypes, "Starfleet Academy Course"),
                        getActionTypeIdByName(actionTypes, "Inter-Department Collaboration")
                ))
                .build());

        missionTypes.add(Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Explorer")
                .description("Boldly go where no one has gone before - demonstrate exploration excellence")
                .badge("🌌")
                .bonusPoints(200)
                .category("Exploration")
                .requiredActionTypeIds(List.of(
                        getActionTypeIdByName(actionTypes, "Away Mission Participation"),
                        getActionTypeIdByName(actionTypes, "First Contact Protocol"),
                        getActionTypeIdByName(actionTypes, "Critical System Repair")
                ))
                .build());

        missionTypes.add(Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Innovator")
                .description("Drive the future of Starfleet through innovation and scientific excellence")
                .badge("💡")
                .bonusPoints(250)
                .category("Innovation")
                .requiredActionTypeIds(List.of(
                        getActionTypeIdByName(actionTypes, "Technological Innovation"),
                        getActionTypeIdByName(actionTypes, "Scientific Discovery"),
                        getActionTypeIdByName(actionTypes, "Cross-Training Certification")
                ))
                .build());

        missionTypes.add(Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Diplomat")
                .description("Master the art of peaceful resolution and inter-species cooperation")
                .badge("🕊️")
                .bonusPoints(175)
                .category("Diplomacy")
                .requiredActionTypeIds(List.of(
                        getActionTypeIdByName(actionTypes, "Diplomatic Success"),
                        getActionTypeIdByName(actionTypes, "First Contact Protocol"),
                        getActionTypeIdByName(actionTypes, "Inter-Department Collaboration")
                ))
                .build());

        missionTypes.add(Organization.MissionType.builder()
                .id(UUID.randomUUID().toString())
                .name("Chief Engineer")
                .description("Demonstrate engineering excellence and problem-solving mastery")
                .badge("⚙️")
                .bonusPoints(200)
                .category("Engineering")
                .requiredActionTypeIds(List.of(
                        getActionTypeIdByName(actionTypes, "Critical System Repair"),
                        getActionTypeIdByName(actionTypes, "Emergency Response"),
                        getActionTypeIdByName(actionTypes, "Technological Innovation")
                ))
                .build());

        return missionTypes;
    }

    private List<Organization.RankConfiguration> createDefaultRanks() {
        List<Organization.RankConfiguration> ranks = new ArrayList<>();

        // Starfleet Science Division ranks with LCARS-inspired insignia
        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Cadet")
                .description("Entry level Starfleet Academy student")
                .pointsThreshold(0)
                .insignia("🔸")
                .order(1)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Ensign")
                .description("Junior commissioned officer - fresh from the Academy")
                .pointsThreshold(100)
                .insignia("⭐")
                .order(2)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Lieutenant Junior Grade")
                .description("Junior Lieutenant with proven competency")
                .pointsThreshold(300)
                .insignia("⭐⭐")
                .order(3)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Lieutenant")
                .description("Full Lieutenant with departmental responsibilities")
                .pointsThreshold(600)
                .insignia("⭐⭐⭐")
                .order(4)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Lieutenant Commander")
                .description("Senior Lieutenant with command experience")
                .pointsThreshold(1200)
                .insignia("🔵⭐")
                .order(5)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Commander")
                .description("Senior officer with significant command responsibility")
                .pointsThreshold(2500)
                .insignia("🔵🔵")
                .order(6)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Captain")
                .description("Ship commander and senior Starfleet officer")
                .pointsThreshold(5000)
                .insignia("🔴🔴")
                .order(7)
                .build());

        ranks.add(Organization.RankConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .name("Admiral")
                .description("Fleet commander and Starfleet leadership")
                .pointsThreshold(10000)
                .insignia("🔴🔴🔴")
                .order(8)
                .build());

        return ranks;
    }

    private String getActionTypeIdByName(List<Organization.ActionType> actionTypes, String name) {
        return actionTypes.stream()
                .filter(at -> at.getName().equals(name))
                .findFirst()
                .map(Organization.ActionType::getId)
                .orElseThrow(() -> new IllegalStateException("Action type not found: " + name));
    }

    private void createDefaultUsers(String organizationId) {
        // Create development user that matches the AuthenticationService
        User devUser = User.builder()
                .id("dev-user-001")
                .organizationId(organizationId)
                .employeeId("NCC-1701")
                .name("James T.")
                .surname("Kirk")
                .managerEmployeeId(null)
                .role(UserRole.ADMIN)
                .totalPoints(1500) // Lieutenant Commander level
                .build();

        userRepository.save(devUser);

        // Create some additional test users
        User testUser1 = User.builder()
                .organizationId(organizationId)
                .employeeId("NCC-1701-A")
                .name("Spock")
                .surname("of Vulcan")
                .managerEmployeeId("NCC-1701")
                .role(UserRole.USER)
                .totalPoints(2200) // Commander level
                .build();

        User testUser2 = User.builder()
                .organizationId(organizationId)
                .employeeId("NCC-1701-B")
                .name("Leonard H.")
                .surname("McCoy")
                .managerEmployeeId("NCC-1701")
                .role(UserRole.USER)
                .totalPoints(800) // Lieutenant level
                .build();

        userRepository.save(testUser1);
        userRepository.save(testUser2);

        log.info("Created {} default users for organization {}", 3, organizationId);
    }
}