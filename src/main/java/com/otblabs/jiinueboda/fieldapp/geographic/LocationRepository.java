package com.otblabs.jiinueboda.fieldapp.geographic;

import com.otblabs.jiinueboda.fieldapp.geographic.models.County;
import com.otblabs.jiinueboda.fieldapp.geographic.models.SubCounty;
import com.otblabs.jiinueboda.fieldapp.geographic.models.Ward;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LocationRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<County> findAllCounties() {
        String sql = "SELECT * FROM counties";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(County.class));
    }

    public List<SubCounty> findAllSubCounties() {
        String sql = "SELECT * FROM sub_counties";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SubCounty.class));
    }

    public List<Ward> findAllWards() {
        String sql = "SELECT * FROM wards";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Ward.class));
    }
}
