package com.fintech.banking.banking.ncba;

import com.fintech.banking.banking.ncba.models.NcbaMpesaDisbursement;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentsService {


    private final RestTemplate restTemplate;

    public PaymentsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String makePayment(NcbaMpesaDisbursement paymentRequest) {

        String apiKey="";
        String apiSecret="";
        String endpointUrl="";

        try {
            // Set the headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("APIkey", apiKey);
            headers.set("apiSecret", apiSecret);

            // Create the request entity with the headers and body
            HttpEntity<NcbaMpesaDisbursement> requestEntity = new HttpEntity<>(paymentRequest, headers);

            // Make the POST request
            ResponseEntity<String> responseEntity = restTemplate.exchange(endpointUrl, HttpMethod.POST, requestEntity, String.class);

            // Return the response body
            return responseEntity.getBody();
        } catch (Exception e) {
            // Handle the exception (e.g., logging, custom exception, etc.)
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
