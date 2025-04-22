package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.restaurant.config.AppComponent;
import com.restaurant.config.DaggerAppComponent;
import com.restaurant.config.ServiceModule;
import com.restaurant.services.ReportsDispatchService;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.ResourceType;

import javax.inject.Inject;
import java.util.logging.Logger;

@DependsOn(resourceType = ResourceType.EVENTBRIDGE_RULE, name = "weekly-report-schedule")
@LambdaHandler(
        lambdaName = "report_sender",
        roleName = "report_sender-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "REGION", value = "${region}"),
        @EnvironmentVariable(key = "REPORTS_TABLE", value = "${reports_table}"),
        @EnvironmentVariable(key = "WAITERS_TABLE", value = "${waiter_table}"),
        @EnvironmentVariable(key = "WAITER_DAILY_STATS_TABLE", value = "${waiter_daily_stats_table}"),
        @EnvironmentVariable(key = "LOCATION_DAILY_STATS_TABLE", value = "${location_daily_stats_table}"),
        @EnvironmentVariable(key = "S3_BUCKET", value = "${reports_bucket}"),
        @EnvironmentVariable(key = "SES_SENDER_EMAIL", value = "${ses_sender_email}"),
        @EnvironmentVariable(key = "SES_RECEIVER_EMAIL", value = "${ses_receiver_email}")
})
public class ReportsSenderHandler implements RequestHandler<Object, Void> {

    private static final Logger logger = Logger.getLogger(ReportsSenderHandler.class.getName());

    @Inject
    ReportsDispatchService reportsDispatchService;

    public ReportsSenderHandler() {
        initDependencies();
    }

    private void initDependencies() {
        AppComponent appComponent = DaggerAppComponent.builder()
                .serviceModule(new ServiceModule())
                .build();
        appComponent.inject(this);
    }

    @Override
    public Void handleRequest(Object input, Context context) {
        logger.info("Triggered ReportsSenderHandler...");
        try {
            reportsDispatchService.generateAndSendReports(); // Handles generation, upload, SES, and saving metadata
            logger.info("Reports successfully generated and sent.");
        } catch (Exception e) {
            logger.severe("Error while generating reports: " + e.getMessage());
        }
        return null;
    }
}
