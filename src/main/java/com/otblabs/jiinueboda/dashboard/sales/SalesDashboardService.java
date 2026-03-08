package com.otblabs.jiinueboda.dashboard.sales;

import com.otblabs.jiinueboda.dashboard.models.ClientLead;
import com.otblabs.jiinueboda.dashboard.models.SalesDashboardData;
import com.otblabs.jiinueboda.dashboard.models.SalesSurvey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class SalesDashboardService {
    private final JdbcTemplate jdbcTemplateOne;

    public SalesDashboardService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public List<ClientLead> getAllLeads(){

        String sql = """
                SELECT id, group_id, user_name, phone_number, current_stage, created_by, created_at FROM marketing_leads ORDER BY created_at DESC
                """;

        return jdbcTemplateOne.query(sql, (rs,i)->setClientLead(rs));

    }

    private ClientLead setClientLead(ResultSet rs) throws SQLException {
        ClientLead clientLead = new ClientLead();
        clientLead.setId(rs.getInt("id"));
        clientLead.setGroupId(rs.getInt("group_id"));
        clientLead.setUserName(rs.getString("user_name"));
        clientLead.setPhoneNumber(rs.getString("phone_number"));
        clientLead.setMarketingStage(rs.getString("current_stage"));
        clientLead.setCreatedAt(rs.getString("created_by"));
        clientLead.setCreatedAt(rs.getString("created_at"));
        return clientLead;
    }

    public SalesDashboardData getSalesStatsForTimeRange(int branch, String startDate, String endDate) {

        String sqlStartdate = "SET @Start_date = ?";
        jdbcTemplateOne.update(sqlStartdate,startDate);

        String sqlEnddate = "SET @endDate = ?";
        jdbcTemplateOne.update(sqlEnddate,endDate);

        if(branch == 0 ){
            String sql = """       
         SELECT
          COUNT(loanAccountMPesa) as loan_count,
          (SELECT COUNT(DISTINCT userID) from loans WHERE DATE(disbursed_at) <= DATE(@endDate)) as total_users_served,
          (SELECT COUNT(DISTINCT userID) from loans WHERE loan_balance > 0 AND DATE(disbursed_at) <= DATE(@endDate)) as total_active_users,
          (SELECT COUNT(DISTINCT loanAccountMPesa) from loans WHERE DATE(disbursed_at) <= DATE(@endDate)) as total_loans_disbursed,
          SUM(loanPrincipal) as loanPrincipal,
          COUNT(IF(loan_balance > 0, 1, NULL)) as active_loans,
          (SELECT COUNT(*)
          FROM loans
          WHERE loan_balance < 1
          AND DATE(last_payment_date) BETWEEN DATE(@Start_date) AND DATE(@endDate)) as completed_loans,
          (SELECT Count(id) FROM `groups` WHERE DATE(created_at) BETWEEN DATE(@Start_date) AND DATE(@endDate)) as total_new_stages,
          (SELECT Count(id) FROM marketing_leads WHERE DATE(created_at) BETWEEN DATE(@Start_date) AND DATE(@endDate)) as total_leads,
          (
            SELECT count(userId) as total_new_loans
            FROM (
                SELECT l.userId, MIN(l.disbursed_at) AS first_time_loan
                FROM loans l
                WHERE l.disbursed_at IS NOT NULL
                GROUP BY l.userId
                HAVING DATE(first_time_loan) BETWEEN DATE(@Start_date) AND DATE(@endDate)
            ) x
          ) as total_first_time_loans
          FROM loans
          WHERE DATE(disbursed_at) BETWEEN DATE(@Start_date) AND DATE(@endDate)
        """;

            return jdbcTemplateOne.queryForObject(sql,(rs, i) -> setSalesDashboardData(rs));
        }else{

            String sqlBranch = "SET @branch = ?";
            jdbcTemplateOne.update(sqlBranch,branch);
            String sql = """
                    SELECT
                         COUNT(loanAccountMPesa) as loan_count,
                         (SELECT COUNT(DISTINCT userID) from loans WHERE application_branch = @branch AND DATE(disbursed_at) <= DATE(@endDate)) as total_users_served,
                         (SELECT COUNT(DISTINCT loanAccountMPesa) from loans WHERE application_branch = @branch AND DATE(disbursed_at) <= DATE(@endDate)) as total_loans_disbursed,
                         (SELECT COUNT(DISTINCT userID) from loans WHERE loan_balance > 0 AND application_branch = @branch AND DATE(disbursed_at) <= DATE(@endDate)) as total_active_users,
    
                         SUM(loanPrincipal) as loanPrincipal,
                         COUNT(IF(loan_balance > 0, 1, NULL)) as active_loans,
                         (SELECT COUNT(*)
                          FROM loans
                          WHERE loan_balance < 1
                          AND application_branch = @branch
                          AND DATE(last_payment_date) BETWEEN DATE(@Start_date) AND DATE(@endDate)) as completed_loans,
                         (SELECT Count(id) FROM `groups` WHERE DATE(created_at) BETWEEN DATE(@Start_date) AND DATE(@endDate)) as total_new_stages,
                         (SELECT Count(id) FROM marketing_leads WHERE DATE(created_at) BETWEEN DATE(@Start_date) AND DATE(@endDate)) as total_leads,
                         (
                              SELECT count(first_timers.userId) as total_new_loans
                             FROM (
                                 SELECT l.userId, MIN(l.disbursed_at) AS first_time_loan
                                 FROM loans l
                                 WHERE l.disbursed_at IS NOT NULL
                                 GROUP BY l.userId
                                 HAVING DATE(first_time_loan) BETWEEN DATE(@Start_date) AND DATE(@endDate)
                             ) first_timers INNER JOIN loans l ON first_timers.userId = l.userId
                               AND DATE(l.disbursed_at) = DATE(first_timers.first_time_loan)
                               WHERE l.application_branch = @branch
                         ) as total_first_time_loans
                       FROM loans
                       WHERE DATE(disbursed_at) BETWEEN DATE(@Start_date) AND DATE(@endDate) AND application_branch = @branch
                    
                    
                    """;

            return jdbcTemplateOne.queryForObject(sql,(rs, i) -> setSalesDashboardData(rs));

        }


    }

    private SalesDashboardData setSalesDashboardData(ResultSet rs) throws SQLException {
        SalesDashboardData salesDashboardData = new SalesDashboardData();
        salesDashboardData.setTotalLoans(rs.getInt("loan_count"));
        salesDashboardData.setTotalNewLoans(rs.getInt("total_first_time_loans"));
        salesDashboardData.setTotalPrincipal(rs.getInt("loanPrincipal"));
        salesDashboardData.setTotalNewStagesMapped(rs.getInt("total_new_stages"));
        salesDashboardData.setCompletedLoans(rs.getInt("completed_loans"));
        salesDashboardData.setTotalLeadsCreated(rs.getInt("total_leads"));
        salesDashboardData.setTotalUsersServed(rs.getInt("total_users_served"));
        salesDashboardData.setTotalActiveUsers(rs.getInt("total_active_users"));
        salesDashboardData.setAllLoansDisbursed(rs.getInt("total_loans_disbursed"));
        return salesDashboardData;
    }

    public Object getAllQuizes() {

        String sql = """
                SELECT group_id, number_of_members, registered_with_sacco, working_hours, has_regular_meetings, meeting_date, meeting_time,
                       (SELECT first_name FROM users WHERE id = stage_survey_responses.created_by) as created_by, created_at
                FROM stage_survey_responses
                """;
        return jdbcTemplateOne.query(sql , (rs,i)->setSurvey(rs));
    }

    private SalesSurvey setSurvey(ResultSet rs) throws SQLException {
        SalesSurvey salesSurvey = new SalesSurvey();
        salesSurvey.setGroupId(rs.getInt("group_id"));
        salesSurvey.setNumberOfMembers(rs.getInt("number_of_members"));
        salesSurvey.setRegisteredWithSacco(rs.getBoolean("registered_with_sacco"));
        salesSurvey.setWorkingHours(rs.getString("working_hours"));
        salesSurvey.setHasRegularMeetings(rs.getBoolean("has_regular_meetings"));
        salesSurvey.setMeetingDate(rs.getString("meeting_date"));
        salesSurvey.setMeetingTime(rs.getString("meeting_time"));
        salesSurvey.setCreatedBy(rs.getString("created_by"));
        salesSurvey.setCreatedAt(rs.getString("created_at"));
        return salesSurvey;
    }
}
