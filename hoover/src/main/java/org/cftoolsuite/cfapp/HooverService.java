package org.cftoolsuite.cfapp;

import org.cftoolsuite.cfapp.hoover.api.DefaultApiClient;
import org.cftoolsuite.cfapp.hoover.model.AppUsageReport;
import org.cftoolsuite.cfapp.hoover.model.Demographics;
import org.cftoolsuite.cfapp.hoover.model.JavaAppDetail;
import org.cftoolsuite.cfapp.hoover.model.Organization;
import org.cftoolsuite.cfapp.hoover.model.ServiceUsageReport;
import org.cftoolsuite.cfapp.hoover.model.SnapshotDetail;
import org.cftoolsuite.cfapp.hoover.model.SnapshotSummary;
import org.cftoolsuite.cfapp.hoover.model.Space;
import org.cftoolsuite.cfapp.hoover.model.SpaceUsers;
import org.cftoolsuite.cfapp.hoover.model.TaskUsageReport;
import org.cftoolsuite.cfapp.hoover.model.TimeKeepers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HooverService {

    private final DefaultApiClient hooverApiClient;

    public HooverService(DefaultApiClient hooverApiClient) {
        this.hooverApiClient = hooverApiClient;
    }

    public AppUsageReport accountingApplicationsGet() {
        return hooverApiClient.accountingApplicationsGet().getBody();
    }

    public ServiceUsageReport accountingServicesGet() {
        return hooverApiClient.accountingServicesGet().getBody();
    }

    public TaskUsageReport accountingTasksGet() {
        return hooverApiClient.accountingTasksGet().getBody();
    }

    public TimeKeepers collectGet() {
        return hooverApiClient.collectGet().getBody();
    }

    public Demographics snapshotDemographicsGet() {
        return hooverApiClient.snapshotDemographicsGet().getBody();
    }

    public String snapshotDetailAiGet() {
        return hooverApiClient.snapshotDetailAiGet().getBody();
    }

    public List<JavaAppDetail> snapshotDetailAiSpringGet() {
        return hooverApiClient.snapshotDetailAiSpringGet().getBody();
    }

    public SnapshotDetail snapshotDetailGet() {
        return hooverApiClient.snapshotDetailGet().getBody();
    }

    public String snapshotDetailSiGet() {
        return hooverApiClient.snapshotDetailSiGet().getBody();
    }

    public SpaceUsers snapshotFoundationOrganizationSpaceUsersGet(String foundation, String organization, String space) {
        return hooverApiClient.snapshotFoundationOrganizationSpaceUsersGet(foundation, organization, space).getBody();
    }

    public List<Organization> snapshotOrganizationsGet() {
        return hooverApiClient.snapshotOrganizationsGet().getBody();
    }

    public List<Space> snapshotSpacesGet() {
        return hooverApiClient.snapshotSpacesGet().getBody();
    }

    public List<SpaceUsers> snapshotSpacesUsersGet() {
        return hooverApiClient.snapshotSpacesUsersGet().getBody();
    }

    public Map<String, Integer> snapshotSummaryAiSpringGet() {
        return hooverApiClient.snapshotSummaryAiSpringGet().getBody();
    }

    public SnapshotSummary snapshotSummaryGet() {
        return hooverApiClient.snapshotSummaryGet().getBody();
    }

    public Long snapshotUsersCountGet() {
        return hooverApiClient.snapshotUsersCountGet().getBody();
    }

    public List<String> snapshotUsersGet() {
        return hooverApiClient.snapshotUsersGet().getBody();
    }
}