package com.otblabs.jiinueboda.assets.tracking.trackers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class TrackerService {

    private final JdbcTemplate jdbcTemplateOne;

    public TrackerService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public List<Tracker> getAllTrackers() throws Exception {
        String sql = """
                SELECT t.id,t.model, t.imei, tu.simcard_number, tu.user_id,tu.loan_id  FROM bike_trackers t left join tracker_usage tu ON t.id = tu.tracker_id
                """;

        return jdbcTemplateOne.query(sql , (rs,i)->trackersRowMapper(rs));
    }

    private Tracker trackersRowMapper(ResultSet rs) throws SQLException {
        Tracker tracker = new Tracker();
        tracker.setId(rs.getInt("id"));
        tracker.setModel(rs.getString("model"));
        tracker.setImei(rs.getString("imei"));
        tracker.setSimcard(rs.getString("simcard_number"));
        tracker.setUserId(rs.getInt("user_id"));
        tracker.setLoanId(rs.getString("loan_id"));
        return tracker;
    }

    public int updateTrackerSimcard(int trackerId, String simcard) {

        String sql = """
                INSERT INTO tracker_usage (tracker_id, simcard_number, created_at) VALUES (?,?,NOW())
        """;
        return jdbcTemplateOne.update(sql, trackerId, simcard);
    }
}
