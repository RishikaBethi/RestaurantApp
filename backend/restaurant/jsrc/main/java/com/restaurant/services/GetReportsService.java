package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.dto.ReportResponseDTO;
import org.json.JSONArray;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;

import static com.restaurant.utils.Helper.*;

public class GetReportsService {
    private static final Logger logger = Logger.getLogger(GetReportsService.class.getName());
    private final Table reportsTable;
    private final DynamoDB dynamoDB;

    @Inject
    public GetReportsService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reportsTable = dynamoDB.getTable(System.getenv("REPORTS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleGetReports(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, String> queryParams = request.getQueryStringParameters() != null ? request.getQueryStringParameters() : new HashMap<>();
            String locationIdFilter = queryParams.getOrDefault("locationId", null);
            String waiterIdFilter = queryParams.getOrDefault("waiterId", null);
            String fromDate = queryParams.getOrDefault("fromDate", null);
            String toDate = queryParams.getOrDefault("toDate", null);

            List<String> filterExpressions = new ArrayList<>();
            ValueMap valueMap = new ValueMap();

            if (locationIdFilter != null) {
                filterExpressions.add("locationId = :loc");
                valueMap.withString(":loc", locationIdFilter);
            }

            if (waiterIdFilter != null) {
                filterExpressions.add("waiterId = :waiter");
                valueMap.withString(":waiter", waiterIdFilter);
            }

            if (fromDate != null && toDate != null) {
                filterExpressions.add("startPeriod >= :from AND endPeriod <= :to");
                valueMap.withString(":from", fromDate).withString(":to", toDate);
            } else if (fromDate != null) {
                filterExpressions.add("startPeriod >= :from");
                valueMap.withString(":from", fromDate);
            } else if (toDate != null) {
                filterExpressions.add("endPeriod <= :to");
                valueMap.withString(":to", toDate);
            }

            ScanSpec scanSpec = new ScanSpec();
            if (!filterExpressions.isEmpty()) {
                String filterExpr = String.join(" AND ", filterExpressions);
                scanSpec.withFilterExpression(filterExpr).withValueMap(valueMap);
            }

            ItemCollection<ScanOutcome> items = reportsTable.scan(scanSpec);
            List<ReportResponseDTO> reports = new ArrayList<>();
            for (Item item : items) {
                reports.add(new ReportResponseDTO(item));
            }

            if (reports.isEmpty()) {
                return createErrorResponse(404, "No reports found matching the provided filters.");
            }

            JSONArray jsonArray = new JSONArray();
            for (ReportResponseDTO dto : reports) {
                jsonArray.put(dto.toJson());
            }

            return createApiResponse(200, jsonArray);
        } catch (Exception e) {
            logger.severe("Error fetching reports: " + e.getMessage());
            return createErrorResponse(500, "Server error: " + e.getMessage());
        }
    }

}