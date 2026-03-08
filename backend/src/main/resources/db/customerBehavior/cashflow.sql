-- INVESTOR CAPITAL REQUIREMENT MODEL
-- Goal: Calculate capital needed to reach 2.25M weekly disbursement capacity (75 people x 30k)
-- Target daily collection: ~321k (to sustain weekly disbursements)

WITH parameters AS (
    SELECT
        30000 AS target_loan_amount,           -- KES per person
        75 AS target_weekly_customers,         -- Number of people per week
        30000 * 75 AS target_weekly_disburse,  -- 2.25M per week
        (30000 * 75) / 7 AS target_daily_collection, -- ~321k per day needed
        60 AS avg_loan_term,                   -- Average loan term in days
        0.20 AS avg_interest_rate,             -- 20% interest rate
    DATE('2024-09-01') AS start_date,      -- Investment start date
    16 AS weeks_to_model                   -- 4 months = ~16 weeks
    ),

-- Calculate loan financials
    loan_metrics AS (
SELECT
    target_loan_amount,
    target_loan_amount * (1 + avg_interest_rate) AS total_repayment,
    (target_loan_amount * (1 + avg_interest_rate)) / avg_loan_term AS daily_collection_per_loan,
    avg_loan_term,
    target_weekly_customers,
    target_weekly_disburse,
    target_daily_collection
FROM parameters, (SELECT avg_loan_term, avg_interest_rate FROM parameters) p
    ),

-- Generate weekly disbursement schedule
    weekly_schedule AS (
SELECT
    w.week_num,
    DATE_ADD(p.start_date, INTERVAL (w.week_num - 1) * 7 DAY) AS week_start_date,
    lm.target_weekly_customers AS customers_this_week,
    lm.target_weekly_disburse AS disbursement_this_week,
    lm.daily_collection_per_loan
FROM parameters p
    CROSS JOIN loan_metrics lm
    CROSS JOIN (
    SELECT 1 AS week_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16
    ) w
WHERE w.week_num <= p.weeks_to_model
    ),

-- Generate daily cashflow for each loan disbursed
    daily_loan_cashflow AS (
SELECT
    ws.week_num,
    ws.week_start_date AS disbursement_date,
    DATE_ADD(ws.week_start_date, INTERVAL 1 DAY) AS collection_start_date,
    DATE_ADD(ws.week_start_date, INTERVAL d.day_offset DAY) AS cashflow_date,
    d.day_offset,
    CASE
    WHEN d.day_offset = 0 THEN -ws.disbursement_this_week  -- Disbursement (cash out)
    WHEN d.day_offset >= 1 AND d.day_offset <= (SELECT avg_loan_term FROM parameters)
    THEN ws.daily_collection_per_loan * ws.customers_this_week  -- Collection (cash in)
    ELSE 0
    END AS daily_cashflow
FROM weekly_schedule ws
    CROSS JOIN (
    -- Generate day offsets from 0 (disbursement day) to 120 (max collection days)
    SELECT a.n + b.n * 10 + c.n * 100 AS day_offset
    FROM
    (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
    (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b,
    (SELECT 0 AS n UNION SELECT 1) c
    WHERE a.n + b.n * 10 + c.n * 100 <= 120
    ) d
    ),

-- Aggregate daily cashflows across all loans
    daily_aggregate AS (
SELECT
    cashflow_date,
    SUM(daily_cashflow) AS net_daily_cashflow,
    SUM(CASE WHEN daily_cashflow < 0 THEN daily_cashflow ELSE 0 END) AS daily_disbursements,
    SUM(CASE WHEN daily_cashflow > 0 THEN daily_cashflow ELSE 0 END) AS daily_collections
FROM daily_loan_cashflow
WHERE cashflow_date <= DATE_ADD((SELECT start_date FROM parameters), INTERVAL (SELECT weeks_to_model FROM parameters) * 7 + 120 DAY)
GROUP BY cashflow_date
ORDER BY cashflow_date
    ),

-- Calculate cumulative cashflow and maximum capital needed
    cumulative_cashflow AS (
SELECT
    cashflow_date,
    net_daily_cashflow,
    daily_disbursements,
    daily_collections,
    SUM(net_daily_cashflow) OVER (ORDER BY cashflow_date) AS cumulative_cashflow,
    ROW_NUMBER() OVER (ORDER BY cashflow_date) AS day_number
FROM daily_aggregate
    ),

-- Find the minimum cumulative cashflow (maximum capital needed)
    capital_requirement AS (
SELECT
    MIN(cumulative_cashflow) AS max_capital_needed,
    (SELECT cashflow_date FROM cumulative_cashflow WHERE cumulative_cashflow = MIN(cc.cumulative_cashflow) LIMIT 1) AS max_capital_date
FROM cumulative_cashflow cc
    ),

-- Find when we break even (cumulative cashflow becomes positive)
    breakeven_analysis AS (
SELECT
    MIN(cashflow_date) AS breakeven_date,
    MIN(day_number) AS days_to_breakeven
FROM cumulative_cashflow
WHERE cumulative_cashflow >= 0 AND day_number > 1
    ),

-- Weekly summary
    weekly_summary AS (
SELECT
    FLOOR(day_number / 7) + 1 AS week_num,
    MIN(cashflow_date) AS week_start,
    MAX(cashflow_date) AS week_end,
    SUM(daily_disbursements) AS total_disbursed,
    SUM(daily_collections) AS total_collected,
    SUM(net_daily_cashflow) AS net_weekly_cashflow,
    AVG(daily_collections) AS avg_daily_collection
FROM cumulative_cashflow
WHERE day_number <= (SELECT weeks_to_model FROM parameters) * 7
GROUP BY FLOOR(day_number / 7) + 1
    )

-- FINAL OUTPUT: Capital Requirements & Projections
SELECT
    '=== INVESTOR CAPITAL REQUIRED ===' AS section,
  CONCAT('KES ', FORMAT(ABS(max_capital_needed), 2)) AS value,
  CONCAT('Maximum capital needed on ', DATE_FORMAT(max_capital_date, '%Y-%m-%d')) AS description
FROM capital_requirement

UNION ALL

SELECT
    '=== TARGET METRICS ===' AS section,
  CONCAT('KES ', FORMAT(target_weekly_disburse, 2)) AS value,
  'Target Weekly Disbursement (75 people x 30k)' AS description
FROM loan_metrics

UNION ALL

SELECT
    '=== TARGET METRICS ===' AS section,
  CONCAT('KES ', FORMAT(target_daily_collection, 2)) AS value,
  'Target Daily Collection Needed' AS description
FROM loan_metrics

UNION ALL

SELECT
    '=== BREAKEVEN ANALYSIS ===' AS section,
  CAST(days_to_breakeven AS CHAR) AS value,
  CONCAT('Days to breakeven (', DATE_FORMAT(breakeven_date, '%Y-%m-%d'), ')') AS description
FROM breakeven_analysis

UNION ALL

SELECT
    '=== LOAN STRUCTURE ===' AS section,
  CONCAT('KES ', FORMAT(target_loan_amount, 2)) AS value,
  'Loan Amount per Person' AS description
FROM loan_metrics

UNION ALL

SELECT
    '=== LOAN STRUCTURE ===' AS section,
  CONCAT('KES ', FORMAT(daily_collection_per_loan, 2)) AS value,
  'Daily Collection per Loan' AS description
FROM loan_metrics

UNION ALL

SELECT
    '=== LOAN STRUCTURE ===' AS section,
  CONCAT(avg_loan_term, ' days') AS value,
  'Average Loan Term' AS description
FROM loan_metrics, (SELECT avg_loan_term FROM parameters) p;


-- Weekly cashflow projection
SELECT
    week_num,
    DATE_FORMAT(week_start, '%Y-%m-%d') AS week_starting,
    CONCAT('KES ', FORMAT(ABS(total_disbursed), 2)) AS disbursed,
    CONCAT('KES ', FORMAT(total_collected, 2)) AS collected,
    CONCAT('KES ', FORMAT(net_weekly_cashflow, 2)) AS net_cashflow,
    CONCAT('KES ', FORMAT(avg_daily_collection, 2)) AS avg_daily_collection
FROM weekly_summary
ORDER BY week_num;


-- Daily cashflow for first 60 days (detailed view)
SELECT
    cashflow_date,
    day_number,
    CONCAT('KES ', FORMAT(ABS(daily_disbursements), 2)) AS disbursed,
    CONCAT('KES ', FORMAT(daily_collections, 2)) AS collected,
    CONCAT('KES ', FORMAT(net_daily_cashflow, 2)) AS net_daily,
    CONCAT('KES ', FORMAT(cumulative_cashflow, 2)) AS cumulative_balance
FROM cumulative_cashflow
WHERE day_number <= 60
ORDER BY cashflow_date;