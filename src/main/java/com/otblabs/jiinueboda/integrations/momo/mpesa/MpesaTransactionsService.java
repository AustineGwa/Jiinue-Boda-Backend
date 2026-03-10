package com.otblabs.jiinueboda.integrations.momo.mpesa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otblabs.jiinueboda.integrations.IncomingPaymentConfirmation;
import com.otblabs.jiinueboda.integrations.momo.mpesa.b2c.models.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.buygoods.models.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.c2b.models.C2BNotification;
import com.otblabs.jiinueboda.integrations.momo.mpesa.express.models.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.paybill.*;
import com.otblabs.jiinueboda.integrations.momo.mpesa.pull.MResponse;
import com.otblabs.jiinueboda.integrations.momo.mpesa.pull.PullRequest;
import com.otblabs.jiinueboda.integrations.momo.mpesa.pull.PullResponse;
import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import com.otblabs.jiinueboda.jiinue.models.UserLoanDetail;
import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.users.models.Signatory;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.utility.EncryptionUtil;
import com.otblabs.jiinueboda.utility.Functions;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Base64.getEncoder;

@Service
public class MpesaTransactionsService {



    private final JdbcTemplate jdbcTemplateOne;
    private final SmsService smsService;

    private final UserService userService;

    private final InvestmentManagementService investmentManagementService;

    public MpesaTransactionsService(JdbcTemplate jdbcTemplateOne, SmsService smsService, UserService userService, InvestmentManagementService investmentManagementService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.smsService = smsService;
        this.userService = userService;
        this.investmentManagementService = investmentManagementService;
    }


    public  String registerURL(int appId) throws Exception {
        MpesaApp mpesaApp = getMpesaApp(appId);
        String appKeySecret = mpesaApp.getConsumerKey() + ":" + mpesaApp.getConsumerSecret();
        JSONArray jsonArray=new JSONArray();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("ShortCode", mpesaApp.getShotCode());
        jsonObject.put("ResponseType", mpesaApp.getResponseType());
        jsonObject.put("ConfirmationURL", mpesaApp.getC2bConfirmationURL());
        jsonObject.put("ValidationURL", mpesaApp.getC2bValidationURL());
        jsonArray.put(jsonObject);
        String requestJson=jsonArray.toString().replaceAll("[\\[\\]]","");
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/mpesa/c2b/v2/registerurl")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                .addHeader("cache-control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public int saveC2bNotification(C2BNotification data, int appId, boolean isManual) throws Exception {


        String sql = "INSERT INTO mpesa_c2b(TransactionType,TransID,PaymentChannel,TransTime,TransAmount,BusinessShortCode,BillRefNumber,is_manual," +
                "InvoiceNumber,OrgAccountBalance, ThirdPartyTransID,MSISDN,FirstName,MiddleName,LastName,appId,created_at,updated_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";

        return jdbcTemplateOne.update(sql,
                data.getTransactionType(),
                data.getTransID(),
                "MPESA DARAJA",
                UtilityFunctions.formatDateTime(data.getTransTime()),
                data.getTransAmount(),
                data.getBusinessShortCode(),
                data.getBillRefNumber().trim(),
                isManual,
                data.getInvoiceNumber(),
                data.getOrgAccountBalance(),
                data.getThirdPartyTransID(),
                data.getMSISDN(),
                data.getFirstName(),
                data.getMiddleName(),
                data.getLastName(),
                appId
        );
    }

    public void confirmPaymentRecieved(String transactionId, String amount, String loanId) {

        IncomingPaymentConfirmation incomingPaymentConfirmation = new IncomingPaymentConfirmation();
        incomingPaymentConfirmation.setTransactionId(transactionId);
        incomingPaymentConfirmation.setTransactionAmount(Double.parseDouble(amount));
        incomingPaymentConfirmation.setLoanAccount(loanId);
        
        try{
            //jiinue loan account user
            SystemUser systemUser = userService.getUserByLoanAccount(loanId);
            UserLoanDetail userLoanDetail = userService.getUserLoanDetail(loanId);
            smsService.sendPaymentConfirmationMessage(incomingPaymentConfirmation,userLoanDetail,systemUser);

        }catch (Exception ignored){}

       
    }

    public String authenticate(String appKeySecret) throws IOException {
        byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
        String encoded = getEncoder().encodeToString(bytes);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("authorization", "Basic "+encoded)
                .addHeader("cache-control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject=new JSONObject(response.body().string());
        return jsonObject.getString("access_token");
    }

    private MpesaApp getMpesaApp(int appId) throws Exception {

        String sql = "SELECT * FROM mpesa_apps WHERE id=?";
        return jdbcTemplateOne.queryForObject(sql,(rs,i) ->{
            MpesaApp mpesaApp1 = new MpesaApp();
            mpesaApp1.setId(rs.getInt("id"));
            mpesaApp1.setAppName(rs.getString("app_name"));
            mpesaApp1.setConsumerKey(rs.getString("consumer_key"));
            mpesaApp1.setConsumerSecret(rs.getString("consumer_secret"));
            mpesaApp1.setB2cConsumerKey(rs.getString("b2c_consumer_key"));
            mpesaApp1.setB2cConsumerSecret(rs.getString("b2c_consumer_secret"));
            mpesaApp1.setApiKey(rs.getString("api_key"));
            mpesaApp1.setShotCode(rs.getString("shotcode"));
            mpesaApp1.setProductsActivated(rs.getString("products_activated"));
            mpesaApp1.setResponseType(rs.getString("response_type"));
            mpesaApp1.setTransactionType(rs.getString("transaction_type"));
            mpesaApp1.setConfirmationURL(rs.getString("confirmation_url"));
            mpesaApp1.setValidationURL(rs.getString("validation_url"));
            mpesaApp1.setC2bConfirmationURL(rs.getString("c2b_confirmation_url"));
            mpesaApp1.setC2bValidationURL(rs.getString("c2b_validation_url"));
            mpesaApp1.setB2cEnabled(rs.getBoolean("is_b2c_enabled"));
            mpesaApp1.setB2cBusinessShortcode(rs.getString("b2c_shortcode"));
            mpesaApp1.setB2cInitiator(rs.getString("b2c_initiator"));
            mpesaApp1.setB2cPassword(rs.getString("b2c_password"));
            mpesaApp1.setB2cCallbackUrl(rs.getString("b2c_callback_url"));
            mpesaApp1.setB2cQueueTimeOutUrl(rs.getString("b2c_queue_timeout_url"));
            mpesaApp1.setBuygoodsCallbackUrl(rs.getString("buygoods_callbackurl"));
            mpesaApp1.setBuygoodsTimeoutUrl(rs.getString("buygoods_timeouturl"));
            mpesaApp1.setPaybillCallBackUrl(rs.getString("paybill_callbackurl"));
            mpesaApp1.setPaybillTimeoutUrl(rs.getString("paybill_timeouturl"));
            mpesaApp1.setPullUrl(rs.getString("pullUrl"));
            mpesaApp1.setNominatedNumber(rs.getString("nominatedNumber"));
            return mpesaApp1;
        },appId);
    }

    public StkPushRequestResponse initiateStkpush(StkPushRequest stkRequest, int appId) throws Exception {

        MpesaApp mpesaApp = getMpesaApp(appId);
        String appKeySecret = mpesaApp.getConsumerKey() + ":" + mpesaApp.getConsumerSecret();
        String timestamp = getCurrentTimestamp();
        String password = getStkPushPassword(mpesaApp.getShotCode(),mpesaApp.getApiKey(),timestamp);

        JSONArray jsonArray=new JSONArray();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("BusinessShortCode", mpesaApp.getShotCode());
        jsonObject.put("Password", password);
        jsonObject.put("Timestamp",timestamp);
        jsonObject.put("TransactionType", mpesaApp.getTransactionType());
        jsonObject.put("Amount",stkRequest.getAmount());
        jsonObject.put("PhoneNumber", Functions.formatPhoneNumber(stkRequest.getPhoneNumber()));
        jsonObject.put("PartyA", Functions.formatPhoneNumber(stkRequest.getPhoneNumber()));
        jsonObject.put("PartyB", mpesaApp.getShotCode());
        jsonObject.put("CallBackURL", mpesaApp.getConfirmationURL());
        jsonObject.put("AccountReference", stkRequest.getLoanId());
        jsonObject.put("QueueTimeOutURL", "");
        jsonObject.put("TransactionDesc", stkRequest.getTransactionDesc());
        jsonArray.put(jsonObject);
        String requestJson=jsonArray.toString().replaceAll("[\\[\\]]","");
        OkHttpClient client = new OkHttpClient();
        String url="https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        String res = response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        StkPushRequestResponse stkPushRequestResponse = objectMapper.readValue(res, StkPushRequestResponse.class);

        String sql = """
                 INSERT INTO mpesa_stk (MerchantRequestID, CheckoutRequestID,ResponseCode,ResponseDescription,CustomerMessage,RequestID,ErrorCode,ErrorMessage,AppID,PaymentFor,UserID,created_at)
                 VALUES (?,?,?,?,?,?,?,?,?,?,?,NOW());
                """;

        jdbcTemplateOne.update(sql,
                stkPushRequestResponse.getMerchantRequestID(),
                stkPushRequestResponse.getCheckoutRequestID(),
                stkPushRequestResponse.getResponseCode(),
                stkPushRequestResponse.getResponseDescription(),
                stkPushRequestResponse.getCustomerMessage(),
                stkPushRequestResponse.getRequestId(),
                stkPushRequestResponse.getErrorCode(),
                stkPushRequestResponse.getErrorMessage(),
                appId,
                stkRequest.getLoanId(),
                stkRequest.getUserId()
                );


        return stkPushRequestResponse;
    }

    private String getStkPushPassword(String shortCode,String passKey, String timestamp) {
        return Base64.getEncoder().encodeToString((shortCode + passKey + timestamp).getBytes(StandardCharsets.UTF_8));
    }

    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return  now.format(formatter);
    }

    public Acknowledgement saveStkPushNotification(String stkPushResponse, int appId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        StkPushNotification data = objectMapper.readValue(stkPushResponse, StkPushNotification.class);

        String sql = """
        UPDATE  mpesa_stk SET ResultCode=?,ResultDesc=?,Amount=?,MpesaReceiptNumber=?,TransactionDate=?,PhoneNumber=?,
        updated_at=NOW() WHERE  MerchantRequestID=? AND CheckoutRequestID=?        
        """;

        StkCallback callback = data.getBody().getStkCallback();
        CallbackMetadata callbackMetadata = data.getBody().getStkCallback().getCallbackMetadata();

        String amount = null;
        String mpesaReceiptNumber = null;
        String transactionDate = null;
        String phoneNumber = null;

        if(callbackMetadata != null){

            amount = callbackMetadata.getItem().get(0).getValue();
            mpesaReceiptNumber = callbackMetadata.getItem().get(1).getValue();
            transactionDate = callbackMetadata.getItem().get(3).getValue();
            phoneNumber = callbackMetadata.getItem().get(4).getValue();
        }


        jdbcTemplateOne.update(sql,
                callback.getResultCode(),
                callback.getResultDesc(),
                amount,
                mpesaReceiptNumber,
                transactionDate,
                phoneNumber,
                callback.getMerchantRequestID(),
                callback.getCheckoutRequestID()
        );

        return new Acknowledgement(0,"Success");
    }

    public B2CRequestResponse initiateb2c(B2CRequest b2CRequest, int appId) throws Exception {

        MpesaApp mpesaApp = getMpesaApp(appId);
        String appKeySecret = mpesaApp.getB2cConsumerKey() + ":" + mpesaApp.getB2cConsumerSecret();

        JSONArray jsonArray=new JSONArray();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("InitiatorName", mpesaApp.getB2cInitiator());
        jsonObject.put("SecurityCredential", getSecurityCredentials(mpesaApp.getB2cPassword()));
        jsonObject.put("CommandID", b2CRequest.getCommandID());
        jsonObject.put("Amount", b2CRequest.getAmount());
        jsonObject.put("PartyA", mpesaApp.getB2cBusinessShortcode());
        jsonObject.put("PartyB", Functions.formatPhoneNumber(b2CRequest.getPartyB()));
        jsonObject.put("Remarks", b2CRequest.getRemarks());
        jsonObject.put("QueueTimeOutURL", mpesaApp.getB2cQueueTimeOutUrl());
        jsonObject.put("ResultURL", mpesaApp.getB2cCallbackUrl());
        jsonObject.put("Occassion", b2CRequest.getOccasion());
        jsonArray.put(jsonObject);
        String requestJson=jsonArray.toString().replaceAll("[\\[\\]]","");

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url( "https://api.safaricom.co.ke/mpesa/b2c/v1/paymentrequest")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        ObjectMapper objectMapper = new ObjectMapper();
        B2CRequestResponse b2CRequestResponse =  objectMapper.readValue(response.body().string(),B2CRequestResponse.class);

        String sql = """
                INSERT INTO mpesa_b2c(command_id,party_b,remarks,occasion,response_code,response_description,conversation_id,
                originator_conversation_id,app_id,created_at) VALUES(?,?,?,?,?,?,?,?,?,NOW())
                """;

               jdbcTemplateOne.update(sql,
                       b2CRequest.getCommandID().name(),
                       b2CRequest.getPartyB(),
                       b2CRequest.getRemarks(),
                       b2CRequest.getOccasion(),
                       b2CRequestResponse.getResponseCode(),
                       b2CRequestResponse.getResponseDescription(),
                       b2CRequestResponse.getConversationID(),
                       b2CRequestResponse.getOriginatorConversationID(),
                       appId
               );

        return b2CRequestResponse;
    }

    public Acknowledgement saveB2cResponse(Result result, int appId) throws Exception {

        int transactionAmount = Integer.parseInt(result.getResultParameters().getB2cResultParameter().get(0).getValue());
        String transactionReceipt = result.getResultParameters().getB2cResultParameter().get(1).getValue();
        String recieverName = result.getResultParameters().getB2cResultParameter().get(2).getValue();
        String transactionCompletionDate = result.getResultParameters().getB2cResultParameter().get(3).getValue();
        String b2cUtilityAccountAvailableFunds = result.getResultParameters().getB2cResultParameter().get(4).getValue();
        String b2cAccountWorkingAccountAvailableFunds = result.getResultParameters().getB2cResultParameter().get(5).getValue();
        String b2cRecipientIsRegisteredCustomer = result.getResultParameters().getB2cResultParameter().get(6).getValue();
        String b2cChargesPaidAccountAvailableFunds = result.getResultParameters().getB2cResultParameter().get(6).getValue();


        String sql = """
                UPDATE mpesa_b2c set reference_Data=?, result_desc=?,result_type=?,result_code=?,transaction_id=?,transaction_receipt=?,
                 transaction_amount=?,reciever_name=?,b2c_working_account_available_funds=?,b2c_utility_account_available_funds=?,
                 transaction_completed_datetime=?,
                 receiver_party_public_name=?,b2c_charges_paid_account_available_funds=?,
                 b2c_recipient_is_registered_customer=?,app_id=?, updated_at=NOW()
                 WHERE originator_conversation_id=? AND conversation_id =?;
        """;


        jdbcTemplateOne.update(sql,
                result.getReferenceData().toString(),
                result.getResultDesc(),
                result.getResultType(),
                result.getResultCode(),
                result.getTransactionID(),
                transactionReceipt,
                transactionAmount,
                recieverName,
                b2cAccountWorkingAccountAvailableFunds,
                b2cUtilityAccountAvailableFunds,
                transactionCompletionDate,
                recieverName,
                b2cChargesPaidAccountAvailableFunds,
                b2cRecipientIsRegisteredCustomer,
                appId,
                result.getOriginatorConversationId(),
                result.getConversationId());

        return new Acknowledgement(0,"Success");
    }

    public BuygoodsRequestResponse initiateBuyGoods(BuyGoodsRequest buyGoodsRequest, int appId) throws Exception {

        MpesaApp mpesaApp = getMpesaApp(appId);
        String appKeySecret = mpesaApp.getB2cConsumerKey() + ":" + mpesaApp.getB2cConsumerSecret();

        JSONArray jsonArray=new JSONArray();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("Initiator", "austine");
        jsonObject.put("SecurityCredential", getSecurityCredentials(mpesaApp.getB2cPassword()));
        jsonObject.put("CommandID", buyGoodsRequest.getCommandID());
        jsonObject.put("SenderIdentifierType", "4");
        jsonObject.put("RecieverIdentifierType", "2");
        jsonObject.put("Amount", buyGoodsRequest.getAmount());
        jsonObject.put("PartyA", mpesaApp.getB2cBusinessShortcode());
        jsonObject.put("PartyB", buyGoodsRequest.getPartyB());
        jsonObject.put("AccountReference", buyGoodsRequest.getAccountReference());
        jsonObject.put("Requester", buyGoodsRequest.getRequester());
        jsonObject.put("Remarks",buyGoodsRequest.getRemarks());
        jsonObject.put("QueueTimeOutURL", mpesaApp.getBuygoodsTimeoutUrl());
        jsonObject.put("ResultURL", mpesaApp.getBuygoodsCallbackUrl());

        jsonArray.put(jsonObject);
        String requestJson=jsonArray.toString().replaceAll("[\\[\\]]","");

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url( "https://api.safaricom.co.ke/mpesa/b2b/v1/paymentrequest")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseString = response.body().string();

        BuygoodsRequestResponse b2CRequestResponse =  objectMapper.readValue(responseString,BuygoodsRequestResponse.class);

        String sql = """
                INSERT INTO mpesa_buygoods (command_id,party_b,remarks,response_code,response_description,conversation_id,
                originator_conversation_id,app_id,created_at) VALUES(?,?,?,?,?,?,?,?,NOW())

                """;


        jdbcTemplateOne.update(sql,
                buyGoodsRequest.getCommandID().name(),
                buyGoodsRequest.getPartyB(),
                buyGoodsRequest.getRemarks(),
                b2CRequestResponse.getResponseCode(),
                b2CRequestResponse.getResponseDescription(),
                b2CRequestResponse.getConversationID(),
                b2CRequestResponse.getOriginatorConversationID(),
                appId
        );

        return b2CRequestResponse;
    }

    public Acknowledgement saveBuyGoodsResponse(String buygoodsResponse, int appId) throws Exception {

        System.out.println("BUY GOODS RESPONSE \n "+ buygoodsResponse );

        ObjectMapper objectMapper = new ObjectMapper();
        BuyGoodsApiResponse buyGoodsApiResponse = objectMapper.readValue(buygoodsResponse, BuyGoodsApiResponse.class);
        BuygoodsResultSuccess result = buyGoodsApiResponse.getResult();

        if(buygoodsTransactionAlreadyProcessed(result.getOriginatorConversationID(),result.getConversationID())){
            return new Acknowledgement(0,"Already processed");
            
            
        }

         String resultType = result.getResultType();
         String resultCode = result.getResultCode();
         String resultDesc = result.getResultDesc();
         String originatorConversationID = result.getOriginatorConversationID();
         String conversationID = result.getConversationID();
         String transactionID = result.getTransactionID();
         BuygoodsResultParameters buygoodsResultParameters = result.getResultParameters();
         List<BuygoodsResultParameter> resultParameter =  buygoodsResultParameters.getResultParameter();
         String currency =  resultParameter.get(0).getValue();
         PaygoodsAmountHolder debitAccountCurrentBalance =  objectMapper.readValue(convertToJson(resultParameter.get(1).getValue()),PaygoodsAmountHolder.class);
         PaygoodsAmountHolder initiatorAccountCurrentBalance =objectMapper.readValue(convertToJson(resultParameter.get(2).getValue()),PaygoodsAmountHolder.class);
         BuygoodsReferenceData referenceData = result.getReferenceData();

         try{
            String sql = """
            UPDATE  mpesa_buygoods SET result_desc=?, result_type=?, result_code=?, transaction_id=?,
            debitAccountBalance=?,currency=?,InitiatorAccountCurrentBalance=?,updated_at=NOW()
            WHERE originator_conversation_id =?
         """;

            jdbcTemplateOne.update(sql,
                    resultDesc,
                    resultType,
                    resultCode,
                    transactionID,
                    debitAccountCurrentBalance.getAmount().getBasicAmount(),
                    currency,
                    initiatorAccountCurrentBalance.getAmount().getBasicAmount(),
                    originatorConversationID
            );
         }catch(Exception exception){
           exception.printStackTrace();
         }
//        Jifuel Implementation
//        if(result.getResultCode().equals("0")){
//            try{
//                String sqlUpdate = "UPDATE fuel_loan SET disbursedAt=NOW() WHERE mpesa_disburse_conversation_id = ? ";
//                jdbcTemplateOne.update(sqlUpdate,conversationID);
//                  smsService.sendFuelMessageToFuelBorrower(transactionID, conversationID);
//            }catch (Exception exception){exception.printStackTrace();}
//        }

//        try{
//            List<Signatory> signatories = userService.getSignatoriesByAppId(appId);
//            smsService.sendMessageToSignatories(appId, Integer.parseInt(amount),receiverPartyPublicName, String.valueOf(initiatorAccountCurrentBalance),signatories);
//        }catch (Exception ignored){}

        return new Acknowledgement(0,"Success");
    }

    String convertToJson(String str){
        str = str.replace('=', ':');
        str = str.replaceAll("(\\w+):", "\"$1\":");
        str = str.replaceAll("(\\w+),", "\"$1\",");
        return str;

    }

    public PaybillRequestResponse initiatePaybill(PaybillRequest paybillRequest, int appId) throws Exception {

                MpesaApp mpesaApp = getMpesaApp(appId);
                String appKeySecret = mpesaApp.getB2cConsumerKey() + ":" + mpesaApp.getB2cConsumerSecret();

                JSONArray jsonArray=new JSONArray();
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("Initiator", "austine");
                jsonObject.put("SecurityCredential", getSecurityCredentials(mpesaApp.getB2cPassword()));
                jsonObject.put("CommandID", paybillRequest.getCommandID());
                jsonObject.put("SenderIdentifierType", "4");
                jsonObject.put("RecieverIdentifierType", "4");
                jsonObject.put("Amount", paybillRequest.getAmount());
                jsonObject.put("PartyA", mpesaApp.getB2cBusinessShortcode());
                jsonObject.put("PartyB", paybillRequest.getPartyB());
                jsonObject.put("AccountReference", paybillRequest.getAccountReference());
                jsonObject.put("Requester", paybillRequest.getRequester());
                jsonObject.put("Remarks",paybillRequest.getRemarks());
                jsonObject.put("QueueTimeOutURL", mpesaApp.getPaybillTimeoutUrl());
                jsonObject.put("ResultURL", mpesaApp.getPaybillCallBackUrl());

                jsonArray.put(jsonObject);
                String requestJson=jsonArray.toString().replaceAll("[\\[\\]]","");

                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, requestJson);
                Request request = new Request.Builder()
                        .url( "https://api.safaricom.co.ke/mpesa/b2b/v1/paymentrequest")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                        .addHeader("cache-control", "no-cache")
                        .build();

                Response response = client.newCall(request).execute();
                ObjectMapper objectMapper = new ObjectMapper();
                String responseString = response.body().string();

                PaybillRequestResponse b2CRequestResponse =  objectMapper.readValue(responseString,PaybillRequestResponse.class);

                String sql = """
                        INSERT INTO mpesa_buygoods (command_id,party_b,remarks,response_code,response_description,conversation_id,
                        originator_conversation_id,app_id,created_at) VALUES(?,?,?,?,?,?,?,?,NOW())
                        """;

                jdbcTemplateOne.update(sql,
                        paybillRequest.getCommandID().name(),
                        paybillRequest.getPartyB(),
                        paybillRequest.getRemarks(),
                        b2CRequestResponse.getResponseCode(),
                        b2CRequestResponse.getResponseDescription(),
                        b2CRequestResponse.getConversationID(),
                        b2CRequestResponse.getOriginatorConversationID(),
                        appId
                );

                return b2CRequestResponse;
    }

    public Acknowledgement savePaybillsResponse(String paybillResponse, int appId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        PaybillApiResponse paybillApiResponse = objectMapper.readValue(paybillResponse, PaybillApiResponse.class);
        PaybillResult result = paybillApiResponse.getPaybillResult();

        if(buygoodsTransactionAlreadyProcessed(result.getOriginatorConversationID(),result.getConversationID())){
            return new Acknowledgement(0,"Already processed");
        }

        String resultType = result.getResultType();
        String resultCode = result.getResultCode();
        String resultDesc = result.getResultDesc();
        String originatorConversationID = result.getOriginatorConversationID();
        String conversationID = result.getConversationID();
        String transactionID = result.getTransactionID();
        PaybillResultParameters resultParameters = result.getResultParameters();

        String currency = resultParameters.getResultParameters().get(0).getValue();
        String receiverPartyPublicName = resultParameters.getResultParameters().get(1).getValue();
        String debitPartyCharges = resultParameters.getResultParameters().get(2).getValue();
        String transCompletedTime = resultParameters.getResultParameters().get(3).getValue();
        String debitPartyAffectedAccountBalance =  resultParameters.getResultParameters().get(4).getValue();
        String amount = resultParameters.getResultParameters().get(5).getValue();

        PaygoodsAmountHolder debitAccountBalance =  objectMapper.readValue(convertToJson(resultParameters.getResultParameters().get(6).getValue()),PaygoodsAmountHolder.class);
        PaygoodsAmountHolder initiatorAccountCurrentBalance =objectMapper.readValue(convertToJson(resultParameters.getResultParameters().get(7).getValue()),PaygoodsAmountHolder.class);

        PaybillReferenceData referenceData = result.getReferenceData();
        String billReferenceNumber = referenceData.getReferenceItems().get(0).getValue();

        String sql = """
            UPDATE  mpesa_buygoods SET result_desc=?, result_type=?, result_code=?, transaction_id=?,debitAccountBalance=?,
            debitPartyAffectedAccountBalance=?,DebitPartyCharges=?,currency=?,InitiatorAccountCurrentBalance=?,
            BillReferenceNumber=?,
            transaction_amount=?, transaction_completed_datetime=?, receiver_party_public_name=?,updated_at=NOW()
            WHERE originator_conversation_id =?
        """;

        jdbcTemplateOne.update(sql,
                resultDesc,
                resultType,
                resultCode,
                transactionID,
                debitAccountBalance.getAmount().getBasicAmount(),
                debitPartyAffectedAccountBalance,
                debitPartyCharges,
                currency,
                initiatorAccountCurrentBalance.getAmount().getBasicAmount(),
                billReferenceNumber,
                amount,
                transCompletedTime,
                receiverPartyPublicName,
                originatorConversationID
        );

        try{
            List<Signatory> signatories = userService.getSignatoriesByAppId(appId);
            smsService.sendMessageToSignatories(appId, Integer.parseInt(amount),receiverPartyPublicName, String.valueOf(initiatorAccountCurrentBalance),signatories);
        }catch (Exception ignored){}


        return new Acknowledgement(0,"Success");
    }

    private String getSecurityCredentials(String password) throws Exception {

        String decryptedText = EncryptionUtil.decrypt(password,"650bbf657394aad65a0bb0dcf67caf7f");
        String certificatePath = "src/main/resources/static/TestCertificate.cer";
        FileInputStream certificateInputStream = new FileInputStream(new File(certificatePath));
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);
        PublicKey publicKey = certificate.getPublicKey();

        // Convert password to a byte array
        byte[] passwordBytes = decryptedText.getBytes(StandardCharsets.UTF_8);

        // Encrypt the password using RSA with PKCS#1.5 padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(passwordBytes);

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public void registerPullUrl(int appId) throws Exception {

        MpesaApp mpesaApp = getMpesaApp(appId);
        String appKeySecret = mpesaApp.getB2cConsumerKey() + ":" + mpesaApp.getB2cConsumerSecret();

        JSONArray jsonArray=new JSONArray();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("ShortCode", mpesaApp.getShotCode());
        jsonObject.put("RequestType", "Pull");
        jsonObject.put("NominatedNumber", mpesaApp.getNominatedNumber());
        jsonObject.put("CallBackURL", mpesaApp.getPullUrl());
        jsonArray.put(jsonObject);

        String requestJson=jsonArray.toString().replaceAll("[\\[\\]]","");

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestJson);
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/pulltransactions/v1/register")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("Accept-Encoding", "application/json")
                .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                .build();

        Response response = client.newCall(request).execute();
    }

    public List<InsertedTransaction> requestPullData(PullRequest pullRequest, int appId) throws Exception {
        MpesaApp mpesaApp = getMpesaApp(appId);
        String appKeySecret = mpesaApp.getB2cConsumerKey() + ":" + mpesaApp.getB2cConsumerSecret();

        JSONObject requestJson =new JSONObject();
        requestJson.put("ShortCode", mpesaApp.getShotCode());
        requestJson.put("StartDate", pullRequest.getStartDate());
        requestJson.put("EndDate", pullRequest.getEndDate());
        requestJson.put("OffSetValue", 0);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, String.valueOf(requestJson));
        Request request = new Request.Builder()
                .url("https://api.safaricom.co.ke/pulltransactions/v1/query")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("Accept-Encoding", "application/json")
                .addHeader("authorization", "Bearer "+authenticate(appKeySecret))
                .build();

        Response response = client.newCall(request).execute();
        AtomicReference<List<InsertedTransaction>> insertedTransactions = new AtomicReference<>(new ArrayList<>());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String result = response.body().string();

            PullResponse data = objectMapper.readValue(result, PullResponse.class);


            data.getResponse().forEach(innerList -> {
                innerList.forEach(res -> {
                    try {
                       insertedTransactions.set(insertMissingTransactionsToDB(res, new ArrayList<InsertedTransaction>()));
                    } catch (Exception e) {
//                        System.err.println("ERROR INSERTING "+res.getTransactionId());
                    }
                });
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return insertedTransactions.get();

    }

    public void updateDisbursement(String conversationId) {
        String sqlUpdate = "UPDATE loans SET disbursed_at=NOW() WHERE loanAccountMpesa = (SELECT occasion FROM mpesa_b2c WHERE conversation_id = ?) ";
        jdbcTemplateOne.update(sqlUpdate,conversationId);

    }

    public String getCurrentAccountLoanBalance(String conversationId) {
        String sqlBalanceQuery = "SELECT b2c_utility_account_available_funds FROM mpesa_b2c WHERE conversation_id = ?";
        return jdbcTemplateOne.queryForObject(sqlBalanceQuery,(rs,i)-> rs.getString("b2c_utility_account_available_funds"),conversationId);

    }

    public boolean isLoan(String conversationId) {

        String sql = "SELECT id FROM loans WHERE loanAccountMPesa = (SELECT occasion FROM mpesa_b2c WHERE conversation_id=?)";
        try{
            jdbcTemplateOne.queryForObject(sql,(rs,i)-> rs.getString("id"),conversationId);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String getB2cTransactionDescription(String conversationId) {

        String sql1 = "SELECT occasion FROM mpesa_b2c WHERE conversation_id=?";
        String occasion = jdbcTemplateOne.queryForObject(sql1,(rs,i)-> rs.getString("occasion"),conversationId);
        String expenseId = occasion.replaceAll("\\D+", ""); // Remove all non-digit characters

        String sql2 = "SELECT description FROM temp_expense_requests WHERE id=?";
        return jdbcTemplateOne.queryForObject(sql2,(rs,i)-> rs.getString("description"),expenseId);

    }

    record InsertedTransaction(String transactionId, String LoanId){}

    private List<InsertedTransaction> insertMissingTransactionsToDB(MResponse data, List<InsertedTransaction> insertedTransactions){

                String sql = """
                        INSERT INTO mpesa_c2b(TransactionType,TransID,TransTime,TransAmount,BusinessShortCode,BillRefNumber,InvoiceNumber,OrgAccountBalance,
                        ThirdPartyTransID,MSISDN,FirstName,MiddleName,LastName,created_at)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?, NOW())
                    """;

                try{
                    jdbcTemplateOne.update(sql,
                            data.getTransactiontype(),
                            data.getTransactionId(),
                            data.getTrxDate(),
                            data.getAmount(),
                            "4125097",
                            data.getBillreference(),
                            "0",
                            "0",
                            "0",
                            data.getMsisdn(),
                            data.getSender(),
                            data.getSender(),
                            data.getSender()
                    );
                    insertedTransactions.add(new InsertedTransaction( data.getTransactionId(),data.getBillreference()));
                }catch (Exception exception){
                    //record already inserted
                }
                return insertedTransactions;

    }

    Acknowledgement postPullTransactions(String pullResponse, int appId) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        PullResponse data = objectMapper.readValue(pullResponse, PullResponse.class);

//        data.getResponse().forEach(res ->{
//            try {
//                insertMissingTransactionsToDB(res);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });

        return new Acknowledgement(0,"Success");
    }

    public boolean b2cTransactionAlreadyProcessed(Result result) {
        String sql = "SELECT transaction_completed_datetime FROM mpesa_b2c WHERE originator_conversation_id = ? AND conversation_id = ?";

        try {
            String completionTime = jdbcTemplateOne.queryForObject(sql, (rs,i) -> { return rs.getString("transaction_completed_datetime");},
                    result.getOriginatorConversationId(),
                    result.getConversationId());
            return completionTime != null;
        } catch (DataAccessException e) {
            return false;
        }
    }

    public boolean buygoodsTransactionAlreadyProcessed(String originatorConversationId, String conversationId) {
        String sql = "SELECT transaction_id FROM mpesa_buygoods WHERE originator_conversation_id = ? AND conversation_id = ?";

        try {
            String TransactionId = jdbcTemplateOne.queryForObject(sql, (rs,i) -> { return rs.getString("transaction_id");},
                    originatorConversationId,
                    conversationId);
            return TransactionId != null;
        } catch (DataAccessException e) {
            return false;
        }
    }

    public void payToPaybill(int amount, String paybill, String accountRef, String remarks){
        PaybillRequest paybillRequest = new PaybillRequest();
        paybillRequest.setCommandID(MpesaCommandId.BusinessPayBill);
        paybillRequest.setAmount(amount);
        paybillRequest.setPartyB(paybill);
        paybillRequest.setAccountReference(accountRef);
        paybillRequest.setRequester("");
        paybillRequest.setRemarks(remarks);
        PaybillRequestResponse buygoodsRequestResponse = null;
        try {
            buygoodsRequestResponse = initiatePaybill(paybillRequest,3);
            System.out.println(buygoodsRequestResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public void payToTill(int amount, String tillNumber, String accountRef){
        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setCommandID(MpesaCommandId.BusinessBuyGoods);
        request.setAmount(amount);
        request.setPartyB(tillNumber);
        request.setAccountReference(accountRef);
        request.setRequester("");
        request.setRemarks("Payment being processed");
        try {
            initiateBuyGoods(request, 3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public void adjustUserLoanBalance(String loanId){

        String balanceSql = """
                SELECT client_loan_total - IFNULL((select sum(TransAmount) from mpesa_c2b m where (loanAccountMPesa =  m.BillRefNumber)),0) as current_balance
                                FROM loans WHERE loanAccountMPesa =?
                """;
        double currentBalance = jdbcTemplateOne.queryForObject(balanceSql,(rs,i)->{
            return rs.getDouble("current_balance");
        },loanId);

        String sql2 = """
            UPDATE loans SET paid_amount =(
                         SELECT IFNULL((select sum(TransAmount) from mpesa_c2b m where (m.BillRefNumber = ? )),0)
                         )
             WHERE loanAccountMPesa=?
""";
        jdbcTemplateOne.update(sql2,loanId,loanId);

        String sql3 = "UPDATE loans SET loan_balance =? WHERE loanAccountMPesa =?";
        jdbcTemplateOne.update(sql3,currentBalance,loanId);

        String sql4 = """
                UPDATE loans SET last_payment_date = NOW() WHERE loanAccountMPesa =?
                """;

        jdbcTemplateOne.update(sql4,loanId);

    }

    public void sendMoney(String amount, String phone, String occasion) {
        B2CRequest b2CRequest = new B2CRequest();
        b2CRequest.setCommandID(MpesaCommandId.BusinessPayment);
        b2CRequest.setAmount(amount);
        b2CRequest.setPartyB(phone);
        b2CRequest.setRemarks(occasion);
        b2CRequest.setAppId(3);
        b2CRequest.setOccasion(occasion);

        try {
            B2CRequestResponse res = initiateb2c(b2CRequest, 3);
            System.out.println(res.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}