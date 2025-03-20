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
}