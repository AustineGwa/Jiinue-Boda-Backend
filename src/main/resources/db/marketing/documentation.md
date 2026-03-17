Good call—this is exactly how you make the system maintainable long-term. I’ll keep it **clean, structured, and documentation-ready** so you can drop it into your design doc.

---

# 📊 Omnichannel Marketing & Attribution – Database Tables

Below is a summary of all the tables introduced for the **marketing, attribution, and funnel tracking system**.

---

# 1. `marketing_channels`

### Purpose

Defines the **primary acquisition channels** through which clients discover your business.

### Examples

* WALK_IN
* REFERRAL
* SOCIAL_MEDIA
* FIELD_AGENT
* WHATSAPP
* CALL_CENTER

### Usage

* Powers **frontend dropdown (Channel)**
* Used for **channel performance analytics**

---

# 2. `marketing_campaigns`

### Purpose

Stores **specific marketing campaigns** under each channel.

### Examples

* Facebook March Ads
* TikTok Campaign
* SMS Promo

### Usage

* Tracks **campaign-level performance**
* Enables **ROI analysis**
* Linked to leads via `campaign_id`

---

# 3. `marketing_leads`

### Purpose

Core table that stores **all incoming client inquiries (leads)**.

### Key Data Captured

* Client details (name, phone)
* Channel (`channel_id`)
* Campaign (`campaign_id`)
* Agent (`agent_id`)
* Referral (`referral_code`)
* Branch (`branch_id`)
* Notes

### Usage

* Entry point for all clients
* Used for **lead volume tracking**
* Feeds into funnel tracking

---

# 4. `marketing_touchpoints` *(Optional but powerful)*

### Purpose

Tracks **multiple interactions** a lead has before conversion.

### Examples

* Saw Facebook ad
* Later called call center
* Then walked into office

### Usage

* Enables **multi-touch attribution**
* Advanced marketing analytics

---

# 5. `lead_client_map`

### Purpose

Links a **marketing lead → actual system user (client)**.

### Why It Exists

Not all leads become customers, so this preserves:

* Lead data integrity
* Conversion tracking

### Usage

* Enables **lead → customer conversion metrics**
* Bridges marketing and core LMS

---

# 6. `client_funnel_events`

### Purpose

Tracks **every step in the client lifecycle (funnel)**.

### Examples

* Prospect created
* Application submitted
* Loan approved
* Loan disbursed

### Key Fields

* `client_id` (lead or user depending on stage)
* `event_type_id`
* `event_time`
* `created_by`

### Usage

* Core table for **conversion funnel analytics**
* Enables **timeline tracking per client**

---

# 7. `funnel_event_types`

### Purpose

Defines all possible **funnel stages (event types)**.

### Examples

| ID | Code                  |
| -- | --------------------- |
| 1  | PROSPECT_CREATED      |
| 2  | APPLICATION_STARTED   |
| 3  | APPLICATION_SUBMITTED |
| 4  | LOAN_APPROVED         |
| 5  | LOAN_DISBURSED        |

### Usage

* Eliminates hardcoded strings
* Used in `client_funnel_events.event_type_id`
* Powers **consistent reporting**

---

# 8. `campaign_sources` *(Optional)*

### Purpose

Standardizes **where campaigns originate from**.

### Examples

* FACEBOOK
* TIKTOK
* WHATSAPP
* SMS

### Usage

* Improves **data cleanliness**
* Used for filtering and reporting

---

# 9. `campaign_mediums` *(Optional)*

### Purpose

Defines **type of marketing delivery**.

### Examples

* PAID_ADS
* ORGANIC
* BROADCAST
* DIRECT

### Usage

* Enables deeper **marketing segmentation**
* Useful for ROI analysis

---

# 🔗 Relationship Overview

```plaintext
marketing_channels
        ↓
marketing_campaigns
        ↓
marketing_leads
        ↓
lead_client_map → users (existing system)
        ↓
client_funnel_events
        ↓
funnel_event_types
```

---

# 🧠 How Everything Works Together

### 1. Lead Capture

* Data saved in `marketing_leads`
* Source tracked via `channel_id`, `campaign_id`

### 2. Funnel Tracking

* First event → `PROSPECT_CREATED`
* Stored in `client_funnel_events`

### 3. Conversion

* Lead becomes user → stored in `lead_client_map`

### 4. Lifecycle Progression

* Events added:

  * APPLICATION_SUBMITTED
  * LOAN_APPROVED
  * LOAN_DISBURSED

### 5. Analytics

* Channel performance → `marketing_channels`
* Campaign ROI → `marketing_campaigns`
* Conversion rates → `client_funnel_events`

---

# 🚀 Final Outcome

This structure enables you to answer:

* Where do clients come from?
* Which campaigns perform best?
* Which agents bring quality leads?
* What is the conversion rate per channel?
* Where do clients drop off in the funnel?

---

# 💡 Final Note (Based on Your System)

Given your:

* Loan system
* Agent workflows
* Power BI usage

This design is **production-grade** and ready for:

* dashboards
* ML models (future prediction of high-converting leads 👀)
* automated decision-making

---


