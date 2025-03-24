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

    @Tool(name = "GetLastSnapshotCollectionTime", description = "(Butler) Get the last snapshot collection date and time.")
    public TimestampResponse getCollectionTime() {
        return snapshotApiClient.collectGet("application/json").getBody();
    }

    @Tool(name = "DownloadPOMFilesForJavaApplicationsAsTarball", description = "(Butler) Download a tarball of POM files for Java applications.")
    public Resource downloadPomFiles() {
        return snapshotApiClient.downloadPomfilesGet().getBody();
    }

    @Tool(name = "GetFoundationDemographics", description =
        """
            (Butler) Get demographic information about the Cloud Foundry foundation.
            Demographics contains counts of: organizations, spaces, user accounts, and service accounts.
        """)
    public Demographics getDemographics() {
        return snapshotApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(name = "GetSpringApplicationDetails", description = "(Butler) Get details of Spring applications.")
    public List<Map<String, String>> getSpringApplicationInstanceDetails() {
        return snapshotApiClient.snapshotDetailAiSpringGet().getBody();
    }

    @Tool(name = "GetDormantWorkloads", description =
        """
            (Butler) Get dormant workloads. Workloads contains lists of: applications, service instances, and application relationships that are dormant.
            An application is considered dormant when the last retained event transpired daysSinceLastUpdate
            or longer from the time of request. A service instance is considered dormant when the last retained event
            transpired daysSinceLastUpdate or longer from the time of request.
            Note: audit events are retained for up to 31 days.
        """)
    public Workloads getDormantWorkloads(@ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate) {
        return snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
    }

    @Tool(name = "GetSnapshotDetail", description =
        """
            (Butler) Get snapshot detail.  This includes...
    
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

    @Tool(name = "GetLegacyWorkloadDetails", description = "(Butler) Get details of legacy workloads based on stacks and service offerings.")
    public Workloads getLegacyWorkloads(
            @ToolParam(description = "Comma-separated list of stacks to filter by.") String stacks,
            @ToolParam(description = "Comma-separated list of service offerings to filter by.") String serviceOfferings) {
        return snapshotApiClient.snapshotDetailLegacyGet(stacks, serviceOfferings).getBody();
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

    @Tool(name = "ListAllOrganizations", description = "(Butler) List all organizations.")
    public List<Organization> getAllOrganizations() {
        return snapshotApiClient.snapshotOrganizationsGet().getBody();
    }

    @Tool(name = "TotalNumberOfSpaces", description = "(Butler) Get the count of spaces.")
    public Long getSpacesCount() {
        return snapshotApiClient.snapshotSpacesCountGet().getBody();
    }

    @Tool(name = "ListAllSpaces", description = "(Butler) List all spaces.")
    public List<Space> getAllSpaces() {
        return snapshotApiClient.snapshotSpacesGet().getBody();
    }

    @Tool(name = "ForEachOrganizationAndSpaceListAllUsersBySpaceRoles", description =
        """
            (Butler) List all space users. Each space user record includes:
            -- Organization
            -- Space
            -- Lists of users with auditor, developer, and manager roles
            -- List of distinct users among all roles
        """)
    public List<SpaceUsers> getAllSpaceUsers() {
        return snapshotApiClient.snapshotSpacesUsersGet().getBody();
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

    @Tool(name = "ListAllUserAccounts", description = "(Butler) List all account names.")
    public List<String> getAllUserAccountNames() {
        return snapshotApiClient.snapshotUsersGet().getBody();
    }
}