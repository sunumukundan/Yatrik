package com.fleetapp.reporting.client;

import com.fleetapp.reporting.web.dto.FleetSummaryResponse;
import com.fleetapp.reporting.web.dto.WorkOrderReportResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Reads the fleet service's aggregate views.
 */
@Component
public class FleetClient extends ServiceClient {

    public FleetClient(@Value("${app.fleet.base-url}") String baseUrl) {
        super(baseUrl);
    }

    public FleetSummaryResponse summary(int dueSoonDays) {
        return get("/fleet/summary", Map.of("dueSoonDays", dueSoonDays), FleetSummaryResponse.class);
    }

    /** The vehicles currently off the road, with the work that put them there. */
    public List<WorkOrderReportResponse> openWorkOrders() {
        WorkOrderReportResponse[] orders = get("/work-orders/open", Map.of(),
                WorkOrderReportResponse[].class);
        return orders == null ? List.of() : List.of(orders);
    }
}
