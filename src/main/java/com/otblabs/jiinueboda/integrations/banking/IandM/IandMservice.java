package com.otblabs.jiinueboda.integrations.banking.IandM;

import tools.jackson.databind.ObjectMapper;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.*;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.auth.AuthToken;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.collections.CustomerValidationRequest;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.collections.CustomerValidationResponse;
import com.otblabs.jiinueboda.integrations.banking.IandM.models.mpesa.PaymentRequest;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

@Service
public class IandMservice {
    private final JdbcTemplate jdbcTemplateOne;

    private final MpesaTransactionsService mpesaTransactionsService;

    String IANDM_BASE_URL = "https://dev-api.ds.imbank.com:2284";

    public IandMservice(JdbcTemplate jdbcTemplateOne, MpesaTransactionsService mpesaTransactionsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.mpesaTransactionsService = mpesaTransactionsService;
    }

    public MpesaBankResponseData saveMpesaTransaction(MpesaBankRequestData mpesaBankRequestData, int appId) throws Exception{

        String sql = """
                INSERT INTO mpesa_c2b(PaymentChannel,TransID,BankRefId,TransTime,TransAmount,BillRefNumber,is_manual,MSISDN,BussinessShortCode,appId,created_at,updated_at) 
                
                VALUES (?,?,?,?,?,?,?,?,?,?,NOW(),NOW())
                
                """;

        jdbcTemplateOne.update(sql,
                "IM BANK",
                mpesaBankRequestData.getMpesaRef(),
                mpesaBankRequestData.getTransRef(),
                UtilityFunctions.formatDateTime(mpesaBankRequestData.getTransactiondate()),
                mpesaBankRequestData.getAmount(),
                mpesaBankRequestData.getAccountRef().trim(),
                0,
                mpesaBankRequestData.getMSISDN(),
                mpesaBankRequestData.getSortCode(),
                appId
        );

        MpesaBankResponseData responseData = new MpesaBankResponseData();
        responseData.setResultCode(0);
        responseData.setResultDesc("Successful");
        responseData.setErpRefId("1225647989");
        return responseData;

    }

    public InitB2cResponse initImMpesaB2cTransaction(PaymentRequest paymentRequest, int appId) throws Exception {


        IAndMConfig iAndMConfig = getIandMConfig(appId);

        String serviceName = "MpesaPayment";
        String initChannelID = iAndMConfig.getChannelId();

        String RequestRefNum = generateRequestReffNum();

        String senderAccountNo = iAndMConfig.getSenderAccount();

        String TranAmount = paymentRequest.getTranDetails().getTransAmount();

        String TranCCY = paymentRequest.getTranDetails().getTranCCY();

        String StrToEnc = serviceName;
        StrToEnc = StrToEnc + initChannelID;
        StrToEnc = StrToEnc + RequestRefNum;
        StrToEnc = StrToEnc + senderAccountNo;
        StrToEnc = StrToEnc + TranAmount;
        StrToEnc = StrToEnc + TranCCY;

        ObjectMapper objectMapper = new ObjectMapper();

            String jsonPayload = objectMapper.writeValueAsString(paymentRequest);

            OkHttpClient httpClient = new OkHttpClient();

            String checksumPK;

//            String checksumPK = """
//                    MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQBavLD8XP75ntnTeImiK/n2
//                    HACMs3ZjRJJAgv5fCBcS7PcXzMwSxENbsPc1dbhQQZmvbveZ/FzuizH2WkfXzVDA
//                    DdKEVgkQhw68sQkiI0II5MpvE9tGSN5GC9pPkDO4lZ+0F9J5lDrOSIGei3g7Y56q
//                    MEFewnUGSzIUBEIQe+lyX5OqhaKKu9tkRf95rNYAY3YLhOxr8mmlaVUbQYdCKie0
//                    lGpowT8FYHjCGLDg7FB0rIsuKSm+Apx26X+aUyjW3YbtD/FspuWhPk6Myg5QKR/B
//                    9CPjLg1oAXMqFBTp3LIHXesIlGBUnckPyFWnDPiPiOYBvQLy0plMFTADCGm4WkIh
//                    AgMBAAE=
//                    """;


//            String checkSumPK_prod = """
//                    -----BEGIN PUBLIC KEY-----
//                    MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo0KOXzE8PmmRNO4KLR4G
//                    WvLz7nsuEmxydJ40o9IzdvMcIB4CdAXi59dkE+NwUUYL3OdiX/ZUD8g7Q2l2Mnlp
//                    VpvZlkb79IwoRETyG/uoIi+n6I9YI6QepaCLpyvtAY0/TSIBj1AWj9qeCTX5lxdE
//                    cQ3tVg7ETDNrOyyq7vS2wN53q79y4NVqr3tx6rZmZFp1N1ETc/rgP3VLSqSZ0lig
//                    CrsEgcXRtZpEODNjM+ftSu7630Fa8E/hllEYKbR8aiUcf3MqfaY+8o/KIHWsqaaC
//                    FRM+i6zyNdMKaqQ69Jv2k2u097y21ajhzUwQy6IahC733b2xuk7M1OtUQFcL8RLe
//                    uQIDAQAB
//                    -----END PUBLIC KEY-----
//                    """;

            checksumPK = iAndMConfig.getChecksumPublicKey().trim();
            String checkSum = getChecksum(checksumPK, StrToEnc);


            Request request = new Request.Builder()
                    .url(IANDM_BASE_URL +"/KEPaymentGatewayService/1.0/MakePayment")
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonPayload))
                    .header("serviceName", serviceName)
                    .header("initChannelID", initChannelID)
                    .header("checkSum",checkSum )
                    .header("requestRefNum", RequestRefNum)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getAuthToken(iAndMConfig.getConsumerKey(),iAndMConfig.getConsumerSecret()))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body().string(),InitB2cResponse.class);
            }

    }

    public String getAuthToken(String consumerKey, String consumerSecret) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AuthToken authToken;



            OkHttpClient httpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(IANDM_BASE_URL +"/KEOAuthTokenService/1.0/GetToken")
                    .post(okhttp3.RequestBody.create(null, new byte[0]))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("grant_type","client_credentials")
                    .header("client_secret",consumerSecret)
                    .header("client_id",consumerKey)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                authToken = mapper.readValue(response.body().string(),AuthToken.class);

            }



      return authToken.getAccessToken();
    }

    private String generateRequestReffNum() {

        int referenceLength = 12;
        Random random = new Random();
        StringBuilder transactionReferenceBuilder = new StringBuilder(referenceLength);
        for (int i = 0; i < referenceLength; i++) {
            int digit = random.nextInt(10);
            transactionReferenceBuilder.append(digit);
        }

        return transactionReferenceBuilder.toString();
    }

    private IAndMConfig getIandMConfig(int appId) {
        String sql = "SELECT * FROM i_and_m_config WHERE app_id = ?";

        try{
            return jdbcTemplateOne.queryForObject(sql, (resultSet, i) -> setIandMConfig(resultSet),appId);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    private IAndMConfig setIandMConfig(ResultSet resultSet) throws SQLException {
        IAndMConfig iAndMConfig = new IAndMConfig();
        iAndMConfig.setId(resultSet.getInt("id"));
        iAndMConfig.setAppId(resultSet.getInt("app_id"));
        iAndMConfig.setCifNumber(resultSet.getString("cif_number"));
        iAndMConfig.setChannelId(resultSet.getString("channel_id"));
        iAndMConfig.setSenderAccount(resultSet.getString("sender_account"));
        iAndMConfig.setConsumerKey(resultSet.getString("consumer_key"));
        iAndMConfig.setConsumerSecret(resultSet.getString("consumer_secret"));
        iAndMConfig.setChecksumPublicKey(resultSet.getString("checksum_public_key"));
        return iAndMConfig;
    }

    public static String getChecksum(String PublicKey, String StrToEnc) {
        String ArrivedHash = "";
        try {
            ArrivedHash = encryptDataRSA(StrToEnc, PublicKey);
        } catch (Exception e) {
            ArrivedHash = "NA";
        }
        return ArrivedHash;
    }

    private static String encryptDataRSA(String message, String PubKey) throws InvalidAlgorithmParameterException, InvalidAlgorithmParameterException {
        String encodedEncryptedBytes = "";
        String hashString = "";
        try {
            byte[] encryptedBytes = encryptRSA(message.getBytes(), PubKey);
            byte[] base64Bytes = Base64.encodeBase64(encryptedBytes, false);
            encodedEncryptedBytes = new String(base64Bytes);
            hashString = DigestUtils.sha256Hex(encodedEncryptedBytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return hashString;
    }

    private static byte[] encryptRSA(byte[] data, String pub) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        PublicKey pubKey = null;
        byte[] publicBytes = Base64.decodeBase64(pub.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        try {
            pubKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cipher ciph = Cipher.getInstance("RSA/ECB/NoPadding");
        ciph.init(Cipher.ENCRYPT_MODE, pubKey);
        return ciph.doFinal(data);
    }

    public CustomerValidationResponse getPaymentValidation(String data, int appId) {

        ObjectMapper objectMapper = new ObjectMapper();
        CustomerValidationRequest request = objectMapper.readValue(data, CustomerValidationRequest.class);
        CustomerValidationResponse customerValidationResponse = new CustomerValidationResponse();
        customerValidationResponse.setResultCode(0);
        customerValidationResponse.setResultDesc("Customer Available");
        customerValidationResponse.setBalance(0);
        customerValidationResponse.setCustomerName("");
        customerValidationResponse.setCustomerID(request.getCustomerId());
        return customerValidationResponse;


    }
}
