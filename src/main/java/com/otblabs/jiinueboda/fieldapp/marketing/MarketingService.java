package com.otblabs.jiinueboda.fieldapp.marketing;


import com.otblabs.jiinueboda.fieldapp.marketing.models.LeadFollowUp;
import com.otblabs.jiinueboda.fieldapp.marketing.models.LeadsComment;
import com.otblabs.jiinueboda.fieldapp.marketing.models.MarketingLead;
import com.otblabs.jiinueboda.fieldapp.marketing.models.MarketingQuestionaire;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class MarketingService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;

    public MarketingService(JdbcTemplate jdbcTemplateOne, UserService userService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
    }


    public int createNewMarketingLeads(MarketingLead marketingLead, String name) {
        SystemUser systemUser = userService.getByEmailOrPhone(name);
        String sql = """
                INSERT INTO marketing_leads(group_id,user_name, phone_number, current_stage, created_by, created_at) VALUE (?,?,?,?,?,NOW())
                """;

        return  jdbcTemplateOne.update(sql,marketingLead.getGroupId(), marketingLead.getUserName(),marketingLead.getPhone(),"LEAD",systemUser.getId());
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
}
