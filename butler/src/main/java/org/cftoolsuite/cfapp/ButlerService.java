package org.cftoolsuite.cfapp;

import org.cftoolsuite.cfapp.butler.api.*;
import org.cftoolsuite.cfapp.butler.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ButlerService {

    private final AccountingApiClient accountingApiClient;
    private final OnDemandApiClient onDemandApiClient;
    private final PoliciesApiClient policiesApiClient;
    private final ProductsApiClient productsApiClient;
    private final SnapshotApiClient snapshotApiClient;

    public ButlerService(
            AccountingApiClient accountingApiClient,
            OnDemandApiClient onDemandApiClient,
            PoliciesApiClient policiesApiClient,
            ProductsApiClient productsApiClient,
            SnapshotApiClient snapshotApiClient) {
        this.accountingApiClient = accountingApiClient;
        this.onDemandApiClient = onDemandApiClient;
        this.policiesApiClient = policiesApiClient;
        this.productsApiClient = productsApiClient;
        this.snapshotApiClient = snapshotApiClient;
    }

    @Tool(description = "Get system-wide application usage report.")
    public AppUsageReport getAccountingApplicationsReport() {
        return accountingApiClient.accountingApplicationsGet().getBody();
    }

    @Tool(description = "Get application usage report for an organization within a date range.")
    public String getAccountingApplicationsOrgReport(
            @ToolParam(description = "Organization name.") String orgName,
            @ToolParam(description = "Start date (YYYY-MM-DD).") LocalDate startDate,
            @ToolParam(description = "End date (YYYY-MM-DD).") LocalDate endDate) {
        return accountingApiClient.accountingApplicationsOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(description = "Get system-wide service usage report.")
    public ServiceUsageReport getAccountingServicesReport() {
        return accountingApiClient.accountingServicesGet().getBody();
    }

    @Tool(description = "Get service usage report for an organization within a date range.")
    public String getAccountingServicesOrgReport(
            @ToolParam(description = "Organization name.") String orgName,
            @ToolParam(description = "Start date (YYYY-MM-DD).") LocalDate startDate,
            @ToolParam(description = "End date (YYYY-MM-DD).") LocalDate endDate) {
        return accountingApiClient.accountingServicesOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(description = "Get system-wide task usage report.")
    public TaskUsageReport getAccountingTasksReport() {
        return accountingApiClient.accountingTasksGet().getBody();
    }

    @Tool(description = "Get task usage report for an organization within a date range.")
    public String getAccountingTasksOrgReport(
            @ToolParam(description = "Organization name.") String orgName,
            @ToolParam(description = "Start date (YYYY-MM-DD).") LocalDate startDate,
            @ToolParam(description = "End date (YYYY-MM-DD).") LocalDate endDate) {
        return accountingApiClient.accountingTasksOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(description = "Get events for a specific entity.")
    public List<Event> getEventsById(
            @ToolParam(description = "ID of the entity to retrieve events for.") String id,
            @ToolParam(description = "Number of events to retrieve.") Integer numberOfEvents,
            @ToolParam(description = "Array of event types to filter by.") List<String> types) {
        return onDemandApiClient.eventsIdGet(id, numberOfEvents, types).getBody();
    }

    @Tool(description = "Get metadata for resources of a specific type.")
    public Resources getMetadataByType(
            @ToolParam(description = "Type of resource metadata to retrieve (e.g., apps, services).") String type,
            @ToolParam(description = "Label selector to filter resources.") String labelSelector,
            @ToolParam(description = "Page number for pagination.") Integer page,
            @ToolParam(description = "Number of resources per page.") Integer perPage) {
        return onDemandApiClient.metadataTypeGet(type, labelSelector, page, perPage).getBody();
    }

    @Tool(description = "Get a specific application policy by ID.")
    public Policies getPolicyApplicationById(@ToolParam(description = "ID of the application policy.") String id) {
        return policiesApiClient.policiesApplicationIdGet(id).getBody();
    }

    @Tool(description = "Get a specific endpoint policy by ID.")
    public Policies getPolicyEndpointById(@ToolParam(description = "ID of the endpoint policy.") String id) {
        return policiesApiClient.policiesEndpointIdGet(id).getBody();
    }

    @Tool(description = "Trigger on-demand policy execution (profile, on-demand).")
    public void executePolicies() {
        policiesApiClient.policiesExecutePost();
    }

    @Tool(description = "List all policies.")
    public Policies getAllPolicies() {
        return policiesApiClient.policiesGet().getBody();
    }

    @Tool(description = "Get a specific hygiene policy by ID.")
    public Policies getPolicyHygieneById(@ToolParam(description = "ID of the hygiene policy.") String id) {
        return policiesApiClient.policiesHygieneIdGet(id).getBody();
    }

    @Tool(description = "Get a specific legacy policy by ID.")
    public Policies getPolicyLegacyById(@ToolParam(description = "ID of the legacy policy.") String id) {
        return policiesApiClient.policiesLegacyIdGet(id).getBody();
    }

    @Tool(description = "Get a specific query policy by ID.")
    public Policies getPolicyQueryById(@ToolParam(description = "ID of the query policy.") String id) {
        return policiesApiClient.policiesQueryIdGet(id).getBody();
    }

    @Tool(description = "Refresh policies from Git repository.")
    public void refreshPolicies() {
        policiesApiClient.policiesRefreshPost();
    }

    @Tool(description = "Generate a historical report of policy executions.")
    public String getPoliciesReport(
            @ToolParam(description = "Start date for the report (YYYY-MM-DD).") LocalDate start,
            @ToolParam(description = "End date for the report (YYYY-MM-DD).") LocalDate end) {
        return policiesApiClient.policiesReportGet(start, end).getBody();
    }

    @Tool(description = "Get a specific service instance policy by ID.")
    public Policies getPolicyServiceInstanceById(@ToolParam(description = "ID of the service instance policy.") String id) {
        return policiesApiClient.policiesServiceInstanceIdGet(id).getBody();
    }

    @Tool(description = "Get deployed products from Ops Manager.")
    public List<DeployedProduct> getDeployedProducts() {
        return productsApiClient.productsDeployedGet().getBody();
    }

    @Tool(description = "Get product metrics from Ops Manager and Pivnet.")
    public ProductMetrics getProductMetrics() {
        return productsApiClient.productsMetricsGet().getBody();
    }

    @Tool(description = "Get Ops Manager info.")
    public OmInfo getOmInfo() {
        return productsApiClient.productsOmInfoGet().getBody();
    }

    @Tool(description = "Get stemcell assignments from Ops Manager.")
    public StemcellAssignments getStemcellAssignments() {
        return productsApiClient.productsStemcellAssignmentsGet().getBody();
    }

    @Tool(description = "Get stemcell associations from Ops Manager (v2.6+).")
    public StemcellAssociations getStemcellAssociations() {
        return productsApiClient.productsStemcellAssociationsGet().getBody();
    }

    @Tool(description = "Get product catalog from Pivnet.")
    public Products getProductCatalog() {
        return productsApiClient.storeProductCatalogGet().getBody();
    }

    @Tool(description = "Get product releases from Pivnet.")
    public List<Release> getProductReleases(@ToolParam(description = "Query option (latest, all, recent).") String q) {
        return productsApiClient.storeProductReleasesGet(q).getBody();
    }

    @Tool(description = "Get the last collection time.")
    public OffsetDateTime getCollectionTime() {
        return snapshotApiClient.collectGet().getBody();
    }

    @Tool(description = "Download a tarball of POM files for Java applications.")
    public Resource downloadPomFiles() {
        return snapshotApiClient.downloadPomfilesGet().getBody();
    }

    @Tool(description = "Get demographic information about the Cloud Foundry instance.")
    public Demographics getDemographics() {
        return snapshotApiClient.snapshotDemographicsGet().getBody();
    }

    @Tool(description = "Get application instance detail as CSV.")
    public String getApplicationInstanceDetailCsv() {
        return snapshotApiClient.snapshotDetailAiGet().getBody();
    }

    @Tool(description = "Get details of Spring applications.")
    public List<Map<String, String>> getSpringApplicationInstanceDetails() {
        return snapshotApiClient.snapshotDetailAiSpringGet().getBody();
    }

    @Tool(description = "Get dormant workloads (applications and service instances).")
    public Workloads getDormantWorkloads(@ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate) {
        return snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
    }

    @Tool(description = "Get snapshot detail as JSON.")
    public SnapshotDetail getSnapshotDetail() {
        return snapshotApiClient.snapshotDetailGet().getBody();
    }

    @Tool(description = "Get details of legacy workloads based on stacks and service offerings.")
    public Workloads getLegacyWorkloads(
            @ToolParam(description = "Comma-separated list of stacks to filter by.") String stacks,
            @ToolParam(description = "Comma-separated list of service offerings to filter by.") String serviceOfferings) {
        return snapshotApiClient.snapshotDetailLegacyGet(stacks, serviceOfferings).getBody();
    }

    @Tool(description = "Get application relationships detail as CSV.")
    public String getApplicationRelationshipsDetailCsv() {
        return snapshotApiClient.snapshotDetailRelationsGet().getBody();
    }

    @Tool(description = "Get service instance detail as CSV.")
    public String getServiceInstanceDetailCsv() {
        return snapshotApiClient.snapshotDetailSiGet().getBody();
    }

    @Tool(description = "Get user account detail as CSV.")
    public String getUserAccountDetailCsv() {
        return snapshotApiClient.snapshotDetailUsersGet().getBody();
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

    @Tool(description = "Get snapshot summary as JSON.")
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