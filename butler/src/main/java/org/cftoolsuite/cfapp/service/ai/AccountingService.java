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

    @Tool(name = "AccountingGetApplicationUsage", description =
            """
            Retrieves historical system-wide application usage metrics for a foundation.
            Two variations of reports are available:
            - Yearly Application Usage
              - This report contains columns for year, average number of application instances, maximum (or peak) number
                of application instances, and total application instance hours.
            - Monthly Application Usage
              - This report contains columns for year, month, average number of application instances, maximum (or peak)
                number of application instances, and total application instance hours.
            Use this to analyze platform-wide resource utilization trends, or generate billing reports for chargeback purposes.
            """
    )
    public AppUsageReport getAccountingApplicationsReport() {
        return accountingApiClient.accountingApplicationsGet().getBody();
    }

    @Tool(name = "AccountingGetFilterableApplicationUsage", description =
            """
            Retrieves targeted application usage report for a specific organization within a custom date range.
            Returns detailed consumption metrics including:
            - organization guid
            - period start
            - period end
            - usage details per application including:
              - space guid
              - application name
              - instance count
              - memory (in MB) per application instance
              - uptime duration (in seconds)
            Use this for organization-specific billing, resource planning, or compliance reporting.
            Requires organization name and precise date range parameters (in YYYY-MM-DD format).
            Results include daily breakdowns of resource utilization for accurate time-based analysis.
            """
    )
    public String getAccountingApplicationsOrgReport(
            @ToolParam(description = "Organization name to filter usage data") String orgName,
            @ToolParam(description = "Start date of reporting period (YYYY-MM-DD)") LocalDate startDate,
            @ToolParam(description = "End date of reporting period (YYYY-MM-DD)") LocalDate endDate) {
        return accountingApiClient.accountingApplicationsOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(name = "AccountingGetServiceUsage", description =
            """
            Retrieves historical system-wide service instance usage metrics for a foundation.
            Two variations of reports are available:
            - Service Usage Monthly Aggregate
              - Then, two sub-reporting breakdowns are available:
                - By service, which includes: year, month, duration (in hours), average number of service instances,
                  maximum (or peak) number or service instances
                - By Plan, which includes: service plan guid, service plan name, year, month, duration (in hours),
                  average number of service instances, maximum (or peak) number or service instances
            - Service Usage Yearly Aggregate
              - Then, two sub-reporting breakdowns are available:
                - By service, which includes: year, duration (in hours), average number of service instances,
                  maximum (or peak) number or service instances
                - By Plan, which includes: service plan guid, service plan name, year, duration (in hours), average number
                  of service instances, maximum (or peak) number or service instances
            Use this to analyze service adoption patterns, manage service quotas, or monitor marketplace utilization.
            """
    )
    public ServiceUsageReport getAccountingServicesReport() {
        return accountingApiClient.accountingServicesGet().getBody();
    }

    @Tool(name = "AccountingGetFilterableServiceUsage", description =
            """
            Retrieves targeted service usage report for a specific organization within a custom date range.
            Returns detailed consumption metrics including:
            - organization guid
            - period start
            - period end
            - usage details per service including:
              - space name
              - service guid
              - service instance name
              - service instance type
              - service name
              - service plan name
              - service instance creation timestamp
              - service instance deletion timestamp
              - uptime duration (in seconds)
              - deleted (boolean)
            Use this for organization-specific billing, resource planning, or compliance reporting.
            Requires organization name and precise date range parameters (in YYYY-MM-DD format).
            Results include daily service plan details and resource allocation metrics for cost analysis.
            """
    )
    public String getAccountingServicesOrgReport(
            @ToolParam(description = "Organization name to filter service usage data") String orgName,
            @ToolParam(description = "Start date of reporting period (YYYY-MM-DD)") LocalDate startDate,
            @ToolParam(description = "End date of reporting period (YYYY-MM-DD)") LocalDate endDate) {
        return accountingApiClient.accountingServicesOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }

    @Tool(name = "AccountingGetTaskUsage", description =
            """
            Retrieves historical system-wide task usage metrics for a foundation.
            Two variations of reports are available:
            - Yearly Task Usage
              - This report contains columns for year, total task runs, maximum (or peak) concurrent tasks, and task hours
            - Monthly Task Usage
              - This report contains columns for year, month, total task runs, maximum (or peak) concurrent tasks, and task hours
            Use this to analyze platform-wide resource utilization trends, or generate billing reports for chargeback purposes.
            """
    )
    public TaskUsageReport getAccountingTasksReport() {
        return accountingApiClient.accountingTasksGet().getBody();
    }

    @Tool(name = "AccountingGetFilterableTaskUsage", description =
            """
            Retrieves targeted task usage metrics for a specific organization within a custom date range.
            Returns detailed consumption metrics including:
            - organization guid
            - period start
            - period end
            - spaces
              - name
              - task summaries
                - memory consumed (in MB) per task instance
                - task occurrences
                - task total duration (in seconds)
            Use this for organization-specific task auditing, resource planning, or performance analysis.
            Requires organization name and precise date range parameters (YYYY-MM-DD format).
            Results include daily task executions and status details for operational reporting.
            """
    )
    public String getAccountingTasksOrgReport(
            @ToolParam(description = "Organization name to filter task usage data") String orgName,
            @ToolParam(description = "Start date of reporting period (YYYY-MM-DD)") LocalDate startDate,
            @ToolParam(description = "End date of reporting period (YYYY-MM-DD)") LocalDate endDate) {
        return accountingApiClient.accountingTasksOrgNameStartDateEndDateGet(orgName, startDate, endDate).getBody();
    }
}