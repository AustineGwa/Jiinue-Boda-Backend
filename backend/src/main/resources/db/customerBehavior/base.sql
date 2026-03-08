-- Step 1: Define the 4-month analysis period
WITH analysis_period AS (
    SELECT
    DATE('2024-05-01') AS start_date,
    DATE('2024-08-31') AS end_date
    ),

-- Step 2: Get all loans disbursed in first 4 months from specific branch
    initial_loans AS (
SELECT
    l.userID,
    l.loanPrincipal,
    l.total_loan_disburse,
    l.client_loan_total,
    l.daily_amount_expected,
    l.loan_term,
    l.loanAccountMPesa AS loanAccount,
    l.disbursed_at,
    l.last_payment_date,
    l.loan_balance,
    l.application_branch,
    -- Calculate collection start date (1 day after disbursement)
    DATE_ADD(l.disbursed_at, INTERVAL 1 DAY) AS collection_start,
    -- Calculate expected maturity date
    DATE_ADD(l.disbursed_at, INTERVAL l.loan_term DAY) AS expected_maturity_date
FROM loans l
    CROSS JOIN analysis_period ap
WHERE l.application_branch = 1
  AND l.disbursed_at BETWEEN ap.start_date AND ap.end_date
    ),

-- Step 3: Calculate total principal needed for first 4 months
    principal_summary AS (
SELECT
    COUNT(DISTINCT loanAccount) AS total_loans_disbursed,
    COUNT(DISTINCT userID) AS unique_customers,
    SUM(loanPrincipal) AS total_principal_required,
    SUM(total_loan_disburse) AS total_disbursed_including_fees,
    AVG(loanPrincipal) AS avg_loan_principal,
    AVG(total_loan_disburse) AS avg_total_disburse,
    MIN(disbursed_at) AS first_disbursement,
    MAX(disbursed_at) AS last_disbursement
FROM initial_loans
    ),

-- Step 4: Identify completed loans and customer behavior
    loan_completions AS (
SELECT
    il.loanAccount AS original_loan_account,
    il.userID,
    il.loanPrincipal AS original_principal,
    il.total_loan_disburse AS original_total_disburse,
    il.expected_maturity_date,
    il.loan_balance,
    CASE
    WHEN il.loan_balance = 0 THEN 'COMPLETED'
    WHEN il.last_payment_date IS NOT NULL THEN 'ACTIVE'
    ELSE 'NO_PAYMENT'
    END AS loan_status,
    -- Check if customer took another loan after this one
    MIN(l2.loanAccountMPesa) AS next_loan_account,
    MIN(l2.disbursed_at) AS next_loan_date,
    MIN(l2.loanPrincipal) AS next_loan_principal,
    MIN(l2.total_loan_disburse) AS next_loan_total,
    CASE
    WHEN MIN(l2.loanAccountMPesa) IS NULL THEN 'No New Loan'
    WHEN MIN(l2.loanPrincipal) > il.loanPrincipal THEN 'Higher Amount'
    WHEN MIN(l2.loanPrincipal) < il.loanPrincipal THEN 'Lower Amount'
    ELSE 'Same Amount'
    END AS customer_behavior
FROM initial_loans il
    LEFT JOIN loans l2
ON il.userID = l2.userID
    AND l2.disbursed_at > il.expected_maturity_date
    AND l2.application_branch = il.application_branch
GROUP BY il.loanAccount, il.userID, il.loanPrincipal, il.total_loan_disburse,
    il.expected_maturity_date, il.loan_balance, il.last_payment_date
    ),

-- Step 5: Generate daily collection schedule for all loans after 4 months
    daily_collections AS (
SELECT
    collection_date,
    SUM(daily_amount_expected) AS total_daily_expected,
    COUNT(DISTINCT loanAccount) AS active_loans_count,
    SUM(CASE WHEN loan_balance > 0 THEN daily_amount_expected ELSE 0 END) AS expected_from_active
FROM (
    -- Generate dates for each loan's collection period
    SELECT
    il.loanAccount,
    il.userID,
    il.daily_amount_expected,
    il.loan_balance,
    il.collection_start,
    il.expected_maturity_date,
    DATE_ADD(il.collection_start, INTERVAL n.n DAY) AS collection_date
    FROM initial_loans il
    -- Generate numbers 0 to 120 to cover maximum loan term
    CROSS JOIN (
    SELECT a.n + b.n * 10 + c.n * 100 AS n
    FROM
    (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
    (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b,
    (SELECT 0 AS n UNION SELECT 1) c
    WHERE a.n + b.n * 10 + c.n * 100 <= 120
    ) n
    WHERE n.n < il.loan_term
    -- Collections start 1 day after disbursement for each loan
    AND DATE_ADD(il.collection_start, INTERVAL n.n DAY) >= (SELECT start_date FROM analysis_period)
    ) daily_schedule
GROUP BY collection_date
ORDER BY collection_date
    ),

-- Step 6: Calculate collection performance metrics
    collection_metrics AS (
SELECT
    COUNT(DISTINCT collection_date) AS total_collection_days,
    MIN(collection_date) AS first_collection_date,
    MAX(collection_date) AS last_collection_date,
    AVG(total_daily_expected) AS avg_daily_expected,
    MAX(total_daily_expected) AS max_daily_expected,
    MIN(total_daily_expected) AS min_daily_expected,
    AVG(active_loans_count) AS avg_active_loans,
    SUM(total_daily_expected) AS total_expected_collections
FROM daily_collections
    ),

-- Step 7: Customer behavior summary
    customer_behavior_summary AS (
SELECT
    customer_behavior,
    loan_status,
    COUNT(*) AS customer_count,
    AVG(original_principal) AS avg_original_principal,
    AVG(next_loan_principal) AS avg_next_principal,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS percentage
FROM loan_completions
GROUP BY customer_behavior, loan_status
    )