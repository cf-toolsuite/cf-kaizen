package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.PoliciesApiClient;
import org.cftoolsuite.cfapp.butler.model.Policies;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PoliciesService {

    private final PoliciesApiClient policiesApiClient;

    public PoliciesService(PoliciesApiClient policiesApiClient) {
        this.policiesApiClient = policiesApiClient;
    }

    @Tool(name = "GetApplicationPolicyById", description = "(Butler) Get a specific application policy by ID.")
    public Policies getPolicyApplicationById(@ToolParam(description = "ID of the application policy.") String id) {
        return policiesApiClient.policiesApplicationIdGet(id).getBody();
    }

    @Tool(name = "GetEndpointPolicyById", description = "(Butler) Get a specific endpoint policy by ID.")
    public Policies getPolicyEndpointById(@ToolParam(description = "ID of the endpoint policy.") String id) {
        return policiesApiClient.policiesEndpointIdGet(id).getBody();
    }

    @Tool(name = "TriggerPolicyExecution", description = "(Butler) Trigger on-demand policy execution.")
    public void executePolicies() {
        policiesApiClient.policiesExecutePost();
    }

    @Tool(name = "ListAllPolicies", description = "(Butler) List all policies in effect.")
    public Policies getAllPolicies() {
        return policiesApiClient.policiesGet().getBody();
    }

    @Tool(name = "GetHygienePolicyById", description = "(Butler) Get a specific hygiene policy by ID.")
    public Policies getPolicyHygieneById(@ToolParam(description = "ID of the hygiene policy.") String id) {
        return policiesApiClient.policiesHygieneIdGet(id).getBody();
    }

    @Tool(name = "GetLegacyPolicyById", description = "(Butler) Get a specific legacy policy by ID.")
    public Policies getPolicyLegacyById(@ToolParam(description = "ID of the legacy policy.") String id) {
        return policiesApiClient.policiesLegacyIdGet(id).getBody();
    }

    @Tool(name = "GetQueryPolicyById", description = "(Butler) Get a specific query policy by ID.")
    public Policies getPolicyQueryById(@ToolParam(description = "ID of the query policy.") String id) {
        return policiesApiClient.policiesQueryIdGet(id).getBody();
    }

    @Tool(name = "RefreshPolicies", description = "(Butler) Refresh policies from Git repository.")
    public void refreshPolicies() {
        policiesApiClient.policiesRefreshPost();
    }

    @Tool(name = "GetHistoricalReportOfPolicyExecutions", description = "(Butler) Generate a historical report of policy executions.")
    public String getPoliciesReport(
            @ToolParam(description = "Start date for the report (YYYY-MM-DD).") LocalDate start,
            @ToolParam(description = "End date for the report (YYYY-MM-DD).") LocalDate end) {
        return policiesApiClient.policiesReportGet(start, end).getBody();
    }

    @Tool(name = "GetServiceInstancePolicyById", description = "(Butler) Get a specific service instance policy by ID.")
    public Policies getPolicyServiceInstanceById(@ToolParam(description = "ID of the service instance policy.") String id) {
        return policiesApiClient.policiesServiceInstanceIdGet(id).getBody();
    }
}