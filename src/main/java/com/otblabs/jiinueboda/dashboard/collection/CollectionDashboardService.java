package com.otblabs.jiinueboda.dashboard.collection;

import com.otblabs.jiinueboda.dashboard.models.DailyCollection;
import com.otblabs.jiinueboda.dashboard.models.DashboardData;
import com.otblabs.jiinueboda.dashboard.models.MainDashboard;
import com.otblabs.jiinueboda.dashboard.models.MonthlyTotals;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class CollectionDashboardService {

    private final JdbcTemplate jdbcTemplateOne;

    public CollectionDashboardService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }


    public DashboardData getDashboardData(int status) {

        String sql;

        if(status == 0){
            ///get all
            sql = """
                SELECT COUNT(Account) as loan_count,
                                                COUNT(IF(MONTH(disbursed_at) = MONTH(CURDATE()) AND YEAR(disbursed_at) = YEAR(CURDATE()), 1, NULL)) as tLoansThisMonth,
                                                SUM(total_loan) as tLoans,
                                                SUM(Principal) as principal,
                                                SUM(TotalPaid) as tLoansPaid,
                                                SUM(totalExpected) as collectionExpected,
                                                SUM(IF(LoanBalance < 0,0,LoanBalance) ) as outStanding_balance,
                                                SUM(variance) as variance,
                                                SUM((totalExpected -TotalPaid)) as par,
                                                Sum((IF(LoanBalance > 0,daily_amount_expected,0))) as collectionExpectedToday
                                                FROM (
                                                select id ,disbursed_at,Account,Principal,total_loan,daily_amount_expected, LoanBalance,TotalPaid,totalExpected,IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) as variance  from (
                                                select u.id,l.disbursed_at,l.daily_amount_expected,l.loanAccountMPesa  as Account,l.loanPrincipal  as Principal,l.client_loan_total as total_loan, l.loan_balance as LoanBalance, l.paid_amount as TotalPaid,
                                                       l.expected_amount as totalExpected
                                                  from loans l
                                                           left join users u on u.id = l.userID
                                                  where l.disbursed_at is not null) x)y
                """;
        }else{
            //get only active
            sql = """
                    SELECT COUNT(Account) as loan_count,
                    COUNT(IF(MONTH(disbursed_at) = MONTH(CURDATE()) AND YEAR(disbursed_at) = YEAR(CURDATE()), 1, NULL)) as tLoansThisMonth,
                    SUM(total_loan) as tLoans,
                    SUM(Principal) as principal,
                    SUM(TotalPaid) as tLoansPaid,
                    SUM(totalExpected) as collectionExpected,
                    SUM(IF(LoanBalance < 0,0,LoanBalance) ) as outStanding_balance,
                    SUM(variance) as variance,
                    SUM((totalExpected -TotalPaid)) as par,
                    Sum((IF(LoanBalance > 0,daily_amount_expected,0))) as collectionExpectedToday
                    FROM (
                    select id ,disbursed_at,Account,Principal,total_loan,daily_amount_expected, LoanBalance,TotalPaid,totalExpected,IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) as variance  from (
                    select u.id,l.disbursed_at,l.daily_amount_expected,l.loanAccountMPesa  as Account,l.loanPrincipal  as Principal,l.client_loan_total as total_loan, l.loan_balance as LoanBalance, l.paid_amount as TotalPaid,
                           l.expected_amount as totalExpected
                      from loans l
                               left join users u on u.id = l.userID
                      where l.disbursed_at is not null AND l.loan_balance > 0) x
                    
                     )y
                    """;
        }
        return jdbcTemplateOne.queryForObject(sql, this::mapRowToDashboardData);
    }

    /**
     * Counts the number of days between two dates, excluding Sundays
     * @param startDate the start date (inclusive)
     * @param endDate the end date (exclusive)
     * @return the number of days between the dates, not counting Sundays
     */
    public static long countDaysExcludingSundays(LocalDate startDate, LocalDate endDate) {
        // Ensure startDate is before endDate
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        long totalDays = 0;
        LocalDate currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            // Only count the day if it's not Sunday
            if (currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                totalDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return totalDays;
    }

    public MainDashboard getMainDashboardData(int branch, String startDate, String endDate) {

        String sqlStartdate = "SET @startDate = ?";
        jdbcTemplateOne.update(sqlStartdate,startDate);

        String sqlEnddate = "SET @endDate = ?";
        jdbcTemplateOne.update(sqlEnddate,endDate);

        if(branch != 0) {
            String sql = """
                          
             SELECT
             (SELECT COUNT(IF(loan_balance > 0, 1, NULL)) FROM loans WHERE disbursed_at <= @endDate ) as active_loans,
             (datediff(@endDate , '2024-02-01') * 7) as expected_active_loans,
             (SELECT COUNT(IF(MONTH(disbursed_at) = MONTH(CURDATE()) AND YEAR(disbursed_at) = YEAR(CURDATE()), 1, NULL)) FROM loans) as tLoansThisMonth,
             (SELECT DAY(CURDATE()) * 21) as expectedtLoansThisMonth,
             (SELECT DAY(CURDATE()) * 6) as expectedtNewLoansThisMonth,
             SUM(loanPrincipal) as principal,
             (datediff(@endDate, @startDate) * 470000) as expectedPrincipal,
             (
             SELECT SUM(TransAmount) FROM mpesa_c2b WHERE DATE(created_at) BETWEEN DATE(@startDate) AND DATE(@endDate)
             AND BillRefNumber IN (SELECT loanAccountMPesa FROM loans WHERE disbursed_at is not null)
             ) as cash_collected,
             (
             SELECT sum(daily_expected) FROM daily_expected_summary WHERE DATE(date) BETWEEN DATE(@startDate) AND DATE(@endDate)
             ) as cash_expected,
             (
             SELECT COUNT(IF(loan_balance > 0 AND loan_term < ABS(DATEDIFF(DATE(disbursed_at), DATE(NOW()))), 1, NULL)) FROM loans WHERE disbursed_at <= @endDate
             )as overdue_loans,
             SUM(IF(paid_amount > expected_amount, 0, loans.expected_amount - loans.paid_amount))  as variance_for_period,
             (SELECT SUM(IF(paid_amount > expected_amount, 0, loans.expected_amount - loans.paid_amount)) FROM loans WHERE disbursed_at <= @endDate ) as variance
             FROM loans WHERE disbursed_at IS NOT NULL AND DATE(disbursed_at) BETWEEN DATE(@startDate) AND DATE(@endDate)
                AND userID IN (SELECT id FROM users WHERE patner_id = ?)
            """;
            return jdbcTemplateOne.queryForObject(sql,(rs,i)-> mapRowToMainDashboardData(rs,i),branch);
        }

        String sql = """   
                
        SELECT
        (SELECT COUNT(IF(loan_balance > 0, 1, NULL)) FROM loans WHERE disbursed_at <= @endDate ) as active_loans,
        (datediff(@endDate , '2024-02-01') * 7) as expected_active_loans,
        (SELECT COUNT(IF(MONTH(disbursed_at) = MONTH(CURDATE()) AND YEAR(disbursed_at) = YEAR(CURDATE()), 1, NULL)) FROM loans) as tLoansThisMonth,
        (SELECT DAY(CURDATE()) * 21) as expectedtLoansThisMonth,
        (SELECT DAY(CURDATE()) * 6) as expectedtNewLoansThisMonth,
        SUM(loanPrincipal) as principal,
        (datediff(@endDate, @startDate) * 470000) as expectedPrincipal,
        (
        SELECT SUM(TransAmount) FROM mpesa_c2b WHERE DATE(created_at) BETWEEN DATE(@startDate) AND DATE(@endDate)
        AND BillRefNumber IN (SELECT loanAccountMPesa FROM loans WHERE disbursed_at is not null)
        ) as cash_collected,
        (
        SELECT sum(daily_expected) FROM daily_expected_summary WHERE DATE(date) BETWEEN DATE(@startDate) AND DATE(@endDate)
        ) as cash_expected,
        (
        SELECT COUNT(IF(loan_balance > 0 AND loan_term < ABS(DATEDIFF(DATE(disbursed_at), DATE(NOW()))), 1, NULL)) FROM loans WHERE disbursed_at <= @endDate
        )as overdue_loans,
        SUM(IF(paid_amount > expected_amount, 0, loans.expected_amount - loans.paid_amount))  as variance_for_period,
        (SELECT SUM(IF(paid_amount > expected_amount, 0, loans.expected_amount - loans.paid_amount)) FROM loans WHERE disbursed_at <= @endDate ) as variance
        FROM loans WHERE disbursed_at IS NOT NULL AND DATE(disbursed_at) BETWEEN DATE(@startDate) AND DATE(@endDate)
        """;
        return jdbcTemplateOne.queryForObject(sql, (rs,i)-> mapRowToMainDashboardData(rs,i));
    }

    private DashboardData mapRowToDashboardData(ResultSet rs, int i) throws SQLException {
        DashboardData dashboardData = new DashboardData();
        dashboardData.setTotalPrincipal(rs.getInt("principal"));
        dashboardData.setTLoans(rs.getInt("tLoans"));
        dashboardData.setTLoansPaid(rs.getInt("tLoansPaid"));
        dashboardData.setCollectionExpected(rs.getInt("collectionExpected"));
        dashboardData.setCollectionVariance(rs.getInt("variance"));
        dashboardData.setPar(rs.getInt("par"));
        dashboardData.setTotalLoanCount(rs.getInt("loan_count"));
        dashboardData.setLoansThisMonth(rs.getInt("tLoansThisMonth"));
        dashboardData.setCollectionExpectedToday(rs.getInt("collectionExpectedToday"));
        dashboardData.setOutstandingBalance(rs.getInt("outStanding_balance"));

        return dashboardData;
    }

    private MainDashboard mapRowToMainDashboardData(ResultSet rs, int i) throws SQLException {

        MainDashboard dashboardData = new MainDashboard();
        dashboardData.setTotalPrincipal(rs.getInt("principal"));
        dashboardData.setPrincipalExpected(rs.getInt("expectedPrincipal"));
        dashboardData.setCashCollected(rs.getInt("cash_collected"));
        dashboardData.setCashExpected(rs.getInt("cash_expected"));
        dashboardData.setActiveLoans(rs.getInt("active_loans"));
        dashboardData.setTotalLoanCountExpected(rs.getInt("expected_active_loans"));
        dashboardData.setOverdueLoans(rs.getInt("overdue_loans"));
        dashboardData.setLoansThisMonth(rs.getInt("tLoansThisMonth"));
        dashboardData.setVariance(rs.getInt("variance"));
        dashboardData.setPeriodVariance(rs.getInt("variance_for_period"));
        dashboardData.setTotalLoanCountExpectedThisMonth(rs.getInt("expectedtLoansThisMonth"));
        dashboardData.setNewLoansCountExpectedThisMonth(rs.getInt("expectedtNewLoansThisMonth"));
        dashboardData.setPrincipalExpected(rs.getInt("expectedPrincipal"));
        return dashboardData;
    }

    public List<MonthlyTotals> getMonthlyTotals(int year) {
        String sql = """
                select YD, MD, MDN,SUM(TL) as totalLoans, SUM(TP)  as totalPayments from (
                
                                    select YEAR(l.createdAt) as YD, MONTHNAME(l.createdAt) as MD,MONTH(l.createdAt) as MDN,SUM(l.client_loan_total) as TL, 0 as TP
                                    from loans l where l.loanStatus = 'APPROVED'
                                    group by MONTHNAME(l.createdAt),MONTH(l.createdAt),YEAR(l.createdAt)
                
                                    UNION ALL
                
                                    select YEAR(created_at) as YD,MONTHNAME(created_at) as MD,MONTH(created_at) as MDN, 0 as TL, CEILING(SUM(TransAmount)) as TP
                                    from mpesa_c2b where BillRefNumber IN (
                                            select loanAccountMPesa from loans where loanStatus = 'APPROVED'
                                            )
                                    group by MONTHNAME(created_at),MONTH(created_at),YEAR(created_at)
                                            ) x
                                    group by MD,MDN,YD
                                    HAVING YD = ?
                                    order by MDN
                """;
        return jdbcTemplateOne.query(sql, this::mapRowToMonthlyTotals,year);

    }

    private MonthlyTotals mapRowToMonthlyTotals(ResultSet rs, int i) throws SQLException {
        MonthlyTotals monthlyTotals = new MonthlyTotals();
        monthlyTotals.setMonthName(rs.getString("MD"));
        monthlyTotals.setMonthNumber(rs.getInt("MDN"));
        monthlyTotals.setTotalLoans(rs.getInt("totalLoans"));
        monthlyTotals.setTotalPayments(rs.getInt("totalPayments"));
        return monthlyTotals;
    }


    public List<DailyCollection> getDailyCollection(int year , int month) {

        // Execute the SET statement first
        String setMonthSql = "SET @month_number = ?";
        String setYearSql = "SET @year_number = ?";

        jdbcTemplateOne.update(setMonthSql, month);
        jdbcTemplateOne.update(setYearSql, year);

        String sql = """
                SELECT a.Date,des.daily_expected,CEILING(SUM(TransTotal)) AS collection_recieved
                FROM (
                         SELECT LAST_DAY(CONCAT(@year_number, '-', @month_number, '-01'))
                                    - INTERVAL (a.a + (10 * b.a) + (100 * c.a)) DAY AS Date,
                           0 AS TransTotal
                FROM (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS a
                    CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS b
                    CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS c
                
                UNION ALL
                
                SELECT DATE(TransTime) AS Date,
                    SUM(TransAmount) AS TransTotal
                FROM mpesa_c2b
                WHERE MONTH(TransTime) = @month_number
                  AND YEAR(TransTime) =  @year_number
                AND  (BillRefNumber In (SELECT  loanAccountMPesa FROM loans))
                GROUP BY DATE(TransTime)
                    ) a
                    LEFT JOIN daily_expected_summary AS des
                ON a.Date = des.date
                WHERE a.Date BETWEEN CONCAT(@year_number, '-', @month_number, '-01')
                  AND LAST_DAY(CONCAT(@year_number, '-', @month_number, '-01'))
                GROUP BY a.Date
                HAVING a.Date <= CURDATE()
                ORDER BY a.Date;
                                 
                """;
        return jdbcTemplateOne.query(sql, this::mapRowToDailyCollection);
    }

    private DailyCollection mapRowToDailyCollection(ResultSet rs, int i) throws SQLException {
        DailyCollection dailyCollection = new DailyCollection();
        dailyCollection.setDate(rs.getString("Date"));
        dailyCollection.setTotalCollected(rs.getString("collection_recieved"));
        dailyCollection.setDailyExpected(rs.getString("daily_expected"));
        return dailyCollection;
    }

    public Object getCollectionStatsForTimeRange(int branch, String startDate, String endDate) {
        return null;
    }
}



