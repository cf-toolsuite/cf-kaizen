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

    @Tool(name = "GetLastSnapshotCollectionTime", description = "(Butler) Get the last snapshot collection date and time.")
    public TimestampResponse getCollectionTime() {
        return snapshotApiClient.collectGet("application/json").getBody();
    }

    @Tool(name = "GetFoundationDemographics", description =
        """
            (Butler) Get demographic information about the Cloud Foundry foundation.
            Demographics contains counts of: organizations, spaces, user accounts, and service accounts.
        """)
    public Demographics getDemographics() {
        return snapshotApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(name ="GetUsersInOrganizationAndSpace", description = "(Butler) Get users in a specific organization and space.")
    public SpaceUsers getSpaceUsers(
            @ToolParam(description = "Organization name.") String organization,
            @ToolParam(description = "Space name.") String space) {
        return snapshotApiClient.snapshotOrganizationSpaceUsersGet(organization, space).getBody();
    }

    @Tool(name = "TotalNumberOfOrganizations", description = "(Butler) Get the count of organizations.")
    public Long getOrganizationsCount() {
        return snapshotApiClient.snapshotOrganizationsCountGet().getBody();
    }

    @Tool(name = "TotalNumberOfSpaces", description = "(Butler) Get the count of spaces.")
    public Long getSpacesCount() {
        return snapshotApiClient.snapshotSpacesCountGet().getBody();
    }

    @Tool(name = "ListAllUsersBySpaceRolesInAnOrganizationAndSpace", description =
        """
            (Butler) List all space users in an organization and space.
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

    @Tool(name = "ListAllSpacesAssociatedWithUserAccount", description = "(Butler) List all the organizations/spaces associated with a single user account")
    public UserSpaces getUserSpaces(@ToolParam(description = "User account name.") String name) {
        return snapshotApiClient.snapshotSpacesUsersNameGet(name).getBody();
    }

    @Tool(name = "GetSpringDependencyFrequenciesSummary", description =
        """
            (Butler) Get a summary of Spring dependency frequencies for applications built with the Java buildpack.
            Essentially a map of key-value pairs where the key is the dependency version
            and value is the number of occurrences of that version.
        """)
    public Map<String, Integer> getSpringDependencyFrequenciesSummary() {
        return snapshotApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    @Tool(name = "GetSnapshotSummary", description =
        """
           (Butler) Get snapshot summary.  This includes:
           -- Application Counts - Total number of applications, running instances, stopped instances, crashed instances, and total instances.
           -- Application Groupings - Counts of applications grouped by organization, buildpack, stack, Docker image, and application status (e.g., started, stopped, crashed).
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

    @Tool(name = "TotalNumberOfUserAccounts", description = "(Butler) Get the total count of user accounts.")
    public Long getUsersCount() {
        return snapshotApiClient.snapshotUsersCountGet().getBody();
    }

}