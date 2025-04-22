package com.restaurant.services;

import jakarta.mail.*;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.internet.*;

import java.nio.ByteBuffer;
import java.util.Properties;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReportsDispatchService {

    private static final Logger logger = Logger.getLogger(ReportsDispatchService.class.getName());

    private final DynamoDB dynamoDB;
    private final AmazonS3 s3Client;
    private final SesClient sesClient;

    private final Table waiterStatsTable;
    private final Table locationStatsTable;
    private final Table reportsTable;
    private final Table waitersTable;

    private static final String WAITER_DAILY_STATS_TABLE = System.getenv("WAITER_DAILY_STATS_TABLE");
    private static final String LOCATION_DAILY_STATS_TABLE = System.getenv("LOCATION_DAILY_STATS_TABLE");
    private static final String REPORTS_TABLE = System.getenv("REPORTS_TABLE");
    private static final String WAITERS_TABLE = System.getenv("WAITERS_TABLE");
    private static final String S3_BUCKET = System.getenv("S3_BUCKET");
    private static final String SES_FROM_EMAIL = System.getenv("SES_SENDER_EMAIL");
    private static final String SES_TO_EMAIL = System.getenv("SES_RECEIVER_EMAIL");

    @Inject
    public ReportsDispatchService(DynamoDB dynamoDB, AmazonS3 s3Client, SesClient sesClient) {
        this.dynamoDB = dynamoDB;
        this.s3Client = s3Client;
        this.sesClient = sesClient;
        this.waiterStatsTable = dynamoDB.getTable(WAITER_DAILY_STATS_TABLE);
        this.locationStatsTable = dynamoDB.getTable(LOCATION_DAILY_STATS_TABLE);
        this.reportsTable = dynamoDB.getTable(REPORTS_TABLE);
        this.waitersTable=dynamoDB.getTable(WAITERS_TABLE);
    }

    public void generateAndSendReports() {
        try {
            // Set the time zone to IST (Indian Standard Time)
            ZoneId istZone = ZoneId.of("Asia/Kolkata");

            // Get the current date and the date 7 days ago in IST
            ZonedDateTime today = ZonedDateTime.now(istZone);
            ZonedDateTime weekAgo = today.minusDays(6);
            ZonedDateTime previousStart = today.minusDays(13);
            ZonedDateTime previousEnd = today.minusDays(7);

            // Extract the local date from the ZonedDateTime
            LocalDate todayLocal = today.toLocalDate();
            LocalDate weekAgoLocal = weekAgo.toLocalDate();
            LocalDate previousStartLocal = previousStart.toLocalDate();
            LocalDate previousEndLocal = previousEnd.toLocalDate();

            // Fetch data for the reports
            List<Item> waiterStats = scanTable(waiterStatsTable, weekAgoLocal, todayLocal);
            List<Item> previousWaiterStats = scanTable(waiterStatsTable, previousStartLocal, previousEndLocal);
            List<Item> locationStats = scanTable(locationStatsTable, weekAgoLocal, todayLocal);
            List<Item> previousLocationStats = scanTable(locationStatsTable, previousStartLocal, previousEndLocal);

            // Generate CSV for both waiter and location reports
            String waiterReportCSV = generateWaiterCSV(waiterStats, previousWaiterStats, weekAgoLocal, todayLocal);
            String locationReportCSV = generateLocationCSV(locationStats, previousLocationStats, weekAgoLocal, todayLocal);

            String waiterFileName = "waiter_report_" + todayLocal + ".csv";
            String locationFileName = "location_report_" + todayLocal + ".csv";

            // Upload reports to S3
            String waiterReportKey = uploadReportToS3(waiterReportCSV, waiterFileName);
            String locationReportKey = uploadReportToS3(locationReportCSV, locationFileName);

            // Store the report metadata in DynamoDB
            storeReportMetadata("N/A", "Waiter Weekly Report", "Weekly performance of waiters", waiterReportKey, "N/A", "Staff", weekAgoLocal, todayLocal);
            storeReportMetadata("N/A", "Location Weekly Report", "Weekly performance by location", locationReportKey, "N/A", "Location", weekAgoLocal, todayLocal);

            // Send email with report links
            logger.info("Sending email with report links.");
            sendEmailWithReports(Arrays.asList(waiterFileName, locationFileName));
            logger.info("Weekly reports generated and dispatched successfully.");
        } catch (Exception e) {
            logger.severe("Error generating or dispatching reports: " + e.getMessage());
        }
    }

    private List<Item> scanTable(Table table, LocalDate from, LocalDate to) {
        List<Item> results = new ArrayList<>();
        ItemCollection<ScanOutcome> items = table.scan();
        for (Item item : items) {
            LocalDate itemDate = LocalDate.parse(item.getString("date"));
            if (!itemDate.isBefore(from) && !itemDate.isAfter(to)) {
                results.add(item);
            }
        }
        return results;
    }

    private String generateWaiterCSV(List<Item> currentItems, List<Item> previousItems, LocalDate reportStart, LocalDate reportEnd) {
        StringBuilder sb = new StringBuilder(
                "Location,Waiter,Waiter's e-mail,Report period start,Report period end,Waiter working hours," +
                        "Waiter Orders processed,Delta of Waiter Orders processed to previous period in %," +
                        "Average Service Feedback Waiter (1 to 5),Minimum Service Feedback Waiter (1 to 5)," +
                        "Delta of Average Service Feedback Waiter to previous period in %\n"
        );

        // Group previous stats by waiterId
        Map<String, List<Item>> previousStatsMap = new HashMap<>();
        for (Item item : previousItems) {
            previousStatsMap.computeIfAbsent(item.getString("waiterId"), k -> new ArrayList<>()).add(item);
        }

        // Group current stats by waiterId
        Map<String, List<Item>> currentStatsMap = new HashMap<>();
        for (Item item : currentItems) {
            currentStatsMap.computeIfAbsent(item.getString("waiterId"), k -> new ArrayList<>()).add(item);
        }

        for (Map.Entry<String, List<Item>> entry : currentStatsMap.entrySet()) {
            String waiterId = entry.getKey();
            List<Item> current = entry.getValue();
            List<Item> previous = previousStatsMap.getOrDefault(waiterId, new ArrayList<>());

            int totalOrders = current.stream().mapToInt(i -> i.getInt("ordersProcessed")).sum();
            int prevOrders = previous.stream().mapToInt(i -> i.getInt("ordersProcessed")).sum();

            double deltaOrders;
            if (prevOrders == 0) {
                deltaOrders = totalOrders == 0 ? 0.0 : 100.0;
            } else {
                deltaOrders = ((double)(totalOrders - prevOrders) * 100) / prevOrders;
            }

            double totalFeedback = current.stream().mapToDouble(i -> i.getDouble("totalServiceFeedback")).sum();
            int feedbackCount = current.stream().mapToInt(i -> i.getInt("serviceFeedbackCount")).sum();
            double avgFeedback = feedbackCount == 0 ? 0 : totalFeedback / feedbackCount;

            double prevTotalFeedback = previous.stream().mapToDouble(i -> i.getDouble("totalServiceFeedback")).sum();
            int prevFeedbackCount = previous.stream().mapToInt(i -> i.getInt("serviceFeedbackCount")).sum();
            double prevAvgFeedback = prevFeedbackCount == 0 ? 0 : prevTotalFeedback / prevFeedbackCount;

            double deltaAvgFeedback;
            if (prevAvgFeedback == 0) {
                deltaAvgFeedback = avgFeedback > 0 ? 100.0 : 0.0;
            } else {
                deltaAvgFeedback = ((avgFeedback - prevAvgFeedback) * 100) / prevAvgFeedback;
            }

            double minFeedback = current.stream().mapToDouble(i -> i.getDouble("minServiceFeedback")).min().orElse(0.0);

            Set<String> allSlots = new HashSet<>();
            String locationId = "";
            for (Item i : current) {
                locationId = i.getString("locationId");
                if (i.hasAttribute("workedSlots")) {
                    allSlots.addAll(i.getStringSet("workedSlots"));
                }
            }

            double workingHours = allSlots.size() * 1.5;

            String[] waiterInfo = getWaiterInfo(waiterId);
            String waiterName = waiterInfo[0];
            String email = waiterInfo[1];

            sb.append(locationId).append(",")
                    .append(waiterName).append(",")
                    .append(email).append(",")
                    .append(reportStart).append(",")
                    .append(reportEnd).append(",")
                    .append(String.format("%.2f", workingHours)).append(",")
                    .append(totalOrders).append(",")
                    .append((deltaOrders >= 0 ? "+" : "")).append(String.format("%.2f", deltaOrders)).append("%,")
                    .append(String.format("%.2f", avgFeedback)).append(",")
                    .append(String.format("%.2f", minFeedback)).append(",")
                    .append((deltaAvgFeedback >= 0 ? "+" : "")).append(String.format("%.2f", deltaAvgFeedback)).append("%\n");
        }

        return sb.toString();
    }


    private String generateLocationCSV(List<Item> items, List<Item> previousItems, LocalDate reportStart, LocalDate reportEnd) {
        StringBuilder sb = new StringBuilder(
                "Location,Report period start,Report period end,Orders processed within location," +
                        "Delta of orders processed within location to previous period (in %)," +
                        "Average cuisine Feedback by Restaurant location (1 to 5)," +
                        "Minimum cuisine Feedback by Restaurant location (1 to 5)," +
                        "Delta of average cuisine Feedback by Restaurant location to previous period (in %)," +
                        "Revenue for orders within reported period,Delta of revenue for orders to previous period %\n"
        );

        // Group and aggregate previous items
        Map<String, List<Item>> previousGrouped = previousItems.stream()
                .collect(Collectors.groupingBy(i -> i.getString("locationId")));

        Map<String, Item> previousAggregated = new HashMap<>();
        for (Map.Entry<String, List<Item>> entry : previousGrouped.entrySet()) {
            String locationId = entry.getKey();
            List<Item> prevItems = entry.getValue();

            int totalOrders = prevItems.stream().mapToInt(i -> i.getInt("ordersProcessed")).sum();
            double totalRevenue = prevItems.stream().mapToDouble(i -> i.getDouble("revenue")).sum();
            double totalFeedback = prevItems.stream().mapToDouble(i -> i.getDouble("totalCuisineFeedback")).sum();
            int feedbackCount = prevItems.stream().mapToInt(i -> i.getInt("cuisineFeedbackCount")).sum();
            double minFeedback = prevItems.stream()
                    .mapToDouble(i -> i.getDouble("minCuisineFeedback"))
                    .min().orElse(0.0);

            Item aggregated = new Item();
            aggregated.withInt("ordersProcessed", totalOrders);
            aggregated.withDouble("revenue", totalRevenue);
            aggregated.withDouble("totalCuisineFeedback", totalFeedback);
            aggregated.withInt("cuisineFeedbackCount", feedbackCount);
            aggregated.withDouble("minCuisineFeedback", minFeedback);
            previousAggregated.put(locationId, aggregated);
        }

        // Group current items by location
        Map<String, List<Item>> currentGrouped = items.stream()
                .collect(Collectors.groupingBy(i -> i.getString("locationId")));

        for (Map.Entry<String, List<Item>> entry : currentGrouped.entrySet()) {
            String locationId = entry.getKey();
            List<Item> currItems = entry.getValue();

            int ordersProcessed = currItems.stream().mapToInt(i -> i.getInt("ordersProcessed")).sum();
            double revenue = currItems.stream().mapToDouble(i -> i.getDouble("revenue")).sum();
            double totalFeedback = currItems.stream().mapToDouble(i -> i.getDouble("totalCuisineFeedback")).sum();
            int feedbackCount = currItems.stream().mapToInt(i -> i.getInt("cuisineFeedbackCount")).sum();
            double minFeedback = currItems.stream()
                    .mapToDouble(i -> i.getDouble("minCuisineFeedback"))
                    .min().orElse(0.0);
            double avgFeedback = feedbackCount > 0 ? totalFeedback / feedbackCount : 0.0;

            Item prevItem = previousAggregated.get(locationId);
            int prevOrders = prevItem != null ? prevItem.getInt("ordersProcessed") : 0;
            double prevRevenue = prevItem != null ? prevItem.getDouble("revenue") : 0.0;
            double prevTotalFeedback = prevItem != null ? prevItem.getDouble("totalCuisineFeedback") : 0.0;
            int prevFeedbackCount = prevItem != null ? prevItem.getInt("cuisineFeedbackCount") : 0;
            double prevAvgFeedback = prevFeedbackCount > 0 ? prevTotalFeedback / prevFeedbackCount : 0.0;

            // Delta calculations
            double deltaOrders = (prevOrders == 0)
                    ? (ordersProcessed == 0 ? 0.0 : 100.0)
                    : ((double) (ordersProcessed - prevOrders) * 100) / prevOrders;

            double deltaAvgFeedback = (prevAvgFeedback == 0.0)
                    ? (avgFeedback > 0 ? 100.0 : 0.0)
                    : ((avgFeedback - prevAvgFeedback) * 100) / prevAvgFeedback;

            double deltaRevenue = (prevRevenue == 0.0)
                    ? (revenue > 0 ? 100.0 : 0.0)
                    : ((revenue - prevRevenue) * 100) / prevRevenue;

            sb.append(locationId).append(",")
                    .append(reportStart).append(",")
                    .append(reportEnd).append(",")
                    .append(ordersProcessed).append(",")
                    .append((deltaOrders >= 0 ? "+" : "")).append(String.format("%.2f", deltaOrders)).append("%,")
                    .append(String.format("%.2f", avgFeedback)).append(",")
                    .append(String.format("%.2f", minFeedback)).append(",")
                    .append((deltaAvgFeedback >= 0 ? "+" : "")).append(String.format("%.2f", deltaAvgFeedback)).append("%,")
                    .append(String.format("%.2f", revenue)).append(",")
                    .append((deltaRevenue >= 0 ? "+" : "")).append(String.format("%.2f", deltaRevenue)).append("%\n");
        }

        return sb.toString();
    }

    private String uploadReportToS3(String csvContent, String fileName) {
        byte[] contentAsBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentAsBytes.length);
        metadata.setContentType("text/csv");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentAsBytes);

        // Use PutObjectRequest
        PutObjectRequest request = new PutObjectRequest(S3_BUCKET, fileName, inputStream, metadata);

        s3Client.putObject(request);

        // Return the static public URL
        return String.format("https://%s.s3.ap-southeast-2.amazonaws.com/%s", S3_BUCKET, fileName);
    }

    private void storeReportMetadata(String waiterId, String reportName, String reportDescription, String downloadLink, String locationId, String reportType, LocalDate startPeriod, LocalDate endPeriod) {

        Item item = new Item()
                .withPrimaryKey("reportId", UUID.randomUUID().toString())
                .withString("waiterId", waiterId)
                .withString("reportName", reportName)
                .withString("reportDescription", reportDescription)
                .withString("downloadLink", downloadLink)
                .withString("locationId", locationId)
                .withString("reportType", reportType)
                .withString("startPeriod", startPeriod.toString())
                .withString("endPeriod", endPeriod.toString());

        reportsTable.putItem(item);
    }

    private void sendEmailWithReports(List<String> fileNames) {
        try {
            MimeMultipart multipart = new MimeMultipart();

            // Add email body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Attached are the weekly reports for waiter and location performance.", "utf-8");
            multipart.addBodyPart(textPart);

            // Add each CSV file from S3 as an attachment
            for (String fileName : fileNames) {
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(S3_BUCKET, fileName));
                InputStream inputStream = s3Object.getObjectContent();

                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(fileName);
                attachmentPart.setContent(inputStream.readAllBytes(), "text/csv");
                attachmentPart.setHeader("Content-Type", "text/csv");

                multipart.addBodyPart(attachmentPart);
            }

            // Create the full email message
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
            message.setFrom(new InternetAddress(SES_FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(SES_TO_EMAIL));
            message.setSubject("Weekly Reports");
            message.setContent(multipart);

            // Convert the MimeMessage to raw bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            ByteBuffer rawMessage = ByteBuffer.wrap(outputStream.toByteArray());

            // Send using AWS SES
            SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder()
                    .rawMessage(RawMessage.builder().data(SdkBytes.fromByteBuffer(rawMessage)).build())
                    .build();

            sesClient.sendRawEmail(rawEmailRequest);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email with reports", e);
        }
    }

    private String[] getWaiterInfo(String waiterId) {
        try {
            Item item = waitersTable.getItem("waiterId", waiterId);

            if (item != null) {
                String waiterName = item.getString("waiterName");
                String email = item.getString("email");
                return new String[]{waiterName, email};
            } else {
                return new String[]{"Unknown", "Unknown"};
            }
        } catch (Exception e) {
            logger.severe("Error fetching waiter info for waiterId: " + waiterId + " - " + e.getMessage());
            return new String[]{"Unknown", "Unknown"};
        }
    }
}