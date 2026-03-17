//package com.otblabs.fintech.sms.ampletech;
//
//import tools.jackson.databind.ObjectMapper;
//import okhttp3.*;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class AMTSmsService {
//
//    private static final Logger logger = LoggerFactory.getLogger(AMTSmsService.class);
//
//    // Shared OkHttpClient instance for better performance
//    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
//            .build();
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final JdbcTemplate jdbcTemplateOne;
//
//    public AMTSmsService(JdbcTemplate jdbcTemplateOne) {
//        this.jdbcTemplateOne = jdbcTemplateOne;
//    }
//
//    /**
//     * Send SMS asynchronously - non-blocking
//     */
//    @Async
//    public CompletableFuture<String> sendSmsAsync(MessageData messageData, String apiKey) {
//        return CompletableFuture.supplyAsync(() -> sendSmsInternal(messageData, apiKey));
//    }
//
//    /**
//     * Send promotional SMS asynchronously - non-blocking
//     */
//    @Async
//    public CompletableFuture<String> sendPromotionalSmsAsync(MessageData messageData, String apiKey) {
//        return CompletableFuture.supplyAsync(() -> sendSmsInternal(messageData, apiKey));
//    }
//
//    /**
//     * Synchronous SMS sending - use only if you need to wait for response
//     */
//    public String sendSms(MessageData messageData, String apiKey) {
//        return sendSmsInternal(messageData, apiKey);
//    }
//
//    /**
//     * Synchronous promotional SMS sending - use only if you need to wait for response
//     */
//    public String sendPromotionalSms(MessageData messageData, String apiKey) {
//        return sendSmsInternal(messageData, apiKey);
//    }
//
//    /**
//     * Internal method that handles the actual SMS sending logic
//     */
//    private String sendSmsInternal(MessageData messageData, String apiKey) {
//        try {
//            MediaType mediaType = MediaType.parse("application/json");
//            RequestBody body = RequestBody.create(mediaType, objectMapper.writeValueAsString(messageData));
//
//            Request request = new Request.Builder()
//                    .url("https://new.ampletech.co.ke/api/sms/send")
//                    .method("POST", body)
//                    .addHeader("Api-key", apiKey)
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//
//            try (Response response = client.newCall(request).execute()) {
//                String responseBody = response.body() != null ? response.body().string() : "";
//
//                // Log message asynchronously to avoid blocking
//                saveMessageLogAsync(messageData);
//
//                if (response.isSuccessful()) {
//                    logger.info("SMS sent successfully for {} contacts",
//                            messageData.getContact() != null ? messageData.getContact().size() : 0);
//                } else {
//                    logger.warn("SMS sending failed with status: {}, response: {}",
//                            response.code(), responseBody);
//                }
//
//                return responseBody;
//            }
//
//        } catch (IOException e) {
//            logger.error("Failed to send SMS: {}", e.getMessage(), e);
//            return null;
//        } catch (Exception e) {
//            logger.error("Unexpected error while sending SMS: {}", e.getMessage(), e);
//            return null;
//        }
//    }
//
//    /**
//     * Save message log asynchronously to avoid blocking SMS operations
//     */
//    @Async
//    public void saveMessageLogAsync(MessageData messageData) {
//        try {
//            saveMessageLog(messageData);
//        } catch (Exception e) {
//            logger.error("Failed to save message log: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Save message log to database
//     */
//    private void saveMessageLog(MessageData messageData) throws Exception {
//        if (messageData.getContact() == null || messageData.getContact().isEmpty()) {
//            logger.warn("No contacts found in messageData for logging");
//            return;
//        }
//
//        String sql = "INSERT INTO messages_logs(phone_number, message, created_at) VALUES (?, ?, NOW())";
//
//        messageData.getContact().forEach(contact -> {
//            try {
//                if (contact.getNumber() != null && contact.getBody() != null) {
//                    jdbcTemplateOne.update(sql, contact.getNumber(), contact.getBody());
//                } else {
//                    logger.warn("Skipping log entry - missing number or body for contact");
//                }
//            } catch (Exception e) {
//                logger.error("Failed to log message for contact {}: {}",
//                        contact.getNumber(), e.getMessage());
//            }
//        });
//
//        logger.debug("Message log saved for {} contacts", messageData.getContact().size());
//    }
//
//    /**
//     * Send multiple SMS messages concurrently
//     */
//    @Async
//    public CompletableFuture<Void> sendBulkSmsAsync(java.util.List<MessageData> messages, String apiKey) {
//        return CompletableFuture.allOf(
//                messages.stream()
//                        .map(message -> sendSmsAsync(message, apiKey))
//                        .toArray(CompletableFuture[]::new)
//        );
//    }
//}



package com.otblabs.jiinueboda.sms.providers.ampletech;


import okhttp3.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class AMTSmsService {
    private final JdbcTemplate jdbcTemplateOne;

    public AMTSmsService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public String sendSms(MessageData messageData, String apiKey) {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body;
        try {
            body = RequestBody.create(mediaType, objectMapper.writeValueAsString(messageData));

            Request request = new Request.Builder()
                    .url("https://new.ampletech.co.ke/api/sms/send")
                    .method("POST", body)
                    .addHeader("Api-key", apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            try {
                saveMessageLog(messageData);
            } catch (Exception e) {
                e.printStackTrace();
            }


            return  response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String sendPromotionalSms(MessageData messageData, String apiKey) {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body;
        try {
            body = RequestBody.create(mediaType, objectMapper.writeValueAsString(messageData));

            Request request = new Request.Builder()
                    .url("https://new.ampletech.co.ke/api/sms/send")
                    .method("POST", body)
                    .addHeader("Api-key", apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            try {
                saveMessageLog(messageData);
            } catch (Exception e) {
                e.printStackTrace();
            }


            return  response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveMessageLog(MessageData messageData) throws Exception{
        String sql = "INSERT INTO messages_logs(phone_number,message,created_at) VALUES (?,?,NOW())";

        messageData.getContact().forEach(contact -> {
            jdbcTemplateOne.update(sql,contact.getNumber(),contact.getBody());
        });
    }

}
