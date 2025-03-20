package org.cftoolsuite.cfapp.service.ai;

import org.cftoolsuite.cfapp.butler.api.OnDemandApiClient;
import org.cftoolsuite.cfapp.butler.model.Event;
import org.cftoolsuite.cfapp.butler.model.Resources;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OnDemandService {

    private final OnDemandApiClient onDemandApiClient;

    public OnDemandService(OnDemandApiClient onDemandApiClient) {
        this.onDemandApiClient = onDemandApiClient;
    }

    @Tool(description = "Get events for a specific entity.")
    public List<Event> getEventsById(
            @ToolParam(description = "ID of the entity to retrieve events for.") String id,
            @ToolParam(description = "Number of events to retrieve.") Integer numberOfEvents,
            @ToolParam(description = "Array of event types to filter by.") List<String> types) {
        return onDemandApiClient.eventsIdGet(id, numberOfEvents, types).getBody();
    }

//    @Tool(description = "Get metadata for resources of a specific type.")
//    public Resources getMetadataByType(
//            @ToolParam(description = "Type of resource metadata to retrieve (e.g., apps, services).") String type,
//            @ToolParam(description = "Label selector to filter resources.") String labelSelector,
//            @ToolParam(description = "Page number for pagination.") Integer page,
//            @ToolParam(description = "Number of resources per page.") Integer perPage) {
//        return onDemandApiClient.metadataTypeGet(type, labelSelector, page, perPage).getBody();
//    }
}