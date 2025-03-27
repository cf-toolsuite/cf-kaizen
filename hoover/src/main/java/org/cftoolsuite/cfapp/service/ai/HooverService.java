package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.hoover.api.DefaultApiClient;
import org.cftoolsuite.cfapp.hoover.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HooverService {

    private final DefaultApiClient hooverApiClient;

    public HooverService(DefaultApiClient hooverApiClient) {
        this.hooverApiClient = hooverApiClient;
    }

    @Tool(name = "AccountingAggregateApplicationUsageReport", description = "Retrieve aggregated application usage report. Fetches an aggregated report of application usage across all Cloud Foundry foundations.")
    public AppUsageReport accountingApplicationsGet() {
        return hooverApiClient.accountingApplicationsGet().getBody();
    }

    @Tool(name = "AccountingAggregateServiceUsageReport", description = "Retrieve aggregated service usage report. Fetches an aggregated report of service usage across all Cloud Foundry foundations.")
    public ServiceUsageReport accountingServicesGet() {
        return hooverApiClient.accountingServicesGet().getBody();
    }

    @Tool(name = "AccountingAggregateTaskUsageReport", description = "Retrieve aggregated task usage report. Fetches an aggregated report of task usage across all Cloud Foundry foundations.")
    public TaskUsageReport accountingTasksGet() {
        return hooverApiClient.accountingTasksGet().getBody();
    }

    @Tool(name = "SnapshotFetchCollectionTimestampsForEachRegisteredFoundation", description = "Retrieve collection timestamps from Cloud Foundry foundations. Retrieves the timestamp of the last data collection from each configured Cloud Foundry foundation.")
    public TimeKeepers collectGet() {
        return hooverApiClient.collectGet().getBody();
    }

    @Tool(name = "SnapshotGetDemographicsAcrossFoundations", description = "Aggregate demographics across Cloud Foundry foundations. Retrieves aggregated demographic information, including counts of organizations, spaces, users, and service accounts across configured Cloud Foundry foundations.")
    public Demographics snapshotDemographicsGet() {
        return hooverApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(name = "SnapshotForAFoundationOrganizationAndSpaceListAllUsersBySpaceRoles", description = "Retrieve space users for a specific foundation, organization, and space. Fetches space user information for a specific space within an organization in a given Cloud Foundry foundation.")
    public SpaceUsers snapshotFoundationOrganizationSpaceUsersGet(
            @ToolParam(description = "Name of the Cloud Foundry foundation.") String foundation,
            @ToolParam(description = "Name of the organization.") String organization,
            @ToolParam(description = "Name of the space.") String space) {
        return hooverApiClient.snapshotFoundationOrganizationSpaceUsersGet(foundation, organization, space).getBody();
    }

    @Tool(name = "SnapshotGetSpringDependencyFrequenciesSummary", description = "Calculate Spring application dependency frequency. Calculates and returns the frequency of Spring application dependencies across all configured Cloud Foundry foundations.")
    public Map<String, Integer> snapshotSummaryAiSpringGet() {
        return hooverApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    @Tool(name = "SnapshotGetSnapshotSummary", description = "Retrieve a summary snapshot. Fetches a summarized snapshot including application and service instance counts from all configured Cloud Foundry foundations.")
    public SnapshotSummary snapshotSummaryGet() {
        return hooverApiClient.snapshotSummaryGet().getBody();
    }

    @Tool(name = "SnapshotTotalNumberOfUserAccounts", description = "Retrieve the total count of users and service accounts. Returns the total count of user and service accounts aggregated across all Cloud Foundry foundations.")
    public Long snapshotUsersCountGet() {
        return hooverApiClient.snapshotUsersCountGet().getBody();
    }

}
