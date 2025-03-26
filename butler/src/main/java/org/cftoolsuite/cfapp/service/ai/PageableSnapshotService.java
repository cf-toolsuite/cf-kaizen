package org.cftoolsuite.cfapp.service.ai;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.butler.api.SnapshotApiClient;
import org.cftoolsuite.cfapp.butler.model.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PageableSnapshotService {

        private final SnapshotApiClient snapshotApiClient;
        private static final int DEFAULT_PAGE_SIZE = 10;

        public PageableSnapshotService(SnapshotApiClient snapshotApiClient) {
                this.snapshotApiClient = snapshotApiClient;
        }

        /**
         * Get a pageable list of Spring application instance details.
         */
        @Tool(name = "Snapshot/GetPageableSpringApplicationDetails", description = """
                        Retrieve SPRING APPLICATION details in paginated format. Specifically for SPRING apps, NOT for general application queries.
                        ONLY returns details of applications using Spring dependencies.
                        Use for queries like 'Show me Spring Boot applications' or 'List all Spring applications'.
                        """)
        public List<JavaAppDetail> getPageableSpringApplicationInstanceDetails(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                List<JavaAppDetail> allDetails = snapshotApiClient.snapshotDetailAiSpringGet().getBody();
                return createPage(allDetails, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of dormant workload applications.
         */
        @Tool(name = "Snapshot/GetPageableDormantApplications", description = """
                        Retrieve INACTIVE applications not updated within specified days. REQUIRES
                        days parameter and ONLY returns dormant/inactive applications. Use for queries
                        like 'List applications not updated in 90 days'. NOT for listing active applications.
                        """)
        public List<AppDetail> getPageableDormantApplications(
                        @ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                Workloads dormantWorkloads = snapshotApiClient
                                .snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
                List<AppDetail> applications = dormantWorkloads != null ? dormantWorkloads.getApplications()
                                : Collections.emptyList();
                return createPage(applications, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of dormant workload service instances.
         */
        @Tool(name = "Snapshot/GetPageableDormantServiceInstances", description = """
                        Retrieve INACTIVE service instances not updated within specified days.
                        REQUIRES days parameter and ONLY returns dormant/inactive services. Use for
                        queries like 'List services not used in 90 days'. NOT for listing active services.
                        """)
        public List<ServiceInstanceDetail> getPageableDormantServiceInstances(
                        @ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                Workloads dormantWorkloads = snapshotApiClient
                                .snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
                List<ServiceInstanceDetail> serviceInstances = dormantWorkloads != null
                                ? dormantWorkloads.getServiceInstances()
                                : Collections.emptyList();
                return createPage(serviceInstances, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of dormant workload relationships.
         */
        @Tool(name = "Snapshot/GetPageableDormantRelationships", description = """
                        Retrieve INACTIVE application-service bindings not updated within specified
                        days. REQUIRES days parameter and ONLY returns dormant relationships. Use for
                        queries about stale connections, NOT for listing active app-service bindings.
                        """)
        public List<AppRelationship> getPageableDormantRelationships(
                        @ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                Workloads dormantWorkloads = snapshotApiClient
                                .snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
                List<AppRelationship> relationships = dormantWorkloads != null
                                ? dormantWorkloads.getApplicationRelationships()
                                : Collections.emptyList();
                return createPage(relationships, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of applications from the snapshot.
         */
        @Tool(name = "Snapshot/GetPageableApplications", description = """
                        Retrieve ALL applications without filtering, in paginated format. Use for
                        broad application listing queries like 'Show me all applications' or 'List
                        all apps'. When specific filtering is needed, use GetPageableFilteredApplications
                        instead.
                        """)
        public List<AppDetail> getPageableApplications(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<AppDetail> applications = snapshotDetail != null ? snapshotDetail.getApplications()
                                : Collections.emptyList();
                return createPage(applications, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of service instances from the snapshot.
         */
        @Tool(name = "Snapshot/GetPageableServiceInstances", description = """
                        Retrieve ALL service instances without filtering, in paginated format. Use
                        for broad service listing queries like 'Show me all services' or 'List all
                        service instances'. When specific filtering is needed, use
                        GetPageableFilteredServiceInstances instead.
                        """)
        public List<ServiceInstanceDetail> getPageableServiceInstances(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<ServiceInstanceDetail> serviceInstances = snapshotDetail != null
                                ? snapshotDetail.getServiceInstances()
                                : Collections.emptyList();
                return createPage(serviceInstances, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of application-service relationships from the snapshot.
         */
        @Tool(name = "Snapshot/GetPageableRelationships", description = """
                        Retrieve ALL application-service binding relationships without filtering, in
                        paginated format. Use for queries like 'Show all app-service connections'.
                        When filtering is needed, use GetPageableFilteredRelationships instead.
                        """)
        public List<AppRelationship> getPageableRelationships(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<AppRelationship> relationships = snapshotDetail != null
                                ? snapshotDetail.getApplicationRelationships()
                                : Collections.emptyList();
                return createPage(relationships, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of user accounts from the snapshot.
         */
        @Tool(name = "Snapshot/GetPageableUserAccounts", description = """
                        Retrieve ALL USER accounts (not service accounts) in paginated format. Use
                        for queries like 'List all users' or 'Show me all user accounts'. For service
                        accounts, use GetPageableServiceAccounts instead.
                        """)
        public List<String> getPageableUserAccounts(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<String> userAccounts = snapshotDetail != null ? snapshotDetail.getUserAccounts()
                                : Collections.emptyList();
                return createPage(userAccounts, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of service accounts from the snapshot.
         */
        @Tool(name = "Snapshot/GetPageableServiceAccounts", description = """
                        Retrieve ALL SERVICE accounts (not user accounts) in paginated format. Use
                        for queries like 'List all service accounts'. For regular user accounts, use
                        GetPageableUserAccounts instead.
                        """)
        public List<String> getPageableServiceAccounts(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<String> serviceAccounts = snapshotDetail != null ? snapshotDetail.getServiceAccounts()
                                : Collections.emptyList();
                return createPage(serviceAccounts, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of applications with a legacy stack.
         */
        @Tool(name = "Snapshot/GetPageableApplicationsWithLegacyStack", description = """
                        Retrieve applications using SPECIFIED LEGACY STACKS only. REQUIRES stack names
                        parameter. Use for queries like 'Show applications using cflinuxfs2 stack'.
                        NOT for querying all applications or those using current stacks.
                        """)
        public List<AppDetail> getPageableApplicationsWithLegacyStack(
                        @ToolParam(description = "Comma-separated list of stacks to filter by.") String stacks,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                Workloads legacyWorkloads = snapshotApiClient.snapshotDetailLegacyGet(stacks, "").getBody();
                List<AppDetail> applications = legacyWorkloads != null ? legacyWorkloads.getApplications()
                                : Collections.emptyList();
                return createPage(applications, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of service instances with a legacy service offering.
         */
        @Tool(name = "Snapshot/GetPageableServiceInstancesWithLegacyServiceOffering", description = """
                        Retrieve services using SPECIFIED LEGACY OFFERINGS only. REQUIRES service
                        offering names parameter. Use for queries like 'Show services using MySQL 5.7'.
                        NOT for querying all services or those using current offerings.
                        """)
        public List<ServiceInstanceDetail> getPageableServiceInstancesWithLegacyServiceOffering(
                        @ToolParam(description = "Comma-separated list of service offerings to filter by.") String serviceOfferings,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                Workloads legacyWorkloads = snapshotApiClient.snapshotDetailLegacyGet("", serviceOfferings).getBody();
                List<ServiceInstanceDetail> serviceInstances = legacyWorkloads != null
                                ? legacyWorkloads.getServiceInstances()
                                : Collections.emptyList();
                return createPage(serviceInstances, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of organizations.
         */
        @Tool(name = "Snapshot/GetPageableOrganizations", description = """
                        Retrieve ALL organizations without filtering, in paginated format. Use for
                        broad queries like 'List all organizations'. When organization name filtering
                        is needed, use GetPageableFilteredOrganizations instead.
                        """)
        public List<Organization> getPageableOrganizations(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                List<Organization> organizations = snapshotApiClient.snapshotOrganizationsGet().getBody();
                return createPage(organizations, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of spaces.
         */
        @Tool(name = "Snapshot/GetPageableSpaces", description = """
                        Retrieve ALL spaces across all organizations, in paginated format. Use for broad queries like 'List all spaces'.
                        When organization or space name filtering is needed, use GetPageableFilteredSpaces instead.
                        """)
        public List<Space> getPageableSpaces(
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                List<Space> spaces = snapshotApiClient.snapshotSpacesGet().getBody();
                return createPage(spaces, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable filtered list of organizations by name pattern.
         */
        @Tool(name = "Snapshot/GetPageableFilteredOrganizations", description = """
                        Retrieve organizations FILTERED BY NAME PATTERN only. REQUIRES name pattern parameter.
                        Use for queries like 'Find organizations with names containing kaizen'. NOT for listing all organizations.
                        """)
        public List<Organization> getPageableFilteredOrganizations(
                        @ToolParam(description = "Organization name pattern to filter by (case-insensitive).") String namePattern,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                List<Organization> allOrganizations = snapshotApiClient.snapshotOrganizationsGet().getBody();

                // Filter by name pattern if provided
                if (StringUtils.isNotBlank(namePattern) && allOrganizations != null) {
                        String pattern = namePattern.toLowerCase();
                        allOrganizations = allOrganizations.stream()
                                        .filter(org -> StringUtils.isNotBlank(org.getName())
                                                        && org.getName().toLowerCase().contains(pattern))
                                        .collect(Collectors.toList());
                }

                return createPage(allOrganizations, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable filtered list of spaces by organization and/or name pattern.
         */
        @Tool(name = "Snapshot/GetPageableFilteredSpaces", description = """
                        Retrieve spaces filtered by ORGANIZATION NAME and/or SPACE NAME PATTERN.
                        Use for queries like 'List spaces in the kaizen organization' or 'Find spaces with dev in their name'.
                        NOT for listing all spaces.
                        """)
        public List<Space> getPageableFilteredSpaces(
                        @ToolParam(required = false, description = "Organization name to filter by.") String organization,
                        @ToolParam(description = "Space name pattern to filter by (case-insensitive).") String namePattern,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                List<Space> allSpaces = snapshotApiClient.snapshotSpacesGet().getBody();

                // Apply filters
                if (allSpaces != null) {
                        if (StringUtils.isNotBlank(organization)) {
                                allSpaces = allSpaces.stream()
                                                .filter(s -> organization.equalsIgnoreCase(s.getOrganizationName()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(namePattern)) {
                                String pattern = namePattern.toLowerCase();
                                allSpaces = allSpaces.stream()
                                                .filter(s -> StringUtils.isNotBlank(s.getSpaceName())
                                                                && s.getSpaceName().toLowerCase().contains(pattern))
                                                .collect(Collectors.toList());
                        }
                }

                return createPage(allSpaces, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of filtered applications based on various criteria.
         */
        @Tool(name = "Snapshot/GetPageableFilteredApplications", description = """
                        Retrieve applications with MULTI-CRITERIA FILTERING. REQUIRES at least organization name parameter,
                        with optional space, name pattern, buildpack, or stack filters.
                        Use for queries like 'Show applications in the kaizen organization' or 'List Java apps in production space'.
                        """)
        public List<AppDetail> getPageableFilteredApplications(
                        @ToolParam(description = "Organization name to filter by.") String organization,
                        @ToolParam(required = false, description = "Space name to filter by.") String space,
                        @ToolParam(required = false, description = "Application name pattern to filter by (case-insensitive).") String namePattern,
                        @ToolParam(required = false, description = "Buildpack to filter by.") String buildpack,
                        @ToolParam(required = false, description = "Stack to filter by.") String stack,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<AppDetail> applications = snapshotDetail != null ? snapshotDetail.getApplications()
                                : Collections.emptyList();

                // Apply filters
                if (!CollectionUtils.isEmpty(applications)) {
                        if (StringUtils.isNotBlank(organization)) {
                                applications = applications.stream()
                                                .filter(app -> organization.equalsIgnoreCase(app.getOrganization()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(space)) {
                                applications = applications.stream()
                                                .filter(app -> space.equalsIgnoreCase(app.getSpace()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(namePattern)) {
                                String pattern = namePattern.toLowerCase();
                                applications = applications.stream()
                                                .filter(app -> StringUtils.isNotBlank(app.getAppName())
                                                                && app.getAppName().toLowerCase().contains(pattern))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(buildpack)) {
                                applications = applications.stream()
                                                .filter(app -> buildpack.equals(app.getBuildpack()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(stack)) {
                                applications = applications.stream()
                                                .filter(app -> stack.equals(app.getStack()))
                                                .collect(Collectors.toList());
                        }
                }

                return createPage(applications, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of filtered service instances based on various criteria.
         */
        @Tool(name = "Snapshot/GetPageableFilteredServiceInstances", description = """
                        Retrieve service instances with MULTI-CRITERIA FILTERING. REQUIRES at least organization name parameter,
                        with optional space, name pattern, service offering, or plan filters.
                        Use for queries like 'Show services in the kaizen organization' or 'List MySQL databases in production space'.
                        """)
        public List<ServiceInstanceDetail> getPageableFilteredServiceInstances(
                        @ToolParam(description = "Organization name to filter by.") String organization,
                        @ToolParam(required = false, description = "Space name to filter by.") String space,
                        @ToolParam(required = false, description = "Service instance name pattern to filter by (case-insensitive).") String namePattern,
                        @ToolParam(required = false, description = "Service offering to filter by.") String serviceOffering,
                        @ToolParam(required = false, description = "Service plan to filter by.") String servicePlan,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<ServiceInstanceDetail> serviceInstances = snapshotDetail != null
                                ? snapshotDetail.getServiceInstances()
                                : Collections.emptyList();

                // Apply filters
                if (!CollectionUtils.isEmpty(serviceInstances)) {
                        if (StringUtils.isNotBlank(organization)) {
                                serviceInstances = serviceInstances.stream()
                                                .filter(svc -> organization.equalsIgnoreCase(svc.getOrganization()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(space)) {
                                serviceInstances = serviceInstances.stream()
                                                .filter(svc -> space.equalsIgnoreCase(svc.getSpace()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(namePattern)) {
                                String pattern = namePattern.toLowerCase();
                                serviceInstances = serviceInstances.stream()
                                                .filter(svc -> StringUtils.isNotBlank(svc.getName())
                                                                && svc.getName().toLowerCase().contains(pattern))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(serviceOffering)) {
                                serviceInstances = serviceInstances.stream()
                                                .filter(svc -> serviceOffering.equalsIgnoreCase(svc.getService()))
                                                .collect(Collectors.toList());
                        }

                        if (StringUtils.isNotBlank(servicePlan)) {
                                serviceInstances = serviceInstances.stream()
                                                .filter(svc -> servicePlan.equalsIgnoreCase(svc.getPlan()))
                                                .collect(Collectors.toList());
                        }
                }

                return createPage(serviceInstances, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of application relationships filtered by application
         * name.
         */
        @Tool(name = "Snapshot/GetPageableFilteredRelationships", description = """
                        Retrieve application-service bindings FILTERED BY APPLICATION NAME only. REQUIRES application name parameter.
                        Use for queries like 'What services does the inventory app use?'
                        NOT for listing all relationships or filtering by service.
                        """)
        public List<AppRelationship> getPageableFilteredRelationships(
                        @ToolParam(description = "Application name to filter by.") String applicationName,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<AppRelationship> relationships = snapshotDetail != null
                                ? snapshotDetail.getApplicationRelationships()
                                : Collections.emptyList();

                // Apply filter
                if (!CollectionUtils.isEmpty(relationships)) {
                        if (StringUtils.isNotBlank(applicationName)) {
                                relationships = relationships.stream()
                                                .filter(rel -> applicationName.equalsIgnoreCase(rel.getAppName()))
                                                .collect(Collectors.toList());
                        }
                }

                return createPage(relationships, pageNumber, pageSize).getContent();
        }

        /**
         * Get a pageable list of filtered user accounts by name pattern.
         */
        @Tool(name = "Snapshot/GetPageableFilteredUserAccounts", description = """
                        Retrieve user accounts FILTERED BY NAME PATTERN only. REQUIRES name pattern parameter.
                        Use for queries like 'Find users with john in their name' or 'Search for users with gmail addresses'.
                        NOT for listing all users.
                        """)
        public List<String> getPageableFilteredUserAccounts(
                        @ToolParam(description = "User account name pattern to filter by (case-insensitive).") String namePattern,
                        @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
                        @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

                SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
                List<String> userAccounts = snapshotDetail != null ? snapshotDetail.getUserAccounts()
                                : Collections.emptyList();

                // Apply filter
                if (StringUtils.isNotBlank(namePattern) && !CollectionUtils.isEmpty(userAccounts)) {
                        String pattern = namePattern.toLowerCase();
                        userAccounts = userAccounts.stream()
                                        .filter(name -> name.toLowerCase().contains(pattern))
                                        .collect(Collectors.toList());
                }

                return createPage(userAccounts, pageNumber, pageSize).getContent();
        }

        /**
         * Helper method to create a page from a list.
         */
        private <T> Page<T> createPage(List<T> list, Integer pageNumber, Integer pageSize) {
                if (list == null) {
                        return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, DEFAULT_PAGE_SIZE), 0);
                }

                int effectivePageNumber = (pageNumber != null && pageNumber >= 0) ? pageNumber : 0;
                int effectivePageSize = (pageSize != null && pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;

                Pageable pageable = PageRequest.of(effectivePageNumber, effectivePageSize);

                int start = Math.min((int) pageable.getOffset(), list.size());
                int end = Math.min(start + pageable.getPageSize(), list.size());

                return new PageImpl<>(
                                list.subList(start, end),
                                pageable,
                                list.size());
        }
}
