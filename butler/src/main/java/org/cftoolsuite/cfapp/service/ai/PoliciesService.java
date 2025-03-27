package org.cftoolsuite.cfapp.service.ai;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.cftoolsuite.cfapp.butler.api.PoliciesApiClient;
import org.cftoolsuite.cfapp.butler.model.ApplicationPolicy;
import org.cftoolsuite.cfapp.butler.model.EndpointPolicy;
import org.cftoolsuite.cfapp.butler.model.HygienePolicy;
import org.cftoolsuite.cfapp.butler.model.LegacyPolicy;
import org.cftoolsuite.cfapp.butler.model.Policies;
import org.cftoolsuite.cfapp.butler.model.QueryPolicy;
import org.cftoolsuite.cfapp.butler.model.ServiceInstancePolicy;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

@Service
public class PoliciesService {

    private final PoliciesApiClient policiesApiClient;

    public PoliciesService(PoliciesApiClient policiesApiClient) {
        this.policiesApiClient = policiesApiClient;
    }

    @Tool(
        name = "PolicyGetApplicationPolicyById",
        description =
        """
        Get a specific application policy by ID.
        Application policies are useful when you want to take action on applications meeting certain criteria.
        There are four types of application policy: delete, stop, scale, and stack change.
        """
    )
    public ApplicationPolicy getApplicationPolicyById(@ToolParam(description = "ID of the application policy.") String id) {
        return Optional.ofNullable(policiesApiClient.policiesApplicationIdGet(id))
                .map(HttpEntity::getBody)
                .map(Policies::getApplicationPolicies)
                .filter(policies -> !policies.isEmpty())
                .map(List::getFirst)
                .orElse(null);
    }

    @Tool(
        name = "PolicyGetEndpointPolicyById",
        description =
        """
        Get a specific endpoint policy by ID.
        Endpoint policies are useful when you want to exercise any of the available GET endpoints and have the results
        sent to one or more designated email recipients.
        """
    )
    public EndpointPolicy getEndpointPolicyById(@ToolParam(description = "ID of the endpoint policy.") String id) {
        return Optional.ofNullable(policiesApiClient.policiesEndpointIdGet(id))
                .map(HttpEntity::getBody)
                .map(Policies::getEndpointPolicies)
                .filter(policies -> !policies.isEmpty())
                .map(List::getFirst)
                .orElse(null);
    }

    @Tool(name = "PolicyTriggerPolicyExecution", description = "Trigger policy execution.")
    public void executePolicies() {
        policiesApiClient.policiesExecutePost();
    }

    @Tool(
        name = "PolicyListAllPolicies",
        description =
        """
        List all policies in effect.  All policy configuration is returned.  Group each set of policies by policy type.
        """
    )
    public Policies getAllPolicies() {
        return policiesApiClient.policiesGet().getBody();
    }

    @Tool(
        name = "PolicyGetHygienePolicyById",
        description =
        """
        Get a specific hygiene policy by ID.
        Hygiene policies are useful when you want to search for and report on dormant workloads, notifying both an operator
        and for each workload the author and/or their space teammates.  Workloads are running applications and
        service instances that have not been updated in N or more days from the date/time of the policy execution.
        Note: hygiene policy configuration has a special case where if the days-since-last-update property value is set to -1,
        then ALL workloads (minus the blacklist) are included in the respective notifications.
        """
    )
    public HygienePolicy getHygienePolicyById(@ToolParam(description = "ID of the hygiene policy.") String id) {
        return Optional.ofNullable(policiesApiClient.policiesHygieneIdGet(id))
                .map(HttpEntity::getBody)
                .map(Policies::getHygienePolicies)
                .filter(policies -> !policies.isEmpty())
                .map(List::getFirst)
                .orElse(null);
    }

    @Tool(
        name = "PolicyGetLegacyPolicyById",
        description =
        """
        Get a specific legacy policy by ID.
        Legacy policies are useful when you want to search for and report on applications deployed to a legacy stack
        (e.g., windows2012R2, cflinuxfs2) or service offering (e.g., using a product slug name like p-config-server, p-service-registry, p-mysql),
        notifying both an operator and for each application, users with the space developer role.
        """
    )
    public LegacyPolicy getLegacyPolicyById(@ToolParam(description = "ID of the legacy policy.") String id) {
        return Optional.ofNullable(policiesApiClient.policiesLegacyIdGet(id))
                .map(HttpEntity::getBody)
                .map(Policies::getLegacyPolicies)
                .filter(policies -> !policies.isEmpty())
                .map(List::getFirst)
                .orElse(null);
    }

    @Tool(
        name = "PolicyGetQueryPolicyById",
        description =
        """
        Get a specific query policy by ID.
        Query policies are useful when you want to step out side the canned snapshot reporting capabilities and leverage
        the underlying schema to author one or more of your own queries and have the results delivered as comma-separated value attachments.
        """
    )
    public QueryPolicy getQueryPolicyById(@ToolParam(description = "ID of the query policy.") String id) {
        return Optional.ofNullable(policiesApiClient.policiesQueryIdGet(id))
                .map(HttpEntity::getBody)
                .map(Policies::getQueryPolicies)
                .filter(policies -> !policies.isEmpty())
                .map(List::getFirst)
                .orElse(null);
    }

    @Tool(name = "PolicyRefreshPolicies", description = "Refresh policies from Git repository.")
    public void refreshPolicies() {
        policiesApiClient.policiesRefreshPost();
    }

    @Tool(name = "PolicyGetHistoricalReportOfPolicyExecutions", description = "Generate a historical report of policy executions.")
    public String getPoliciesReport(
            @ToolParam(description = "Start date for the report (YYYY-MM-DD).") LocalDate start,
            @ToolParam(description = "End date for the report (YYYY-MM-DD).") LocalDate end) {
        return policiesApiClient.policiesReportGet(start, end).getBody();
    }

    @Tool(
        name = "PolicyGetServiceInstancePolicyById",
        description =
        """
        Get a specific service instance policy by ID.
        A Service instance policy has one purpose, to delete service instances that meet certain criteria.
        """
    )
    public ServiceInstancePolicy getPolicyServiceInstanceById(@ToolParam(description = "ID of the service instance policy.") String id) {
        return Optional.ofNullable(policiesApiClient.policiesServiceInstanceIdGet(id))
                .map(HttpEntity::getBody)
                .map(Policies::getServiceInstancePolicies)
                .filter(policies -> !policies.isEmpty())
                .map(List::getFirst)
                .orElse(null);
    }
}