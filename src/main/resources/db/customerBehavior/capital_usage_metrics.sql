--include base.sql ---

-- Final Output: Comprehensive Report
SELECT
    'PRINCIPAL REQUIRED (First 4 Months)' AS metric,
    CAST(total_loans_disbursed AS CHAR) AS value,
  'Total Loans Disbursed' AS description
FROM principal_summary

UNION ALL

SELECT
    'PRINCIPAL REQUIRED (First 4 Months)' AS metric,
    CAST(unique_customers AS CHAR) AS value,
  'Unique Customers' AS description
FROM principal_summary

UNION ALL

SELECT
    'PRINCIPAL REQUIRED (First 4 Months)' AS metric,
    CONCAT('KES ', FORMAT(total_principal_required, 2)) AS value,
  'Total Principal Amount' AS description
FROM principal_summary

UNION ALL

SELECT
    'PRINCIPAL REQUIRED (First 4 Months)' AS metric,
    CONCAT('KES ', FORMAT(total_disbursed_including_fees, 2)) AS value,
  'Total Disbursed (less fees)' AS description
FROM principal_summary

UNION ALL

SELECT
    'PRINCIPAL REQUIRED (First 4 Months)' AS metric,
    CONCAT('KES ', FORMAT(avg_loan_principal, 2)) AS value,
  'Average Loan Principal' AS description
FROM principal_summary

UNION ALL

SELECT
    'DAILY COLLECTIONS (After 4 Months)' AS metric,
    CAST(total_collection_days AS CHAR) AS value,
  'Total Collection Days' AS description
FROM collection_metrics

UNION ALL

SELECT
    'DAILY COLLECTIONS (After 4 Months)' AS metric,
    CONCAT('KES ', FORMAT(avg_daily_expected, 2)) AS value,
  'Average Daily Expected Collection' AS description
FROM collection_metrics

UNION ALL

SELECT
    'DAILY COLLECTIONS (After 4 Months)' AS metric,
    CONCAT('KES ', FORMAT(max_daily_expected, 2)) AS value,
  'Max Daily Expected Collection' AS description
FROM collection_metrics

UNION ALL

SELECT
    'DAILY COLLECTIONS (After 4 Months)' AS metric,
    CONCAT('KES ', FORMAT(total_expected_collections, 2)) AS value,
  'Total Expected Collections' AS description
FROM collection_metrics

UNION ALL

SELECT
    'DAILY COLLECTIONS (After 4 Months)' AS metric,
    CAST(ROUND(avg_active_loans) AS CHAR) AS value,
  'Average Active Loans per Day' AS description
FROM collection_metrics;