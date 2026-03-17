package com.otblabs.jiinueboda.assets.valuation.online;

import com.otblabs.jiinueboda.assets.valuation.online.models.OnlineAssetValuation;
import com.otblabs.jiinueboda.assets.valuation.online.models.ValuationRequest;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Service
public class OnlineValuationService {

    private final JdbcTemplate jdbc;
    private final UserService userService;

    public OnlineValuationService(JdbcTemplate jdbc, UserService userService) {
        this.jdbc = jdbc;
        this.userService = userService;
    }

    // ── Scoring weights ──────────────────────────────────────

    // Section 2 – Wiring: 6 components, total /30  → each rating point = 5/3
    private static final double[] WIRING_WEIGHTS = { 0.0, 1.6667, 3.3333, 5.0 };

    // Section 3 – Tyres: 2 components, total /10  → each rating point = 5/3
    private static final double[] TYRE_WEIGHTS   = { 0.0, 1.6667, 3.3333, 5.0 };

    // Section 4 – Body: 5 components, total /20  → each rating point = 4/3
    private static final double[] BODY_WEIGHTS   = { 0.0, 1.3333, 2.6667, 4.0 };

    // Section 5 – Accessories: 5 components, total /10  → 0=0, 1=2, 2=3
    private static final double[] ACC_WEIGHTS    = { 0.0, 2.0, 3.0 };


    public OnlineAssetValuation create(ValuationRequest req, String user) {

        SystemUser systemUser = userService.getByEmailOrPhone(user);
        OnlineAssetValuation v = mapRequestToModel(req);
        v.setTechnicianId(systemUser.getId());
        computeScores(v);
        v.setGrade(deriveGrade(v.getTotalScore()));
        int newId = insert(v);
        v.setId(newId);
        return v;
    }

    public List<OnlineAssetValuation> ValuationForAssetByAssetId(int assetId) {
        String sql = "SELECT * FROM asset_valuations WHERE asset_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, rowMapper(), assetId);
    }

    public Optional<OnlineAssetValuation> findValuationByById(Integer id) {
        String sql = "SELECT * FROM asset_valuations WHERE id = ?";
        List<OnlineAssetValuation> results = jdbc.query(sql, rowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private void computeScores(OnlineAssetValuation v) {

        // ── Engine (max 30): 4 items × weights [0, 2.5, 5, 7.5]
        double[] engineW = {0, 2.5, 5, 7.5};
        double engineScore =
                engineW[safe(v.getEnginePerformance())] +
                        engineW[safe(v.getGearbox())]           +
                        engineW[safe(v.getSuspension())]         +
                        engineW[safe(v.getBraking())];
        v.setEngineScore(bd(engineScore));

        // ── Wiring (max 30): 6 items × weights [0, 1.67, 3.33, 5]
        double[] wiringW = {0, 1.67, 3.33, 5};
        double wiringScore =
                wiringW[safe(v.getWiringHarness())]   +
                        wiringW[safe(v.getBatteryHealth())]   +
                        wiringW[safe(v.getChargingSystem())]  +
                        wiringW[safe(v.getWiringNeatness())]  +
                        wiringW[safe(v.getElectricalFunc())]  +
                        wiringW[safe(v.getGpsFeasibility())];
        v.setWiringScore(bd(wiringScore));

        // ── Tyres (max 10): 2 items × weights [0, 1.67, 3.33, 5]
        double[] tyreW = {0, 1.67, 3.33, 5};
        double tyreScore =
                tyreW[safe(v.getFrontTyre())] +
                        tyreW[safe(v.getRearTyre())];
        v.setTyreScore(bd(tyreScore));

        // ── Body (max 20): 5 items × weights [0, 1.33, 2.67, 4]
        double[] bodyW = {0, 1.33, 2.67, 4};
        double bodyScore =
                bodyW[safe(v.getFrameAlignment())] +
                        bodyW[safe(v.getFuelTank())]       +
                        bodyW[safe(v.getBodyPanels())]     +
                        bodyW[safe(v.getPaintCondition())] +
                        bodyW[safe(v.getGeneralAppear())];
        v.setBodyScore(bd(bodyScore));

        // ── Accessories (max 10): 5 items × weights [0, 1, 2]
        double[] accW = {0, 1, 2};
        double accessoryScore =
                accW[safe(v.getAccSideMirrors())] +
                        accW[safe(v.getAccCrashBars())]   +
                        accW[safe(v.getAccItem3())]       +
                        accW[safe(v.getAccItem4())]       +
                        accW[safe(v.getAccItem5())];
        v.setAccessoryScore(bd(accessoryScore));

        // ── Total
        v.setTotalScore(bd(engineScore + wiringScore + tyreScore + bodyScore + accessoryScore));
    }

    private int safe(Integer val) { return val != null ? val : 0; }
    private BigDecimal bd(double v) { return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP); }

    private BigDecimal sum(double[] weights, Integer... ratings) {
        double total = 0.0;
        for (Integer r : ratings) {
            if (r != null) total += weights[r];
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }

    private String deriveGrade(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 80) return "A";
        if (s >= 65) return "B";
        if (s >= 50) return "C";
        if (s >= 35) return "D";
        return "F";
    }

    private int insert(OnlineAssetValuation v) {
        String sql = """
             INSERT INTO asset_valuations (
                                                      asset_id, inspector,
                                                      engine_performance, gearbox, suspension, braking, engine_score,
                                                      wiring_harness, battery_health, charging_system,
                                                      wiring_neatness, electrical_func, gps_feasibility, wiring_score,
                                                      front_tyre, rear_tyre, tyre_score,
                                                      frame_alignment, fuel_tank, body_panels,
                                                      paint_condition, general_appear, body_score,
                                                      acc_side_mirrors, acc_crash_bars, acc_item_3, acc_item_4, acc_item_5, accessory_score,
                                                      total_score, grade, remarks, assigned_value
                                                  ) VALUES (
                                                       ?, ?,
                                                      ?, ?,?,?,?,
                                                      ?, ?, ?, ?, ?, ?, ?,
                                                      ?, ?, ?,
                                                      ?, ?, ?, ?, ?, ?,
                                                      ?, ?, ?, ?, ?, ?,
                                                      ?, ?, ?,?
                                                  )
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setInt   (i++, v.getAssetId());
            ps.setInt(i++, v.getTechnicianId());

            //SECTION 1
            ps.setObject(i++, v.getEnginePerformance());
            ps.setObject(i++, v.getGearbox());
            ps.setObject(i++, v.getSuspension());
            ps.setObject(i++, v.getBraking());
            ps.setBigDecimal(i++, v.getEngineScore());

            // Section 2
            ps.setObject(i++, v.getWiringHarness());
            ps.setObject(i++, v.getBatteryHealth());
            ps.setObject(i++, v.getChargingSystem());
            ps.setObject(i++, v.getWiringNeatness());
            ps.setObject(i++, v.getElectricalFunc());
            ps.setObject(i++, v.getGpsFeasibility());
            ps.setBigDecimal(i++, v.getWiringScore());

            // Section 3
            ps.setObject(i++, v.getFrontTyre());
            ps.setObject(i++, v.getRearTyre());
            ps.setBigDecimal(i++, v.getTyreScore());

            // Section 4
            ps.setObject(i++, v.getFrameAlignment());
            ps.setObject(i++, v.getFuelTank());
            ps.setObject(i++, v.getBodyPanels());
            ps.setObject(i++, v.getPaintCondition());
            ps.setObject(i++, v.getGeneralAppear());
            ps.setBigDecimal(i++, v.getBodyScore());

            // Section 5
            ps.setObject(i++, v.getAccSideMirrors());
            ps.setObject(i++, v.getAccCrashBars());
            ps.setObject(i++, v.getAccItem3());
            ps.setObject(i++, v.getAccItem4());
            ps.setObject(i++, v.getAccItem5());
            ps.setBigDecimal(i++, v.getAccessoryScore());

            // Totals
            ps.setBigDecimal(i++, v.getTotalScore());
            ps.setString    (i++, v.getGrade());
            ps.setString    (i++,   v.getRemarks());
            ps.setInt    (i++,   v.getAssignedValue());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    private RowMapper<OnlineAssetValuation> rowMapper() {
        return (rs, rowNum) -> {
            OnlineAssetValuation v = new OnlineAssetValuation();
            v.setId             (rs.getInt("id"));
            v.setAssetId        (rs.getInt("asset_id"));
            v.setTechnicianId      (rs.getInt("inspector"));
            v.setEnginePerformance(rs.getInt("engine_performance"));
            v.setGearbox(rs.getInt("gearbox"));
            v.setSuspension(rs.getInt("suspension"));
            v.setBraking(rs.getInt("braking"));
            v.setEngineScore(rs.getBigDecimal("engine_score"));
            v.setWiringHarness  (getNullableInt(rs, "wiring_harness"));
            v.setBatteryHealth  (getNullableInt(rs, "battery_health"));
            v.setChargingSystem (getNullableInt(rs, "charging_system"));
            v.setWiringNeatness (getNullableInt(rs, "wiring_neatness"));
            v.setElectricalFunc (getNullableInt(rs, "electrical_func"));
            v.setGpsFeasibility (getNullableInt(rs, "gps_feasibility"));
            v.setWiringScore    (rs.getBigDecimal("wiring_score"));

            v.setFrontTyre      (getNullableInt(rs, "front_tyre"));
            v.setRearTyre       (getNullableInt(rs, "rear_tyre"));
            v.setTyreScore      (rs.getBigDecimal("tyre_score"));

            v.setFrameAlignment (getNullableInt(rs, "frame_alignment"));
            v.setFuelTank       (getNullableInt(rs, "fuel_tank"));
            v.setBodyPanels     (getNullableInt(rs, "body_panels"));
            v.setPaintCondition (getNullableInt(rs, "paint_condition"));
            v.setGeneralAppear  (getNullableInt(rs, "general_appear"));
            v.setBodyScore      (rs.getBigDecimal("body_score"));

            v.setAccSideMirrors (getNullableInt(rs, "acc_side_mirrors"));
            v.setAccCrashBars   (getNullableInt(rs, "acc_crash_bars"));
            v.setAccItem3       (getNullableInt(rs, "acc_item_3"));
            v.setAccItem4       (getNullableInt(rs, "acc_item_4"));
            v.setAccItem5       (getNullableInt(rs, "acc_item_5"));
            v.setAccessoryScore (rs.getBigDecimal("accessory_score"));

            v.setTotalScore     (rs.getBigDecimal("total_score"));
            v.setGrade          (rs.getString("grade"));
            v.setRemarks        (rs.getString("remarks"));
            v.setAssignedValue  (rs.getInt("assigned_value"));


            v.setCreatedAt      (rs.getTimestamp("created_at").toLocalDateTime());
            v.setUpdatedAt      (rs.getTimestamp("updated_at").toLocalDateTime());
            return v;
        };
    }

    /** Returns null instead of 0 for nullable TINYINT columns. */
    private Integer getNullableInt(java.sql.ResultSet rs, String col) throws java.sql.SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    private OnlineAssetValuation mapRequestToModel(ValuationRequest req) {
        OnlineAssetValuation v = new OnlineAssetValuation();

        v.setAssetId        (req.getAssetId());
        v.setEnginePerformance(req.getEnginePerformance());
        v.setGearbox(req.getGearbox());
        v.setSuspension(req.getSuspension());
        v.setBraking(req.getBraking());
        v.setWiringHarness  (req.getWiringHarness());
        v.setBatteryHealth  (req.getBatteryHealth());
        v.setChargingSystem (req.getChargingSystem());
        v.setWiringNeatness (req.getWiringNeatness());
        v.setElectricalFunc (req.getElectricalFunc());
        v.setGpsFeasibility (req.getGpsFeasibility());
        v.setFrontTyre      (req.getFrontTyre());
        v.setRearTyre       (req.getRearTyre());
        v.setFrameAlignment (req.getFrameAlignment());
        v.setFuelTank       (req.getFuelTank());
        v.setBodyPanels     (req.getBodyPanels());
        v.setPaintCondition (req.getPaintCondition());
        v.setGeneralAppear  (req.getGeneralAppear());
        v.setAccSideMirrors (req.getAccSideMirrors());
        v.setAccCrashBars   (req.getAccCrashBars());
        v.setAccItem3       (req.getAccItem3());
        v.setAccItem4       (req.getAccItem4());
        v.setAccItem5       (req.getAccItem5());
        v.setRemarks        (req.getRemarks());
        v.setAssignedValue  (req.getAssignedValue());
        return v;
    }
}
