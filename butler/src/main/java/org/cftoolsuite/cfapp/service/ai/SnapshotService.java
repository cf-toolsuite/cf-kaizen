package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.SnapshotApiClient;
import org.cftoolsuite.cfapp.butler.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SnapshotService {

    private final SnapshotApiClient snapshotApiClient;


    public SnapshotService(SnapshotApiClient snapshotApiClient) {
        this.snapshotApiClient = snapshotApiClient;
    }

    @Tool(description = "Get the last snapshot collection date and time.")
    public TimestampResponse getCollectionTime() {
        return snapshotApiClient.collectGet("application/json").getBody();
    }

    @Tool(description = "Download a tarball of POM files for Java applications.")
    public Resource downloadPomFiles() {
        return snapshotApiClient.downloadPomfilesGet().getBody();
    }

    @Tool(description = "Get demographic information about the Cloud Foundry foundation.")
    public Demographics getDemographics() {
        return snapshotApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(description = "Get details of Spring applications.")
    public List<Map<String, String>> getSpringApplicationInstanceDetails() {
        return snapshotApiClient.snapshotDetailAiSpringGet().getBody();
    }

    @Tool(description = "Get dormant workloads (applications and service instances).")
    public Workloads getDormantWorkloads(@ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate) {
        return snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
    }

    @Tool(description =
        """
            Get snapshot detail.  This includes...
    
            -- Applications
                -- Detailed information about each application, including:
                    -- Identification: Name, ID, organization, and space.
                    -- Configuration: Buildpack, buildpack version, Docker image, and stack.
                    -- Instance Information: Number of running and total instances.
                    -- Resource Usage: Memory and disk space used and quota.
                    -- Lifecycle: Last push timestamp, last event details (description, actor, timestamp), and requested state.
                    -- Network: URLs associated with the application.
                    -- Buildpack Release: Latest buildpack release type, date, version, and URL.
        
            -- Service Instances
                -- Detailed information about each service instance, including:
                    -- Identification: Name, ID, organization, and space.
                    -- Service Information: Service plan, service offering, and type.
                    -- Resource Usage: Disk space used and quota.
                    -- Lifecycle: Last operation details (type, state, description, timestamp).
                    -- Relationships: List of application IDs bound to the service instance.
                    -- Dashboard: URL for the service instance dashboard.
                    -- Tags: User-defined tags associated with the service instance.
        
            -- Relationships
                -- A list of relationships between applications and service instances, showing which applications are bound to which service instances.
        
            -- Accounts
                -- A list of user account names.
                -- A list of service account names.
        """)
    public SnapshotDetail getSnapshotDetail() {
        return snapshotApiClient.snapshotDetailGet().getBody();
    }

    @Tool(description = "Get details of legacy workloads based on stacks and service offerings.")
    public Workloads getLegacyWorkloads(
            @ToolParam(description = "Comma-separated list of stacks to filter by.") String stacks,
            @ToolParam(description = "Comma-separated list of service offerings to filter by.") String serviceOfferings) {
        return snapshotApiClient.snapshotDetailLegacyGet(stacks, serviceOfferings).getBody();
    }

    @Tool(description = "Get users in a specific organization and space.")
    public SpaceUsers getSpaceUsers(
            @ToolParam(description = "Organization name.") String organization,
            @ToolParam(description = "Space name.") String space) {
        return snapshotApiClient.snapshotOrganizationSpaceUsersGet(organization, space).getBody();
    }

    @Tool(description = "Get the count of organizations.")
    public Long getOrganizationsCount() {
        return snapshotApiClient.snapshotOrganizationsCountGet().getBody();
    }

    @Tool(description = "List all organizations.")
    public List<Organization> getAllOrganizations() {
        return snapshotApiClient.snapshotOrganizationsGet().getBody();
    }

    @Tool(description = "Get the count of spaces.")
    public Long getSpacesCount() {
        return snapshotApiClient.snapshotSpacesCountGet().getBody();
    }

    @Tool(description = "List all spaces.")
    public List<Space> getAllSpaces() {
        return snapshotApiClient.snapshotSpacesGet().getBody();
    }

    @Tool(description = "List all space users.")
    public List<SpaceUsers> getAllSpaceUsers() {
        return snapshotApiClient.snapshotSpacesUsersGet().getBody();
    }

    @Tool(description = "Get spaces for a specific user account name.")
    public UserSpaces getUserSpaces(@ToolParam(description = "User account name.") String name) {
        return snapshotApiClient.snapshotSpacesUsersNameGet(name).getBody();
    }

    @Tool(description = "Get a summary of Spring dependency frequencies.")
    public Map<String, Integer> getSpringDependencyFrequenciesSummary() {
        return snapshotApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    @Tool(description =
        """
           Get snapshot summary.  This includes:
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

    @Tool(description = "Get the total count of user accounts.")
    public Long getUsersCount() {
        return snapshotApiClient.snapshotUsersCountGet().getBody();
    }

    @Tool(description = "List all account names.")
    public List<String> getAllUserAccountNames() {
        return snapshotApiClient.snapshotUsersGet().getBody();
    }
}