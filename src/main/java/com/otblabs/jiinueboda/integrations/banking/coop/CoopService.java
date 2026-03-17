package com.otblabs.jiinueboda.integrations.banking.coop;

import tools.jackson.databind.ObjectMapper;
import com.otblabs.jiinueboda.integrations.banking.coop.core.CoopRequestBody;
import com.otblabs.jiinueboda.integrations.banking.coop.core.CoopResponseBody;
import com.otblabs.jiinueboda.integrations.banking.coop.mpesa.Destination;
import com.otblabs.jiinueboda.integrations.banking.coop.mpesa.SendToMpesaRequestBody;
import com.otblabs.jiinueboda.integrations.banking.coop.mpesa.Source;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import okhttp3.*;
import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class CoopService {

    private final JdbcTemplate jdbcTemplateOne;

    public CoopService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

//    First, you need to register your client to obtain client credentials.
//    Use OkHttp to make a POST request to the DCR endpoint.
//    Extract clientId and clientSecret from the response for the next step.
    String obtainDCR() throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"callbackUrl\":\"www.google.lk\",\"clientName\":\"rest_api_devportal\",\"owner\":\"admin\",\"grantType\":\"client_credentials password refresh_token\",\"saasApp\":true}");

        Request request = new Request.Builder()
                .url("https://<host>:<servlet_port>/client-registration/v0.17/register")
                .post(body)
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        // Parse the response JSON to get clientId and clientSecret
        String responseBody = response.body().string();
        return responseBody;
    }

//    Obtain Access Token:
//    Use the obtained clientId and clientSecret to get an access token.
    String obtainAccessToken() throws IOException {

        OkHttpClient client = new OkHttpClient();

        String clientId = "fOCi4vNJ59PpHucC2CAYfYuADdMa";
        String clientSecret = "a4FwHlq0iCIKVs2MPIIDnepZnYMa";

        String credentials = Credentials.basic(clientId, clientSecret);

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", "<admin_username>")
                .add("password", "<admin_password>")
                .add("scope", "apim:subscribe apim:api_key")
                .build();

        Request request = new Request.Builder()
                .url("https://<host>:<gateway_port>/token")
                .post(formBody)
                .addHeader("Authorization", credentials)
                .build();

        Response response = client.newCall(request).execute();

// Parse the response JSON to get the access token
        String responseBody = response.body().string();
        return responseBody;
    }

    private String getAuthToken(String appKeySecret) throws Exception {
        byte[] bytes = appKeySecret.getBytes("ISO-8859-1");

        String auth = Base64.getEncoder().encodeToString(bytes);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,auth);


        Request request = new Request.Builder()
                .url("https://openapi-sandbox.co-opbank.co.ke/token")
                .post(body)
                .addHeader("authorization", "Basic " + auth)
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    public CoopResponseBody saveCoreBanking(String data) throws Exception{


        ObjectMapper objectMapper = new ObjectMapper();
        CoopRequestBody coopRequestBody = objectMapper.readValue(data, CoopRequestBody.class);
        String sql = """
                   INSERT  into coop_transactions(acct_no, amount, booked_balance, cleared_balance, currency, cust_memo_line1, 
                   cust_memo_line2, cust_memo_line3, event_type, exchange_rate, narration, payment_ref, posting_date, value_date, 
                   transaction_date, transaction_id,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())                            
                """;
        jdbcTemplateOne.update(sql,
                coopRequestBody.getAccountNumber(),
                coopRequestBody.getAmount(),
                coopRequestBody.getBookedBalance(),
                coopRequestBody.getClearedBalance(),
                coopRequestBody.getCurrency(),
                coopRequestBody.getCustMemoLine1(),
                coopRequestBody.getCustMemoLine2(),
                coopRequestBody.getCustMemoLine3(),
                coopRequestBody.getEventType(),
                coopRequestBody.getExchangeRate(),
                coopRequestBody.getNarration(),
                coopRequestBody.getPaymentRef(),
                coopRequestBody.getPostingDate(),
                coopRequestBody.getValueDate(),
                coopRequestBody.getTransactionDate(),
                coopRequestBody.getTransactionId());

        CoopResponseBody coopResponseBody = new CoopResponseBody();
        coopResponseBody.setMessageCode("200");
        coopResponseBody.setMessage("Successfully received data");
        return coopResponseBody;

    }

//    public Object initiatePesalinkToPhoneTransfer() throws Exception {
//
//        OkHttpClient client = new OkHttpClient();
////        String consumerKey="";
////        String secretKey="";
////        String appKeySecret = consumerKey + ":" + secretKey;
////        String token = getAuthToken(appKeySecret);
//
//        MediaType mediaType = MediaType.parse("application/json");
//        B2cCallbackBody b2cCallbackBody = new B2cCallbackBody();
//        RequestBody body = RequestBody.create(mediaType,b2cCallbackBody.toString());
//        Request request1 = new Request.Builder()
//                .url("https://developer.co-opbank.co.ke:8243/FundsTransfer/External/A2M/PesaLink/1.0.0")
//                .post(body)
//                .addHeader("authorization", "Bearer "+token)
//                .addHeader("content-type", "application/json")
//                .build();
//
//        Response response  = client.newCall(request1).execute();
//        return response.body().string();
//    }

    public static Object initiateSendToMpesaFundsTransfer() throws IOException {

        String endpointUrl ="https://openapi-sandbox.co-opbank.co.ke/FundsTransfer/External/A2M/Mpesa/1.0.0";
        String callbackUrl = "https://fintech.tequelabstechnologies.tech/banking/coop/send-to-mpes-ipn";
        String messageRefference = String.format("%06d", new Random().nextInt(10000)) + UtilityFunctions.getCurrentTimestamp();

        Source source = new Source();
        source.setAccountNumber("36001873000");
        source.setTransactionCurrency("KES");
        source.setAmount(1);
        source.setNarration("TEST");

        List<Destination> destinations = new ArrayList<>();
        Destination destination = new Destination();
        destination.setRefferenceNumber(messageRefference);
        destination.setAmount(1);
        destination.setMobileNumber("254718728894");
        destination.setNarration("Test");
        destinations.add(destination);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        SendToMpesaRequestBody sendToMpesaRequestBody = new SendToMpesaRequestBody();
        sendToMpesaRequestBody.setMessageRefference(messageRefference);
        sendToMpesaRequestBody.setCallbackUrl(callbackUrl);
        sendToMpesaRequestBody.setSource(source);
        sendToMpesaRequestBody.setDestinations(destinations);


        RequestBody body = RequestBody.create(mediaType,sendToMpesaRequestBody.toString());

        Request request1 = new Request.Builder()
                .url(endpointUrl)
                .post(body)
                .addHeader("authorization", "Bearer "+"mjgyfte4edsweawszweaTRAWAqwQhgbyftrcsxsadhgvsxasaszcgjhgfazs")
                .addHeader("content-type", "application/json")
                .build();

        Response response  = client.newCall(request1).execute();

        return response.body().string();

    }

    public Object saveSendToMpesaIpn(String data) {

        return null;
    }
}
