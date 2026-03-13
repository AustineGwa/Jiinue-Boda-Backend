--full monthly disbursements loan book for investors sharing
select id as userId,Account,principal_applied,disbursed_amount,loan_processing_fee,battery_loan,tracking_fee,insuarance_fee, TotalLoan,Term,DisburseDate,LastPaymentDate,DSLP,totalExpected,TotalPaid,LoanBalance,
       IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) as variance,dailyExpected
from (
         SELECT
             l.loanPrincipal as principal_applied,l.total_loan_disburse as disbursed_amount,l.loan_processing_fee,l.ntsa_fee as battery_loan, l.total_mon_fee as tracking_fee,l.credit_life_insurance as insuarance_fee,
             u.id,l.loanAccountMPesa as Account, l.client_loan_total as TotalLoan,l.loan_term as Term,
             (client_loan_total - IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0)) as LoanBalance,

             DATE(l.disbursed_at) as DisburseDate,
     IFNULL(DATE((select MAX(created_at) from mpesa_c2b m2 where (l.loanAccountMPesa = m2.BillRefNumber or l.loanAccountMPesa = m2.ManualRefNumber))),'NO PAYMENT YET') as LastPaymentDate,
     DATEDIFF(curdate(),
              IFNULL((select MAX(created_at) from mpesa_c2b m2 where (l.loanAccountMPesa = m2.BillRefNumber or l.loanAccountMPesa = m2.ManualRefNumber)),l.disbursed_at)
     )  as DSLP,

     IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0) as TotalPaid,
     IF(l.loan_term < DATEDIFF(now(), disbursed_at), l.loan_term * l.daily_amount_expected, DATEDIFF(now(), disbursed_at) * l.daily_amount_expected) as totalExpected,
     l.daily_amount_expected  as dailyExpected

    from loans l
                        inner join users u on u.id = l.userID
where u.id not in (1,2,3,4,5,6,7,8,13,14,15,33,482)
  AND l.disbursed_at is not null
  AND MONTH(DATE(l.disbursed_at)) = 11
    )x



--    Monthly collection report for investors
SET @month_number = 11;

SELECT a.Date,
       des.daily_expected,
       CEILING(SUM(TransTotal)) AS collection_recieved
FROM (
         SELECT LAST_DAY(CONCAT(YEAR(NOW()), '-', @month_number, '-01'))
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
  AND YEAR(TransTime) = YEAR(NOW())
  AND TransactionType = 'Pay Bill'
  AND TransAmount < 50000
GROUP BY DATE(TransTime)
    ) a
    LEFT JOIN daily_expected_summary AS des
ON a.Date = des.date
WHERE a.Date BETWEEN CONCAT(YEAR(NOW()), '-', @month_number, '-01')
  AND LAST_DAY(CONCAT(YEAR(NOW()), '-', @month_number, '-01'))
GROUP BY a.Date
HAVING a.Date <= CURDATE()
ORDER BY a.Date;
