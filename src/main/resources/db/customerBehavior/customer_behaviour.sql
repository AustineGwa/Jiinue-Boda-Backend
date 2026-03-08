---include base ---

-- Detailed customer behavior breakdown
SELECT
    customer_behavior,
    loan_status,
    customer_count,
    percentage AS percentage_of_total,
    CONCAT('KES ', FORMAT(avg_original_principal, 2)) AS avg_original_loan,
    CONCAT('KES ', FORMAT(avg_next_principal, 2)) AS avg_next_loan,
    CONCAT('KES ', FORMAT(avg_next_principal - avg_original_principal, 2)) AS loan_change
FROM customer_behavior_summary
ORDER BY customer_count DESC;

