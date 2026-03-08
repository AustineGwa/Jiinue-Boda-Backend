package com.otblabs.jiinueboda.customerassignments;

import com.otblabs.jiinueboda.collections.models.DefaultList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class CollectionAssinmentService {

    private final JdbcTemplate jdbcTemplateOne;

    public CollectionAssinmentService(JdbcTemplate jdbcTemplateOne ) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    private DefaultList mapRowToDefaultList(ResultSet rs, int i) throws SQLException {
        DefaultList loanRecord = new DefaultList();
        loanRecord.setId(rs.getInt("id"));
        loanRecord.setFirstName(rs.getString("first_name"));
        loanRecord.setLastName(rs.getString("last_name"));
        loanRecord.setPhone(rs.getString("phone"));
        loanRecord.setAccount(rs.getString("Account"));
        loanRecord.setTotalPaid(rs.getDouble("TotalPaid"));
        loanRecord.setTotalExpected(rs.getDouble("totalExpected"));
        loanRecord.setVariance(rs.getDouble("variance"));
        double roundedUpToTwoDecimalPlaces = Math.round(rs.getDouble("varRatio") * 100) / 100.0;
        loanRecord.setVarRatio(roundedUpToTwoDecimalPlaces);
        loanRecord.setDailyExpected(rs.getDouble("dailyExpected"));
        loanRecord.setLoanBalance(rs.getInt("LoanBalance"));
        loanRecord.setLoanStatus(rs.getString("Status"));
        loanRecord.setLoanAge(rs.getInt("loanAge"));

        return loanRecord;
    }

    private void assignDefaulters(List<DefaultList> defaulters, List<Integer> reps) {
        Collections.shuffle(defaulters);
        int repCount = reps.size();
        for (int i = 0; i < defaulters.size(); i++) {
            int repIndex = i % repCount;
            DefaultList defaulter = defaulters.get(i);
            assign_(defaulter.getAccount(), reps.get(repIndex));
        }
    }

    private void assign_(String loanId, Integer repId) {
        String sql = "INSERT INTO collections_assignments(loan_id, rep_id, assigned_on, valid_until, created_at) VALUES (?,?,DATE(NOW()),DATE(NOW() + INTERVAL 7 DAY),NOW())";
        jdbcTemplateOne.update(sql, loanId, repId);
    }

}
