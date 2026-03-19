package com.otblabs.jiinueboda.collections.recoveryV2;

import com.otblabs.jiinueboda.collections.CollectionsService;
import com.otblabs.jiinueboda.collections.models.BadLoans;
import com.otblabs.jiinueboda.collections.models.LoansByAge;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BikeRecoveryRecordService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;

    public BikeRecoveryRecordService(JdbcTemplate jdbcTemplateOne, UserService userService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
    }

    public int insertRecoveryRadar(BikeRecoveryRadarRequestDTO bikeRecoveryRadarRequestDTO, String user) throws Exception {

        SystemUser systemUser = userService.getByEmailOrPhone(user);

        String sql = """
                INSERT INTO recovery_radar(loan_id,creation_comment,created_by,created_at) VALUES(?,?,?,NOW())
                """;
        return jdbcTemplateOne.update(sql, bikeRecoveryRadarRequestDTO.getLoanId(), bikeRecoveryRadarRequestDTO.getCreationComment(),systemUser.getId());

    }

    public List<BikeRecoveryRadaDAO> getAllRequestedRecovery() {
        String sql = """
                SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                                                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                                                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                                                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate,
                                                      rr.creation_comment,rr.created_at as requested_on, rr.admin_approval,rr.admin_comment,rr.admin_comment_on
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
        badLoans.setRequestedOn(LocalDateTime.parse(rs.getString("requested_on")));
        badLoans.setAdminApproved(rs.getBoolean("admin_approval"));
        badLoans.setAdminComment(rs.getString("admin_comment"));
        badLoans.setAdminCommentOn(LocalDateTime.parse(rs.getString("admin_comment_on")));
        return badLoans;
    }


    public Integer saveUpdateAdminComment(AdminRecoveryCommentDTO adminRecoveryCommentDTO, String name) {

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        String sql = """
                UPDATE recovery_radar SET admin_approval=? , admin_comment=?, admin_id=?,admin_comment_on='NOW()'
                """;
        return jdbcTemplateOne.update(sql,adminRecoveryCommentDTO.isAdminApproval(), adminRecoveryCommentDTO.getAdminComment(),systemUser.getId());
    }
}
