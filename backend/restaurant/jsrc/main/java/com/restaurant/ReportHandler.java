package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.restaurant.config.DaggerAppComponent;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.restaurant.config.AppComponent;
import com.restaurant.config.ServiceModule;
import com.restaurant.services.ReportService;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.ResourceType;

import javax.inject.Inject;
import java.util.logging.Logger;

@DependsOn(resourceType = ResourceType.SQS_QUEUE, name = "report-updates-queue")
@LambdaHandler(
        lambdaName = "report_handler",
        roleName = "report_handler-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "REGION", value = "${region}"),
        @EnvironmentVariable(key = "REPORTS_TABLE", value = "${reports_table}"),
        @EnvironmentVariable(key = "SQS_QUEUE", value = "${sqs}"),
        @EnvironmentVariable(key = "WAITER_DAILY_STATS_TABLE", value = "${waiter_daily_stats_table}"),
        @EnvironmentVariable(key = "LOCATION_DAILY_STATS_TABLE", value = "${location_daily_stats_table}")
})

@SqsTriggerEventSource(targetQueue = "report-updates-queue", batchSize = 5)
public class ReportHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = Logger.getLogger(ReportHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    ReportService reportService;

    public ReportHandler() {
        initDependencies();
    }

    private void initDependencies() {
        AppComponent appComponent = DaggerAppComponent.builder()
                .serviceModule(new ServiceModule())
                .build();
        appComponent.inject(this);
    }

    public void handleEvent(JsonNode event) {
        String eventType = event.get("eventType").asText(); // "OrderFinished" or "FeedbackGiven"

        if ("OrderFinished".equals(eventType)) {
            reportService.processOrderFinished(event);
        } else if ("FeedbackGiven".equals(eventType)) {
            reportService.processFeedbackGiven(event);
        }
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        logger.info("Received SQS event with " + event.getRecords().size() + " message(s).");
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                String body = message.getBody();
                logger.info("Processing SQS message: " + body);
                JsonNode eventNode = objectMapper.readTree(body);
                handleEvent(eventNode);
            } catch (Exception e) {
                logger.severe("Error while processing SQS message: " + e.getMessage());
                // Optionally: send to DLQ or log failed message for retry
            }
        }
        return null;
    }
}
