package com.otblabs.jiinueboda.collections.recoveryV2;

import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class BikeRecoveryRecordService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;
    private final SmsService smsService;

    public BikeRecoveryRecordService(JdbcTemplate jdbcTemplateOne, UserService userService, SmsService smsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
        this.smsService = smsService;
    }

    public int insertRecoveryRadar(BikeRecoveryRadarRequestDTO bikeRecoveryRadarRequestDTO, String user) throws Exception {

        SystemUser systemUser = userService.getByEmailOrPhone(user);

        String sql = """
                INSERT INTO recovery_radar(loan_id,creation_comment,created_by,created_at) VALUES(?,?,?,NOW())
                """;
        return jdbcTemplateOne.update(sql,
                bikeRecoveryRadarRequestDTO.getLoanId(),
                bikeRecoveryRadarRequestDTO.getCreationComment(),
                systemUser.getId()
        );

    }

    public List<BikeRecoveryRadaDAO> getAllRequestedRecovery() {
        String sql = """
                SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                                                                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                                                                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                                                                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate,
                                                                      rr.creation_comment,rr.created_at as requested_on, rr.admin_approval,rr.admin_comment,rr.admin_comment_on, rr.recovered_on, rr.recovery_comment
                                                                      FROM loans LEFT JOIN users u ON loans.userID = u.id
                                                                      left join client_assets ca on u.id = ca.user_id
                                                                      left join recovery_radar rr on rr.loan_id = loans.loanAccountMPesa
                                                                      WHERE loan_balance > 0
                                                                      AND expected_amount > paid_amount
                                                                      AND disbursed_at is not null
                                                                      And loanAccountMPesa IN (SELECT loan_id from recovery_radar WHERE deleted_at is null)
                """;

        return jdbcTemplateOne.query(sql, (rs,i)-> mapRowToObject(rs));
    }

    private BikeRecoveryRadaDAO mapRowToObject(ResultSet rs) throws SQLException {

        BikeRecoveryRadaDAO badLoans = new BikeRecoveryRadaDAO();
        badLoans.setId(rs.getInt("id"));
        badLoans.setFirstName(rs.getString("first_name"));
        badLoans.setLastName(rs.getString("last_name"));
        badLoans.setAccount(rs.getString("Account"));
        badLoans.setBranch(rs.getInt("patner_id"));
        badLoans.setPhone(rs.getString("phone"));
        badLoans.setLoanTerm(rs.getInt("loan_term"));
        badLoans.setLoanAge(rs.getInt("loanAge"));
        badLoans.setVariance(rs.getInt("variance"));
        badLoans.setVarRatio(rs.getInt("varRatio"));
        badLoans.setDisbursedAt(rs.getString("disbursed_at"));
        badLoans.setNumberPlate(rs.getString("l_plate"));
        badLoans.setCreationComment(rs.getString("creation_comment"));
        badLoans.setRequestedOn(rs.getString("requested_on"));
        badLoans.setAdminApproved(rs.getBoolean("admin_approval"));
        badLoans.setAdminComment(rs.getString("admin_comment"));
        badLoans.setAdminCommentOn(rs.getString("admin_comment_on"));
        badLoans.setRecoveredOn(rs.getString("recovered_on"));
        badLoans.setRecoveryComment(rs.getString("recovery_comment"));

        return badLoans;
    }


    @Transactional
    public Integer saveUpdateAdminComment(AdminRecoveryCommentDTO adminRecoveryCommentDTO, String name) throws Exception {

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        String sql = """
                UPDATE recovery_radar SET admin_approval=? , admin_comment=?, admin_id=?,admin_comment_on=NOW() WHERE loan_id=?
                """;
        jdbcTemplateOne.update(sql,adminRecoveryCommentDTO.isAdminApproval(), adminRecoveryCommentDTO.getAdminComment(),systemUser.getId(),adminRecoveryCommentDTO.getLoanAccount());

        if(adminRecoveryCommentDTO.isAdminApproval()){
            smsService.sendBikeToRecovery(adminRecoveryCommentDTO.getLoanAccount());
        }

        return 1;


    }

    @Transactional
    public Integer saveConfirmRecovery(ConfirmRecoveryDTO confirmRecoveryDTO, String name) throws Exception {

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        String sql = """
                
                UPDATE recovery_radar SET recovery_amount=?, recovery_comment=?, recovered_by=?,recovered_on=NOW() WHERE loan_id=?
                """;
        jdbcTemplateOne.update(sql,confirmRecoveryDTO.getRecoveryAmount(),
                confirmRecoveryDTO.getRecoveryComment(),
                systemUser.getId(),confirmRecoveryDTO.getLoanAccount());

        smsService.removeRecovery(confirmRecoveryDTO.getLoanAccount());


        return 1;


    }

    public List<ApprovedRecovery> getAllApprovedRecovery() {

        String sql = """
                SELECT loanAccountMpesa as Account, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                                                                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                                                                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                                                                      ca.l_plate,
                                                                      rr.created_at as requested_on
                                                                      FROM loans LEFT JOIN users u ON loans.userID = u.id
                                                                      left join client_assets ca on u.id = ca.user_id
                                                                      left join recovery_radar rr on rr.loan_id = loans.loanAccountMPesa
                                                                      WHERE loan_balance > 0
                                                                      AND expected_amount > paid_amount
                                                                      AND admin_approval = true
                                                                      And recovered_on is null
                                                                      AND disbursed_at is not null
                                                                      And loanAccountMPesa IN (SELECT loan_id from recovery_radar WHERE deleted_at is null)
                """;

        return jdbcTemplateOne.query(sql, (rs,i)-> mapRowToApprovedRecovery(rs));

    }

    private ApprovedRecovery mapRowToApprovedRecovery(ResultSet rs) throws SQLException {
        ApprovedRecovery approvedRecovery = new ApprovedRecovery();
        approvedRecovery.setId(rs.getInt("id"));
        approvedRecovery.setFirstName(rs.getString("first_name"));
        approvedRecovery.setLastName(rs.getString("last_name"));
        approvedRecovery.setAccount(rs.getString("Account"));
        approvedRecovery.setPhone(rs.getString("phone"));
        approvedRecovery.setVariance(rs.getInt("variance"));
        approvedRecovery.setVarRatio(rs.getInt("varRatio"));
        approvedRecovery.setDisbursedAt(rs.getString("disbursed_at"));
        approvedRecovery.setNumberPlate(rs.getString("l_plate"));
        approvedRecovery.setRequestedOn(rs.getString("requested_on"));
        return approvedRecovery;
    }

    public Integer assetReleaseDTO(AssetReleaseDTO dto, String name) {

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        String sql = """
        UPDATE recovery_radar SET
              release_type = ?,
              release_notes = ?,
              sale_amount = ?,
              checklist_loan_clearance = ?,
              checklist_sale_agreement = ?,
              checklist_recovery_fees_paid = ?,
              checklist_storage_fees_cleared = ?,
              checklist_tracker_cleared = ?,
              checklist_proof_loan_clearance = ?,
              checklist_proof_sale_agreement = ?,
              checklist_proof_recovery_fees_paid = ?,
              checklist_proof_storage_fees_cleared = ?,
              checklist_proof_tracker_cleared = ?,
              ops_manager_name = ?,
              ops_manager_comment = ?,
              released_by = ?,
              released_on = NOW()
          WHERE loan_id = ?
    """;

        return jdbcTemplateOne.update(
                sql,
                dto.getReleaseType(),
                dto.getReleaseNotes(),
                dto.getSaleAmount(),

                // checklist
                dto.getChecklist().getLoanClearance(),
                dto.getChecklist().getSaleAgreement(),
                dto.getChecklist().getRecoveryFeesPaid(),
                dto.getChecklist().getStorageFeesCleared(),
                dto.getChecklist().getTrackerCleared(),

                // checklist proofs
                dto.getChecklistProofs().getLoanClearance(),
                dto.getChecklistProofs().getSaleAgreement(),
                dto.getChecklistProofs().getRecoveryFeesPaid(),
                dto.getChecklistProofs().getStorageFeesCleared(),
                dto.getChecklistProofs().getTrackerCleared(),

                // ops
                dto.getOpsManagerName(),
                dto.getOpsManagerComment(),
                systemUser.getId(),
                dto.getLoanAccount()
        );
    }
}
