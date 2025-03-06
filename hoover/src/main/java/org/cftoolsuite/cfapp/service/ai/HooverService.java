package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.hoover.api.DefaultApiClient;
import org.cftoolsuite.cfapp.hoover.model.AppUsageReport;
import org.cftoolsuite.cfapp.hoover.model.Demographics;
import org.cftoolsuite.cfapp.hoover.model.JavaAppDetail;
import org.cftoolsuite.cfapp.hoover.model.Organization;
import org.cftoolsuite.cfapp.hoover.model.ServiceUsageReport;
import org.cftoolsuite.cfapp.hoover.model.SnapshotDetail;
import org.cftoolsuite.cfapp.hoover.model.SnapshotSummary;
import org.cftoolsuite.cfapp.hoover.model.Space;
import org.cftoolsuite.cfapp.hoover.model.SpaceUsers;
import org.cftoolsuite.cfapp.hoover.model.TaskUsageReport;
import org.cftoolsuite.cfapp.hoover.model.TimeKeepers;
import org.springframework.stereotype.Service;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

@Service
public class HooverService {

    private final DefaultApiClient hooverApiClient;

    public HooverService(DefaultApiClient hooverApiClient) {
        this.hooverApiClient = hooverApiClient;
    }

    @Tool(name = "accountingApplicationsGet", description = "Retrieve aggregated application usage report. Fetches an aggregated report of application usage across all Cloud Foundry foundations.")
    public AppUsageReport accountingApplicationsGet() {
        return hooverApiClient.accountingApplicationsGet().getBody();
    }

    @Tool(name = "accountingServicesGet", description = "Retrieve aggregated service usage report. Fetches an aggregated report of service usage across all Cloud Foundry foundations.")
    public ServiceUsageReport accountingServicesGet() {
        return hooverApiClient.accountingServicesGet().getBody();
    }

    @Tool(name = "accountingTasksGet", description = "Retrieve aggregated task usage report. Fetches an aggregated report of task usage across all Cloud Foundry foundations.")
    public TaskUsageReport accountingTasksGet() {
        return hooverApiClient.accountingTasksGet().getBody();
    }

    @Tool(name = "collectGet", description = "Retrieve collection timestamps from Cloud Foundry foundations. Retrieves the timestamp of the last data collection from each configured Cloud Foundry foundation.")
    public TimeKeepers collectGet() {
        return hooverApiClient.collectGet().getBody();
    }

    @Tool(name = "snapshotDemographicsGet", description = "Aggregate demographics across Cloud Foundry foundations. Retrieves aggregated demographic information, including counts of organizations, spaces, users, and service accounts across configured Cloud Foundry foundations.")
    public Demographics snapshotDemographicsGet() {
        return hooverApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(name = "snapshotDetailAiGet", description = "Retrieve a CSV report of application instance details. Generates and returns a CSV formatted report of application details aggregated from all Cloud Foundry foundations.")
    public String snapshotDetailAiGet() {
        return hooverApiClient.snapshotDetailAiGet().getBody();
    }

    @Tool(name = "snapshotDetailAiSpringGet", description = "Retrieve details of Spring Boot applications. Fetches detailed information about Spring Boot applications deployed across all configured Cloud Foundry foundations.")
    public List<JavaAppDetail> snapshotDetailAiSpringGet() {
        return hooverApiClient.snapshotDetailAiSpringGet().getBody();
    }

    @Tool(name = "snapshotDetailGet", description = "Retrieve detailed snapshot information. Fetches a comprehensive snapshot detail including applications, service instances, and application relationships from all configured Cloud Foundry foundations.")
    public SnapshotDetail snapshotDetailGet() {
        return hooverApiClient.snapshotDetailGet().getBody();
    }

    @Tool(name = "snapshotDetailSiGet", description = "Retrieve a CSV report of service instance details. Generates and returns a CSV formatted report of service instance details aggregated from all Cloud Foundry foundations.")
    public String snapshotDetailSiGet() {
        return hooverApiClient.snapshotDetailSiGet().getBody();
    }

    @Tool(name = "snapshotFoundationOrganizationSpaceUsersGet", description = "Retrieve space users for a specific foundation, organization, and space. Fetches space user information for a specific space within an organization in a given Cloud Foundry foundation.")
    public SpaceUsers snapshotFoundationOrganizationSpaceUsersGet(
            @ToolParam(description = "Name of the Cloud Foundry foundation.") String foundation,
            @ToolParam(description = "Name of the organization.") String organization,
            @ToolParam(description = "Name of the space.") String space) {
        return hooverApiClient.snapshotFoundationOrganizationSpaceUsersGet(foundation, organization, space).getBody();
    }

    @Tool(name = "snapshotOrganizationsGet", description = "Retrieve a list of organizations across Cloud Foundry foundations. Fetches a list of organizations from all configured Cloud Foundry foundations, enriched with foundation information.")
    public List<Organization> snapshotOrganizationsGet() {
        return hooverApiClient.snapshotOrganizationsGet().getBody();
    }

    @Tool(name = "snapshotSpacesGet", description = "Retrieve a list of spaces across Cloud Foundry foundations. Fetches a list of spaces from all configured Cloud Foundry foundations, enriched with foundation and organization information.")
    public List<Space> snapshotSpacesGet() {
        return hooverApiClient.snapshotSpacesGet().getBody();
    }

    @Tool(name = "snapshotSpacesUsersGet", description = "Retrieve all space users across foundations. Fetches space user information for all spaces across all configured Cloud Foundry foundations.")
    public List<SpaceUsers> snapshotSpacesUsersGet() {
        return hooverApiClient.snapshotSpacesUsersGet().getBody();
    }

    @Tool(name = "snapshotSummaryAiSpringGet", description = "Calculate Spring application dependency frequency. Calculates and returns the frequency of Spring application dependencies across all configured Cloud Foundry foundations.")
    public Map<String, Integer> snapshotSummaryAiSpringGet() {
        return hooverApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    @Tool(name = "snapshotSummaryGet", description = "Retrieve a summary snapshot. Fetches a summarized snapshot including application and service instance counts from all configured Cloud Foundry foundations.")
    public SnapshotSummary snapshotSummaryGet() {
        return hooverApiClient.snapshotSummaryGet().getBody();
    }

    @Tool(name = "snapshotUsersCountGet", description = "Retrieve the total count of users and service accounts. Returns the total count of user and service accounts aggregated across all Cloud Foundry foundations.")
    public Long snapshotUsersCountGet() {
        return hooverApiClient.snapshotUsersCountGet().getBody();
    }

    @Tool(name = "snapshotUsersGet", description = "Retrieve all user and service account names. Returns a set of unique user and service account names aggregated across all Cloud Foundry foundations.")
    public List<String> snapshotUsersGet() {
        return hooverApiClient.snapshotUsersGet().getBody();
    }
}
