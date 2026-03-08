package com.otblabs.jiinueboda.integrations.momo.pesapal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otblabs.jiinueboda.integrations.momo.pesapal.models.PaymentRequestResponse;
import com.otblabs.jiinueboda.integrations.momo.pesapal.models.Credentials;

import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
public class PesapalPaymentService {

    public String authenticate(Credentials credentials){

        String result =null;

        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        MediaType mediaType = MediaType.parse("application/json");
        try {
            String jsonData = objectMapper.writeValueAsString(credentials);
            RequestBody requestBody = RequestBody.create(mediaType, jsonData);

            String endpointUrl = Constants.Auth_URL_SANDBOX;
            Request request = new Request.Builder()
                    .url(endpointUrl)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("accept","application/json")
                    .build();


            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    result = response.body().string();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
     return result;
    }

    public PaymentRequestResponse submitOnetimePaymentRequest(PaymentInfo paymentInfo){
        return null;
    }

}
