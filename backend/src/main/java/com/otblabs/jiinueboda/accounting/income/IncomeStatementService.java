package com.otblabs.jiinueboda.accounting.income;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class IncomeStatementService {
    private final JdbcTemplate jdbcTemplateOne;

    public IncomeStatementService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public Income getMonthlyIncomeStatement(String month, String year) {

        String sql = """
                SELECT SUM(ntsa_fee) as battery_charge,SUM(credit_life_insurance) as insuarance,Sum(loan_processing_fee) as proccessing_fee,
                       Sum(total_interest_amount) as interest_earned,SUM(total_mon_fee) as tracker_monitoring FROM loans
                WHERE MONTH(disbursed_at) = ? AND year(disbursed_at) = ?
                """;

        return jdbcTemplateOne.queryForObject(sql,(rs,i)->{
            Income income = new Income();
            income.setBatteryCharge(rs.getInt("battery_charge"));
            income.setInsuarance(rs.getInt("insuarance"));
            income.setProccessingFee(rs.getInt("proccessing_fee"));
            income.setInterestEarned(rs.getInt("interest_earned"));
            income.setTrackerMonitoring(rs.getInt("tracker_monitoring"));
            return income;
        },month,year);
    }
}
