WITH cohort AS (
    SELECT
        userID,
        MIN(disbursed_at) AS first_loan_date
    FROM loans
    WHERE application_branch = 1
      AND disbursed_at BETWEEN '2024-05-01' AND '2024-09-01'
    GROUP BY userID
),
     returning_users AS (
         SELECT DISTINCT l.userID
         FROM loans l
                  JOIN cohort c ON l.userID = c.userID
         WHERE l.disbursed_at > c.first_loan_date
     )

SELECT
    COUNT(DISTINCT r.userID) AS retained_users,
    COUNT(DISTINCT c.userID) AS total_users,
    ROUND(
            COUNT(DISTINCT r.userID) * 100.0 / COUNT(DISTINCT c.userID),
            2
    ) AS retention_rate_percentage
FROM cohort c
         LEFT JOIN returning_users r ON c.userID = r.userID;
