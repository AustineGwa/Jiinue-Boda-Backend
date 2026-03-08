package com.otblabs.jiinueboda.assets;

import com.otblabs.jiinueboda.assets.models.*;
import com.otblabs.jiinueboda.filemanagement.FileManagementService;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class AssetsService {

    private final JdbcTemplate jdbcTemplateOne;
    private final FileManagementService fileManagementService;
    private final UserService userService;

    public AssetsService(JdbcTemplate jdbcTemplateOne, FileManagementService fileManagementService, UserService userService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.fileManagementService = fileManagementService;
        this.userService = userService;
    }

    public List<AssetAttachment> getAllClientAssetAttachments(int userId) {

        String sql = "SELECT * FROM asset_attachments WHERE asset_id IN (SELECT id FROM client_assets WHERE user_id = ?)";
        return jdbcTemplateOne.query(sql,(rs,i) ->mapResultToAssetAttachments(rs),userId);
    }

    private AssetAttachment mapResultToAssetAttachments(ResultSet rs) throws SQLException {
        AssetAttachment assetAttachment = new AssetAttachment();
        assetAttachment.setDocId(rs.getInt("id"));
        assetAttachment.setDocType(rs.getString("doc_type"));
        assetAttachment.setPublicUrl(rs.getString("public_url"));
        assetAttachment.setUploadedOn(rs.getString("created_at"));
        return assetAttachment;
    }

    public List<ClientAsset> getAllClientAssets(int userId) {

        String sql = """
                 SELECT id, brand, make, model, l_plate, chassis, odometer, a_condition, user_id, eval_status, eval_assigned_to,
                                                  eval_req_date, eval_comp_date, created_by, created_at,
                                                  (SELECT first_name FROM users WHERE id=eval_assigned_to) as first_name,
                                                  (SELECT last_name FROM users WHERE id=eval_assigned_to) as last_name,
                                                  (SELECT public_url FROM asset_attachments WHERE asset_id = client_assets.id AND doc_type = 'EVAL-REPORT' ORDER BY asset_attachments.created_at LIMIT 1) as eval_report_url,
                                                  (SELECT public_url FROM asset_attachments WHERE asset_id = client_assets.id AND doc_type = 'CHARGED-LOGBOOK' ORDER BY asset_attachments.created_at LIMIT 1) as charged_logbook_url
                                  from client_assets
                                  WHERE user_id =?
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->mapResultToAsset(rs),userId);
    }

    public List<ClientAsset> getAllAssets() {

        String sql = """
                SELECT id, brand, make, model, l_plate, chassis, odometer, a_condition, user_id, eval_status, eval_assigned_to,
                eval_req_date, eval_comp_date, created_by, created_at,
                (SELECT first_name FROM users WHERE id=eval_assigned_to) as first_name,
                (SELECT last_name FROM users WHERE id=eval_assigned_to) as last_name,
                (SELECT public_url FROM asset_attachments WHERE asset_id = client_assets.id AND doc_type = 'EVAL-REPORT' ORDER BY asset_attachments.created_at LIMIT 1) as eval_report_url,
                (SELECT public_url FROM asset_attachments WHERE asset_id = client_assets.id AND doc_type = 'CHARGED-LOGBOOK' ORDER BY asset_attachments.created_at LIMIT 1) as charged_logbook_url
                from client_assets ORDER BY created_at desc
                """;

        return jdbcTemplateOne.query(sql,(rs,i)->mapResultToAsset(rs));
    }




    private ClientAsset mapResultToAsset(ResultSet rs) throws SQLException {
        ClientAsset clientAsset = new ClientAsset();
        clientAsset.setId(rs.getInt("id"));
        clientAsset.setBrand(rs.getString("brand"));
        clientAsset.setMake(rs.getString("make"));
        clientAsset.setModel(rs.getString("model"));
        clientAsset.setLplate(rs.getString("l_plate"));
        clientAsset.setChassis(rs.getString("chassis"));
        clientAsset.setOdometer(rs.getString("odometer"));
        clientAsset.setAcondition(rs.getString("a_condition"));
        clientAsset.setUserId(rs.getString("user_id"));
        clientAsset.setEvalStatus(rs.getInt("eval_status"));
        clientAsset.setEvalAssignedTo(rs.getString("first_name") +" "+ rs.getString("last_name"));
        clientAsset.setEvalReqDate(rs.getString("eval_req_date"));
        clientAsset.setEvalCompDate(rs.getString("eval_comp_date"));
        clientAsset.setEvaluationReport(rs.getString("eval_report_url"));
        clientAsset.setChargedLogBook(rs.getString("charged_logbook_url"));
        return clientAsset;
    }

    public String createNewAsset(NewAssetDto assetData, String user) throws Exception{

        SystemUser systemUser = userService.getByEmailOrPhone(user);

        String sql = """
                         INSERT INTO client_assets(brand, make, model, l_plate, chassis,engine_number,rating,yom,color, odometer, a_condition, user_id, created_by, created_at)\s
                         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?, NOW())
                        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, assetData.getBrand());
            ps.setString(2, assetData.getMake());
            ps.setString(3, assetData.getModel());
            ps.setString(4, assetData.getLplate());
            ps.setString(5, assetData.getChassis());
            ps.setString(6, assetData.getEngineNumber());
            ps.setInt(7, assetData.getRating());
            ps.setInt(8, assetData.getYom());
            ps.setString(9, assetData.getColor());
            ps.setString(10, assetData.getOdometer());
            ps.setString(11, assetData.getAcondition());
            ps.setInt(12, assetData.getUserId());
            ps.setInt(13, systemUser.getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return "success";
    }

    public String createNewAsset(NewAssetData assetData, String user) throws Exception{

        SystemUser systemUser = userService.getByEmailOrPhone(user);

        int assetId = createnewAssetEtry(assetData, systemUser.getId());

        fileManagementService.uploadAssetAttachment(assetData.getChargedLogBook(),"ASSET-DOCUMENT",assetId);

        assetData.getAssetImages().forEach(assetImage ->{
            try {
                fileManagementService.uploadAssetAttachment(assetImage,"ASSET-DOCUMENT-IMAGES",assetId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
       return "success";
    }

    public String updateValuationForAsset(ValuationSubmissionData valuationSubmissionData) throws Exception{
        fileManagementService.uploadValuationForm(valuationSubmissionData.getEvaluationForm(),"EVAL-REPORT", valuationSubmissionData.getAssetId());
        updateEvalStatusOnAsset(valuationSubmissionData);
        return "success";
    }


    public Object saveChargedLogbookForAsset(LogbookSubmissionData logbookSubmissionData) throws Exception{
        fileManagementService.uploadChargedLogbbok(logbookSubmissionData.getChargedLogbook(),"CHARGED-LOGBOOK", logbookSubmissionData.getAssetId());

        return "success";
    }

    public Object saveLoanAgreement(LogbookSubmissionData logbookSubmissionData) throws Exception{


        return "success";
    }


    private void updateEvalStatusOnAsset(ValuationSubmissionData valuationSubmissionData) throws Exception{
        String sql = "UPDATE client_assets SET eval_status=1,eval_assigned_to=?,eval_comp_date=? WHERE id=?";
        jdbcTemplateOne.update(sql,valuationSubmissionData.getEvalAssignedTo(),valuationSubmissionData.getEvalCompletionDateTime(), valuationSubmissionData.getAssetId());

    }


    private int createnewAssetEtry(NewAssetData assetData, int loggedInUser) {

        /*

         */

        String sql = """
                         INSERT INTO client_assets(brand, make, model, l_plate, chassis,engine_number,rating,yom,color, odometer, a_condition, user_id, created_by, created_at)\s
                         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?, NOW())
                        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, assetData.getBrand());
            ps.setString(2, assetData.getMake());
            ps.setString(3, assetData.getModel());
            ps.setString(4, assetData.getLplate());
            ps.setString(5, assetData.getChassis());
            ps.setString(6, assetData.getEngineNumber());
            ps.setInt(7, assetData.getRating());
            ps.setInt(8, assetData.getYom());
            ps.setString(9, assetData.getColor());
            ps.setString(10, assetData.getOdometer());
            ps.setString(11, assetData.getAcondition());
            ps.setInt(12, assetData.getUserId());
            ps.setInt(13, loggedInUser);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return (key != null) ? key.intValue() : 0;
    }


}
