package org.cftoolsuite.cfapp;

import org.cftoolsuite.cfapp.butler.api.*;
import org.cftoolsuite.cfapp.butler.model.*;
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

    public AppUsageReport getAccountingApplicationsReport() {
        return accountingApiClient.accountingApplicationsGet().getBody();
    }

    public String getAccountingApplicationsOrgReport(String orgName, LocalDate startDate, LocalDate endDate) {
        return accountingApiClient.accountingApplicationsOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    public ServiceUsageReport getAccountingServicesReport() {
        return accountingApiClient.accountingServicesGet().getBody();
    }

    public String getAccountingServicesOrgReport(String orgName, LocalDate startDate, LocalDate endDate) {
        return accountingApiClient.accountingServicesOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    public TaskUsageReport getAccountingTasksReport() {
        return accountingApiClient.accountingTasksGet().getBody();
    }

    public String getAccountingTasksOrgReport(String orgName, LocalDate startDate, LocalDate endDate) {
        return accountingApiClient.accountingTasksOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    public List<Event> getEventsById(String id, Integer numberOfEvents, List<String> types) {
        return onDemandApiClient.eventsIdGet(id, numberOfEvents, types).getBody();
    }

    public Resources getMetadataByType(String type, String labelSelector, Integer page, Integer perPage) {
        return onDemandApiClient.metadataTypeGet(type, labelSelector, page, perPage).getBody();
    }

    public Policies getPolicyApplicationById(String id) {
        return policiesApiClient.policiesApplicationIdGet(id).getBody();
    }

    public Policies getPolicyEndpointById(String id) {
        return policiesApiClient.policiesEndpointIdGet(id).getBody();
    }

    public void executePolicies() {
        policiesApiClient.policiesExecutePost();
    }

    public Policies getAllPolicies() {
        return policiesApiClient.policiesGet().getBody();
    }

    public Policies getPolicyHygieneById(String id) {
        return policiesApiClient.policiesHygieneIdGet(id).getBody();
    }

    public Policies getPolicyLegacyById(String id) {
        return policiesApiClient.policiesLegacyIdGet(id).getBody();
    }

    public Policies getPolicyQueryById(String id) {
        return policiesApiClient.policiesQueryIdGet(id).getBody();
    }

    public void refreshPolicies() {
        policiesApiClient.policiesRefreshPost();
    }

    public String getPoliciesReport(LocalDate start, LocalDate end) {
        return policiesApiClient.policiesReportGet(start, end).getBody();
    }

    public Policies getPolicyServiceInstanceById(String id) {
        return policiesApiClient.policiesServiceInstanceIdGet(id).getBody();
    }

    public List<DeployedProduct> getDeployedProducts() {
        return productsApiClient.productsDeployedGet().getBody();
    }

    public ProductMetrics getProductMetrics() {
        return productsApiClient.productsMetricsGet().getBody();
    }

    public OmInfo getOmInfo() {
        return productsApiClient.productsOmInfoGet().getBody();
    }

    public StemcellAssignments getStemcellAssignments() {
        return productsApiClient.productsStemcellAssignmentsGet().getBody();
    }

    public StemcellAssociations getStemcellAssociations() {
        return productsApiClient.productsStemcellAssociationsGet().getBody();
    }

    public Products getProductCatalog() {
        return productsApiClient.storeProductCatalogGet().getBody();
    }

    public List<Release> getProductReleases(String q) {
        return productsApiClient.storeProductReleasesGet(q).getBody();
    }

    public OffsetDateTime getCollectionTime() {
        return snapshotApiClient.collectGet().getBody();
    }

    public Resource downloadPomFiles() {
        return snapshotApiClient.downloadPomfilesGet().getBody();
    }

    public Demographics getDemographics() {
        return snapshotApiClient.snapshotDemographicsGet().getBody();
    }

    public String getApplicationInstanceDetailCsv() {
        return snapshotApiClient.snapshotDetailAiGet().getBody();
    }

    public List<Map<String, String>> getSpringApplicationInstanceDetails() {
        return snapshotApiClient.snapshotDetailAiSpringGet().getBody();
    }

    public Workloads getDormantWorkloads(Integer daysSinceLastUpdate) {
        return snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
    }

    public SnapshotDetail getSnapshotDetail() {
        return snapshotApiClient.snapshotDetailGet().getBody();
    }

    public Workloads getLegacyWorkloads(String stacks, String serviceOfferings) {
        return snapshotApiClient.snapshotDetailLegacyGet(stacks, serviceOfferings).getBody();
    }

    public String getApplicationRelationshipsDetailCsv() {
        return snapshotApiClient.snapshotDetailRelationsGet().getBody();
    }

    public String getServiceInstanceDetailCsv() {
        return snapshotApiClient.snapshotDetailSiGet().getBody();
    }

    public String getUserAccountDetailCsv() {
        return snapshotApiClient.snapshotDetailUsersGet().getBody();
    }

    public SpaceUsers getSpaceUsers(String organization, String space) {
        return snapshotApiClient.snapshotOrganizationSpaceUsersGet(organization, space).getBody();
    }

    public Long getOrganizationsCount() {
        return snapshotApiClient.snapshotOrganizationsCountGet().getBody();
    }

    public List<Organization> getAllOrganizations() {
        return snapshotApiClient.snapshotOrganizationsGet().getBody();
    }

    public Long getSpacesCount() {
        return snapshotApiClient.snapshotSpacesCountGet().getBody();
    }

    public List<Space> getAllSpaces() {
        return snapshotApiClient.snapshotSpacesGet().getBody();
    }

    public List<SpaceUsers> getAllSpaceUsers() {
        return snapshotApiClient.snapshotSpacesUsersGet().getBody();
    }

    public UserSpaces getUserSpaces(String name) {
        return snapshotApiClient.snapshotSpacesUsersNameGet(name).getBody();
    }

    public Map<String, Integer> getSpringDependencyFrequenciesSummary() {
        return snapshotApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    public SnapshotSummary getSnapshotSummary() {
        return snapshotApiClient.snapshotSummaryGet().getBody();
    }

    public Long getUsersCount() {
        return snapshotApiClient.snapshotUsersCountGet().getBody();
    }

    public List<String> getAllUserAccountNames() {
        return snapshotApiClient.snapshotUsersGet().getBody();
    }
}
