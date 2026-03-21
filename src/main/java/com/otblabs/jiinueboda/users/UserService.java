package com.otblabs.jiinueboda.users;

import com.otblabs.jiinueboda.auth.LoggedInUser;
import com.otblabs.jiinueboda.loans.models.LoanPayeeDetail;
import com.otblabs.jiinueboda.loans.models.UserLoanDetail;
import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.staff.NewStaffRequest;
import com.otblabs.jiinueboda.users.models.*;
import com.otblabs.jiinueboda.users.profile.UserKyc;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplateOne;
    private final SmsService smsService;
    private final BCryptPasswordEncoder passwordEncoder;


    public UserService(JdbcTemplate jdbcTemplateOne, SmsService smsService, BCryptPasswordEncoder passwordEncoder) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.smsService = smsService;
        this.passwordEncoder = passwordEncoder;
    }

    List<SystemUser> getAllUsers() throws Exception{
        String sql = """
                SELECT u.id,u.app_id, first_name, middle_name, last_name, nationalId,email,phone,dob,is_online_rider, password,created_at,group_id, patner_id,
                ur.usertype FROM users u left JOIN  user_roles ur on u.id = ur.user_id WHERE deleted_at is null
                """;

        return jdbcTemplateOne.query(sql,(rs,i) ->setUser(rs));
    }

    List<SystemUser> getAllUsersForPartner(int partnerId) throws Exception{
        String sql = """
                SELECT u.id,u.app_id, first_name, middle_name, last_name, nationalId,email,phone, password,created_at,group_id, patner_id,
                ur.usertype FROM users u left JOIN  user_roles ur on u.id = ur.user_id WHERE deleted_at is null AND u.patner_id =? ORDER BY created_at Desc
                """;

        return jdbcTemplateOne.query(sql,(rs,i) ->setUser(rs),partnerId);
    }

    List<SystemUser> getAllUsersRefferals(String refferalId) throws Exception{
        String sql = """
                SELECT u.id,u.app_id, first_name, middle_name, last_name, nationalId,email,phone, password,created_at,group_id, patner_id,
                                ur.usertype FROM users u left JOIN  user_roles ur on u.id = ur.user_id WHERE deleted_at is null AND u.reffererd_by =?
                                ORDER BY created_at Desc
                """;

        return jdbcTemplateOne.query(sql,(rs,i) ->setUser(rs),refferalId);
    }

    @Transactional
    public Object createNewUserWithRole(NewUserRequest systemUser) throws Exception{


        String sql1 = """
         INSERT INTO users(first_name, middle_name, last_name, email,reffererd_by, phone, password, nationalId, created_at,
         created_by, group_id, app_id,refferal_id, patner_id,dob,gender,alternative_phone,joining_chanel)
         VALUES (?,?,?,?,?,?,?,?,NOW(),?,?,?,?,?,?,?,?,?)
        """;

        String refferalCode = getRefferalCode(5);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, systemUser.getFirstName());
            ps.setString(2, systemUser.getMiddleName());
            ps.setString(3, systemUser.getLastName());
            ps.setString(4, systemUser.getEmail());
            ps.setString(5,systemUser.getRefferedBy());
            ps.setString(6, UtilityFunctions.formatPhoneNumber(systemUser.getPhone()));
            ps.setString(7, passwordEncoder.encode(systemUser.getNationalID()));
            ps.setString(8, systemUser.getNationalID());
            ps.setInt(9, systemUser.getCreatedBy());
            ps.setInt(10, systemUser.getGroupId());
            ps.setInt(11, 3);
            ps.setString(12,refferalCode);
            ps.setInt(13, systemUser.getPartnerId());
            ps.setString(14,systemUser.getDob());
            ps.setString(15,systemUser.getGender());
            ps.setString(16, systemUser.getAlternativeNumber());
            ps.setString(17,systemUser.getJoiningChanel());
            return ps;
        }, keyHolder);

        int userId = keyHolder.getKey().intValue();

        String sql2 = "INSERT INTO user_roles(user_id, usertype, uproval_level) VALUES (?,?,0)";
        jdbcTemplateOne.update(sql2, userId, "Client");

        smsService.sendUserWelcomeMessage(systemUser.getFirstName(), UtilityFunctions.formatPhoneNumber(systemUser.getPhone()));

        return userId;
    }

    @Transactional
    public Object createNewStaff(NewStaffRequest systemUser) throws Exception{


        String sql1 = """
         INSERT INTO users(first_name, middle_name, last_name, email,reffererd_by, phone, password, nationalId, created_at,
         created_by, group_id, app_id,refferal_id, patner_id,dob,gender)
         VALUES (?,?,?,?,?,?,?,?,NOW(),?,?,?,?,?,?,?)
        """;

        String refferalCode = getRefferalCode(5);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, systemUser.getFirstName());
            ps.setString(2, systemUser.getMiddleName());
            ps.setString(3, systemUser.getLastName());
            ps.setString(4, systemUser.getEmail());
            ps.setString(5,null);
            ps.setString(6, UtilityFunctions.formatPhoneNumber(systemUser.getPhone()));
            ps.setString(7, passwordEncoder.encode(systemUser.getNationalID()));
            ps.setString(8, systemUser.getNationalID());
            ps.setInt(9, systemUser.getCreatedBy());
            ps.setInt(10, 2);
            ps.setInt(11, 3);
            ps.setString(12,refferalCode);
            ps.setInt(13, 4);
            ps.setString(14,null);
            ps.setString(15,systemUser.getGender());
            return ps;
        }, keyHolder);

        int userId = keyHolder.getKey().intValue();

        String sql2 = "INSERT INTO user_roles(user_id, usertype, uproval_level) VALUES (?,?,0)";
        jdbcTemplateOne.update(sql2, userId, systemUser.getRole());
        smsService.sendUserWelcomeMessage(systemUser.getFirstName(), UtilityFunctions.formatPhoneNumber(systemUser.getPhone()));
        return userId;
    }

    @Transactional
    public Object updateUserKyc(UserKyc userKyc, String name) throws Exception {

        SystemUser systemUser = getByEmailOrPhone(name);

        String sql = """
        INSERT INTO applicant_kyc_details(
            user_id,
            marital_status,
            county_of_residence,
            current_residential_address,
            income_per_day,
            has_other_sources_of_income,
            income_sources_details,
            total_income_from_other_sources_per_day,
            guarantor_full_name,
            guarantor_phone_number,
            guarantor_id_number,
            guarantor_relationship,
            next_of_kin_full_name,
            next_of_kin_phone_number,
            next_of_kin_id_number,
            next_of_kin_relationship,
            created_by,
            created_at
        )
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())
        """;

        jdbcTemplateOne.update(
                sql,
                userKyc.getUserId(),
                userKyc.getMaritalStatus().name(),
                userKyc.getCountyOfResidence(),
                userKyc.getCurrentResidentialAddress(),
                userKyc.getIncomePerDay(),
                userKyc.isHasOtherSourcesOfIncome(),
                userKyc.getIncomeSourcesDetails(),
                userKyc.getTotalIncomeFromOtherSourcesPerDay(),
                userKyc.getGuarantor().getFullName(),
                UtilityFunctions.formatPhoneNumber(userKyc.getGuarantor().getPhoneNumber()),
                userKyc.getGuarantor().getIdNumber(),
                userKyc.getGuarantor().getRelationship(),
                userKyc.getNextOfKin().getFullName(),
                UtilityFunctions.formatPhoneNumber(userKyc.getNextOfKin().getPhoneNumber()),
                userKyc.getNextOfKin().getIdNumber(),
                userKyc.getNextOfKin().getRelationship(),
                systemUser.getId()
        );

        jdbcTemplateOne.update(
                "UPDATE users SET has_updated_kyc = 1 WHERE id = ?",
                userKyc.getUserId()
        );

        return userKyc;
    }

    public void updateReffalCodes(){
        System.out.println("updateReffalCodes ---- START");

        List<Integer> userIds = jdbcTemplateOne.query(
                "SELECT id FROM users WHERE refferal_id IS NULL",
                (rs, i) -> rs.getInt("id")
        );

        System.out.println("Found " + userIds.size() + " users without referral codes");

        int updated = 0;
        for (Integer userId : userIds) {
            String refferalCode = getRefferalCode(5);
            jdbcTemplateOne.update("UPDATE users SET refferal_id = ? WHERE id = ?", refferalCode, userId);
            updated++;

            if (updated % 100 == 0) {
                System.out.println("Processed " + updated + " users...");
            }
        }

        System.out.println("updateReffalCodes ---- FINISH. Updated " + updated + " users");
    }

    public String getRefferalCode(int length) {
            String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Random RANDOM = new Random();
            StringBuilder refferalCode = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int randomIndex = RANDOM.nextInt(CHARACTERS.length());
                refferalCode.append(CHARACTERS.charAt(randomIndex));
            }

            String generatedID = refferalCode.toString();

            if(isUniqueInDb(generatedID)){
                return generatedID;
            }else{
                return getRefferalCode(length);
            }
    }
    private boolean isUniqueInDb(String generatedID) {
        String sql = "SELECT COUNT(*) FROM users WHERE refferal_id = ?";
        int count = jdbcTemplateOne.queryForObject(sql, new Object[]{generatedID}, Integer.class);
        return count == 0;
    }

    public SystemUser getByEmailOrPhone(String user){
            String sql = "SELECT T0.*, T1.* FROM users T0  LEFT JOIN user_roles T1 ON T0.id =  T1.user_id WHERE T0.email=? OR T0.phone = ?";

            try{
                return jdbcTemplateOne.queryForObject(sql, (resultSet, i) -> setUser(resultSet),user, UtilityFunctions.formatPhoneNumber(user));
            }catch (Exception e){
                return  null;
            }
    }

    public LoggedInUser getLoggedInUser(String user){
        String sql = "SELECT T0.*, T1.* FROM users T0  LEFT JOIN user_roles T1 ON T0.id =  T1.user_id WHERE T0.email=? OR T0.phone = ?";

        try{
            return jdbcTemplateOne.queryForObject(sql, (resultSet, i) -> setLoggedInUser(resultSet),user, UtilityFunctions.formatPhoneNumber(user));
        }catch (Exception e){
            return  null;
        }
    }

    public SystemUser getUserByLoanAccount(String loanAccount){
        String sql ="SELECT * FROM users WHERE id = (SELECT userID FROM loans WHERE loanAccountMPesa =?)";

        return jdbcTemplateOne.queryForObject(sql,(rs,i)-> {
            try {
                return setUser(rs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },loanAccount);
    }

    public SystemUser getUserByFuelLoanId(String loanId){
        String sql ="SELECT * FROM users WHERE id = (SELECT userID FROM fuel_loan WHERE loanId =?)";

        return jdbcTemplateOne.queryForObject(sql,(rs,i)-> {
            try {
                return setUser(rs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },loanId);
    }

    public LoanPayeeDetail getLoanPayeeDetail(String conversationId) {
        String sqlGetPayeeDetail = """
                    SELECT i.party_b, i.occasion, i.app_id, (SELECT shotcode from mpesa_apps WHERE id = i.app_id) as shotcode,
                           (SELECT daily_amount_expected from loans WHERE loanAccountMPesa=i.occasion) as daily_amount_expecetd
                    FROM mpesa_b2c as i WHERE i.conversation_id = ?
                    """;
        return jdbcTemplateOne.queryForObject(sqlGetPayeeDetail,(rs, i)->setLoanPayeeDetail(rs),conversationId);
    }

    public SystemUser getUserByID(int userID) {
        String sql = "SELECT * FROM users WHERE id=?";
        return jdbcTemplateOne.queryForObject(sql, (resultSet, i) -> {
            try {
                return setUser(resultSet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },userID);
    }

    public List<Signatory> getSignatoriesByAppId(int appId) {
        String sql = "SELECT * FROM admin_signatories WHERE app_id =? AND deleted_at is null";
        return  jdbcTemplateOne.query(sql,(rs,i)->{
            try {
                return setSignatory(rs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },appId);

    }

    private Signatory setSignatory(ResultSet rs) throws Exception {
        Signatory signatory = new Signatory();
        signatory.setUserId(rs.getInt("user_id"));
        signatory.setAppId(rs.getInt("app_id"));
        signatory.setNotificationNumber(rs.getString("notification_number"));
        signatory.setNotificationEmail(rs.getString("notification_email"));
        signatory.setLevel(rs.getInt("admin_level"));
        return signatory;
    }

    public List<SystemUser> getAllUsers(Usertype usertype) {
        String sql = "SELECT * FROM users where id IN (SELECT user_id FROM user_roles WHERE usertype=?)";
        return  jdbcTemplateOne.query(sql,(rs,i)-> {
            try {
                return setUser(rs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },usertype.name());
    }

    public UserLoanDetail getUserLoanDetail(String loanID) throws Exception{
        String sql = """
                select u.first_name, u.last_name, u.phone,l.id as loanid, l.loanPrincipal, l.interestPercentage,l.client_loan_total,
                 (select shotcode from mpesa_apps where id = u.app_id) as shotcode,
                 l.client_loan_total as tLoan,
                 l.paid_amount as tPaid,
                 (l.client_loan_total - l.paid_amount) as balance
                from loans l
                inner join users u on l.userID = u.id
                where l.loanAccountMPesa = ?
                """;

        return  jdbcTemplateOne.queryForObject(sql,(rs,i)->setUserLoanDetail(rs),loanID);
    }

    private UserLoanDetail setUserLoanDetail(ResultSet rs) throws SQLException {

        UserLoanDetail userLoanDetail = new UserLoanDetail();
        userLoanDetail.setFirstName(rs.getString("first_name"));
        userLoanDetail.setLastName(rs.getString("last_name"));
        userLoanDetail.setPhone(rs.getString("phone"));
        userLoanDetail.setLoanId(rs.getInt("loanid"));
        userLoanDetail.setLoanPrincipal(rs.getDouble("loanPrincipal"));
        userLoanDetail.setInterestPecentage(rs.getDouble("interestPercentage"));
        userLoanDetail.setTotalLoan(rs.getDouble("tLoan"));
        userLoanDetail.setTotalPaid(rs.getDouble("tPaid"));
        userLoanDetail.setBalance(rs.getDouble("balance"));
        userLoanDetail.setClientLoanTotal(rs.getInt("client_loan_total"));
        userLoanDetail.setShotcode(rs.getInt("shotcode"));
        return userLoanDetail;
    }

    public List<SystemUser> getUserForGroup(int groupId){
        String sql = """
                SELECT T0.*, T1.* FROM users T0  LEFT JOIN user_roles T1 ON T0.id =  T1.user_id WHERE T0.group_id=?  AND T1.usertype = ?
            """;
        return jdbcTemplateOne.query(sql,(rs,i) ->setUser(rs),groupId,"Client");
    }

    private LoggedInUser setLoggedInUser(ResultSet resultSet) throws SQLException {
        LoggedInUser loggedInUser = new LoggedInUser();
        loggedInUser.setId(resultSet.getInt("id"));
        loggedInUser.setEmail(resultSet.getString("email"));
        loggedInUser.setPhone(resultSet.getString("phone"));
        loggedInUser.setFirstName(resultSet.getString("first_name"));
        loggedInUser.setLastName(resultSet.getString("last_name"));
        loggedInUser.setPassword(resultSet.getString("password"));
        loggedInUser.setUsertype(Usertype.valueOf(resultSet.getString("usertype")));
        loggedInUser.setAprovalLevel(resultSet.getInt("uproval_level"));

        return loggedInUser;

    }

    private SystemUser setUser(ResultSet resultSet) throws SQLException {
        SystemUser user = new SystemUser();
        user.setId(resultSet.getInt("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setNationalID(resultSet.getString("nationalId"));
        user.setAppId(resultSet.getInt("app_id"));

        try{
            user.setOnlineRider(resultSet.getBoolean("is_online_rider"));
        }catch (Exception ignored){}

        try{
            user.setRefferalId(resultSet.getString("refferal_id"));
        }catch (Exception ignored){}

        try{
            user.setDob(resultSet.getString("dob"));
        }catch (Exception ignored){}

        try{
            user.setCreatedAt(resultSet.getString("created_at"));
        }catch (Exception ignored){}

        try{
            user.setPartnerId(resultSet.getInt("patner_id"));
        }catch (Exception ignored){}

        try{
            user.setGroupId(resultSet.getInt("group_id"));
        }catch (Exception ignored){}

        return user;
    }

    private LoanPayeeDetail setLoanPayeeDetail(ResultSet rs) throws SQLException {
        LoanPayeeDetail loanPayeeDetail = new LoanPayeeDetail();
        loanPayeeDetail.setAppId(rs.getInt("app_id"));
        loanPayeeDetail.setPartyB(rs.getString("party_b"));
        loanPayeeDetail.setOccasion(rs.getString("occasion"));
        loanPayeeDetail.setShotcode(rs.getInt("shotcode"));
        loanPayeeDetail.setDailyPayment(rs.getInt("daily_amount_expecetd"));
        return loanPayeeDetail;
    }

    public List<UserProfile> getUserProfiles() throws Exception{
        String sql = """        
         SELECT u.id,u.first_name, u.middle_name, u.last_name, u.email, u.refferal_id, u.reffererd_by, u.phone, u.password,
                                                u.nationalId, u.user_status, u.created_by, u.group_id, u.app_id, u.has_updated_profile, up.stage_name, up.county, up.constituency,
                                                up.ward, up.welfare, up.sacco, up.refferee, up.appID, ur.usertype, ur.uproval_level FROM users u LEFT JOIN user_profile up on u.id = up.user_id
                                                LEFT JOIN user_roles ur on u.id = ur.user_id
                                                WHERE ur.usertype = 'FuelLoan'
                                                AND (partner_approved = 1)
                                                AND (user_status != 1 OR user_status is null)
        """;
        return jdbcTemplateOne.query(sql, (rs,i)-> mapRowToUserProfile(rs));
    }

    public List<UserProfile> getUserProfilesForPartner(int partnerId) throws Exception{
        String sql = """        
         SELECT u.id,u.first_name, u.middle_name, u.last_name, u.email, u.refferal_id, u.reffererd_by, u.phone, u.password,
                                                                           u.nationalId, u.user_status, u.created_by, u.group_id, u.app_id, u.has_updated_profile, up.stage_name, up.county, up.constituency,
                                                                           up.ward, up.welfare, up.sacco, up.refferee, up.appID, ur.usertype, ur.uproval_level FROM users u LEFT JOIN user_profile up on u.id = up.user_id
                                                                           LEFT JOIN user_roles ur on u.id = ur.user_id
                                                                           WHERE patner_id=?
        """;
        return jdbcTemplateOne.query(sql, (rs,i)-> mapRowToUserProfile(rs),partnerId);
    }

    public UserProfile getUserProfile(int userId) {
        String sql = """        
        SELECT
            u.id, u.first_name, u.middle_name, u.last_name, u.email, u.refferal_id, u.reffererd_by,
            u.phone, u.password, u.nationalId, u.user_status, u.created_by, u.group_id, u.app_id,
            u.has_updated_profile,u.has_updated_kyc,up.stage_name, up.county, up.constituency, up.ward, 
            up.welfare, up.sacco, up.refferee, up.appID,
            ur.usertype, ur.uproval_level,
            (SELECT public_url FROM client_attachments WHERE doc_type='NATIONAL-ID' AND user_id = u.id) as national_id_url,
            (SELECT public_url FROM client_attachments WHERE doc_type='PASSPORT-PICTURE' AND user_id = u.id) as passport_picture_url,
            (SELECT public_url FROM client_attachments WHERE doc_type='KRA-PIN' AND user_id = u.id) as kra_pin_url,
            (SELECT public_url FROM client_attachments WHERE doc_type='DRIVING-LICENSE' AND user_id = u.id) as driving_licence_url
        FROM
            users u
        LEFT JOIN
            user_profile up ON u.id = up.user_id
        LEFT JOIN
            user_roles ur ON u.id = ur.user_id
        WHERE
            u.id = ?    
        """;
        try{
            return jdbcTemplateOne.queryForObject(sql, (rs,i)-> mapRowToUserProfile(rs),userId);
        }catch (Exception exception){
            return null;
        }

    }

    public UserProfile mapRowToUserProfile(ResultSet rs) throws SQLException {
        UserProfile userProfile = new UserProfile();
        userProfile.setId(rs.getInt("id"));
        userProfile.setUserId(rs.getInt("id"));
        userProfile.setAppId(rs.getString("appID"));
        userProfile.setUserType(rs.getString("usertype"));
        userProfile.setApprovalLevel(rs.getInt("uproval_level"));
        userProfile.setFirstName(rs.getString("first_name"));
        userProfile.setMiddleName(rs.getString("middle_name"));
        userProfile.setLastName(rs.getString("last_name"));
        userProfile.setEmail(rs.getString("email"));
        userProfile.setReferralId(rs.getString("refferal_id"));
        userProfile.setReferredBy(rs.getString("reffererd_by"));
        userProfile.setPhone(rs.getString("phone"));
        userProfile.setPassword(rs.getString("password"));
        userProfile.setNationalId(rs.getString("nationalId"));
        userProfile.setUserStatus(rs.getString("user_status"));
        userProfile.setCreatedBy(rs.getString("created_by"));
        userProfile.setGroupId(rs.getString("group_id"));
        userProfile.setAppId(rs.getString("app_id"));
        userProfile.setHasUpdatedProfile(rs.getBoolean("has_updated_profile"));
        userProfile.setHasUpdatedKyc(rs.getBoolean("has_updated_kyc"));
        userProfile.setStageName(rs.getString("stage_name"));
        userProfile.setCounty(rs.getString("county"));
        userProfile.setConstituency(rs.getString("constituency"));
        userProfile.setWard(rs.getString("ward"));
        userProfile.setWelfare(rs.getString("welfare"));
        userProfile.setSacco(rs.getString("sacco"));
        userProfile.setReferee(rs.getString("refferee"));

        try{
            userProfile.setNationalIdUrl(rs.getString("national_id_url"));
        }catch (Exception ignored){  }

        try{
            userProfile.setPassportPictureUrl(rs.getString("passport_picture_url"));
        }catch (Exception ignored){}

        try{
            userProfile.setKraPinUrl(rs.getString("kra_pin_url"));
        }catch (Exception ignored){}

        try{
            userProfile.setDrivingLicenceUrl(rs.getString("driving_licence_url"));
        }catch (Exception ignored){}

        return userProfile;
    }

    public SystemUser updateUserProfile(UserProfile userProfile, int appID) throws Exception{
        String sql = "INSERT INTO user_profile(user_id, stage_name, county, constituency, ward, welfare, sacco, refferee,appID,created_at) " +
                "VALUES (?,?,?,?,?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,
                userProfile.getUserId(),
                userProfile.getStageName(),
                userProfile.getCounty(),
                userProfile.getConstituency(),
                userProfile.getWard(),
                userProfile.getWelfare(),
                userProfile.getSacco(),
                userProfile.getRefferee(),
                appID);

        setProfileUpdated(userProfile.getUserId());
        setPartnerId(userProfile.getUserId(),userProfile.getPartnerId());

        return getUserByID(userProfile.getUserId());
    }

    private int setProfileUpdated(int userId) throws Exception{
        String sql = "UPDATE users set has_updated_profile = 1 WHERE id=?";
        return jdbcTemplateOne.update(sql,userId);
    }

    private int setPartnerId(int userId, int partnerId) throws Exception{
        String sql = "UPDATE users SET patner_id =? WHERE id=?";
        return jdbcTemplateOne.update(sql,partnerId,userId);
    }

    public int updateUserStatus(String status, int userID, int appID) throws Exception {
        String sql = "UPDATE users SET user_status=? WHERE id=? and app_id=?";
        return jdbcTemplateOne.update(sql,status,userID,appID);

    }

    public int partnerUpdateUserStatus(String status, int userID, int appID) throws Exception {
        String sql = "UPDATE users SET user_status=? WHERE id=? and app_id=?";
        return jdbcTemplateOne.update(sql,status,userID,appID);

    }

    public Object enrollRefferal(UserReferral userReferral, String name) {
        String sql = "";
        return null;
    }

    public Object approveUser(int userId, int approved) throws Exception{
        String sql = "UPDATE users SET user_status =? WHERE id =?";
        jdbcTemplateOne.update(sql,approved,userId);
        return "success";
    }

    public int[] updateUserGroup(UpdateUserDto updateUserDto) {
        String sql = """
                 UPDATE users SET group_id = ? WHERE nationalId = ?; 
                 UPDATE user_roles SET usertype = ? WHERE user_id = ?,
                """;

        int[] updateCounts = null;
        try {
            updateCounts = jdbcTemplateOne.batchUpdate(
                    "UPDATE users SET group_id = ? WHERE nationalId = ?; " +
                            "UPDATE user_roles SET usertype = ? WHERE user_id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            if (i == 0) {
                                // Set parameters for the first statement
                                ps.setInt(1, updateUserDto.getGroupId());
                                ps.setString(2, updateUserDto.getIdNumber());
                            } else {
                                // Set parameters for the second statement
                                ps.setString(1, "Client");
                                ps.setInt(2, updateUserDto.getUserId());
                            }
                        }

                        @Override
                        public int getBatchSize() {
                            return 2;
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateCounts;
    }

    public List<InactiveUser> getAllInactiveClientsWithPastLoan() throws Exception{

        String sql = """
                    SELECT
                        u.id,
                        u.first_name,
                        u.middle_name,
                        u.last_name,
                        u.phone,
                        u.created_at,
                        u.group_id,
                        u.patner_id,
                        (SELECT ca.l_plate
                         FROM client_assets ca
                         WHERE ca.user_id = u.id
                         LIMIT 1) AS plate
                    FROM users u
                    WHERE u.id IN (
                        SELECT DISTINCT l.userID
                        FROM loans l
                        WHERE l.userID IS NOT NULL AND l.userID not in (1,2,3,4,5,6,7,8,13,14,15,33,482)
                        )
                    AND u.id NOT IN (
                        SELECT DISTINCT l2.userID
                        FROM loans l2
                        WHERE l2.loan_balance > 0
                        AND l2.userID IS NOT NULL  -- Exclude NULL
                    )
                """;
        return jdbcTemplateOne.query(sql,(rs,i)->{
            InactiveUser inactiveUser = new InactiveUser();
            inactiveUser.setUserId(rs.getInt("id"));
            inactiveUser.setFirstName(rs.getString("first_name"));
            inactiveUser.setMiddleName(rs.getString("middle_name"));
            inactiveUser.setLastName(rs.getString("last_name"));
            inactiveUser.setPhone(rs.getString("phone"));
            inactiveUser.setGroup(rs.getInt("group_id"));
            inactiveUser.setBranch(rs.getInt("patner_id"));
            inactiveUser.setUserSince(rs.getString("created_at"));
            inactiveUser.setNumberPlate(rs.getString("plate"));
            return inactiveUser;
        });
    }

    public int updateDateOfBirthForClient(String dob,int userId) throws Exception{
        String sql = "UPDATE users SET dob=? WHERE id=?";
        return jdbcTemplateOne.update(sql,dob,userId);
    }

    public List<UserKyc> getAllUserKYCEntries(int userId) {
        String sql = "SELECT * FROM applicant_kyc_details WHERE user_id=? ORDER BY created_at desc";
        return jdbcTemplateOne.query(sql, (rs, i) -> KYCRowMapper(rs), userId);
    }

    private UserKyc KYCRowMapper(ResultSet rs) throws SQLException {

        UserKyc userKyc = new UserKyc();
        // Basic user info
        userKyc.setUserId(rs.getInt("user_id"));
        userKyc.setMaritalStatus(UserKyc.MaritalStatus.valueOf(rs.getString("marital_status")));
        userKyc.setCountyOfResidence(rs.getString("county_of_residence"));
        userKyc.setCurrentResidentialAddress(rs.getString("current_residential_address"));

        // Income details
        userKyc.setIncomePerDay(rs.getDouble("income_per_day"));
        userKyc.setHasOtherSourcesOfIncome(rs.getBoolean("has_other_sources_of_income"));
        userKyc.setIncomeSourcesDetails(rs.getString("income_sources_details"));
        userKyc.setTotalIncomeFromOtherSourcesPerDay(rs.getDouble("total_income_from_other_sources_per_day"));

        // Guarantor details
        GuarantorDetails guarantor = new GuarantorDetails();
        guarantor.setFullName(rs.getString("guarantor_full_name"));
        guarantor.setPhoneNumber(rs.getString("guarantor_phone_number"));
        guarantor.setIdNumber(rs.getString("guarantor_id_number"));
        guarantor.setRelationship(rs.getString("guarantor_relationship"));
        userKyc.setGuarantor(guarantor);

        // Next of Kin details
        NextOfKinDetails nextOfKin = new NextOfKinDetails();
        nextOfKin.setFullName(rs.getString("next_of_kin_full_name"));
        nextOfKin.setPhoneNumber(rs.getString("next_of_kin_phone_number"));
        nextOfKin.setIdNumber(rs.getString("next_of_kin_id_number"));
        nextOfKin.setRelationship(rs.getString("next_of_kin_relationship"));
        userKyc.setNextOfKin(nextOfKin);
        userKyc.setCreatedAt(rs.getString("created_at"));

        return userKyc;
    }

    public List<AgentsUserView> getAllUsersByAgents() {

         String sql = """
                 SELECT
                       t1.id,t1.first_name,t1.middle_name,t1.last_name, t1.created_at,
                       (SELECT first_name FROM users WHERE refferal_id = t1.reffererd_by) AS reffered_by,
                       (SELECT name FROM counties WHERE county_id = t2.county_id) AS county,
                       (SELECT name FROM sub_counties WHERE sub_county_id = t2.sub_county_id) AS sub_county,
                       (SELECT name FROM wards WHERE ward_id = t2.ward_id) AS ward,
                       t2.id AS stage_id,
                       t2.group_name,
                       IF(
                           (SELECT COUNT(*) FROM loans WHERE userID = t1.id AND loanAccountMPesa IS NOT NULL) >= 1,
                           'Approved',
                           'Pending'
                       ) AS loanStatus
                   FROM users AS t1
                   LEFT JOIN `groups` t2 ON t1.group_id = t2.id
                   WHERE t1.joining_chanel = 'Agents App'
                   ORDER BY created_at DESC
                    """;
        return jdbcTemplateOne.query(sql, (rs, i) -> setAgentUser(rs));
    }

    public List<AgentsUserView> getAllUsersByAgentRefference(String refId) {
        String sql = """
                SELECT
                     t1.id,t1.first_name,t1.middle_name,t1.last_name, t1.created_at,
                     (SELECT first_name FROM users WHERE refferal_id = t1.reffererd_by) AS reffered_by,
                     (SELECT name FROM counties WHERE county_id = t2.county_id) AS county,
                     (SELECT name FROM sub_counties WHERE sub_county_id = t2.sub_county_id) AS sub_county,
                     (SELECT name FROM wards WHERE ward_id = t2.ward_id) AS ward,
                     t2.id AS stage_id,
                     t2.group_name,
                     IF(
                         (SELECT COUNT(*) FROM loans WHERE userID = t1.id AND loanAccountMPesa IS NOT NULL) >= 1,
                         'Approved',
                         'Pending'
                     ) AS loanStatus
                 FROM users AS t1
                 LEFT JOIN `groups` t2 ON t1.group_id = t2.id
                 WHERE t1.joining_chanel = 'Agents App'
                AND reffererd_by = ?
                 """;
        return jdbcTemplateOne.query(sql, (rs, i) -> setAgentUser(rs),refId);
    }

    private AgentsUserView setAgentUser(ResultSet rs) throws SQLException {

        AgentsUserView agentsUserView = new AgentsUserView();
        agentsUserView.setUserId(rs.getInt("id"));
        agentsUserView.setFirstName(rs.getString("first_name"));
        agentsUserView.setMiddleName(rs.getString("middle_name"));
        agentsUserView.setLastName(rs.getString("last_name"));
        agentsUserView.setCreatedAt( rs.getString("created_at") );
        agentsUserView.setReferredBy(rs.getString("reffered_by"));
        agentsUserView.setCounty(rs.getString("county"));
        agentsUserView.setSubCounty(rs.getString("sub_county"));
        agentsUserView.setWard(rs.getString("ward"));
        agentsUserView.setStageName(rs.getString("group_name"));
        agentsUserView.setLoanStatus(rs.getString("loanStatus"));
        agentsUserView.setStageId(rs.getInt("stage_id"));
        return agentsUserView;
    }

    public int updateUserStage(StageUpdateDTO stageUpdateDTO ) throws Exception {
        String sql = "Update users SET group_id=? where id=?";
        return jdbcTemplateOne.update(sql,stageUpdateDTO.getNewStageId(), stageUpdateDTO.getUserId());
    }


    public Object updateOnlineRiderStatus(OnlineRiderStatusDTO onlineRiderStatusDTO) throws Exception {
        String sql = "UPDATE users SET is_online_rider = ? WHERE id =?";
        return jdbcTemplateOne.update(sql,onlineRiderStatusDTO.isOnlineRider(), onlineRiderStatusDTO.getUserId());
    }
}
