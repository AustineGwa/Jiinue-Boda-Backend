-- all clients by channel
SELECT mc.name, COUNT(*) clients
FROM marketing_leads ml
         JOIN marketing_channels mc ON mc.id = ml.channel_id
GROUP BY mc.name;

-- Conversion by Channel
SELECT
    mc.name,
    COUNT(DISTINCT ml.id) leads,
    COUNT(DISTINCT lcm.client_id) converted
FROM marketing_leads ml
         LEFT JOIN lead_client_map lcm ON lcm.lead_id = ml.id
         JOIN marketing_channels mc ON mc.id = ml.channel_id
GROUP BY mc.name;

-- agent performance
SELECT
    u.first_name,
    COUNT(al.lead_id) leads_generated
FROM agent_leads al
         JOIN users u ON u.id = al.agent_id
GROUP BY al.agent_id;

-- DESIGN QUESTIONS ANSWERED
| Question                         | How                  |
| -------------------------------- | -------------------- |
| Where do most clients come from? | marketing_channels   |
| Which campaign works best?       | marketing_campaigns  |
| Which agents perform best?       | agent_leads          |
| Which channel converts best?     | lead_client_map      |
| What is the funnel conversion?   | client_funnel_events |


UI PROPOSAL
| Field                 | Source Table        |
| --------------------- | ------------------- |
| Channel               | marketing_channels  |
| Campaign              | marketing_campaigns |
| Source (optional)     | campaign_sources    |
| Medium (optional)     | campaign_mediums    |
| Branch                | branches            |
| Event Type (internal) | funnel_event_types  |

-- dashboard
SELECT
    c.name AS channel_name,
    COUNT(ml.id) AS total_leads,
    SUM(CASE WHEN cfe.event_type_id = 2 THEN 1 ELSE 0 END) AS applications_started,
    SUM(CASE WHEN cfe.event_type_id = 3 THEN 1 ELSE 0 END) AS applications_submitted,
    SUM(CASE WHEN cfe.event_type_id = 4 THEN 1 ELSE 0 END) AS loans_approved,
    SUM(CASE WHEN cfe.event_type_id = 5 THEN 1 ELSE 0 END) AS loans_disbursed,
    ROUND(SUM(CASE WHEN cfe.event_type_id = 5 THEN 1 ELSE 0 END) / COUNT(ml.id) * 100, 2) AS conversion_to_disbursed_percentage
FROM marketing_leads ml
         LEFT JOIN marketing_channels c ON ml.channel_id = c.id
         LEFT JOIN client_funnel_events cfe ON ml.id = cfe.client_id
GROUP BY c.id, c.name
ORDER BY total_leads DESC;