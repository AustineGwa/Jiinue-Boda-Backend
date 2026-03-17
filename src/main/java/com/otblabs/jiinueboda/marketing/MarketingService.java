package com.otblabs.jiinueboda.marketing;


import com.otblabs.jiinueboda.marketing.models.LeadFollowUp;
import com.otblabs.jiinueboda.marketing.models.LeadsComment;
import com.otblabs.jiinueboda.marketing.models.MarketingLead;
import com.otblabs.jiinueboda.marketing.models.MarketingQuestionaire;
import com.otblabs.jiinueboda.sms.SmsService;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.utility.Functions;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class MarketingService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;
    private final SmsService smsService;

    public MarketingService(JdbcTemplate jdbcTemplateOne, UserService userService, SmsService smsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
        this.smsService = smsService;
    }


    @Transactional
    public int createNewMarketingLeads(MarketingLead lead, String name) {

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        // 1. Insert Lead
        String insertLeadSql = """
        INSERT INTO marketing_leads (
                    group_id,
                    user_name,
                    phone_number,
                    channel_id,
                    campaign_id,
                    agent_id,
                    branch_id,
                    notes,
                    current_stage,
                    created_by,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertLeadSql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, lead.getGroupId());
            ps.setString(2, lead.getUserName());
            ps.setString(3, Functions.formatPhoneNumber(lead.getPhone()));
            ps.setObject(4, lead.getChannelId());
            ps.setObject(5, lead.getCampaignId());
            ps.setObject(6, lead.getAgentId());
            ps.setObject(7, lead.getBranchId());
            ps.setString(8, lead.getNotes());
            ps.setString(9, "PROSPECT");
            ps.setLong(10, systemUser.getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to create lead");
        }

        Long leadId = key.longValue();

        // 2. Insert Funnel Event (event_type_id = 1 → PROSPECT_CREATED)
        String funnelSql = """
        INSERT INTO client_funnel_events (
            client_id,
            event_type_id,
            event_time,
            created_by
        )
        VALUES (?, ?, NOW(), ?)
    """;

        jdbcTemplateOne.update(funnelSql,
                leadId,
                1, // PROSPECT_CREATED
                systemUser.getId()
        );

        smsService.sendUserWelcomeMessage(lead.getUserName(), Functions.formatPhoneNumber(lead.getPhone()));

        return leadId.intValue();
    }

    public Object followupOnMarketingLeads(LeadFollowUp leadFollowUp, String name) {

        SystemUser systemUser = userService.getByEmailOrPhone(name);
        String sql = """
                INSERT INTO leads_follow_up_tracker(lead_id, comment, called_by, created_at) VALUES (?,?,?,NOW())
                """;

        return  jdbcTemplateOne.update(sql,leadFollowUp.getLeadId(), leadFollowUp.getComment(),systemUser.getId());

    }

    public Object createNewMarketingQuestionaire(MarketingQuestionaire marketingQuestionaire, String name) {

        SystemUser systemUser = userService.getByEmailOrPhone(name);

        String sql = """
                INSERT INTO stage_survey_responses(
                 group_id, number_of_members, registered_with_sacco, working_hours, has_regular_meetings,
                 meeting_date, meeting_time, created_by, created_at
                 )
                 VALUES(?,?,?,?,?,?,?,?,NOW())
                """;

        return jdbcTemplateOne.update(sql,
                marketingQuestionaire.getGroupId(),
                marketingQuestionaire.getNumberOfMembers(),
                marketingQuestionaire.isRegisteredWithSacco(),
                marketingQuestionaire.getWorkingHours(),
                marketingQuestionaire.isHasRegularMeetings(),
                marketingQuestionaire.getMeetingDate(),
                marketingQuestionaire.getMeetingTime(),
                systemUser.getId());
    }


    public List<LeadsComment> getLeadsComments(int leadId) {
        String sql = """
               SELECT lead_id,comment,(SELECT first_name from users WHERE id = leads_follow_up_tracker.called_by) as called_by,
                created_at FROM leads_follow_up_tracker WHERE lead_id=?
                """;

        return jdbcTemplateOne.query(sql,(rs,i)-> setLeadComment(rs),leadId);

    }

    private LeadsComment setLeadComment(ResultSet rs) throws SQLException {
        LeadsComment leadsComment = new LeadsComment();
        leadsComment.setLeadId(rs.getInt("lead_id"));
        leadsComment.setComment(rs.getString("comment"));
        leadsComment.setCalledBy(rs.getString("called_by"));
        leadsComment.setCreatedAt(rs.getString("created_at"));
        return leadsComment;
    }

    public List<Map<String, Object>> getChannels() {
        String sql = "SELECT id, name FROM marketing_channels ORDER BY name";
        return jdbcTemplateOne.queryForList(sql);
    }


    public List<Map<String, Object>> getCampaigns() {
        String sql = """
        SELECT id, campaign_name 
        FROM marketing_campaigns 
        WHERE (end_date IS NULL OR end_date >= CURDATE())
        ORDER BY campaign_name
    """;
        return jdbcTemplateOne.queryForList(sql);
    }

    public List<Map<String, Object>> getAgents() {
        String sql = """
        SELECT id, first_name
        FROM users
        WHERE id in (SELECT user_id from user_roles WHERE usertype = 'AMBOSSODOR')
        ORDER BY id
    """;
        return jdbcTemplateOne.queryForList(sql);
    }

    public List<Map<String, Object>> getBranches() {
        String sql = "SELECT id, name FROM partners WHERE deleted_at is null ORDER BY id";
        return jdbcTemplateOne.queryForList(sql);
    }

    public List<String> getFunnelStages() {
        return List.of(
                "PROSPECT",
                "LEAD",
                "APPLICATION_SUBMITTED",
                "LOAN_APPROVED",
                "LOAN_DISBURSED"
        );
    }

    public List<Map<String, Object>> getFunnelEventTypes() {
        String sql = "SELECT id, code, description FROM funnel_event_types ORDER BY id";
        return jdbcTemplateOne.queryForList(sql);
    }
}
