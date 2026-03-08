--include base --
-- Daily collection trend (showing first 60 days after 4-month period)
SELECT
    collection_date,
    active_loans_count,
    CONCAT('KES ', FORMAT(total_daily_expected, 2)) AS expected_daily_collection,
    CONCAT('KES ', FORMAT(expected_from_active, 2)) AS expected_from_active_loans
FROM daily_collections
ORDER BY collection_date
    LIMIT 60;