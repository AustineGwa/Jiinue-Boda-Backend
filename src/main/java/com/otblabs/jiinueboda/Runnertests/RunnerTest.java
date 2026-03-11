package com.otblabs.jiinueboda.Runnertests;

import com.otblabs.jiinueboda.accounting.hr.PayrollService;
import com.otblabs.jiinueboda.collections.CollectionsService;
import com.otblabs.jiinueboda.collections.models.LoansByAge;
import com.otblabs.jiinueboda.customerassignments.CollectionAssinmentService;
import com.otblabs.jiinueboda.directors.DirectorService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.hashing.MssidHash;
import com.otblabs.jiinueboda.investors.InvestmentManagementService;
import com.otblabs.jiinueboda.jifuel.FuelLoanService;
import com.otblabs.jiinueboda.jifuel.models.PendingJifuel;
import com.otblabs.jiinueboda.jiinue.LoanManagementService;
import com.otblabs.jiinueboda.sms.ApiMessageDTO;
import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.sms.providers.ampletech.Contact;
import com.otblabs.jiinueboda.sms.providers.ampletech.MessageData;
import com.otblabs.jiinueboda.staff.StaffService;
import com.otblabs.jiinueboda.assets.tracking.activations.ActivationService;
import com.otblabs.jiinueboda.assets.tracking.trackerExcel.BulkSimCardNumberExtractor;
import com.otblabs.jiinueboda.users.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RunnerTest {

    private final MpesaTransactionsService mpesaTransactionsService;
    private final CollectionAssinmentService collectionAssinmentService;
    private final FuelLoanService fuelLoanService;
    private final InvestmentManagementService investmentManagementService;
    private final SmsService smsService;
    private final DirectorService directorService;
    private final UserService userService;
    private final LoanManagementService loanManagementService;
    private final PayrollService payrollService;
    private final ActivationService activationService;
    private final BulkSimCardNumberExtractor bulkSimCardNumberExtractor;
    private final MssidHash mssidHash;
    private final StaffService staffService;
    private final JdbcTemplate jdbcTemplateOne;
    private final CollectionsService collectionsService;

    public RunnerTest(MpesaTransactionsService mpesaTransactionsService,
                      CollectionAssinmentService collectionAssinmentService,
                      FuelLoanService fuelLoanService,
                      InvestmentManagementService investmentManagementService,
                      SmsService smsService,
                      DirectorService directorService,
                      UserService userService, LoanManagementService loanManagementService, PayrollService payrollService,
                      ActivationService activationService, BulkSimCardNumberExtractor bulkSimCardNumberExtractor,
                      MssidHash mssidHash, StaffService staffService, JdbcTemplate jdbcTemplateOne, CollectionsService collectionsService) {

        this.mpesaTransactionsService = mpesaTransactionsService;
        this.collectionAssinmentService = collectionAssinmentService;
        this.fuelLoanService = fuelLoanService;
        this.investmentManagementService = investmentManagementService;
        this.smsService = smsService;
        this.directorService = directorService;
        this.userService = userService;
        this.loanManagementService = loanManagementService;
        this.payrollService = payrollService;
        this.activationService = activationService;
        this.bulkSimCardNumberExtractor = bulkSimCardNumberExtractor;
        this.mssidHash = mssidHash;
        this.staffService = staffService;
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.collectionsService = collectionsService;
    }

	public void sendParentsPackage(){
		directorService.sendParentsPackage();
	}

	public void sendDirectorsPackage(){
		directorService.sendPersonalPackage();
	}

    public void paySalaries() throws Exception {
        staffService.getAllStaff().forEach(staff -> {
            staffService.stageStaffSalary(staff);
        });
    }

    void sendMessageForPendingDisbursements() {
        var allLoans = loanManagementService.getAllPendingDisburesments();
        System.out.println("Total pending ="+allLoans.size());
        allLoans.forEach(loan -> {
            String phone = userService.getUserByID(loan.getUserId()).getPhone();
            try {
                smsService.sendMessageForDelayedDisbursements(loan, phone);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("complete");
    }

    void sendPromotionForInactiveClients() throws Exception {

        String message = """
          Mid-term imeisha, shule zinafunguliwa. Usikwame na fees! Jiinue Boda inakupea loan na logbook ya boda. Call 0725000201 ama 0781000201
          """;

        List<String> users = userService.getAllInactiveClientsWithPastLoan()
                .stream()
                .map(user -> user.getPhone())
                .collect(Collectors.toList());

        List<String> internalStaff = staffService.getInternalStaff()
                .stream()
                .map(staff -> staff.getPhone())
                .collect(Collectors.toList());

        users.addAll(internalStaff);

        List<ApiMessageDTO> apiMessageDTOList = users.stream()
                .map(number -> new ApiMessageDTO(number, message))
                .collect(Collectors.toList());

        smsService.sendBulkAPIPromotionalMessage(apiMessageDTOList);
    }

    public void sendPromotionForFirstTimeLoans(){

    String sql = """
            SELECT first_name, phone FROM users WHERE id NOT IN (SELECT userID from loans )
            AND id NOT IN (1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 33, 482)
            AND deleted_at is null
            AND phone is not null
            """;

    record UserPhoneHolder(String firstName, String phone){}

    var users = jdbcTemplateOne.query(sql,(rs,i)-> new UserPhoneHolder(rs.getString("first_name"),rs.getString("phone")));

    int allUsers = users.size();

    users.forEach(user ->{

             String message =  "Hi "+ user.firstName +", need quick cash? Get up to Ksh 30,000 with your Boda Boda logbook and " +
                     "pay as low as Ksh 200 daily! Fast approval in 4 hours. Call 0781000201 or 0708484028 to apply now! Happy customer service week";

             String message1 =  "Habari "+ user.firstName + ", Pata mkopo wa hadi Ksh 30,000 na logbook ya pikipiki yako. Lipia Ksh 200 pekee kila siku." +
                     " Usifinyike, piga 0781000201 ama 0708484028 kuomba mkopo sasa hivi!";

        String message2 = "Habari "+ user.firstName + ",Fungua biashara ya side hustle! Pata mkopo wa hadi Ksh 30,000 na logbook ya pikipiki yako. " +
                "Lipia Ksh 200 pekee kila siku. Usifinyike, piga 0781000201 kuomba mkopo sasa hivi";

        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact();
        contact.setNumber(user.phone);
        contact.setBody(message);
        contact.setSms_type("plain");
        contactList.add(contact);

        MessageData messageData = new MessageData();
        messageData.setContact(contactList);

        try{
//            smsService.sendPromotionalSms(messageData);
            System.out.println("Sent message to "+user.firstName +" of phone " +user.phone + " remaining "+allUsers);
        }catch (Exception ignored){}

    });

    System.out.println("Finished sending messages \n======>");

}

	public void sendPaymentReminderForVariance15() {

		List<LoansByAge> pendingLoanUserDetailList = collectionsService.getLoansByVariance(0)
				.stream()
				.filter(loan -> loan.getLoanAge() < loan.getLoanTerm())
				.filter(loan -> loan.getVarRatio() < 21)
				.toList();

		System.out.println("total count "+ pendingLoanUserDetailList.size());

		pendingLoanUserDetailList.forEach(pendingLoanUserDetail -> {

			try{

				String reminderMessage = "Hello "+pendingLoanUserDetail.getFirstName()+", We value your progress. Paying off  your current loan arrears of "+pendingLoanUserDetail.getVariance()+" , will enable you to access higher\n" +
						" loan limits in future  for school fees, farming, or emergencies. Call us for help.";

				System.out.println(reminderMessage);

				List<Contact> contactList = new ArrayList<>();
				Contact contact = new Contact();
				contact.setNumber(pendingLoanUserDetail.getPhone());
				contact.setBody(reminderMessage);
				contact.setSms_type("plain");
				contactList.add(contact);

				MessageData messageData = new MessageData();
				messageData.setContact(contactList);
				try{
//					sendSms(3, messageData);
				}catch (Exception ignored){

				}

			}catch (Exception exception){
				exception.printStackTrace();
			}
		});
	}

	public void sendPaymentReminderForOverdues() {

		List<LoansByAge> pendingLoanUserDetailList = collectionsService.getLoansByVariance(0)
				.stream()
				.filter(loan -> loan.getLoanAge() > loan.getLoanTerm())
				.toList();

		System.out.println(" loan count "+ pendingLoanUserDetailList.size());

		pendingLoanUserDetailList.forEach(pendingLoanUserDetail -> {

			try{

				String reminderMessage = "Dear "+ pendingLoanUserDetail.getFirstName() +", kindly complete your current loan to keep your account active " +
                        "and qualify for a school fees, farming, or emergency top-up loan. We’re here to support you. Call for help.";

				System.out.println(reminderMessage);

				List<Contact> contactList = new ArrayList<>();
				Contact contact = new Contact();
				contact.setNumber(pendingLoanUserDetail.getPhone());
				contact.setBody(reminderMessage);
				contact.setSms_type("plain");
				contactList.add(contact);

				MessageData messageData = new MessageData();
				messageData.setContact(contactList);
				try{
//					sendSms(3, messageData);
				}catch (Exception ignored){

				}

			}catch (Exception exception){
				exception.printStackTrace();
			}
		});

		System.out.println("completed");
	}

	public void sendDailyPaymentReminderForRedZone() {
		String sql = """
                SELECT  u.first_name,u.phone, (expected_amount-paid_amount) as variance FROM loans l LEFT JOIN users u ON u.id = l.userID
                          WHERE ((expected_amount - paid_amount)/daily_amount_expected) > 9 AND l.loanAccountMPesa NOT IN (SELECT loan_account FROM bike_recovery WHERE is_excempted =1)
                """;

		jdbcTemplateOne.query(sql,(rs,i)->{

			System.out.println("SENDING MESSAGE TO " +rs.getString("first_name") +"\n =========");
			String reminderMessage = "Dear " +rs.getString("first_name") + ", Please settle your Jiinue loan arrears of KSH "+rs.getInt("variance") +
					" by end of day to avoid recovery of your asset at your expense." +
					"\nFor any queries, call us on  0708484028";

			System.out.println(reminderMessage);

			List<Contact> contactList = new ArrayList<>();
			Contact contact = new Contact();
			contact.setNumber(rs.getString("phone"));
			contact.setBody(reminderMessage);
			contact.setSms_type("plain");
			contactList.add(contact);

			MessageData messageData = new MessageData();
			messageData.setContact(contactList);
			try{
//				sendSms(3, messageData);
			}catch (Exception ignored){

			}
			return null;
		});


	}

	public void sendPaymentReminderForDSLP(int dslp) {

		String sql = """
                 SELECT  u.first_name,u.phone, (expected_amount-paid_amount) as variance, DATEDIFF(NOW(),disbursed_at) AS loanAge FROM loans l LEFT JOIN users u ON u.id = l.userID
                                     WHERE DATEDIFF(Now(),last_payment_date) > ? AND loan_balance > 0 and (expected_amount-paid_amount) > 0
                                     AND l.loanAccountMPesa NOT IN (SELECT loan_account FROM bike_recovery WHERE is_excempted =1 OR bike_recoverd_at is not null)
                """;
		jdbcTemplateOne.query(sql,(rs,i) -> {

			System.out.println("SENDING MESSAGE TO " +rs.getString("first_name") +"\n =========");
			String reminderMessage = "Dear " +rs.getString("first_name")  + ", you have not made any jiinue payment in the last "+dslp+" days. please pay  "+rs.getInt("variance") +
					" by end of day to get back on track with your payments and avoid recovery on your asset" +
					"\nFor any queries, call us on  0708484028";

			List<Contact> contactList = new ArrayList<>();
			Contact contact = new Contact();
			contact.setNumber(rs.getString("phone"));
			contact.setBody(reminderMessage);
			contact.setSms_type("plain");
			contactList.add(contact);

			MessageData messageData = new MessageData();
			messageData.setContact(contactList);
			try{
//				sendSms(3, messageData);
			}catch (Exception ignored){

			}

			return null;

		},dslp);
	}

	public void sendJifuelPaymentRemider(){

		getPendingJifuelPayments().forEach(loan -> {

			try{
				String reminderMessage = "Hello "+loan.getFirstName()+" "+loan.getLastName() +" Your jifuel advance of "+ loan.getLoanPrincipal() +
						" is still pending, please make a payment today to qualify for better deals.\n" +
						"Paybill number 4125097"+
						"\naccount number "+loan.getLoanId()+
						"\nFor any queries, call us on {enquiryNumber}";

				List<Contact> contactList = new ArrayList<>();
				Contact contact = new Contact();
				contact.setNumber(loan.getPhone());
				contact.setBody(reminderMessage);
				contact.setSms_type("plain");
				contactList.add(contact);

				MessageData messageData = new MessageData();
				messageData.setContact(contactList);

				try{
//					sendSms(loan.getAppId(), messageData);
				}catch (Exception ignored){}

			}catch (Exception exception){
				exception.printStackTrace();
			}
		});
	}

	public Iterable<PendingJifuel> getPendingJifuelPayments() {
		String sql = """
                  SELECT T1.loanID, T1.loanPrincipal,x.first_name, x.middle_name, x.last_name, x.app_id,x.phone FROM fuel_loan T1
                                       LEFT JOIN (
                                       SELECT id, first_name, middle_name, last_name, app_id,phone FROM users
                                       ) x ON T1.userID = x.id
                                       WHERE T1.disbursedAt is not null
                                       AND T1.payedAt is null
                                       AND  T1.loanID NOT IN(SELECT BillRefNumber FROM mpesa_c2b)
                """;
		return jdbcTemplateOne.query(sql,(rs,i)->setPendingJifuel(rs));
	}

	public PendingJifuel setPendingJifuel(ResultSet rs) throws SQLException {
		PendingJifuel pendingJifuel = new PendingJifuel();
		pendingJifuel.setFirstName(rs.getString("first_name"));
		pendingJifuel.setMiddleName(rs.getString("middle_name"));
		pendingJifuel.setLastName(rs.getString("last_name"));
		pendingJifuel.setPhone(rs.getString("phone"));
		pendingJifuel.setAppId(rs.getInt("app_id"));
		pendingJifuel.setLoanId(rs.getString("loanID"));
		pendingJifuel.setLoanPrincipal(rs.getInt("loanPrincipal"));
		return pendingJifuel;
	}

    public void topUpLoan(int amount,String phone, String loanId) {
        mpesaTransactionsService.sendMoney(String.valueOf(amount),phone,loanId);
    }

	public void sendSogomoMonthlyInterest(){
		investmentManagementService.sendPeriodicInterestToInvestor("542542","03405413296150");
	}
}
