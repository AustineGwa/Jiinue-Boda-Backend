package com.otblabs.jiinueboda.collections.recovery;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BikeRecoveryService {

    private final JdbcTemplate jdbcTemplateOne;

    public BikeRecoveryService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public int exemptFromRecovery(ExemptionData exemptionData) throws Exception{
        String insertExeptionEntrySql = """
                INSERT INTO recovery_exemptions(recovery_id, exempted_on,daysExempted, reason_for_exemption, exempted_by, created_at) VALUES (?,CURDATE(),?,?,?,NOW())
                """;

        String updateRecoveryExemption = """
                UPDATE bike_recovery SET is_excempted = 1 WHERE id=?
                """;
        jdbcTemplateOne.update(insertExeptionEntrySql,
                exemptionData.getRecoveryId(),
                exemptionData.getDaysExempted(),
                exemptionData.getReason(),
                exemptionData.getUserId());

        jdbcTemplateOne.update(updateRecoveryExemption,exemptionData.getRecoveryId());
        return 1;
    }

    void printFinalWeeklyRecoveryList(){
        String sql = """
                SELECT loanAccountMPesa AS Account, NUMBER_PLATE, FIRST_NAME, MIDDLE_NAME, LAST_NAME, PHONE,VARIANCE FROM (SELECT ca.l_plate  AS number_plate,
                                      u.first_name,
                                      u.middle_name,
                                      u.last_name,
                                      u.phone,
                                      u.group_id,
                                      u.patner_id,
                                      l.loanAccountMPesa,
                                      l.disbursed_at,
                                      l.loan_term,
                                      (expected_amount - paid_amount) as variance,
                                      DATEDIFF(NOW(), l.disbursed_at) as loanAge,
                                      ROUND(((expected_amount - paid_amount) / daily_amount_expected)) as varRatio
                               FROM bike_recovery br
                                        JOIN (SELECT loan_account, MAX(created_at) AS max_created_at
                                              FROM bike_recovery
                                              GROUP BY loan_account) latest_br
                                             ON br.loan_account = latest_br.loan_account AND br.created_at = latest_br.max_created_at
                                        LEFT JOIN loans l ON br.loan_account = l.loanAccountMPesa
                                        LEFT JOIN users u ON u.id = br.userId
                                        LEFT JOIN client_assets ca ON ca.user_id = br.userId
                               WHERE br.is_excempted = 0
                                 AND br.bike_recoverd_at IS NULL
                                 AND patner_id <> 1
                                 AND ((expected_amount - paid_amount) / daily_amount_expected) > 7
                )X WHERE loanAge > loan_term AND variance > 3999  ORDER BY variance DESC;
                """;

        List<FinalRecoveryList> recoveryListByLoanAge = jdbcTemplateOne.query(sql,(rs,i)->finalRecoveryRowMapper(rs));
    }

    private FinalRecoveryList finalRecoveryRowMapper(ResultSet rs) {
        FinalRecoveryList finalRecoveryList = new FinalRecoveryList();
        return finalRecoveryList;
    }

    public final void  updateExpiredExcemption(){

        String sql = """
                    UPDATE bike_recovery SET is_excempted = 0 WHERE id in (
                    SELECT recovery_id FROM (
                    SELECT recovery_id, daysExempted, DATEDIFF(NOW(), exempted_on) as daysElapsed FROM recovery_exemptions
                    )Y WHERE daysElapsed - daysExempted > 0
                )
                """;

        jdbcTemplateOne.update(sql);
    }

    public final void updateWeeklyRecovery(){

        String sql = """
                INSERT INTO bike_recovery (
                userId,loan_account, loan_age_as_at_revory_entry, expected_amount_as_at_recovory_entry, 
                paid_amount_as_at_recovory_entry, variance_as_at_recovory_entry,var_ratio_as_at_recovory_entry, last_payment_date_as_at_recovory_entry,
                 created_at
                 )
                                
                SELECT userID,account, loanAge, expected_amount, paid_amount, variance, varRatio, last_payment_date,created_at FROM (
                                                        SELECT l.userID, l.loanAccountMPesa                                               as account,
                                                               DATEDIFF(NOW(), disbursed_at)                                    as loanAge,
                                                               expected_amount,
                                                               paid_amount,
                                                               (expected_amount - paid_amount)                                  as variance,
                                                               ROUND(((expected_amount - paid_amount) / daily_amount_expected)) as varRatio,
                                                               last_payment_date,
                                                               NOW() as created_at
                                                        FROM loans l
                                                        WHERE disbursed_at is not null
                                                          AND loan_balance > 0
                                                          AND expected_amount - paid_amount > daily_amount_expected
                                                    ) x
                WHERE varRatio > 7
                  AND account not in (SELECT loan_account FROM bike_recovery WHERE  is_excempted=1 OR bike_recoverd_at is not null)
                """;

        jdbcTemplateOne.update(sql);

    }

    public List<BikesInStorage> getStorageList(){
        String sql = """
                SELECT u.first_name, u.middle_name, u.last_name, u.phone, u.group_id, u.patner_id, br.id, br.loan_account,
                                                                       ca.l_plate AS number_plate,l.client_loan_total as total_loan, (l.expected_amount - l.paid_amount) as current_variance,\s
                                                                       DATEDIFF(NOW(),l.disbursed_at) as loanAge, br.bike_recoverd_at,br.recovery_amount
                                                                    FROM bike_recovery br
                                                                    JOIN (
                                                                        SELECT loan_account, MAX(created_at) AS max_created_at
                                                                        FROM bike_recovery
                                                                        GROUP BY loan_account
                                                                    ) latest_br ON br.loan_account = latest_br.loan_account AND br.created_at = latest_br.max_created_at
                                                                    LEFT JOIN loans l ON br.loan_account = l.loanAccountMPesa
                                                                    LEFT JOIN users u ON u.id = br.userId
                                                                    LEFT JOIN client_assets ca ON ca.user_id = br.userId
                                                                    WHERE br.bike_recoverd_at IS NOT NULL AND released_at is null
                                                                    ORDER BY br.bike_recoverd_at;
                """;

       return jdbcTemplateOne.query(sql,(rs,i)->bikeStorageRowMapper(rs));
    }

    private BikesInStorage bikeStorageRowMapper(ResultSet rs) throws SQLException {
        BikesInStorage bikeRecovery = new BikesInStorage();
        bikeRecovery.setRecoveryId(rs.getInt("id"));
        bikeRecovery.setFirstName(rs.getString("first_name"));
        bikeRecovery.setMiddleName(rs.getString("middle_name"));
        bikeRecovery.setLastName(rs.getString("last_name"));
        bikeRecovery.setPhone(rs.getString("phone"));
        bikeRecovery.setGroup(rs.getInt("group_id"));
        bikeRecovery.setBranch(rs.getInt("patner_id"));
        bikeRecovery.setLoanAccount(rs.getString("loan_account"));
        bikeRecovery.setAsset(rs.getString("number_plate"));
        bikeRecovery.setTotalLoan(rs.getInt("total_loan"));
        bikeRecovery.setVariance(rs.getInt("current_variance"));
        bikeRecovery.setLoanAge(rs.getInt("loanAge"));
        bikeRecovery.setBikeRecoveryDate(rs.getString("bike_recoverd_at"));
        bikeRecovery.setRecoveryAmount(rs.getInt("recovery_amount"));
        return bikeRecovery;
    }


    public Map<String,List<BikeRecovery>> getRecoveryList(int branch){

        String sql;
        List<BikeRecovery> recoveryListByLoanAge = null;

        if(branch == 0){
            sql = """
                  SELECT u.first_name, u.middle_name, u.last_name, u.phone, u.group_id, u.patner_id,
                                                           br.id,br.loan_age_as_at_revory_entry, br.loan_account, br.expected_amount_as_at_recovory_entry,
                                                           br.paid_amount_as_at_recovory_entry, br.variance_as_at_recovory_entry, br.var_ratio_as_at_recovory_entry,
                                                           br.last_payment_date_as_at_recovory_entry, br.created_at, ca.l_plate AS number_plate,
                                                           l.disbursed_at, l.loanPrincipal, DATEDIFF(NOW(),l.disbursed_at) as loanAge
                                                    FROM bike_recovery br
                                                    JOIN (
                                                        SELECT loan_account, MAX(created_at) AS max_created_at
                                                        FROM bike_recovery
                                                        GROUP BY loan_account
                                                    ) latest_br ON br.loan_account = latest_br.loan_account AND br.created_at = latest_br.max_created_at
                                                    LEFT JOIN loans l ON br.loan_account = l.loanAccountMPesa
                                                    LEFT JOIN users u ON u.id = br.userId
                                                    LEFT JOIN client_assets ca ON ca.user_id = br.userId
                                                    WHERE br.is_excempted = 0 AND br.bike_recoverd_at IS NULL
                                                    AND ((expected_amount - paid_amount)/daily_amount_expected) > 7
                                                    ORDER BY l.disbursed_at;
                """;

            recoveryListByLoanAge = jdbcTemplateOne.query(sql,(rs,i)->bikeRecoveryRowMapper(rs));
        }else {

            sql = """
                    SELECT u.first_name, u.middle_name, u.last_name, u.phone, u.group_id, u.patner_id,
                                                                               br.id,br.loan_age_as_at_revory_entry, br.loan_account, br.expected_amount_as_at_recovory_entry,
                                                                               br.paid_amount_as_at_recovory_entry, br.variance_as_at_recovory_entry, br.var_ratio_as_at_recovory_entry,
                                                                               br.last_payment_date_as_at_recovory_entry, br.created_at, ca.l_plate AS number_plate,
                                                                               l.disbursed_at, l.loanPrincipal, DATEDIFF(NOW(),l.disbursed_at) as loanAge
                                                                        FROM bike_recovery br
                                                                        JOIN (
                                                                            SELECT loan_account, MAX(created_at) AS max_created_at
                                                                            FROM bike_recovery
                                                                            GROUP BY loan_account
                                                                        ) latest_br ON br.loan_account = latest_br.loan_account AND br.created_at = latest_br.max_created_at
                                                                        LEFT JOIN loans l ON br.loan_account = l.loanAccountMPesa
                                                                        LEFT JOIN users u ON u.id = br.userId
                                                                        LEFT JOIN client_assets ca ON ca.user_id = br.userId
                                                                        WHERE br.is_excempted = 0 AND br.bike_recoverd_at IS NULL AND u.patner_id=?
                                                                        AND ((expected_amount - paid_amount)/daily_amount_expected) > 7
                                                                        ORDER BY l.disbursed_at;
                 """;

            recoveryListByLoanAge = jdbcTemplateOne.query(sql,(rs,i)->bikeRecoveryRowMapper(rs),branch);

        }


        return recoveryListByLoanAge.stream()
                .collect(Collectors.groupingBy(loan -> {
                    int age = loan.getLoanAge();
                    if (age <= 30) return "30";
                    if (age <= 60) return "60";
                    if (age <= 90) return "90";
                    if (age <= 120) return "120";
                    return "Overdue";
                }));
    }

    private BikeRecovery bikeRecoveryRowMapper(ResultSet rs) throws SQLException {
        BikeRecovery bikeRecovery = new BikeRecovery();
        bikeRecovery.setDisburesementDate(rs.getString("disbursed_at"));
        bikeRecovery.setLoanPrincipal(rs.getInt("loanPrincipal"));
        bikeRecovery.setRecoveryId(rs.getInt("id"));
        bikeRecovery.setFirstName(rs.getString("first_name"));
        bikeRecovery.setMiddleName(rs.getString("middle_name"));
        bikeRecovery.setLastName(rs.getString("last_name"));
        bikeRecovery.setPhone(rs.getString("phone"));
        bikeRecovery.setAsset(rs.getString("number_plate"));
        bikeRecovery.setGroup(rs.getInt("group_id"));
        bikeRecovery.setBranch(rs.getInt("patner_id"));
        bikeRecovery.setLoanAccount(rs.getString("loan_account"));
        bikeRecovery.setLoanAgeAsAtRecoveryEntry(rs.getInt("loan_age_as_at_revory_entry"));
        bikeRecovery.setExpectedAmountAsAtRecoveryEntry(rs.getInt("expected_amount_as_at_recovory_entry"));
        bikeRecovery.setPaidAmountAsAtRecoveryEntry(rs.getInt("paid_amount_as_at_recovory_entry"));
        bikeRecovery.setVarianceAsAtRecoveryEntry(rs.getInt("variance_as_at_recovory_entry"));
        bikeRecovery.setVarRatioAsAtRecoveryEntry(rs.getInt("var_ratio_as_at_recovory_entry"));
        // For datetime fields, handle potential null values
        Timestamp lastPaymentDate = rs.getTimestamp("last_payment_date_as_at_recovory_entry");
        if (lastPaymentDate != null) {
            bikeRecovery.setLastPaymentDateAsAtRecoveryEntry(lastPaymentDate.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            bikeRecovery.setCreatedAt(createdAt.toLocalDateTime());
        }

        bikeRecovery.setLoanAge(rs.getInt("loanAge"));

        return bikeRecovery;
    }

    public int updateRepo(RepoData repoData) {

        String sql = """
               UPDATE bike_recovery SET recovery_updated_by=?,bike_recoverd_at=?,recovery_amount=?, recovery_comment=? WHERE id=?
                """;
        return jdbcTemplateOne.update(sql,repoData.userId, repoData.recoveryDate,repoData.recoveryAmount,repoData.recoveryComment,repoData.recoveryId);
    }

    public int updateRelease(BikeReleaseData bikeReleaseData) {

        String sql = """
                UPDATE bike_recovery SET bike_released_by=?,released_at=?,storage_amount=?, release_comment=? WHERE id=?
                """;

        return jdbcTemplateOne.update(sql,bikeReleaseData.getUserId(),bikeReleaseData.getReleaseDate(),
                bikeReleaseData.getStorageAmount(),
                bikeReleaseData.getReleaseComment(),
                bikeReleaseData.getRecoveryId());

    }
}
