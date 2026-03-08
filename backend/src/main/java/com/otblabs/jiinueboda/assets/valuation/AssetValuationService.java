package com.otblabs.jiinueboda.assets.valuation;

import com.otblabs.jiinueboda.assets.models.ClientAsset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class AssetValuationService {

    private final JdbcTemplate jdbcTemplateOne;

    public AssetValuationService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    List<ClientAsset> getPendingAssignedValuations(String evaluatorUserId) throws Exception{
        String sql = "SELECT * FROM client_assets WHERE eval_status = 0 AND eval_assigned_to =?";
        return  jdbcTemplateOne.query(sql,(rs,i)->setAsset(rs),evaluatorUserId);
    }

    public boolean saveAssetEvaluation(AssetValuationForm assetValuationForm, int valuerID) throws Exception {

        int status = insertAssetValuation(assetValuationForm);
//        if(status == 1){
//            updatEValuatedAsset(assetForm.getAssetId(),valuerID);
//        }
        return true;
    }

    public int insertAssetValuation(AssetValuationForm assetValuationForm) throws Exception{
        String sql = """ 
                INSERT INTO asset_valuation (
                user_id, asset_id, make, model, color, registration, frame_no, engine_no,year_manufacture,millage,
                en_leakage_comment, en_noise_comment, en_smoke_comment,en_power_performance_comment, en_other_comment, en_rating, ch_frame_comment, ch_rating,b_tank_com,b_seat_com,
                b_front_rim_tyre_com, b_back_rim_tyre_com, b_covers_com, b_breaks_com, b_rear_shocks_com, b_front_shocks_com, b_foot_rest_com, b_crash_guard_com, b_cables_com,b_dashboard_com,
                b_carrier_com, b_stands_com, b_fenders_com, b_kick_start_com,b_steering_com, b_exhaust_com, b_gear_levers_com, b_levers_com, b_mirrors_com,b_body_final_com,                 
                b_body_rating, e_battery_status_com, e_horn_com, e_lights_com, e_starter_com,e_signals_com, e_charging_system_com, e_electric_final_com, e_electric_rating,general_com,                
                 general_rating, tracker_imei, tracker_sim_number, asset_total_value, valuer_name,valuer_signature, supervisor_name, supervisor_signature, created_at) 
                VALUES (
                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 
                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 
                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 
                 ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                 ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                
                """;

        jdbcTemplateOne.update(sql,
                assetValuationForm.getUserId(),
                assetValuationForm.getAssetId(),
                assetValuationForm.getMake(),
                assetValuationForm.getModel(),
                assetValuationForm.getColor(),
                assetValuationForm.getRegistration(),
                assetValuationForm.getFrameNo(),
                assetValuationForm.getEngineNo(),
                assetValuationForm.getYearManufacture(),
                assetValuationForm.getMillage(),
                assetValuationForm.getEnLeakageComment(),
                assetValuationForm.getEnNoiseComment(),
                assetValuationForm.getEnSmokeComment(),
                assetValuationForm.getEnPowerPerformanceComment(),
                assetValuationForm.getEnOtherComment(),
                assetValuationForm.getEnRating(),
                assetValuationForm.getChFrameComment(),
                assetValuationForm.getChRating(),
                assetValuationForm.getBTankCom(),
                assetValuationForm.getBSeatCom(),
                assetValuationForm.getBFrontRimTyreCom(),
                assetValuationForm.getBBackRimTyreCom(),
                assetValuationForm.getBCoversCom(),
                assetValuationForm.getBBreaksCom(),
                assetValuationForm.getBRearShocksCom(),
                assetValuationForm.getBFrontShocksCom(),
                assetValuationForm.getBFootRestCom(),
                assetValuationForm.getBCrashGuardCom(),
                assetValuationForm.getBCablesCom(),
                assetValuationForm.getBDashboardCom(),
                assetValuationForm.getBCarrierCom(),
                assetValuationForm.getBStandsCom(),
                assetValuationForm.getBFendersCom(),
                assetValuationForm.getBKickStartCom(),
                assetValuationForm.getBSteeringCom(),
                assetValuationForm.getBExhaustCom(),
                assetValuationForm.getBGearLeversCom(),
                assetValuationForm.getBLeversCom(),
                assetValuationForm.getBMirrorsCom(),
                assetValuationForm.getBBodyFinalCom(),
                assetValuationForm.getBBodyRating(),
                assetValuationForm.getEBatteryStatusCom(),
                assetValuationForm.getEHornCom(),
                assetValuationForm.getELightsCom(),
                assetValuationForm.getEStarterCom(),
                assetValuationForm.getESignalsCom(),
                assetValuationForm.getEChargingSystemCom(),
                assetValuationForm.getEElectricFinalCom(),
                assetValuationForm.getEElectricRating(),
                assetValuationForm.getGeneralCom(),
                assetValuationForm.getGeneralRating(),
                assetValuationForm.getTrackerImei(),
                assetValuationForm.getTrackerSimNumber(),
                assetValuationForm.getAssetTotalValue(),
                assetValuationForm.getValuerName(),
                assetValuationForm.getValuerSignature(),
                assetValuationForm.getSupervisorName(),
                assetValuationForm.getSupervisorSignature());

        return 1;
    }

    public void updatEValuatedAsset(int assetId,int valuerId){
        String sql = "UPDATE client_assets SET eval_status =1,eval_comp_date=NOW() WHERE id=? AND eval_assigned_to=?";
        jdbcTemplateOne.update(sql,assetId,valuerId);

    }

    private ClientAsset setAsset(ResultSet rs) throws SQLException {
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
        clientAsset.setEvalReqDate(rs.getString("eval_req_date"));
        clientAsset.setEvalCompDate(rs.getString("eval_comp_date"));
        return clientAsset;
    }


}
