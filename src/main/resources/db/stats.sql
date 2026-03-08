-- general summary (system)
                                                                                                                                                                          and u.id not in (1, 2, 3, 4, 5, 6, 7, 8, 13, 14, 15, 33)) x)y

-- 1.user growth per month
SELECT MONTHNAME(DATE(created_at)) as month, count(id) as total_new_users  FROM users WHERE YEAR(created_at) = YEAR(curdate()) GROUP BY month
-- 2.Investment amount growth.

-- 3.loans disbursements per month
SELECT MONTHNAME(DATE(createdAt)) as month,
       count(id) as total_new_loans,
       SUM(loanPrincipal) as principal_recorded,
       SUM(total_loan_disburse) as net_proceeds_to_customer,
       SUM(client_loan_total) as future_loan_value,
       Sum(client_loan_total - total_loan_disburse) as expected_gross_earnings
FROM loans WHERE YEAR(createdAt) = YEAR(curdate()) GROUP BY month
-- 2.loans collections per month

SELECT MONTHNAME(DATE(created_at)) as month,
       count(TransID) as total_transactions,
       SUM(TransAmount) as total_amount_recieved
FROM mpesa_c2b WHERE YEAR(created_at) = YEAR(curdate())
                 AND TransAmount < 50000
GROUP BY month

-- 3. collections expected per month
SELECT MONTHNAME(DATE(date)) as month,
       SUM(daily_expected) as total_amount_recieved
FROM daily_expected_summary WHERE YEAR(date) = YEAR(curdate())
GROUP BY month
-- 4. Completed loans , repeat loans
SELECT (Select first_name From users WHERE id =loans.userID) as first_name,
       (Select last_name From users WHERE id =loans.userID) as last_name ,
       COUNT(userID) as numer_of_loans_taken,
       Sum(loanPrincipal) loan_principal_borrowed,
       Sum(total_loan_disburse) total_amount_recieved,
       Sum(client_loan_total) loan_value_created
FROM loans GROUP BY userID HAVING numer_of_loans_taken > 1 ORDER BY numer_of_loans_taken DESC

-- non performing loans
SELECT first_name,last_name, Account,TotalPaid,totalExpected,LoanBalance,loan_term, loanAge FROM (
select id,first_name,last_name,phone, Account,
TotalPaid,totalExpected,
IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected as varRatio,
LoanBalance,Status,loanAge,patner_id,loan_term,
dailyExpected
from (
select
u.id,u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,u.patner_id,l.loan_term,
IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0) as TotalPaid,
IF(l.loan_term < DATEDIFF(now(), disbursed_at), l.loan_term * l.daily_amount_expected, DATEDIFF(now(), disbursed_at) * l.daily_amount_expected) as totalExpected,
l.daily_amount_expected  as dailyExpected,
(client_loan_total - IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0)) as LoanBalance,
IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan') as Status,
DATEDIFF(now(),disbursed_at)  as loanAge
from loans l
inner join users u on u.id = l.userID
where l.disbursed_at is not null and u.id not in (1,2,3,4,5,6,7,8,13,14,15,33,482)
)x WHERE Status = 'Overdue  Loan' AND LoanBalance > 0)Y


-- Active loans
SELECT first_name,last_name, Account,TotalPaid,totalExpected,LoanBalance,loan_term, loanAge FROM (
select id,first_name,last_name,phone, Account,
TotalPaid,totalExpected,
IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid)                as variance,
IF(TotalPaid > totalExpected, 0, totalExpected - TotalPaid) / dailyExpected as varRatio,
LoanBalance,Status,loanAge,patner_id,loan_term,
dailyExpected
from (
select
u.id,u.first_name, u.last_name,u.phone,l.loanAccountMPesa as Account,u.patner_id,l.loan_term,
IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0) as TotalPaid,
IF(l.loan_term < DATEDIFF(now(), disbursed_at), l.loan_term * l.daily_amount_expected, DATEDIFF(now(), disbursed_at) * l.daily_amount_expected) as totalExpected,
l.daily_amount_expected  as dailyExpected,
(client_loan_total - IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber or l.loanAccountMPesa = m.ManualRefNumber)),0)) as LoanBalance,
IF(l.loan_term < DATEDIFF(now(), disbursed_at), 'Overdue  Loan', 'Active Loan') as Status,
DATEDIFF(now(),disbursed_at)  as loanAge
from loans l
inner join users u on u.id = l.userID
where l.disbursed_at is not null and u.id not in (1,2,3,4,5,6,7,8,13,14,15,33,482)
)x WHERE Status = 'Active Loan' )Y


-- GET COLLECTIONS DISTRIBUTION PER HOUR

SELECT
    DATE_FORMAT(TransTime, '%Y-%m-%d %H:00:00') AS hour,
    COUNT(*) AS transaction_count,
    SUM(TransAmount) AS total_amount
FROM
    mpesa_c2b
WHERE DATE(TransTime) = DATE('2024-08-05')
GROUP BY
    DATE_FORMAT(TransTime, '%Y-%m-%d %H:00:00')
ORDER BY
    hour;


/*

# SELECT (select name from sub_counties WHERE id=T.sub_county_id) as sub_county, (select name from wards WHERE id=ward_id) as ward, T.*
# FROM `groups` T WHERE DATE(created_at) = date('2025-07-25')
#
# SELECT * FROM loans


# SELECT count(userId) as total_new_loans, first_time_loan from (
#                 SELECT l.userId, DATE(MIN(l.createdAt)) AS first_time_loan
#                 FROM loans l
#                 WHERE l.disbursed_at is not null
#                 GROUP BY l.userId
#                 HAVING MONTH(first_time_loan) = 7 AND YEAR(first_time_loan) = YEAR(CURDATE())
#                 )x
# GROUP BY first_time_loan
#
#
#
#
# SELECT (select name from wards where wards.ward_id=`groups`.ward_id) as sub_county,group_name, created_at AS date_onboarded FROM `groups` WHERE id IN(
#     SELECT group_id FROM users WHERE id IN(
#     SELECT userId FROM (
#         SELECT l.userId, DATE(MIN(l.createdAt)) AS first_time_loan
#                 FROM loans l
#                 WHERE l.disbursed_at is not null
#                 GROUP BY l.userId
#                 HAVING MONTH(first_time_loan) = 7 AND YEAR(first_time_loan) = YEAR(CURDATE())
#
#                )X
#     )
#     ) ORDER BY date_onboarded DESC



# SELECT
#     g.group_name,
#     (SELECT name FROM counties WHERE counties.county_id = g.county_id) as county,
#     (SELECT name FROM sub_counties WHERE sub_counties.sub_county_id = g.sub_county_id) as sub_county,
#     (SELECT name FROM wards WHERE wards.ward_id = g.ward_id) as ward,
#     g.created_at AS date_onboarded,
#     ftl.first_time_loan,
#     COUNT(ftl.userId) as total_new_loans
# FROM (
#     SELECT
#         l.userId,
#         DATE(MIN(l.createdAt)) AS first_time_loan
#     FROM loans l
#     WHERE l.disbursed_at IS NOT NULL
#     GROUP BY l.userId
#     HAVING MONTH(first_time_loan) = 7
#         AND YEAR(first_time_loan) = YEAR(CURDATE())
# ) ftl
# INNER JOIN users u ON ftl.userId = u.id
# INNER JOIN `groups` g ON u.group_id = g.id
# GROUP BY ftl.first_time_loan, g.id, g.group_name, g.ward_id, g.created_at
# ORDER BY date_onboarded DESC;


/*
# CALL CENTER
# SELECT (SELECT first_name from users WHERE id = rep_id) as first_name,
# (SELECT last_name from users WHERE id = rep_id) as last_name,
# count(rep_id) as total_calls_made
# # (SELECT SUM(TransAmount) from mpesa_c2b where (BillRefNumber IN (SELECT loan_id FROM collections_assignments WHERE rep_id = call_center_logs.rep_id  AND DATE(TransTime) = DATE(CURDATE()) )
# #            OR ManualRefNumber IN (SELECT loan_id FROM collections_assignments WHERE rep_id = call_center_logs.rep_id AND DATE(TransTime) = DATE(CURDATE())))
# # )as amount_collected
# from call_center_logs
# # WHERE MONTH(DATE(created_at)) = 1 AND YEAR(created_at) = 2025
# WHERE DATE(created_at) = DATE(CURDATE())
# group by rep_id ORDER BY total_calls_made DESC


# Martha,Geoffrey,purity,Ndombi,Elvis
# weekly call performance per rep

# SELECT
#        Date(created_at) as day_of_week,
#        (SELECT first_name from users WHERE id = rep_id) as first_name,
#        (SELECT last_name from users WHERE id = rep_id) as last_name,
#        count(rep_id) as total_calls
# FROM call_center_logs
# WHERE DATEDIFF(NOW(),DATE(created_at)) < 11
# GROUP BY rep_id,
#          Date(created_at) ,
#          first_name , first_name


# SELECT * FROM call_center_logs WHERE call_picked = 0 AND date(created_at) = date(now())

# get payments for calls made
# SELECT ccl.loan_account,(SELECT first_name FROM users WHERE id=ccl.rep_id) as called_by, IF(ccl.call_picked = 1,'YES','NO') as call_picked, ccl.reason_not_picked,
# ccl.client_response,TransAmount FROM mpesa_c2b mc2b
#  Right JOIN call_center_logs ccl on mc2b.BillRefNumber = ccl.loan_account
#  WHERE  DATE(CURDATE()) = DATE(mc2b.created_at)

# promises that were not kept
# SELECT loan_account,client_response FROM call_center_logs where call_picked = 1 AND loan_account NOT IN
# (SELECT BillRefNumber from mpesa_c2b WHERE DATE(TransTime) = (CURDATE() -1))
# hourly calls per day
# SELECT HOUR(ccl.created_at) as hour_of_day,Count(ccl.created_at) as calls_made FROM call_center_logs ccl LEFT JOIN users u ON u.id = ccl.rep_id
#                                                              WHERE DATE(ccl.created_at) = CURDATE()
#                                                              GROUP BY hour_of_day
#                                                              ORDER BY hour_of_day


# get daily calls
# SELECT loan_account,ccl.created_at call_time, u.first_name,call_picked,client_response
# FROM call_center_logs ccl LEFT JOIN users u ON u.id = ccl.rep_id  WHERE DATE(ccl.created_at) = CURDATE() ORDER BY ccl.created_at

# calls per person today
# SELECT ccl.created_at call_time FROM call_center_logs ccl LEFT JOIN users u ON u.id = ccl.rep_id  WHERE DATE(ccl.created_at) = CURDATE() GROUP BY ccl.created_at
 */




# # loan book for earnings calculator
# -- Optimized loan query with date cutoffs
# -- Set your cutoff dates here
# SET @disbursement_cutoff = '2024-12-31';  -- Only loans disbursed before this date
# SET @payment_cutoff = '2024-12-31';       -- Only payments made before this date
#
# WITH loan_payments AS (
#     SELECT
#         l.disbursed_at as disbursement_date,
#         l.loanAccountMPesa,
#         l.loanPrincipal,
#         l.total_loan_disburse as customer_disbursed_amount,
#         l.ntsa_fee as battery_charge,
#         l.total_interest_amount,
#         l.total_mon_fee as total_monitoring_fee,
#         l.credit_life_insurance,
#         l.loan_processing_fee,
#         COALESCE((
#             SELECT SUM(TransAmount)
#             FROM mpesa_c2b m
#             WHERE (l.loanAccountMPesa = m.BillRefNumber OR l.loanAccountMPesa = m.ManualRefNumber) AND DATE(m.TransTime) <= @payment_cutoff
#         ), 0) as TotalPaid
#     FROM loans l
#     WHERE l.disbursed_at IS NOT NULL
#         AND DATE(l.disbursed_at) <= @disbursement_cutoff  -- Filter disbursements by cutoff date
# )
# SELECT
#     u.first_name,
#     u.last_name,
#     l.loanAccountMPesa as Account,
#     DATE(lp.disbursement_date) as disbursement_date,
#     lp.loanPrincipal,
#     lp.customer_disbursed_amount,
#     lp.battery_charge,
#     lp.total_interest_amount,
#     lp.total_monitoring_fee,
#     lp.credit_life_insurance,
#     lp.loan_processing_fee,
#     lp.TotalPaid,
#     CASE
#         WHEN l.loan_term < DATEDIFF(@payment_cutoff, l.disbursed_at)
#         THEN l.loan_term * l.daily_amount_expected
#         ELSE DATEDIFF(@payment_cutoff, l.disbursed_at) * l.daily_amount_expected
#     END as totalExpected,
#     (l.client_loan_total - lp.TotalPaid) as LoanBalance,
#     l.loan_term,
#     DATEDIFF(@payment_cutoff, l.disbursed_at) as loanAge,
#     CASE
#         WHEN l.loan_term < DATEDIFF(@payment_cutoff, l.disbursed_at)
#         THEN 'Overdue Loan'
#         ELSE 'Active Loan'
#     END as Status
# FROM loans l
# INNER JOIN users u ON u.id = l.userID
# INNER JOIN loan_payments lp ON l.loanAccountMPesa = lp.loanAccountMPesa
# WHERE l.disbursed_at IS NOT NULL
#     AND DATE(l.disbursed_at) <= @disbursement_cutoff
#     AND u.id NOT IN (1,2,3,4,5,6,7,8,13,14,15,33,482)
# ORDER BY l.disbursed_at;
 */


-- recovery summery
/*
 # SELECT * FROM bike_recovery WHERE loan_account ='C38945172G'

# # UPDATE RECOVERY
# INSERT INTO bike_recovery (userId,loan_account, loan_age_as_at_revory_entry, expected_amount_as_at_recovory_entry, paid_amount_as_at_recovory_entry, variance_as_at_recovory_entry,
#                            var_ratio_as_at_recovory_entry, last_payment_date_as_at_recovory_entry, created_at)
#
# SELECT userID,account, loanAge, expected_amount, paid_amount, variance, varRatio, last_payment_date,created_at FROM (
#                                                                                                                         SELECT l.userID, l.loanAccountMPesa                                               as account,
#                                                                                                                                DATEDIFF(NOW(), disbursed_at)                                    as loanAge,
#                                                                                                                                expected_amount,
#                                                                                                                                paid_amount,
#                                                                                                                                (expected_amount - paid_amount)                                  as variance,
#                                                                                                                                ROUND(((expected_amount - paid_amount) / daily_amount_expected)) as varRatio,
#                                                                                                                                last_payment_date,
#                                                                                                                                NOW() as created_at
#                                                                                                                         FROM loans l
#                                                                                                                         WHERE loanAccountMPesa = 'K85412739M'
#                                                                                                                     )X
# UPDATE bike_recovery SET recovery_updated_by=482,bike_recoverd_at=DATE('2025-05-19'),recovery_amount='2500', recovery_comment='done'
#                      WHERE id= 2127

# select id,recovery_amount from bike_recovery WHERE loan_account='P69734285P' ORDER BY created_at DESC LIMIT 1

# RECOVERY PAYMENTS
# SELECT sum(recovery_amount) FROM bike_recovery WHERE bike_recoverd_at is not null AND  DATE(bike_recoverd_at) BETWEEN  DATE('2025-03-23') AND DATE('2025-03-28')

# SELECT u.first_name, u.last_name, u.patner_id,loan_account, ca.l_plate as number_plate ,ca.make,DATE(bike_recoverd_at) as recovery_date,
# recovery_amount FROM bike_recovery
# LEFT JOIN client_assets ca on bike_recovery.userId = ca.user_id
# LEFT JOIN users u ON u.id = bike_recovery.userId
# WHERE bike_recoverd_at is not null
# AND  DATE(bike_recoverd_at) BETWEEN  DATE('2025-07-12') AND DATE('2025-07-18')

# SELECT * FROM bike_recovery WHERE loan_account='X12698753Y'

 */


