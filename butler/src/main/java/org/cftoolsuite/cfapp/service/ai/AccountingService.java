package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.AccountingApiClient;
import org.cftoolsuite.cfapp.butler.model.AppUsageReport;
import org.cftoolsuite.cfapp.butler.model.ServiceUsageReport;
import org.cftoolsuite.cfapp.butler.model.TaskUsageReport;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AccountingService {

    private final AccountingApiClient accountingApiClient;

    public AccountingService(AccountingApiClient accountingApiClient) {
        this.accountingApiClient = accountingApiClient;
    }

    @Tool(name = "GetApplicationUsage", description = "(Butler) Get system-wide application usage report.")
    public AppUsageReport getAccountingApplicationsReport() {
        return accountingApiClient.accountingApplicationsGet().getBody();
    }

    @Tool(name = "GetFilterableApplicationUsage", description = "(Butler) Get application usage report for an organization within a date range.")
    public String getAccountingApplicationsOrgReport(
            @ToolParam(description = "Organization name.") String orgName,
            @ToolParam(description = "Start date (YYYY-MM-DD).") LocalDate startDate,
            @ToolParam(description = "End date (YYYY-MM-DD).") LocalDate endDate) {
        return accountingApiClient.accountingApplicationsOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(name = "GetServiceUsage", description = "(Butler) Get system-wide service usage report.")
    public ServiceUsageReport getAccountingServicesReport() {
        return accountingApiClient.accountingServicesGet().getBody();
    }

    @Tool(name = "GetFilterableServiceUsage", description = "(Butler) Get service usage report for an organization within a date range.")
    public String getAccountingServicesOrgReport(
            @ToolParam(description = "Organization name.") String orgName,
            @ToolParam(description = "Start date (YYYY-MM-DD).") LocalDate startDate,
            @ToolParam(description = "End date (YYYY-MM-DD).") LocalDate endDate) {
        return accountingApiClient.accountingServicesOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(name = "GetTaskUsage", description = "(Butler) Get system-wide task usage report.")
    public TaskUsageReport getAccountingTasksReport() {
        return accountingApiClient.accountingTasksGet().getBody();
    }

    @Tool(name = "GetFilterableTaskUsage", description = "(Butler) Get task usage report for an organization within a date range.")
    public String getAccountingTasksOrgReport(
            @ToolParam(description = "Organization name.") String orgName,
            @ToolParam(description = "Start date (YYYY-MM-DD).") LocalDate startDate,
            @ToolParam(description = "End date (YYYY-MM-DD).") LocalDate endDate) {
        return accountingApiClient.accountingTasksOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }
}