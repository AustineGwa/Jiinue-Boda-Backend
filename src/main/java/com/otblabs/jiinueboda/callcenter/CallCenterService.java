package com.otblabs.jiinueboda.callcenter;

import com.otblabs.jiinueboda.filemanagement.FileManagementService;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import com.otblabs.jiinueboda.utility.UtilityFunctions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class CallCenterService {

    private final JdbcTemplate jdbcTemplateOne;
    private final UserService userService;
    private final FileManagementService fileManagementService;

    public CallCenterService(JdbcTemplate jdbcTemplateOne, UserService userService, FileManagementService fileManagementService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.userService = userService;
        this.fileManagementService = fileManagementService;
    }

    public int postCallComment(CallComment callComment, String name) {
        SystemUser systemUser = userService.getByEmailOrPhone(name);
        String sql = """
                INSERT INTO call_center_logs(loan_account,comment_type, call_picked, client_response, reason_not_picked,rep_id, created_at) VALUES (?,?,?,?,?,?,?)
                """;
        return jdbcTemplateOne.update(sql, callComment.getLoanAccount(),callComment.getCommentType(),callComment.isCallPicked(),
                callComment.getClientResponse(),callComment.getReasonNotPicked(),systemUser.getId(), UtilityFunctions.getCurrentTimestamp());
    }

    public int postCallCommentMultipart(CallCommentMultipart callComment, String name) throws Exception{
        SystemUser systemUser = userService.getByEmailOrPhone(name);

        String sql = """
        INSERT INTO call_center_logs(
            loan_account, comment_type, call_picked, promise_to_pay, ptp_date,
            client_attitude, call_back_later, call_back_date_time, payment_issue,
            client_response, reason_not_picked, rep_id, created_at
        )
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, callComment.getLoanAccount());
            ps.setString(2, callComment.getCommentType());
            ps.setBoolean(3, callComment.isCallPicked());
            ps.setBoolean(4, callComment.isPromiseToPay());
            ps.setObject(5, callComment.getPaymentDate());
            ps.setString(6, callComment.getClientAttitude());
            ps.setBoolean(7, callComment.isCallBackLater());
            ps.setObject(8, callComment.getCallBackDateTime());
            ps.setString(9, callComment.getPaymentIssue());
            ps.setString(10, callComment.getClientResponse());
            ps.setString(11, callComment.getReasonNotPicked());
            ps.setLong(12, systemUser.getId());
            ps.setString(13, UtilityFunctions.getCurrentTimestamp());

            return ps;
        }, keyHolder);

        int commentId =  keyHolder.getKey().intValue();

        try{
            if(!callComment.getProofFiles().isEmpty()){
                uploadProofFiles(callComment.getProofFiles(), commentId);
            }
        }catch (Exception ignored){}


        return commentId;
    }

    void uploadProofFiles(List<MultipartFile> proofFiles, int commentId){
        proofFiles.forEach(assetImage ->{
            try {
                fileManagementService.uploadCommentAttachment(assetImage,"COMMENT-ATTACHMENT-PROOFS",commentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<CallComment> getAllLoanComments(String loanId) {
        String sql = """
                     SELECT *,u.first_name, u.last_name , (SELECT public_url FROM comment_attachments WHERE asset_id=cl.id LIMIT 1) as attachment_url  FROM call_center_logs cl LEFT JOIN users u on cl.rep_id = u.id
                 WHERE loan_account =? ORDER BY cl.created_at DESC
                    """;
        return jdbcTemplateOne.query(sql,(rs,i)->setComment(rs),loanId);

    }

    private CallComment setComment(ResultSet rs) throws SQLException {
        CallComment callComment = new CallComment();
        callComment.setLoanAccount(rs.getString("loan_account"));
        callComment.setCallPicked(rs.getBoolean("call_picked"));
        callComment.setClientResponse(rs.getString("client_response"));
        callComment.setReasonNotPicked(rs.getString("reason_not_picked"));
        callComment.setRepId(rs.getInt("rep_id"));
        callComment.setRepName(rs.getString("first_name") +" "+ rs.getString("last_name"));
        callComment.setDate(rs.getString("cl.created_at"));
        callComment.setAttachmentUrl(rs.getString("attachment_url"));
        return callComment;
    }

    public MinimalProfile getMinimalProfileInfo(String loanId) {
        String sql = """
                SELECT first_name, middle_name, last_name ,phone FROM users WHERE id = (SELECT userID FROM loans WHERE loanAccountMPesa =?)
                """;

        return jdbcTemplateOne.queryForObject(sql,(rs,i) -> {
            MinimalProfile minimalProfile = new MinimalProfile();
            minimalProfile.setFirstName(rs.getString("first_name"));
            minimalProfile.setMiddleName(rs.getString("middle_name"));
            minimalProfile.setLastName(rs.getString("last_name"));
            minimalProfile.setPhone(rs.getString("phone"));
            return minimalProfile;
        },loanId);
    }
}
