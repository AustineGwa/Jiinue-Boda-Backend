package com.otblabs.jiinueboda.patners;

import com.otblabs.jiinueboda.utility.UtilityFunctions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class PartnersService {

    private final JdbcTemplate jdbcTemplateOne;

    public PartnersService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public Partner insertPartner(Partner partner) {
        String sql = "INSERT INTO partners (name, organisation, contact_phone, contact_email, login_username, password, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        jdbcTemplateOne.update(sql, partner.getName(), partner.getOrganisation(),partner.getContactPhone(), partner.getContactEmail(), partner.getLoginUsername(), partner.getLoginUsername());
     return null;
    }

    public List<Partner> getAllPartners() {
        String sql = "SELECT id, name, organisation, contact_phone, contact_email, login_username, password, created_at, updated_at, deleted_at FROM partners WHERE deleted_at is null";
        return jdbcTemplateOne.query(sql, (rs,i) ->mapRowToPartner(rs));
    }

    public Partner mapRowToPartner(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String organisation = rs.getString("organisation");
        String contactPhone = rs.getString("contact_phone");
        String contactEmail = rs.getString("contact_email");
        String loginUsername = rs.getString("login_username");
        String password = rs.getString("password");
        String createdAt = rs.getString("created_at");
        Partner partner = new Partner();
        partner.setId(id);
        partner.setName(name);
        partner.setOrganisation(organisation);
        partner.setContactPhone(contactPhone);
        partner.setContactEmail(contactEmail);
        partner.setLoginUsername(loginUsername);
        partner.setPassword(password);
        partner.setCreatedAt(createdAt);
        return partner;
    }

    public Partner getByEmailOrPhone(String user) {

        String sql = "SELECT * FROM partners WHERE contact_email =? OR contact_phone =?";

        try{
            return jdbcTemplateOne.queryForObject(sql, (resultSet, i) -> mapRowToPartner(resultSet),user, UtilityFunctions.formatPhoneNumber(user));
        }catch (Exception e){
            return  null;
        }
    }
}
