package com.restaurant.services;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import java.util.logging.Logger;

public class NotificationService {
    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    private final AmazonSNS snsClient;
    private final String snsTopicArn;

    public NotificationService(AmazonSNS snsClient, String snsTopicArn) {
        this.snsClient = snsClient;
        this.snsTopicArn = snsTopicArn;
    }

    public String sendNotification(String message, String subject) {
        try {
            PublishRequest publishRequest = new PublishRequest(snsTopicArn, message, subject);
            PublishResult result = snsClient.publish(publishRequest);
            logger.info("Notification sent. Message ID: " + result.getMessageId());
            return "Message Sent! Message ID: " + result.getMessageId();
        } catch (Exception e) {
            logger.severe("Failed to send notification: " + e.getMessage());
            return "Failed to send notification.";
        }
    }
}
