package org.cftoolsuite.cfapp.service.ai;

import org.apache.commons.lang3.StringUtils;
import org.cftoolsuite.cfapp.hoover.api.DefaultApiClient;
import org.cftoolsuite.cfapp.hoover.model.*;
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
public class PageableHooverService {

    private final DefaultApiClient hooverApiClient;
    private static final int DEFAULT_PAGE_SIZE = 10;

    public PageableHooverService(DefaultApiClient hooverApiClient) {
        this.hooverApiClient = hooverApiClient;
    }

    /**
     * Get a pageable list of Spring Boot application details.
     */
    @Tool(name = "SnapshotGetPageableSpringApplicationDetails", description = "Get pageable details of Spring Boot applications.")
    public Page<JavaAppDetail> getPageableSpringApplicationDetails(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<JavaAppDetail> allDetails = hooverApiClient.snapshotDetailAiSpringGet().getBody();
        return createPage(allDetails, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of applications from the snapshot.
     */
    @Tool(name = "SnapshotGetPageableApplications", description = "Get pageable list of all applications.")
    public Page<AppDetail> getPageableApplications(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
        List<AppDetail> applications = snapshotDetail != null ? snapshotDetail.getApplications() : Collections.emptyList();
        return createPage(applications, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of service instances from the snapshot.
     */
    @Tool(name = "SnapshotGetPageableServiceInstances", description = "Get pageable list of all service instances.")
    public Page<ServiceInstanceDetail> getPageableServiceInstances(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
        List<ServiceInstanceDetail> serviceInstances = snapshotDetail != null ? snapshotDetail.getServiceInstances() : Collections.emptyList();
        return createPage(serviceInstances, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of application-service relationships from the snapshot.
     */
    @Tool(name = "SnapshotGetPageableRelationships", description = "Get pageable list of all application-service relationships.")
    public Page<AppRelationship> getPageableRelationships(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
        List<AppRelationship> relationships = snapshotDetail != null ? snapshotDetail.getApplicationRelationships() : Collections.emptyList();
        return createPage(relationships, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of user accounts from the snapshot.
     */
    @Tool(name = "SnapshotGetPageableUserAccounts", description = "Get pageable list of all user accounts.")
    public Page<String> getPageableUserAccounts(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
        List<String> userAccounts = snapshotDetail != null ? snapshotDetail.getUserAccounts() : Collections.emptyList();
        return createPage(userAccounts, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of service accounts from the snapshot.
     */
    @Tool(name = "SnapshotGetPageableServiceAccounts", description = "Get pageable list of all service accounts.")
    public Page<String> getPageableServiceAccounts(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
        List<String> serviceAccounts = snapshotDetail != null ? snapshotDetail.getServiceAccounts() : Collections.emptyList();
        return createPage(serviceAccounts, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of organizations.
     */
    @Tool(name = "SnapshotGetPageableOrganizations", description = "Get pageable list of all organizations.")
    public Page<Organization> getPageableOrganizations(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<Organization> organizations = hooverApiClient.snapshotOrganizationsGet().getBody();
        return createPage(organizations, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of spaces.
     */
    @Tool(name = "SnapshotGetPageableSpaces", description = "Get pageable list of all spaces.")
    public Page<Space> getPageableSpaces(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<Space> spaces = hooverApiClient.snapshotSpacesGet().getBody();
        return createPage(spaces, pageNumber, pageSize);
    }

    /**
     * Get a pageable list of space users.
     */
    @Tool(name = "SnapshotGetPageableSpaceUsers", description = "Get pageable list of all space users.")
    public Page<SpaceUsers> getPageableSpaceUsers(
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<SpaceUsers> spaceUsers = hooverApiClient.snapshotSpacesUsersGet().getBody();
        return createPage(spaceUsers, pageNumber, pageSize);
    }

    /**
     * Get a pageable filtered list of organizations by name pattern.
     */
    @Tool(name = "SnapshotGetPageableFilteredOrganizations", description = "Get pageable list of organizations filtered by name pattern.")
    public Page<Organization> getPageableFilteredOrganizations(
            @ToolParam(description = "Organization name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<Organization> allOrganizations = hooverApiClient.snapshotOrganizationsGet().getBody();

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
    @Tool(name = "SnapshotGetPageableFilteredSpaces", description = "Get pageable list of spaces filtered by organization and/or name pattern.")
    public Page<Space> getPageableFilteredSpaces(
            @ToolParam(required = false, description = "Organization name to filter by.") String organization,
            @ToolParam(description = "Space name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<Space> allSpaces = hooverApiClient.snapshotSpacesGet().getBody();

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
     * Get a pageable filtered list of applications based on various criteria.
     */
    @Tool(name = "SnapshotGetPageableFilteredApplications", description = "Get pageable list of applications filtered by various criteria.")
    public Page<AppDetail> getPageableFilteredApplications(
            @ToolParam(description = "Organization name to filter by.") String organization,
            @ToolParam(required = false, description = "Space name to filter by.") String space,
            @ToolParam(required = false, description = "Application name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
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
        }

        return createPage(applications, pageNumber, pageSize);
    }

    /**
     * Get a pageable filtered list of Spring Boot applications based on various criteria.
     */
    @Tool(name = "SnapshotGetPageableFilteredSpringApplications", description = "Get pageable list of Spring Boot applications filtered by various criteria.")
    public Page<JavaAppDetail> getPageableFilteredSpringApplications(
            @ToolParam(description = "Organization name to filter by.") String organization,
            @ToolParam(required = false, description = "Space name to filter by.") String space,
            @ToolParam(required = false, description = "Application name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Dependency pattern to filter by (case-insensitive).") String dependencyPattern,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<JavaAppDetail> allJavaApps = hooverApiClient.snapshotDetailAiSpringGet().getBody();

        // Apply filters
        if (!CollectionUtils.isEmpty(allJavaApps)) {
            if (StringUtils.isNotBlank(organization)) {
                allJavaApps = allJavaApps.stream()
                        .filter(app -> organization.equalsIgnoreCase(app.getOrganization()))
                        .collect(Collectors.toList());
            }

            if (StringUtils.isNotBlank(space)) {
                allJavaApps = allJavaApps.stream()
                        .filter(app -> space.equalsIgnoreCase(app.getSpace()))
                        .collect(Collectors.toList());
            }

            if (StringUtils.isNotBlank(namePattern)) {
                String pattern = namePattern.toLowerCase();
                allJavaApps = allJavaApps.stream()
                        .filter(app -> StringUtils.isNotBlank(app.getAppName()) && app.getAppName().toLowerCase().contains(pattern))
                        .collect(Collectors.toList());
            }

            if (StringUtils.isNotBlank(dependencyPattern)) {
                String pattern = dependencyPattern.toLowerCase();
                allJavaApps = allJavaApps.stream()
                        .filter(app -> StringUtils.isNotBlank(app.getSpringDependencies()) &&
                                app.getSpringDependencies().toLowerCase().contains(pattern))
                        .collect(Collectors.toList());
            }
        }

        return createPage(allJavaApps, pageNumber, pageSize);
    }

    /**
     * Get a pageable filtered list of service instances based on various criteria.
     */
    @Tool(name = "SnapshotGetPageableFilteredServiceInstances", description = "Get pageable list of service instances filtered by various criteria.")
    public Page<ServiceInstanceDetail> getPageableFilteredServiceInstances(
            @ToolParam(description = "Organization name to filter by.") String organization,
            @ToolParam(required = false, description = "Space name to filter by.") String space,
            @ToolParam(required = false, description = "Service instance name pattern to filter by (case-insensitive).") String namePattern,
            @ToolParam(required = false, description = "Service offering to filter by.") String serviceOffering,
            @ToolParam(required = false, description = "Service plan to filter by.") String servicePlan,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        SnapshotDetail snapshotDetail = hooverApiClient.snapshotDetailGet().getBody();
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
     * Get a pageable filtered list of space users by foundation, organization, and/or space.
     */
    @Tool(name = "SnapshotGetPageableFilteredSpaceUsers", description = "Get pageable list of space users filtered by foundation, organization, and/or space.")
    public Page<SpaceUsers> getPageableFilteredSpaceUsers(
            @ToolParam(required = false, description = "Foundation name to filter by.") String foundation,
            @ToolParam(required = false, description = "Organization name to filter by.") String organization,
            @ToolParam(required = false, description = "Space name to filter by.") String space,
            @ToolParam(required = false, description = "Page number (zero-based).") Integer pageNumber,
            @ToolParam(required = false, description = "Page size (default is 10).") Integer pageSize) {

        List<SpaceUsers> allSpaceUsers = hooverApiClient.snapshotSpacesUsersGet().getBody();

        // Apply filters
        if (!CollectionUtils.isEmpty(allSpaceUsers)) {
            if (StringUtils.isNotBlank(foundation)) {
                allSpaceUsers = allSpaceUsers.stream()
                        .filter(su -> foundation.equalsIgnoreCase(su.getFoundation()))
                        .collect(Collectors.toList());
            }

            if (StringUtils.isNotBlank(organization)) {
                allSpaceUsers = allSpaceUsers.stream()
                        .filter(su -> organization.equalsIgnoreCase(su.getOrganization()))
                        .collect(Collectors.toList());
            }

            if (StringUtils.isNotBlank(space)) {
                allSpaceUsers = allSpaceUsers.stream()
                        .filter(su -> space.equalsIgnoreCase(su.getSpace()))
                        .collect(Collectors.toList());
            }
        }

        return createPage(allSpaceUsers, pageNumber, pageSize);
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
