# Gamifier Application Requirements

## Overview

Employee behavior incentivization system through gamification, built using Domain Driven Design principles with
multi-tenant SaaS architecture.

## Domain Model

### Ubiquitous Language

- **Action**: Measurable employee behavior that earns points
- **Action Type**: Configuration defining what actions are available and their point values
- **Mission**: Collection of actions that, when completed, awards bonus points and a badge
- **Event**: System activity record (action captures, mission completions, rank changes)
- **Rank**: Point-based progression system with configurable names
- **Organization**: Tenant boundary for multi-tenant system

### Aggregate Roots

#### Organization Aggregate (Knowledge Layer)

- **Organization**: organization_id, name, settings
- **ActionType**: organization_id, description, points, capture_methods (UI/Import), reporter_types (self/peer/manager),
  approval_required
- **MissionType**: organization_id, name, badge, required_actions[], bonus_points
- **RankConfiguration**: organization_id, rank_name, point_threshold

#### User Aggregate

- **User**: organization_id, employee_id, name, surname, manager_employee_id, role (user/admin), last_login
- **UserPoints**: total_points, current_rank_id
- **UserMissionProgress**: organization_id, user_id, mission_type_id, completed_actions[], completion_status

#### ActionCapture Aggregate

- **ActionCapture**: organization_id, user_id, action_type_id, date, capture_method, status, audit_info (timestamp,
  user_id/system_id)

#### MissionCompletion Aggregate

- **MissionCompletion**: organization_id, user_id, mission_type_id, completion_date, bonus_points_awarded

#### Event Aggregate

- **Event**: organization_id, user_id, event_type, timestamp, data
- Users see only their events; Admins see all org events with filtering

### Domain Services

- **ActionCaptureService**: Handle UI and import-based action recording
- **MissionProgressService**: Track and calculate mission completion
- **RankCalculationService**: Determine user ranks based on points
- **EventGenerationService**: Create system activity events

### Business Rules

1. **Unified Point System**: Single point scale across organization
2. **Idempotent Action Capture**: One action per type per user per day
3. **Multi-tenant Isolation**: All data scoped by organization_id
4. **Same Action, Multiple Missions**: Actions can count toward multiple missions
5. **One-time Mission Completion**: Missions cannot be repeated
6. **Permanent Ranks**: No rank decay over time
7. **Import Auto-approval**: Imported actions don't require manager approval
8. **UI Approval Workflow**: Manual actions may require manager approval (configurable)

## Service Architecture

### Configuration Service (Knowledge Layer)

**Responsibilities:**

- Organization management
- Action Type configuration (CRUD)
- Mission Type configuration (CRUD)
- Rank Configuration (CRUD)
- Admin-only operations

**APIs:**

- `/api/organization` - CRUD for organizations (top-level)
- `/api/organization/{orgId}/action-types` - CRUD operations
- `/api/organization/{orgId}/mission-types` - CRUD operations
- `/api/organization/{orgId}/ranks` - CRUD operations
- `/api/organization/{orgId}/settings` - Organization settings

### Gamification Service (Game Engine)

**Responsibilities:**

- User management and profiles
- Action capture (UI + Import)
- Mission progress tracking
- Rank calculation
- Event generation
- Leaderboards
- Dashboards and reporting

**APIs:**

- `/api/users` - User profiles, dashboards, and mission progress
- `/api/users` - POST multipart file for CSV user import
- `/api/actions` - Action capture and history
- `/api/actions` - POST multipart file for CSV action import
- `/api/leaderboards` - Rankings and leaderboards
- `/api/events` - User activity feeds

## Functional Requirements

### User Roles & Permissions

- **User**: View dashboard, capture actions, view leaderboards/reports
- **Admin**: All user permissions + configure knowledge layer (action types, missions, ranks)

### User Dashboard

- Personal total points and current rank
- Mission progress (earned, in-progress, available badges)
- List of available actions with capture options
- Personal event feed since last login (expandable to older events)

### Leaderboards

- Current month view
- All-time view
- Organization-scoped (single tenant view)

### Action Capture

- **Manual UI Capture**: Self, peer, or manager reporting with optional approval workflow
- **CSV Import**: Standardized format (employee_id, behaviour_type, date) via API
- **Validation**: Data types, employee existence, idempotency
- **Audit Trail**: All captures logged with timestamp and source

### Mission System

- Badge-based achievement tracking
- Progress visualization (X/Y actions completed)
- Bonus points on completion
- No time limits or prerequisites
- Available to all organization users

### Reporting

- Basic on-screen reports for admins
- Participation rates, mission completion stats, user activity

## Technical Requirements

### Technology Stack

- **Backend**: Java Spring Boot
- **Database**: MongoDB
- **Frontend**: Angular
- **Deployment**: OpenShift
- **Authentication**: Azure AD
- **Architecture**: Reverse proxy with Azure token injection between Angular and Spring Boot

### Architecture Patterns

- **Hexagonal Architecture** (Ports & Adapters)
- **Domain Driven Design**
- **Multi-tenant SaaS** (organization_id scoping)

### Data Management

- **Multi-tenancy**: organization_id in all collections
- **Indexing**: organization_id, user_id, dates for performance
- **Session Management**: Azure AD tokens maintained by reverse proxy
- **Data Validation**: Input sanitization and business rule enforcement

### Non-Functional Requirements

- **Security**: Multi-tenant data isolation, role-based access control
- **Performance**: Efficient queries with proper indexing, leaderboard caching
- **Scalability**: Horizontal scaling ready for OpenShift
- **Testing**: >95% line and branch coverage for all stages
- **Quality**: Unit and integration tests for each feature branch

### Default Configuration

- **Ranks**: Star Trek science ranks (Cadet, Ensign, Lieutenant JG, Lieutenant, Lt. Commander, Commander, Captain,
  Admiral)
- **Sample Action Types**: Training completion, collaboration activities, innovation submissions
- **Sample Missions**: Onboarding badges, skill development paths

## Success Criteria

- Multi-tenant system supporting multiple organizations
- Configurable gamification rules per organization
- Real-time action capture and mission progress tracking
- Intuitive user experience with immediate feedback
- Robust audit trail and reporting capabilities
- Scalable architecture ready for enterprise deployment
- Comprehensive test coverage (>95% lines and branches)
- Successful feature branch integration workflow