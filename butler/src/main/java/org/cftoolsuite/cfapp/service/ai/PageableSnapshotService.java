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
import java.util.Map;
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
    @Tool(name = "GetPageableSpringApplicationDetails", description = "(Butler) Get pageable details of Spring applications.")
    public Page<Map<String, String>> getPageableSpringApplicationInstanceDetails(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        List<Map<String, String>> allDetails = snapshotApiClient.snapshotDetailAiSpringGet().getBody();
        return createPage(allDetails, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of dormant workload applications.
     */
    @Tool(name = "GetPageableDormantApplications", description = "(Butler) Get pageable list of dormant applications.")
    public Page<AppDetail> getPageableDormantApplications(
            @ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        Workloads dormantWorkloads = snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
        List<AppDetail> applications = dormantWorkloads != null ? dormantWorkloads.getApplications() : Collections.emptyList();
        return createPage(applications, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of dormant workload service instances.
     */
    @Tool(name = "GetPageableDormantServiceInstances", description = "(Butler) Get pageable list of dormant service instances.")
    public Page<ServiceInstanceDetail> getPageableDormantServiceInstances(
            @ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        Workloads dormantWorkloads = snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
        List<ServiceInstanceDetail> serviceInstances = dormantWorkloads != null ? dormantWorkloads.getServiceInstances() : Collections.emptyList();
        return createPage(serviceInstances, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of dormant workload relationships.
     */
    @Tool(name = "GetPageableDormantRelationships", description = "(Butler) Get pageable list of dormant application-service relationships.")
    public Page<AppRelationship> getPageableDormantRelationships(
            @ToolParam(description = "Number of days since the last update to consider workloads dormant.") Integer daysSinceLastUpdate,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        Workloads dormantWorkloads = snapshotApiClient.snapshotDetailDormantDaysSinceLastUpdateGet(daysSinceLastUpdate).getBody();
        List<AppRelationship> relationships = dormantWorkloads != null ? dormantWorkloads.getApplicationRelationships() : Collections.emptyList();
        return createPage(relationships, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of applications from the snapshot.
     */
    @Tool(name = "GetPageableApplications", description = "(Butler) Get pageable list of all applications.")
    public Page<AppDetail> getPageableApplications(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<AppDetail> applications = snapshotDetail != null ? snapshotDetail.getApplications() : Collections.emptyList();
        return createPage(applications, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of service instances from the snapshot.
     */
    @Tool(name = "GetPageableServiceInstances", description = "(Butler) Get pageable list of all service instances.")
    public Page<ServiceInstanceDetail> getPageableServiceInstances(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<ServiceInstanceDetail> serviceInstances = snapshotDetail != null ? snapshotDetail.getServiceInstances() : Collections.emptyList();
        return createPage(serviceInstances, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of application-service relationships from the snapshot.
     */
    @Tool(name = "GetPageableRelationships", description = "(Butler) Get pageable list of all application-service relationships.")
    public Page<AppRelationship> getPageableRelationships(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<AppRelationship> relationships = snapshotDetail != null ? snapshotDetail.getApplicationRelationships() : Collections.emptyList();
        return createPage(relationships, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of user accounts from the snapshot.
     */
    @Tool(name = "GetPageableUserAccounts", description = "(Butler) Get pageable list of all user accounts.")
    public Page<String> getPageableUserAccounts(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<String> userAccounts = snapshotDetail != null ? snapshotDetail.getUserAccounts() : Collections.emptyList();
        return createPage(userAccounts, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of service accounts from the snapshot.
     */
    @Tool(name = "GetPageableServiceAccounts", description = "(Butler) Get pageable list of all service accounts.")
    public Page<String> getPageableServiceAccounts(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<String> serviceAccounts = snapshotDetail != null ? snapshotDetail.getServiceAccounts() : Collections.emptyList();
        return createPage(serviceAccounts, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of applications with a legacy stack.
     */
    @Tool(name = "GetPageableApplicationsWithLegacyStack", description = "(Butler) Get pageable list of applications with legacy stack.")
    public Page<AppDetail> getPageableApplicationsWithLegacyStack(
            @ToolParam(description = "Comma-separated list of stacks to filter by.") String stacks,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        Workloads legacyWorkloads = snapshotApiClient.snapshotDetailLegacyGet(stacks, "").getBody();
        List<AppDetail> applications = legacyWorkloads != null ? legacyWorkloads.getApplications() : Collections.emptyList();
        return createPage(applications, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of service instances with a legacy service offering.
     */
    @Tool(name = "GetPageableServiceInstancesWithLegacyServiceOffering", description = "(Butler) Get pageable list of service instances with legacy service offering.")
    public Page<ServiceInstanceDetail> getPageableServiceInstancesWithLegacyServiceOffering(
            @ToolParam(description = "Comma-separated list of service offerings to filter by.") String serviceOfferings,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        Workloads legacyWorkloads = snapshotApiClient.snapshotDetailLegacyGet("", serviceOfferings).getBody();
        List<ServiceInstanceDetail> serviceInstances = legacyWorkloads != null ? legacyWorkloads.getServiceInstances() : Collections.emptyList();
        return createPage(serviceInstances, pageNumber, pageSize);
    }


    /**
     * Get a pageable list of organizations.
     */
    @Tool(name = "GetPageableOrganizations", description = "(Butler) Get pageable list of all organizations.")
    public Page<Organization> getPageableOrganizations(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        List<Organization> organizations = snapshotApiClient.snapshotOrganizationsGet().getBody();
        return createPage(organizations, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of spaces.
     */
    @Tool(name = "GetPageableSpaces", description = "(Butler) Get pageable list of all spaces.")
    public Page<Space> getPageableSpaces(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        List<Space> spaces = snapshotApiClient.snapshotSpacesGet().getBody();
        return createPage(spaces, pageNumber, pageSize);
    }

    /**
     * Get a pageable filtered list of organizations by name pattern.
     */
    @Tool(name = "GetPageableFilteredOrganizations", description = "(Butler) Get pageable list of organizations filtered by name pattern.")
    public Page<Organization> getPageableFilteredOrganizations(
            @ToolParam(description = "Organization name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        List<Organization> allOrganizations = snapshotApiClient.snapshotOrganizationsGet().getBody();
        
        // Filter by name pattern if provided
        if (StringUtils.isNotBlank(namePattern) && allOrganizations != null) {
            String pattern = namePattern.toLowerCase();
            allOrganizations = allOrganizations.stream()
                    .filter(org -> StringUtils.isNotBlank(org.getName()) && org.getName().toLowerCase().contains(pattern))
                    .collect(Collectors.toList());
        }
        
        return createPage(allOrganizations, pageNumber, pageSize);
    }

    /**
     * Get a pageable filtered list of spaces by organization and/or name pattern.
     */
    @Tool(name = "GetPageableFilteredSpaces", description = "(Butler) Get pageable list of spaces filtered by organization and/or name pattern.")
    public Page<Space> getPageableFilteredSpaces(
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
                        .filter(s -> StringUtils.isNotBlank(s.getSpaceName()) && s.getSpaceName().toLowerCase().contains(pattern))
                        .collect(Collectors.toList());
            }
        }
        
        return createPage(allSpaces, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of filtered applications based on various criteria.
     */
    @Tool(name = "GetPageableFilteredApplications", description = "(Butler) Get pageable list of applications filtered by various criteria.")
    public Page<AppDetail> getPageableFilteredApplications(
            @ToolParam(description = "Organization name to filter by.") String organization,
            @ToolParam(required = false, description = "Space name to filter by.") String space,
            @ToolParam(required = false, description = "Application name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Buildpack to filter by.") String buildpack,
            @ToolParam(required = false, description = "Stack to filter by.") String stack,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<AppDetail> applications = snapshotDetail != null ? snapshotDetail.getApplications() : Collections.emptyList();
        
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
                        .filter(app -> StringUtils.isNotBlank(app.getAppName()) && app.getAppName().toLowerCase().contains(pattern))
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
        
        return createPage(applications, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of filtered service instances based on various criteria.
     */
    @Tool(name = "GetPageableFilteredServiceInstances", description = "(Butler) Get pageable list of service instances filtered by various criteria.")
    public Page<ServiceInstanceDetail> getPageableFilteredServiceInstances(
            @ToolParam(description = "Organization name to filter by.") String organization,
            @ToolParam(required = false, description = "Space name to filter by.") String space,
            @ToolParam(required = false, description = "Service instance name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Service offering to filter by.") String serviceOffering,
            @ToolParam(required = false, description = "Service plan to filter by.") String servicePlan,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<ServiceInstanceDetail> serviceInstances = snapshotDetail != null ? snapshotDetail.getServiceInstances() : Collections.emptyList();
        
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
                        .filter(svc -> StringUtils.isNotBlank(svc.getName()) && svc.getName().toLowerCase().contains(pattern))
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
        
        return createPage(serviceInstances, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of application relationships filtered by application name.
     */
    @Tool(name = "GetPageableFilteredRelationships", description = "(Butler) Get pageable list of relationships filtered by application name.")
    public Page<AppRelationship> getPageableFilteredRelationships(
            @ToolParam(description = "Application name to filter by.") String applicationName,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<AppRelationship> relationships = snapshotDetail != null ? snapshotDetail.getApplicationRelationships() : Collections.emptyList();
        
        // Apply filter
        if (!CollectionUtils.isEmpty(relationships)) {
            if (StringUtils.isNotBlank(applicationName)) {
                relationships = relationships.stream()
                        .filter(rel -> applicationName.equalsIgnoreCase(rel.getAppName()))
                        .collect(Collectors.toList());
            }
        }
        
        return createPage(relationships, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of filtered user accounts by name pattern.
     */
    @Tool(name = "GetPageableFilteredUserAccounts", description = "(Butler) Get pageable list of user accounts filtered by name pattern.")
    public Page<String> getPageableFilteredUserAccounts(
            @ToolParam(description = "User account name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {
        
        SnapshotDetail snapshotDetail = snapshotApiClient.snapshotDetailGet().getBody();
        List<String> userAccounts = snapshotDetail != null ? snapshotDetail.getUserAccounts() : Collections.emptyList();
        
        // Apply filter
        if (StringUtils.isNotBlank(namePattern) && !CollectionUtils.isEmpty(userAccounts)) {
            String pattern = namePattern.toLowerCase();
            userAccounts = userAccounts.stream()
                    .filter(name -> name.toLowerCase().contains(pattern))
                    .collect(Collectors.toList());
        }
        
        return createPage(userAccounts, pageNumber, pageSize);
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
                list.size()
        );
    }
}
