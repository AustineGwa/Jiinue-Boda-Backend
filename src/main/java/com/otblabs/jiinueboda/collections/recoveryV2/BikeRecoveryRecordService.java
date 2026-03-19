package com.otblabs.jiinueboda.collections.recoveryV2;

import com.otblabs.jiinueboda.collections.CollectionsService;
import com.otblabs.jiinueboda.collections.models.BadLoans;
import com.otblabs.jiinueboda.collections.models.LoansByAge;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class BikeRecoveryRecordService {

    private final JdbcTemplate jdbcTemplateOne;
    private final CollectionsService collectionsService;

    public BikeRecoveryRecordService(JdbcTemplate jdbcTemplateOne, CollectionsService collectionsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.collectionsService = collectionsService;
    }

    public int insertRecoveryRadar(BikeRecoveryRadarRequestDTO bikeRecoveryRadarRequestDTO) throws Exception{
        String sql = """
                INSERT INTO recovery_radar(loan_id,creation_comment,created_at) VALUES(?,?,NOW())
                """;
        return jdbcTemplateOne.update(sql, bikeRecoveryRadarRequestDTO.getLoanId(), bikeRecoveryRadarRequestDTO.getCreationComment());

    }

    public List<BikeRecoveryRadaDAO> getAllRequestedRecovery() {
        String sql = """
                SELECT loanAccountMpesa as Account,u.patner_id, u.id,u.first_name,u.last_name,u.phone,disbursed_at,
                                      (IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as variance,
                                      loan_term,ROUND(((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))/loans.daily_amount_expected)) as varRatio,
                                      (DATEDIFF(now(), DATE(disbursed_at))) as loanAge,ca.l_plate, rr.creation_comment
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
        return badLoans;
    }


}
