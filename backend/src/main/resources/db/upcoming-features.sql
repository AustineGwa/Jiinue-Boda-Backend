--    insert new mpesa app
INSERT INTO mpesa_apps(id, consumer_key, consumer_secret, app_name, api_key,
                       shotcode, products_activated, response_type,
                       c2b_confirmation_url, c2b_validation_url, created_at,
                       updated_at,transaction_type,
                       is_b2c_enabled, b2c_shortcode,
                       b2c_initiator)

VALUES (3,'PIVENQ4LupWGvvAWfIvnddrTlMi3sA0D','OF7szzA9up3YV0K1','pikipata','5342321d568c1f23cdb4ef71f9a8255dabdfe39ee138f997c21777de25491b69','4125097', 1,'Complete',
        'https://fintech.misierraltd.com/payments/momo/postc2b/3','https://fintech.misierraltd.com/payments/momo/postc2b/3', NOW(),NOW(),'CustomerPayBillOnline',1,'4125097','austine')


--get all loans for sacco for a given month
SELECT loanPrincipal as principal_borrowed,total_loan_disburse as amount_disbursed,client_loan_total as total_owed,
       daily_amount_expected,loan_term,loanPurpose,loanAccountMPesa as loan_id,createdAt as date_applied,disbursed_at as date_disbursed
FROM loans WHERE userID IN (SELECT id from users WHERE patner_id = 1) AND MONTH(DATE(disbursed_at)) = 1 AND YEAR(DATE(disbursed_at))=2025

--get all transactions for sacco loans disbursed on a given month
SELECT FirstName,TransID,TransTime,TransAmount,BillRefNumber,ManualRefNumber FROM mpesa_c2b
WHERE BillRefNumber IN (SELECT loanAccountMPesa FROM loans WHERE userID IN (SELECT id from users WHERE patner_id = 1) AND MONTH(DATE(TransTime)) = 7 AND YEAR(DATE(TransTime)) = 2025)
   OR ManualRefNumber IN (SELECT loanAccountMPesa FROM loans WHERE userID IN (SELECT id from users WHERE patner_id = 1) AND MONTH(DATE(TransTime)) =  7  AND YEAR(DATE(TransTime)) = 2025)
AND MONTH(DATE(TransTime)) = 7 AND YEAR(DATE(TransTime)) = 2025
 
--loan restructure
update loans set loan_term = 60, total_mon_fee = 1200, total_interest_amount = 3000, client_loan_total = 34200, daily_amount_expected = 570 where loanAccountMPesa = ''
 
--approve user to partner
UPDATE users SET partner_approved = 1, user_status=1, patner_id=2 WHERE id= (SELECT  id FROM users WHERE phone = '254759100412');
 

--get distribution of DSLP of outstanding balances
SELECT DSLP,count(variance) as total_customers ,sum(variance) as total_variance_today FROM (
select  DSLP,TotalPaid,totalExpected, IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) as variance from (
select
DATEDIFF(curdate(),IFNULL(last_payment_date,l.disbursed_at))  as DSLP,
IFNULL(l.paid_amount,0) as TotalPaid,
l.expected_amount as totalExpected from loans l
inner join users u on u.id = l.userID
where l.disbursed_at is not null AND loan_balance > 0 AND (expected_amount - paid_amount) > 0
)x
) Y GROUP BY DSLP order by DSLP
 

--select from b2c and insert to expenses for all non recorded transactions

INSERT INTO expenses(amount, description, occasion, transaction_id, recipient, disbursed_at, created_at)
SELECT transaction_amount, remarks, occasion, transaction_id, receiver_party_public_name,
STR_TO_DATE(transaction_completed_datetime, '%d.%m.%Y %H:%i:%s'),
created_at
FROM mpesa_b2c
WHERE occasion NOT IN (SELECT loanAccountMPesa FROM loans) AND transaction_id not in (SELECT transaction_id from expenses) AND result_code = 0;



  -- EXPENSES ANALYTICS
SELECT  MONTHNAME (DATE(disbursed_at)) as month, SUM(amount) total_expenses FROM expenses group by month --totals per month
SELECT amount,description,chanel,occasion,transaction_id,recipient,disbursed_at FROM expenses WHERE MONTH(DATE(disbursed_at)) = 6 --individual expenses every month
SELECT expense_type, SUM(amount)as total_amount, COUNT(amount)as total_transactions from expenses GROUP BY expense_type ORDER BY total_amount DESC --
SELECT expense_type, SUM(amount) as total_amount, COUNT(amount)as total_transactions from expenses WHERE MONTH(DATE(disbursed_at)) = 6 GROUP BY expense_type ORDER BY total_amount DESC --
 

  -- monthly loan performance (disbursements)
    select
    u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,
    l.loanPrincipal as Principal,
    l.total_interest_amount as InterestAmount,l.loan_processing_fee as ProcessingFee,
    l.ntsa_fee as BatteryFee,l.credit_life_insurance as Insurance,
    l.total_mon_fee as MonitoringFee,l.total_loan_disburse as Disbursed,
    l.client_loan_total as TotalLoan,l.loan_term as Term,
    DATE(l.disbursed_at) as DisburseDate,
    IFNULL(DATE((select MAX(created_at) from mpesa_c2b m2 where (l.loanAccountMPesa = m2.BillRefNumber or l.loanAccountMPesa = m2.ManualRefNumber))),'NO PAYMENT YET') as LastPaymentDate,
    DATEDIFF(curdate(),
    IFNULL((select MAX(created_at) from mpesa_c2b m2 where (l.loanAccountMPesa = m2.BillRefNumber or l.loanAccountMPesa = m2.ManualRefNumber)),l.disbursed_at)
            )                                                                                                                     as DSLP,
    DATEDIFF(now(),disbursed_at)                                                                                          as loanAge,
    IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan')                                       as Status,
    l.daily_amount_expected                                                                                               as dailyExpected
    from loans l
    inner join users u on u.id = l.userID
    where l.disbursed_at is not null AND MONTH(DATE(l.disbursed_at)) = 6

    

  --monthly performance (collections)
   SELECT
     m.TransID,
     m.TransTime,
     m.TransAmount,
     m.BillRefNumber,
     m.ManualRefNumber,
     m.FirstName,
     (l.client_loan_total - IFNULL(SUM(m.TransAmount), 0)) AS LoanBalance,
     IFNULL(SUM(m.TransAmount), 0) * (l.interestPercentage / 100) AS InterestEarned,
     DATE(l.disbursed_at) AS DisburseDate,
     IFNULL(DATE(MAX(m2.created_at)), 'NO PAYMENT YET') AS LastPaymentDate,
     DATEDIFF(CURDATE(), IFNULL(MAX(m2.created_at), l.disbursed_at)) AS DSLP,
     DATEDIFF(NOW(), l.disbursed_at) AS loanAge,
     IF(l.loan_term < DATEDIFF(NOW(), l.disbursed_at), 'Overdue Loan', 'Active Loan') AS Status,
     IFNULL(SUM(m.TransAmount), 0) AS TotalPaid,
     IF(l.loan_term < DATEDIFF(NOW(), l.disbursed_at), l.loan_term * l.daily_amount_expected, DATEDIFF(NOW(), l.disbursed_at) * l.daily_amount_expected) AS totalExpected,
     l.daily_amount_expected AS dailyExpected,
     FLOOR(IFNULL(SUM(m.TransAmount), 0) / l.daily_amount_expected) AS daysPaid
 FROM
     mpesa_c2b m
 LEFT JOIN
     loans l ON l.loanAccountMPesa = m.BillRefNumber OR l.loanAccountMPesa = m.ManualRefNumber
 LEFT JOIN
     mpesa_c2b m2 ON l.loanAccountMPesa = m2.BillRefNumber OR l.loanAccountMPesa = m2.ManualRefNumber
 WHERE
     MONTH(DATE(m.TransTime)) = 6
 GROUP BY
     m.TransID,
     m.TransTime,
     m.TransAmount,
     m.BillRefNumber,
     m.ManualRefNumber,
     m.FirstName,
     l.client_loan_total,
     l.interestPercentage,
     l.disbursed_at,
     l.loan_term,
     l.daily_amount_expected;
 
   

  --current loan standing as of today
    select first_name,last_name,phone,group_id,sacco,Account,IF(LoanBalance > 0, 'PENDING COMPLETION', 'COMPLETED FULL PAYMENT') as GeneralLoanStanding,
       CASE
            WHEN TotalPaid = TotalExpected THEN 'AT PER'
            WHEN TotalPaid > TotalExpected THEN 'OVER PAYMENT'
            ELSE 'ARREAS'
        END as CurrentLoanStanding,
       IF(TotalPaid > totalExpected, TotalPaid - totalExpected, 0 ) as OverPayment,
       IF(TotalPaid < totalExpected, totalExpected - TotalPaid, 0) as Arreas,
       Principal,InterestAmount, ProcessingFee,BatteryFee,MonitoringFee,Disbursed,TotalLoan,Term,loanAge,LoanBalance,(totalExpected - TotalPaid) as BalanceToday,
       DisburseDate,LastPaymentDate,DSLP,Status,TotalPaid,totalExpected,dailyExpected,daysPaid
        from (
        select
        u.id,u.first_name, u.last_name,u.phone,u.group_id, (select name from partners WHERE id=u.patner_id) as sacco, l.loanAccountMPesa as Account,
        l.loanPrincipal as Principal,l.interestPercentage as Interest,
        l.total_interest_amount as InterestAmount,l.loan_processing_fee as ProcessingFee,
        l.ntsa_fee as BatteryFee,
        l.total_mon_fee as MonitoringFee,l.total_loan_disburse as Disbursed,
        l.client_loan_total as TotalLoan,l.loan_term as Term,
        (client_loan_total - IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0)) as LoanBalance,
        DATE(l.disbursed_at) as DisburseDate,
        IFNULL(DATE((select MAX(created_at) from mpesa_c2b m2 where (l.loanAccountMPesa = m2.BillRefNumber or l.loanAccountMPesa = m2.ManualRefNumber))),'NO PAYMENT YET') as LastPaymentDate,
        DATEDIFF(curdate(),
        IFNULL((select MAX(created_at) from mpesa_c2b m2 where (l.loanAccountMPesa = m2.BillRefNumber or l.loanAccountMPesa = m2.ManualRefNumber)),l.disbursed_at)
        )                                                                                                                     as DSLP,
        DATEDIFF(now(),disbursed_at)                                                                                          as loanAge,
        IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan')                                       as Status,
        IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0) as TotalPaid,
        IF(l.loan_term < DATEDIFF(now(), disbursed_at), l.loan_term * l.daily_amount_expected, DATEDIFF(now(), disbursed_at) * l.daily_amount_expected) as totalExpected,
        l.daily_amount_expected                                                                                               as dailyExpected,
        floor(IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0) / l.daily_amount_expected) as daysPaid
        from loans l
        inner join users u on u.id = l.userID
        where l.disbursed_at is not null and u.id not in (1,2,3,4,5,6,7,8,13,14,15,33,482)
        )x ORDER BY BalanceToday desc

 



--get distribution of total number of loans taken
SELECT total_loans_taken, count(total_loans_taken) as number_of_clients FROM (
SELECT userID, count(userID) as total_loans_taken FROM loans GROUP BY UserID )X GROUP BY total_loans_taken

--loan distribution per branch
SELECT
    MONTH(DATE(disbursed_at)) AS month,
    COUNT(l.id) AS total_loans,
    SUM(CASE WHEN u.patner_id = 1 THEN 1 ELSE 0 END) AS Githurai,
    SUM(CASE WHEN u.patner_id = 2 THEN 1 ELSE 0 END) AS Jiride,
    SUM(CASE WHEN u.patner_id = 3 THEN 1 ELSE 0 END) AS Ekirapa,
    SUM(CASE WHEN u.patner_id = 4 THEN 1 ELSE 0 END) AS Nairobi,
    SUM(CASE WHEN u.patner_id = 5 THEN 1 ELSE 0 END) AS Njiru
FROM loans l
    JOIN users u ON u.id = l.userID
WHERE YEAR(DATE(l.disbursed_at)) = YEAR(DATE(CURDATE()))
GROUP BY month
ORDER BY month;


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



--     update amount expected sum
UPDATE loans l
SET expected_amount = (
    IF(l.loan_term < DATEDIFF(NOW(), l.disbursed_at),
       l.loan_term * l.daily_amount_expected,
       DATEDIFF(NOW(), l.disbursed_at) * l.daily_amount_expected)
    )
WHERE l.loanAccountMPesa = 'A74531689T';

-- update paid amount
UPDATE loans SET paid_amount =(
    SELECT IFNULL((select sum(TransAmount) from mpesa_c2b m where (m.BillRefNumber = loanAccountMPesa or  m.ManualRefNumber = loanAccountMPesa )),0)
)

--update loan balances
UPDATE loans l
    JOIN (
    SELECT loanAccountMPesa,
    client_loan_total - IFNULL((SELECT SUM(TransAmount)
    FROM mpesa_c2b m
    WHERE m.BillRefNumber = loans.loanAccountMPesa
    OR m.ManualRefNumber = loans.loanAccountMPesa), 0) AS calculated_balance
    FROM loans
    ) AS calc
ON l.loanAccountMPesa = calc.loanAccountMPesa
    SET l.loan_balance = calc.calculated_balance


--top 10 clients by clv
SELECT l.userID, u.first_name, u.last_name, DATE(u.created_at) user_since, u.patner_id, COUNT(l.userID) as repeats, sum(l.loanPrincipal) as principal from loans l
    LEFT JOIN users u ON u.id = l.userID
GROUP BY
    l.userID ORDER BY principal desc, repeats desc
    limit 10

-- first month loan performance
SELECT userID,loanPrincipal as principal,loan_term,(DATEDIFF(curdate(),DATE(disbursed_at))) as loan_age,daily_amount_expected,client_loan_total as future_value,
       IFNULL(paid_amount,0) as paid_amount, expected_amount,(IFNULL(expected_amount,0) - IFNULL(paid_amount,0)) as balance ,(paid_amount / loans.expected_amount) as repayment_rate, loanPurpose, disbursed_at as date_disbursed
FROM loans WHERE MONTH(disbursed_at) = 1 AND YEAR(disbursed_at) = 2025 ORDER BY balance desc

-- get all overdue loans + assets
SELECT loanAccountMPesa as account, u.first_name, u.last_name,ca.model, ca.brand, ca.l_plate,ca.make, u.group_id, u.patner_id,loanPrincipal as principal, client_loan_total as total_loan, daily_amount_expected, loan_term, paymentDate as due_date,
    Date(disbursed_at) as disbused_at,paid_amount, expected_amount, loan_balance
FROM loans LEFT JOIN users u ON loans.userID = u.id LEFT JOIN  client_assets ca ON u.id = ca.user_id WHERE loan_balance > 0 and DATE(paymentDate) < Date(curdate()) AND userID NOT IN (1,2,3,4,5,6,7,8,10,13,14,15,33,482)


-- get all users and assets
SELECT loanAccountMPesa as account, u.first_name, u.last_name,u.phone,ca.model, ca.brand, ca.l_plate,ca.make, u.group_id, u.patner_id, paymentDate as due_date
FROM loans LEFT JOIN users u ON loans.userID = u.id LEFT JOIN  client_assets ca ON u.id = ca.user_id

-- Arrears distribution
SELECT
    (SELECT SUM(expected_amount - paid_amount) from loans WHERE loans.paid_amount < loans.expected_amount) as total_arrears,
    (SELECT SUM(loan_balance) from loans WHERE paymentDate < CURDATE() AND loan_balance >0 ) as amount_in_overdue,
    (SELECT SUM((IFNULL(expected_amount,0) - IFNULL(paid_amount,0))) FROM loans WHERE MONTH(disbursed_at) = 1 AND YEAR(disbursed_at) = 2025 ) as arrears_created_this_month,
    (SELECT SUM(variance) from (
    SELECT  IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) as variance
    from (
    select
    l.paid_amount as TotalPaid,
    l.expected_amount as totalExpected
    from loans l
    inner join users u on u.id = l.userID
    where l.loan_balance > 0
    AND l.disbursed_at is not null
    AND ((loan_term - (DATEDIFF(now(),disbursed_at))) < 3 )
    AND paymentDate > curdate()
    AND (IF(paid_amount > expected_amount, 0, expected_amount - paid_amount)) > 0
    )x)y
    ) as amount_3_days_to_completion


--     update last payment data for all loans
UPDATE loans
SET last_payment_date = (
    SELECT DATE(MAX(m2.TransTime))
FROM mpesa_c2b m2
WHERE loans.loanAccountMPesa = m2.BillRefNumber
   OR loans.loanAccountMPesa = m2.ManualRefNumber
    );

-- no payment since disbursement
SELECT     l.loanAccountMPesa as account, u.first_name, u.middle_name, u.last_name,l.loanPrincipal, DATEDIFF(NOW(),disbursed_at) as loanAge,
           ((expected_amount - paid_amount)/daily_amount_expected) as varRation
FROM loans l LEFT JOIN users u ON l.userID = u.id
WHERE last_payment_date is null
  AND disbursed_at is not null
  AND DATEDIFF(NOW(), disbursed_at) > 1
  and u.id not in (1,2,3,4,5,6,7,8,13,14,15,33,482,1068)
ORDER BY disbursed_at desc

-- bad/ non performing loans
-- # paid less than 1/3 of expected
-- # variance is more than 1/3 of total loan
-- # loan age is more than half the loan term
-- # has not paid anything within the last 7 days
SELECT     l.loanAccountMPesa as account, u.first_name, u.middle_name, u.last_name,l.loanPrincipal,loan_term, DATEDIFF(NOW(),disbursed_at) as loanAge,expected_amount,paid_amount,(expected_amount - paid_amount) as variance,
           ROUND(((expected_amount - paid_amount)/daily_amount_expected)) as varRation,last_payment_date
FROM loans l LEFT JOIN users u ON l.userID = u.id
WHERE  disbursed_at is not null
  AND paid_amount < (expected_amount/3)
  AND (expected_amount - paid_amount) > (client_loan_total/3)
  AND DATEDIFF(NOW(),disbursed_at) > (loan_term/2)
  AND DATEDIFF(NOW(),last_payment_date) > 7
  and u.id not in (1,2,3,4,5,6,7,8,13,14,15,33,482,1068)
ORDER BY varRation desc


-- get collections per loan age group
SELECT
    CASE
        WHEN l.loanAge BETWEEN 0 AND 30 THEN '1-30 days'
        WHEN l.loanAge BETWEEN 31 AND 60 THEN '31-60 days'
        WHEN l.loanAge BETWEEN 61 AND 90 THEN '61-90 days'
        WHEN l.loanAge BETWEEN 91 AND 120 THEN '91-120 days'
        WHEN l.loanAge > 120 THEN 'Active Overdues'
        ELSE 'Unknown'
        END AS LoanAgeGroup,
    SUM(m.TransAmount) AS TotalCollected,
    SUM(balance) AS TotalExpected
FROM  (
          SELECT
              loanAccountMpesa AS Account,
              DATEDIFF(NOW(), DATE(disbursed_at)) AS loanAge,
              IF((expected_amount - paid_amount) < 0, 0, expected_amount - paid_amount) AS balance
          FROM loans
          WHERE (loan_balance > 0 OR last_payment_date = '2025-01-29')
            AND disbursed_at IS NOT NULL
      ) l
          LEFT JOIN mpesa_c2b m
                    ON (l.Account = m.BillRefNumber OR l.Account = m.ManualRefNumber)
                        AND DATE(m.TransTime) = '2025-01-29'
GROUP BY LoanAgeGroup
ORDER BY LoanAgeGroup;


-- NON PERFORMING
SELECT disbursed_at,loanAccountMPesa,u.first_name,u.middle_name, u.last_name,u.group_id,client_loan_total as total_loan, loan_balance,DATEDIFF(NOW(),last_payment_date) as DSLP,loan_term,
       DATEDIFF(NOW(), disbursed_at) as loan_age,
       IF(DATE(NOW())> date(paymentDate),'Overdue Loan','Active Loan') as loan_status
FROM loans l LEFT JOIN users u ON u.id = l.userID
WHERE loan_balance > 0
    #      and datediff(NOW(),last_payment_date) > 29
         AND datediff(NOW(),last_payment_date) > 59 AND loanAccountMPesa NOT IN (SELECT loan_account FROM bike_recovery WHERE is_excempted = 1)
ORDER BY dslp desc, loan_balance desc


-- payment red flags before overdue
SELECT  u.first_name, u.last_name,last_name, u.phone, (expected_amount-paid_amount) as variance,loan_term, DATEDIFF(NOW(),disbursed_at) AS loanAge FROM loans l LEFT JOIN users u ON u.id = l.userID
WHERE DATEDIFF(Now(),last_payment_date) > 5
  AND loan_balance > 0
  and (expected_amount-paid_amount) > (daily_amount_expected * 2)
  AND  DATEDIFF(NOW(),disbursed_at) <= loan_term
  AND l.loanAccountMPesa NOT IN (SELECT loan_account FROM bike_recovery WHERE is_excempted =1 OR bike_recoverd_at is not null)
ORDER BY loan_term DESC , loanAge desc


-- PAR CALCULATOR
SELECT loanAccountMPesa as loan_id,loanPrincipal as total_loan_principal, ((ROUND(((loanPrincipal / client_loan_total) ),2))*expected_amount) as expected_principal,((ROUND(((loanPrincipal / client_loan_total)),2))*paid_amount) as paid_principal, client_loan_total as total_active_loan_portfolio,ROUND(((loanPrincipal / client_loan_total) * 100),2) as percentage_principal,
       expected_amount,paid_amount,loan_balance as total_loan_balance ,(expected_amount-paid_amount) as variance,
       ROUND(((expected_amount - paid_amount)/daily_amount_expected)) as varRatio,loan_term,datediff(now() , disbursed_at) as loan_age  FROM loans WHERE loan_balance > 0

--all loans for a moth and their paymants over time
SELECT TransactionType,TransID,TransTime,TransAmount,BillRefNumber as loan_account,FirstName as paid_by FROM mpesa_c2b where BillRefNumber IN (
    SELECT loan_account FROM(
    SELECT loanAccountMPesa as loan_account,loanPrincipal as borrrowed_principal, client_loan_total as total_amount_owed,
           paid_amount as total_paid_to_date, expected_amount as total_expected_to_date, DATE_ADD(DATE(disbursed_at), INTERVAL loan_term DAY ) as expected_end_date,
           last_payment_date from loans WHERE YEAR(DATE(disbursed_at)) = 2025 AND MONTH(DATE(disbursed_at)) = 1
)x
)

--WEEKLY TRANSACTIONS (B2C)
SELECT party_b,occasion,transaction_id,transaction_amount,reciever_name,transaction_completed_datetime FROM mpesa_b2c WHERE DATE(created_at) BETWEEN  DATE('2025-05-19') AND DATE(NOW())

--payroll
SELECT p.user_id,p.mpesa_account, p.bank_account, u.first_name,u.last_name, p.base_salary,
       (select sum(amount) from salary_advances WHERE user_id = p.user_id AND MONTH(DATE(advance_date)) = 4) as advance_total,last_payment_date
FROM payroll p LEFT JOIN users u on p.user_id = u.id WHERE p.deleted_at is null ORDER BY u.created_at

--daily disbursment summery
SELECT SUM(loanPrincipal) as total_principal, count(id) as total_clients, SUM(total_loan_disburse),sum(client_loan_total) from loans WHERE DATE(createdAt) = CURDATE()
-- grouped daily pending loans summery
SELECT Date(createdAt), Sum(total_loan_disburse) FROM loans WHERE disbursed_at is null GROUP BY DATE(createdAt)
--day on day disbursements increment
SELECT Date(createdAt) as date, Sum(loanPrincipal) as principal, COUNT(id) as totalClients FROM loans WHERE month(DATE(createdAt)) = 6 AND YEAR(DATE(createdAt)) = 2025 GROUP BY date order by principal desc
--disburesed initiated but sending failed
SELECT * FROM loans WHERE disburse_initiated =1 AND disbursed_at IS NULL

 --insert excel payment
="INSERT INTO mpesa_c2b(TransactionType,TransID,TransTime,TransAmount,BusinessShortCode,BillRefNumber,is_manual,ManualRefNumber,InvoiceNumber,FirstName,appId,created_at,updated_at) VALUES ('Pay Bill','" &A2  & "',STR_TO_DATE('" &B2  & "','%d-%m-%Y %H:%i:%s')," &F2  & ",'4125097','" &C2  & "',0,null,null,null,3,NOW(),NOW());"

-- # mapping summary
SELECT (SELECT name from counties WHERE county_id=`groups`.county_id) as county,
       (SELECT name from sub_counties WHERE sub_county_id=`groups`.sub_county_id) as sub_county,
       (SELECT name from wards WHERE ward_id=`groups`.ward_id) as ward ,
       group_name as stage_name,stage_latitude,stage_longitude,chair_name,chair_phone,access_roads,created_at,
       (SELECT first_name from users WHERE id = `groups`.created_by) as createdBy
FROM `groups` WHERE `groups`.stage_longitude > 0.00 ORDER BY created_at DESC


-- stage survey responses
SELECT (SELECT group_name FROM `groups` WHERE id=stage_survey_responses.group_id) stage_name , number_of_members,IF(registered_with_sacco = 1, 'YES','NO'),
       working_hours,has_regular_meetings,meeting_date,meeting_time,(SELECT first_name FROM users WHERE id= stage_survey_responses.created_by) as created_by,
       created_at FROM stage_survey_responses


-- pausing loans for known conditions like medical

-- investors
SELECT investors.p_phone, investors.s_phone, investors.email, investors.user_name, investors.password, investors.can_login, investors.created_at, investors.updated_at, investments.deleted_at,
 SELECT investors.first_name, investors.last_name,investors.status,investments.investment_amount,investments.created_at as date_invested
 FROM investors left join investments ON investors.id = investments.investor_id



     # users with dublicate loans

SELECT
    userID,
    COUNT(*) as active_loan_count,
    GROUP_CONCAT(loanAccountMPesa) as loan_ids,
    GROUP_CONCAT(loan_balance) as balances
FROM loans
WHERE loan_balance > 0
  AND disbursed_at IS NOT NULL
GROUP BY userID
HAVING COUNT(*) > 1
ORDER BY active_loan_count DESC;


# unique trackers used

WITH daily_changes AS (
    SELECT
        DATE(disbursed_at) as activity_date,
        COUNT(*) as loans_disbursed,
        0 as loans_cleared
    FROM loans
    WHERE disbursed_at IS NOT NULL
    GROUP BY DATE(disbursed_at)

    UNION ALL

    SELECT
        DATE(last_payment_date) as activity_date,
        0 as loans_disbursed,
        COUNT(*) as loans_cleared
    FROM loans
    WHERE loan_balance = 0
      AND last_payment_date IS NOT NULL
    GROUP BY DATE(last_payment_date)
),
running_total AS (
    SELECT
        activity_date,
        SUM(loans_disbursed) as daily_disbursed,
        SUM(loans_cleared) as daily_cleared,
        SUM(SUM(loans_disbursed)) OVER (ORDER BY activity_date) as cumulative_disbursed,
        SUM(SUM(loans_cleared)) OVER (ORDER BY activity_date) as cumulative_cleared,
        SUM(SUM(loans_disbursed) - SUM(loans_cleared)) OVER (ORDER BY activity_date) as active_loans
    FROM daily_changes
    GROUP BY activity_date
)
SELECT
    activity_date,
    daily_disbursed as trackers_deployed_today,
    daily_cleared as trackers_returned_today,
    cumulative_disbursed as total_trackers_ever_deployed,
    cumulative_cleared as total_trackers_ever_returned,
    active_loans as trackers_in_use
FROM running_total
ORDER BY active_loans DESC
    LIMIT 1;

# loan balance distribution
SELECT
    COUNT(*) as total_loan_count,
    COUNT(DISTINCT userID) as total_unique_users,
    SUM(CASE WHEN loan_balance > 0 THEN 1 ELSE 0 END) as active_loans,
    COUNT(DISTINCT CASE WHEN loan_balance > 0 THEN userID END) as active_loan_users,
    SUM(CASE WHEN loan_balance <= 0 THEN 1 ELSE 0 END) as zero_balance_loans,
    COUNT(DISTINCT CASE WHEN loan_balance <= 0 THEN userID END) as zero_balance_users
FROM loans
WHERE disbursed_at IS NOT NULL
  AND loan_balance IS NOT NULL;

-- Excel to insert trackers to db
 INSERT INTO bike_trackers(model, imei, created_at) VALUES ('','',NOW())
 ="INSERT INTO bike_trackers(model, imei, created_at) VALUES ('"&F3&"','"&C3&"',NOW());"

-- match loan to user to tracker to IMEI
SELECT l.disbursed_at, l.userID, l.loanAccountMPesa as loanId, ca.l_plate,'' as tracker_imei, '' as simcard
FROM loans l left join client_assets ca on l.userID = ca.user_id
WHERE loan_balance > 0 AND ca.deleted_at is null

--COLLECTIONS DASHBOARD TEST SCRIPTS / DESIGN
-- break each loan into its daily expected for all the valid loan term days

-- bulk insert query for back dating
INSERT INTO loan_daily_payment_expectations
(loan_id,account_number, day_number, due_date, daily_amount, cumulative_amount)
WITH RECURSIVE numbers AS (
    SELECT 1 as n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 360
    )
SELECT
    l.id as loan_id,
    l.loanAccountMPesa,
    n.n as day_number,
    DATE_ADD(l.disbursed_at, INTERVAL n.n - 1 DAY) as due_date,
    l.daily_amount_expected as daily_amount,
    l.daily_amount_expected * n.n as cumulative_amount
FROM loans l
         CROSS JOIN numbers n
WHERE l.disbursed_at IS NOT NULL
  AND n.n <= l.loan_term
ORDER BY l.id, n.n;



-- get loan repayment status
SELECT
    e.loan_id,
    e.day_number,
    e.due_date,
    e.cumulative_amount as expected_to_date,
    COALESCE(SUM(m.TransAmount), 0) as total_paid,
    e.cumulative_amount - COALESCE(SUM(m.TransAmount), 0) as arrears
FROM loan_daily_payment_expectations e
         LEFT JOIN mpesa_c2b m
                   ON e.account_number = m.BillRefNumber
                       AND DATE(m.TransTime) <= e.due_date
WHERE e.loan_id = 800
GROUP BY e.loan_id, e.day_number, e.due_date, e.cumulative_amount;


-- get status for all active loans today
SELECT e.loan_id,
       e.account_number,
       (SELECT first_name from users WHERE id = l.userID)    as first_name,
       e.cumulative_amount                                   as expected_to_date,
       COALESCE(SUM(m.TransAmount), 0)                       as total_paid,
       e.cumulative_amount - COALESCE(SUM(m.TransAmount), 0) as arrears,
       CASE
           WHEN e.cumulative_amount - COALESCE(SUM(m.TransAmount), 0) > 0 THEN 'In Arrears'
           WHEN e.cumulative_amount - COALESCE(SUM(m.TransAmount), 0) < 0 THEN 'Overpaid'
           ELSE 'Up to Date'
           END  as status
FROM loan_daily_payment_expectations e
         LEFT JOIN loans l ON e.loan_id = l.id
         LEFT JOIN mpesa_c2b m ON e.account_number = m.BillRefNumber AND  DATE(m.TransTime) <= e.due_date
WHERE e.due_date = CURDATE()
GROUP BY e.loan_id, e.account_number, e.cumulative_amount
ORDER BY arrears

-- get leads + location details
SELECT ml.user_name, ml.phone_number,g.group_name,
       (SELECT name FROM counties WHERE counties.county_id = g.county_id) as county,
       (SELECT name FROM sub_counties WHERE sub_counties.sub_county_id = g.sub_county_id) as sub_county,
       (SELECT name FROM wards WHERE wards.ward_id = g.ward_id) as sub_county,
       (SELECT first_name from users WHERE users.id = ml.created_by) as created_by,
       ml.created_at
FROM marketing_leads ml LEFT JOIN `groups` g ON ml.group_id = g.id

-- pending users (no loans)

SELECT first_name, last_name,phone,g.group_name,
       (SELECT name FROM counties WHERE counties.county_id = g.county_id) as county,
       (SELECT name FROM sub_counties WHERE sub_counties.sub_county_id = g.sub_county_id) as sub_county,
       (SELECT name FROM wards WHERE wards.ward_id = g.ward_id) as sub_county,
       (SELECT first_name from users WHERE users.id = created_by) as created_by,
       u.created_at
FROM users u LEFT JOIN `groups` g On u.group_id = g.id
WHERE u.id NOT IN (select loanAccountMPesa from loans)
  AND u.id NOT IN (SELECT user_id FROM user_roles WHERE usertype != 'Client')
ORDER BY created_at desc

-- aggregating daily expectation per loan per client (expected amount vs paid for all the days of the loan duration)

SELECT
    e.loan_id,
    e.day_number,
    e.due_date,
    e.daily_amount as daily_expected,
    e.cumulative_amount as cumulative_expected,
    COALESCE(SUM(m.TransAmount), 0) as cumulative_paid,
    e.cumulative_amount - COALESCE(SUM(m.TransAmount), 0) as balance
FROM loan_daily_payment_expectations e
         LEFT JOIN mpesa_c2b m
                   ON e.account_number = m.BillRefNumber
                       AND DATE(m.TransTime) <= e.due_date
GROUP BY e.loan_id, e.day_number, e.due_date, e.daily_amount, e.cumulative_amount
ORDER BY e.day_number;

--     #  SELECT FirstName,(SELECT number FROM mpesa_hash_table where hash = mpesa_c2b.MSISDN) as  MSISDN,BillRefNumber as LoanID,TransID,TransTime,TransAmount
--                                                                                                 #                                      FROM mpesa_c2b
--                                                                                                                                                 #                                      WHERE BillRefNumber NOT IN (SELECT loanAccountMPesa FROM loans )
--            #                                      AND TransAmount < 20000
-- #                                      AND BillRefNumber NOT IN (SELECT loanID FROM fuel_loan )
-- #                                      AND BillRefNumber != 'JWINV2'
-- #                                      ORDER BY created_at DESC
--
--
--
--            # -- Most important: covering index on mpesa_c2b
--            # CREATE INDEX idx_mpesa_c2b_optimized
--     # ON mpesa_c2b(TransAmount, BillRefNumber, created_at DESC, FirstName, TransID, TransTime, MSISDN);
-- #
-- # -- For the hash lookup (critical with 200M rows!)
-- # CREATE INDEX idx_mpesa_hash_table_hash_number
--     # ON mpesa_hash_table(hash, number);
-- #
-- # -- For EXISTS checks
-- # CREATE INDEX idx_loans_account_mpesa
--     # ON loans(loanAccountMPesa);
-- #
-- # CREATE INDEX idx_fuel_loan_id
--     # ON fuel_loan(loanID);

-- get call collections per rep

SELECT
    CONCAT(u.first_name,' ' , u.last_name )            AS officer,
    COUNT(DISTINCT ccl.id)                        AS clients_reached_today,
    COALESCE(SUM(m.TransAmount), 0)               AS amount_collected_from_contacted_clients,
    SUM(l.expected_amount - l.paid_amount ) AS total_arrears
FROM call_center_logs ccl
         JOIN users u
              ON u.id = ccl.rep_id
         LEFT JOIN mpesa_c2b m ON m.BillRefNumber = ccl.loan_account AND DATE(m.created_at) = CURDATE()
    LEFT JOIN loans l on l.loanAccountMPesa = ccl.loan_account
WHERE
    DATE(ccl.created_at) = CURDATE()
  AND  ccl.comment_type = 'Field Call'
GROUP BY officer
ORDER BY amount_collected_from_contacted_clients DESC;

-- bulk insert wages  excel
="INSERT INTO temp_expense_requests(category_id, description, reciever_type, reciever, amount, status, created_at) VALUES(1, '" & B2 & " December  salary', 'PHONENUMBER', '" & E2 & "', " & J2 & ", 'PENDING_APPROVAL', NOW());"