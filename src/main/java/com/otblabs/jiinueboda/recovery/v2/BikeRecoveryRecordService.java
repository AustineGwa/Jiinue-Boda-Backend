package com.otblabs.jiinueboda.recovery.v2;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BikeRecoveryRecordService {

    private final JdbcTemplate jdbcTemplateOne;

    public BikeRecoveryRecordService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public int insertRecoveryRadar(BikeRecoveryRadar bikeRecoveryRadar) throws Exception{
        String sql = """
                INSERT INTO recovery_radar(loan_id,creation_comment,created_at) VALUES(?,?,NOW())
                """;
        return jdbcTemplateOne.update(sql,bikeRecoveryRadar.getLoanId(),bikeRecoveryRadar.getCreationComment());

    }
}
