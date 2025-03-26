package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.SnapshotApiClient;
import org.cftoolsuite.cfapp.butler.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {

    private final SnapshotApiClient snapshotApiClient;

    public SnapshotService(SnapshotApiClient snapshotApiClient) {
        this.snapshotApiClient = snapshotApiClient;
    }

    @Tool(name = "Snapshot/GetLastSnapshotCollectionTime", description =
        """
            Retrieve the most recent timestamp when snapshot data was collected from the Cloud Foundry foundation.
            Use this ONLY for checking data collection status and freshness, NOT for retrieving actual foundation data.
        """)
    public TimestampResponse getCollectionTime() {
        return snapshotApiClient.collectGet("application/json").getBody();
    }

    @Tool(name = "Snapshot/GetFoundationDemographics", description =
        """
            Retrieve aggregate statistics and counts for the entire Cloud Foundry foundation.
            Use for high-level infrastructure overview queries like:
            'How many organizations exist?' or 'What are the total counts of resources?'.
            NOT for listing specific organizations, spaces, or users.
        """)
    public Demographics getDemographics() {
        return snapshotApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(name ="Snapshot/GetUsersInOrganizationAndSpace", description =
        """
            Retrieve the complete user roster for a SPECIFIC organization and space combination. REQUIRES both
            organization name AND space name parameters. NOT for listing users across multiple spaces or for
            role-specific user information.
        """)
    public SpaceUsers getSpaceUsers(
            @ToolParam(description = "Organization name.") String organization,
            @ToolParam(description = "Space name.") String space) {
        return snapshotApiClient.snapshotOrganizationSpaceUsersGet(organization, space).getBody();
    }

    @Tool(name = "Snapshot/TotalNumberOfOrganizations", description =
        """
            Retrieve ONLY the total COUNT of organizations as a single number. Use for queries like 'How many
            organizations exist?' or 'What is the organization count?'. NOT for listing organization names or details -
            use GetPageableOrganizations for that.
        """)
    public Long getOrganizationsCount() {
        return snapshotApiClient.snapshotOrganizationsCountGet().getBody();
    }

    @Tool(name = "Snapshot/TotalNumberOfSpaces", description =
        """
            Retrieve ONLY the total COUNT of spaces as a single number. Use for queries like 'How many spaces exist?'
            or 'What is the total space count?'. NOT for listing space names or details - use GetPageableSpaces for that.
        """)
    public Long getSpacesCount() {
        return snapshotApiClient.snapshotSpacesCountGet().getBody();
    }

    @Tool(name = "Snapshot/ListAllUsersBySpaceRolesInAnOrganizationAndSpace", description =
        """
            Retrieve users with DETAILED ROLE INFORMATION (auditor, developer, manager) for a specific organization and space.
            REQUIRES both organization name AND space name parameters.
            Use for role-based queries like 'Who has manager access in the marketing space?'
            A space user record includes:
            -- Organization
            -- Space
            -- Lists of users with auditor, developer, and manager roles
            -- List of distinct users among all roles
        """)
    public SpaceUsers getSpaceUsersForAnOrganizationAndSpace(
            @ToolParam(description = "Organization name.") String organization,
            @ToolParam(description = "Space name.") String space) {

        return Optional.ofNullable(snapshotApiClient.snapshotSpacesUsersGet())
                .map(ResponseEntity::getBody)
                .flatMap(spaceUsersList -> spaceUsersList.stream()
                        .filter(su -> organization.equalsIgnoreCase(su.getOrganization())
                                && space.equalsIgnoreCase(su.getSpace()))
                        .findFirst())
                .orElse(null);
    }

    @Tool(name = "Snapshot/ListAllSpacesAssociatedWithUserAccount", description =
        """
            List all spaces a SPECIFIC USER has access to across all organizations. REQUIRES user account name parameter.
            Use for user-centric queries like 'What spaces can john.doe@example.com access?' NOT for listing users in a space.
        """)
    public UserSpaces getUserSpaces(@ToolParam(description = "User account name.") String name) {
        return snapshotApiClient.snapshotSpacesUsersNameGet(name).getBody();
    }

    @Tool(name = "Snapshot/GetSpringDependencyFrequenciesSummary", description =
        """
            Analyze Spring dependency version statistics across Java applications. Returns frequency distribution data
            ONLY, not details about specific applications.
            Use for Spring dependency analysis questions like 'Which Spring Boot versions are most common?'
            Returns a map of key-value pairs where the key is the dependency version
            and value is the number of occurrences of that version.
        """)
    public Map<String, Integer> getSpringDependencyFrequenciesSummary() {
        return snapshotApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    @Tool(name = "Snapshot/GetSnapshotSummary", description =
        """
           Retrieve a comprehensive AGGREGATED SUMMARY of the entire foundation, including counts, metrics, and statistics.
           Use for broad overview queries like 'Give me a summary of the foundation'. NOT for listing specific applications, services, or users.
           This summary includes:
           -- Application Counts - Total number of applications, running instances, stopped instances, crashed instances, and total instances.
           -- Application Groupings - Counts of applications grouped by organization, buildpack, stack, Docker image, and application status.
           -- Application Resource Usage - Total memory and disk space used by applications (in gigabytes).
           -- Application Velocity - Metrics showing application creation and deletion trends over different time ranges.
           -- Service Instance Counts - Total number of service instances.
           -- Service Instance Groupings - Counts of service instances grouped by organization, service offering, and service plan.
           -- Service Instance Resource Usage - Total disk space used by service instances (in gigabytes).
           -- Service Instance Velocity - Metrics showing service instance creation and deletion trends over different time ranges.
           -- User Counts - Total number of users and service accounts.
       """)
    public SnapshotSummary getSnapshotSummary() {
        return snapshotApiClient.snapshotSummaryGet().getBody();
    }

    @Tool(name = "Snapshot/TotalNumberOfUserAccounts", description =
        """
            Retrieve ONLY the total COUNT of user accounts as a single number. Use for queries like 'How many users exist?'
            or 'What is the user count?'. NOT for listing user names or details - use GetPageableUserAccounts for that.
        """)
    public Long getUsersCount() {
        return snapshotApiClient.snapshotUsersCountGet().getBody();
    }

}