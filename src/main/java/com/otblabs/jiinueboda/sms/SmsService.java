package com.otblabs.jiinueboda.sms;

import com.otblabs.jiinueboda.collections.models.LoansByAge;
import com.otblabs.jiinueboda.integrations.IncomingPaymentConfirmation;
import com.otblabs.jiinueboda.loans.models.LoanPayeeDetail;
import com.otblabs.jiinueboda.loans.models.PendingDisbursement;
import com.otblabs.jiinueboda.loans.models.UserLoanDetail;
import com.otblabs.jiinueboda.users.models.Signatory;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsService {

    private final JdbcTemplate jdbcTemplateOne;
    private final SmsCore smsCore;

    public SmsService(JdbcTemplate jdbcTemplateOne, SmsCore smsCore) {
        this.smsCore = smsCore;
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public void sendMessageToBorrower(String recieverName, int transactionAmount, LoanPayeeDetail loanPayeeDetail) {

        try {

            String messageToUser = "Dear "+recieverName+", your KES: "+ transactionAmount + " loan is disbursed." +
                    "Pay KES "+loanPayeeDetail.getDailyPayment()+" daily to Paybill "+loanPayeeDetail.getShotcode()+" Acc "+loanPayeeDetail.getOccasion()+". Help call: 0781000201  – Jiinue Boda";

            SystemUser systemUser = new SystemUser();
            systemUser.setAppId(loanPayeeDetail.getAppId());
            systemUser.setPhone(loanPayeeDetail.getPartyB());

            ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
            apiMessageDTO.setReciver(systemUser.getPhone());
            apiMessageDTO.setMessage(messageToUser);

            smsCore.sendSingleTransactionalSms(apiMessageDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageToReciever(String recieverName, int transactionAmount, String phone, int appId, String description) {

        try {

            String messageToUser = "Dear "+recieverName+", You have recieved KES: "+ transactionAmount +". for  "+description;

            SystemUser systemUser = new SystemUser();
            systemUser.setAppId(appId);
            systemUser.setPhone(phone);

            ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
            apiMessageDTO.setReciver(systemUser.getPhone());
            apiMessageDTO.setMessage(messageToUser);

            smsCore.sendSingleTransactionalSms(apiMessageDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageToSignatories(int appId, int transactionAmount, String recieverName, String orgBalance,List<Signatory> signatories) {

        signatories.forEach(signatory -> {

            try{

                String message;
                if (signatory.getLevel() == 0) {
                    message = "Successfully disbursed amount : " + transactionAmount + " to " + recieverName + " Current Organisation Account Balance : " + orgBalance;
                } else {
                    message = "Successfully disbursed amount : " + transactionAmount + " to " + recieverName;
                }

                ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
                apiMessageDTO.setReciver(signatory.getNotificationNumber());
                apiMessageDTO.setMessage(message);
                smsCore.sendSingleTransactionalSms(apiMessageDTO);

            }catch (Exception exception){
                exception.printStackTrace();
            }
        });


    }

    public void sendPaymentConfirmationMessage(IncomingPaymentConfirmation incomingPaymentConfirmation, UserLoanDetail userLoanDetail, SystemUser systemUser) throws Exception {

        String message = "Your payment of "+ incomingPaymentConfirmation.getTransactionAmount() +" for account " +incomingPaymentConfirmation.getLoanAccount() +" has been received. mpesa ref "+incomingPaymentConfirmation.getTransactionId() +
                ". Total loan paid "+userLoanDetail.getTotalPaid()+".  loan balance "+userLoanDetail.getBalance() +". Paybill "+userLoanDetail.getShotcode() +" Account "+incomingPaymentConfirmation.getLoanAccount();

        ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
        apiMessageDTO.setMessage(message);
        apiMessageDTO.setReciver(systemUser.getPhone());
        smsCore.sendSingleTransactionalSms(apiMessageDTO);
    }

    public  void sendFuelMessageToFuelBorrower(String transactionId, String conversationID) throws Exception {

        try {
            String sql = "SELECT T1.userId,T2.name,T1.appID , (select phone from users WHERE id = T1.userId) as phone,fuel_amount_purchased FROM fuel_loan T1 LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id\n" +
                    "WHERE mpesa_disburse_conversation_id =?";

            jdbcTemplateOne.queryForObject(sql,(rs,i)->{
                int userId = rs.getInt("userId");
                String petrolStationName = rs.getString("name");
                String phoneNmber = rs.getString("phone");
                double amount = rs.getInt("fuel_amount_purchased");
                int appId = rs.getInt("appID");

                String messageToUser = "Congratulations! your fuel purchase of  KES: "+ amount +
                        ". With transactionId "+transactionId+" has been successfully purchased to " +petrolStationName;


                ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
                apiMessageDTO.setReciver(phoneNmber);
                apiMessageDTO.setMessage(messageToUser);

                try {
                    smsCore.sendSingleTransactionalSms(apiMessageDTO);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return null;
            },conversationID);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public  void sendFuelLoanPaymentConfirmationMessage(IncomingPaymentConfirmation incomingPaymentConfirmation, SystemUser user) throws Exception {
//
//        try {
//                String messageToUser = "Congratulations! your payment  of  KES: "+ incomingPaymentConfirmation.getTransactionAmount() +
//                        ". With transactionId "+incomingPaymentConfirmation.getTransactionId()+" has been successfully recieved Keep using Jifuel";
//
//                List<Contact> contactList = new ArrayList<>();
//                Contact contact = new Contact();
//                contact.setNumber(user.getPhone());
//                contact.setBody(messageToUser);
//                contact.setSms_type("plain");
//                contactList.add(contact);
//                MessageData messageData = new MessageData();
//                messageData.setContact(contactList);
//
//                try {
//                    smsCore.sendSingleTransactionalSms(user.getAppId(),messageData);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

//    public  void sendFuelMessageToSignatories(String transactionId, String conversationID, List<Signatory> signatories) throws Exception {
//        List<Contact> contactList = new ArrayList<>();
//
//        String sql = "SELECT T1.userId,T2.name,T1.appID ,(SELECT InitiatorAccountCurrentBalance FROM mpesa_buygoods WHERE originator_conversation_id = T1.mpesa_disburse_conversation_id) as InitiatorAccountCurrentBalance,(select phone from users WHERE id = T1.userId) as phone,fuel_amount_purchased FROM fuel_loan T1 LEFT JOIN petrol_station T2 ON T1.petrolStationID = T2.id\n" +
//                "WHERE mpesa_disburse_conversation_id =?";
//
//        jdbcTemplateOne.queryForObject(sql,(rs,i)->{
//            int userId = rs.getInt("userId");
//            String petrolStationName = rs.getString("name");
//            String phoneNmber = rs.getString("phone");
//            double amount = rs.getInt("fuel_amount_purchased");
//            int appId = rs.getInt("appID");
//            double InitiatorAccountCurrentBalance = rs.getDouble("InitiatorAccountCurrentBalance");
//
//            for (Signatory signatory : signatories) {
//
//                String message;
//                if (signatory.getLevel() == 0) {
//                    message = "Successfully disbursed amount : " + amount + " to " + petrolStationName + " Current Organisation Account Balance : " + InitiatorAccountCurrentBalance;
//                } else {
//                    message = "Successfully disbursed amount : " + amount + " to " + petrolStationName;
//                }
//
//                Contact contact = new Contact();
//                contact.setNumber(signatory.getNotificationNumber());
//                contact.setBody(message);
//                contact.setSms_type("plain");
//                contactList.add(contact);
//            }
//
//            if (!contactList.isEmpty()) {
//
//                try {
//                    MessageData messageData = new MessageData();
//                    messageData.setContact(contactList);
//                    smsCore.sendSingleTransactionalSms(appId, messageData);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            return null;
//        },conversationID);
//    }

    public String sendSingleAPITransactionalMessage(ApiMessageDTO apiMessageDTO) throws Exception {
        return smsCore.sendSingleTransactionalSms(apiMessageDTO);
    }

    public String sendSingleAPIPromotionalMessage(ApiMessageDTO apiMessageDTO) throws Exception {
        return smsCore.sendSinglePromotionalSms(apiMessageDTO);
    }

    public String sendBulkTransactionalSms(List<ApiMessageDTO> apiMessageDTO) throws Exception {
        return smsCore.sendBulkTransactionalSms(apiMessageDTO);
    }

    public String sendBulkAPIPromotionalMessage(List<ApiMessageDTO> apiMessageDTO) {
        return smsCore.sendBulkPromotionalSms(apiMessageDTO);
    }

    public void sendLoanApplicationConfirmation(String phone) throws Exception {

        String message = """
            Jiinue Boda: We confirm receipt of your loan application.
            Our team is reviewing your details and will contact you shortly.
            For inquiries, call 0725000201  or 0781000201
        """;

        ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
        apiMessageDTO.setReciver(phone);
        apiMessageDTO.setMessage(message);
        smsCore.sendSingleTransactionalSms(apiMessageDTO);
    }

    public void sendUserWelcomeMessage(String firstName,String phone){

        String message = """
                        Hello, %s
                        Thank you for choosing Jiinue Boda. To proceed with your application, please visit our office with your motorcycle for valuation,
                        original logbook, and National ID. For assistance, call 0781000201 or 0725000201.
                """.formatted(firstName.toUpperCase());


       ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
       apiMessageDTO.setReciver(phone);
       apiMessageDTO.setMessage(message);

        try{
            smsCore.sendSingleTransactionalSms(apiMessageDTO);
        }catch (Exception ignored){}

    }

    public void sendMessageForDelayedDisbursements(PendingDisbursement loan, String phone) throws Exception {
        String message = "Dear "+loan.getFirstName()+", we’re upgrading our system to serve you better. As a result, " +
                "disbursements may take slightly longer than usual. Rest assured, your funds will be processed. Thank you for your patience and understanding.";

        ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
        apiMessageDTO.setReciver(phone);
        apiMessageDTO.setMessage(message);

        smsCore.sendSingleTransactionalSms(apiMessageDTO);

    }

    public void sendDailyReminder(List<LoansByAge> pendingLoanUserDetailList) {

        pendingLoanUserDetailList.forEach(pendingLoanUserDetail -> {

            try {
                String reminderMessage = buildSmsMessage(
                        pendingLoanUserDetail,
                        pendingLoanUserDetail.getVariance(),
                        pendingLoanUserDetail.getVariance(),
                        "4125097"
                );

                ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
                apiMessageDTO.setMessage(reminderMessage);
                apiMessageDTO.setReciver(pendingLoanUserDetail.getPhone());
                smsCore.sendSingleTransactionalSms(apiMessageDTO);


            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public String buildSmsMessage(LoansByAge loan, double amountDue, double arrearsAmount, String paybill) {
        String firstName = loan.getFirstName();
        String accountNo = loan.getAccount();
        int daysInArrears = loan.getVarRatio();

        if (daysInArrears <= 1) {
            // Day 1 - Courtesy reminder
            return String.format(
                    "Dear %s, our records indicate that today's repayment of KES %.2f was not received. " +
                            "Kindly make payment at your earliest convenience. Paybill: %s a/c no: %s",
                    firstName, amountDue, paybill, accountNo
            );

        } else if (daysInArrears == 2) {
            // Day 2 - Gentle follow-up
            return String.format(
                    "Dear %s, your loan account shows arrears of KES %.2f (2 days). " +
                            "Please make payment to avoid further accumulation. Paybill %s A/c no: %s",
                    firstName, arrearsAmount, paybill, accountNo
            );

        } else if (daysInArrears == 3) {
            // Day 3 - Payment advisory
            return String.format(
                    "Dear %s, your Jiinue loan is 3 days in arrears with an outstanding amount of KES %.2f. " +
                            "Please pay via Paybill %s, Account %s.",
                    firstName, arrearsAmount, paybill, accountNo
            );

        } else if (daysInArrears <= 5) {
            // Day 5 - Account status notice
            return String.format(
                    "Dear %s, your loan account is currently 5 days overdue with arrears of KES %.2f. " +
                            "Kindly regularize your account to avoid escalation to recovery under the loan agreement. " +
                            "Please pay via Paybill %s, Account %s.",
                    firstName, arrearsAmount, paybill, accountNo
            );

        } else if (daysInArrears <= 7) {
            // Day 7 - Escalation notice
            return String.format(
                    "Dear %s, your loan is 7 days in arrears with an outstanding amount of KES %.2f. " +
                            "If payment is not received, your account will be referred for recovery in line with agreed terms. " +
                            "Please pay via Paybill %s, Account %s. For assistance, call 0725000201 / 0781000201.",
                    firstName, arrearsAmount, paybill, accountNo
            );

        } else if (daysInArrears <= 10) {
            // Day 10 - Recovery advisory
            return String.format(
                    "Dear %s, your loan account remains unpaid after 10 days. Recovery processes may commence as " +
                            "provided in your loan agreement. Please contact us urgently to discuss payment options. " +
                            "Paybill %s, Account %s.",
                    firstName, paybill, accountNo
            );

        } else {
            // Recovery Review - Inform without intimidation
            return String.format(
                    "Dear %s, this is to inform you that due to outstanding arrears of KES %.2f, your account " +
                            "has been flagged for recovery review. Payment may regularize the account.",
                    firstName, arrearsAmount
            );
        }
    }


    public void sendLoginOTPSms(String phoneNumber, String message) throws Exception {
        ApiMessageDTO apiMessageDTO = new ApiMessageDTO();
        apiMessageDTO.setReciver(phoneNumber);
        apiMessageDTO.setMessage(message);
        smsCore.sendSingleTransactionalSms(apiMessageDTO);
    }

    public void sendDaily8PmLoanUpdate() throws Exception {
        String sql = """
                SELECT
                    u.phone AS phone_number,
                    CONCAT(
                        'Dear ', u.first_name,
                        ', your loan is ', DATEDIFF(NOW(), disbursed_at),
                        ' days active. Arrears: KES ',
                        GREATEST((expected_amount - paid_amount), 0),
                        '. Balance: KES ', loan_balance,
                        '. Kindly pay today via Paybill 4125097 A/C: ', l.loanAccountMPesa,
                        '. For support call 0725000201 / 0781000201.'
                    ) AS message
                
                FROM loans l
                LEFT JOIN users u ON u.id = l.userID
                WHERE loan_balance > 0
                  AND l.loanAccountMPesa NOT IN (SELECT loan_id FROM special_cases);
                """;

        List<ApiMessageDTO> apiMessageDTOList = jdbcTemplateOne.query(sql,
                (rs,i)-> new ApiMessageDTO(rs.getString("phone_number"), rs.getString("message"))
        );
        smsCore.sendBulkTransactionalSms(apiMessageDTOList);
    }
}
