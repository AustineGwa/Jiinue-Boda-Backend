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
