package com.otblabs.jiinueboda.groups;

import com.otblabs.jiinueboda.groups.models.Group;
import com.otblabs.jiinueboda.groups.models.NewGroupRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

@Service
public class GroupsService {

    private final JdbcTemplate jdbcTemplateOne;
    private String BASE_UPLOAD_DIR = "/var/www/jiinue/public/storage/";

    public GroupsService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    List<Group> getAllGroups() throws Exception{
        String sql = """
        SELECT t1.*,(SELECT name from counties where county_id=t1.county_id)  as county,
       (SELECT name from sub_counties where sub_county_id=t1.sub_county_id) as sub_county,
       (SELECT name from wards where ward_id=t1.ward_id) as ward,
       (SELECT count(id) FROM users WHERE group_id = t1.id) as total_members,
       (SELECT first_name from users WHERE id=t1.created_by) as created_by_name
       FROM `groups` as t1 WHERE deleted_at is null ORDER BY created_at DESC
        """;

        return  jdbcTemplateOne.query(sql,(rs,i)->setGroup(rs));
    }

    List<Group> getAllGroupsInWard(int wardId) throws Exception{
        String sql = """
                        SELECT t1.*,
                               (SELECT name from counties where county_id=t1.county_id)  as county,
                               (SELECT name from sub_counties where sub_county_id=t1.sub_county_id) as sub_county,
                               (SELECT name from wards where ward_id=t1.ward_id) as ward,
                               (SELECT count(id) FROM users WHERE group_id = t1.id) as total_members,
                               (SELECT first_name from users WHERE id=t1.created_by) as created_by_name
                                 FROM `groups` as t1 WHERE deleted_at is null AND ward_id=? ORDER BY created_at DESC
                        """;

        return  jdbcTemplateOne.query(sql,(rs,i)->setGroup(rs),wardId);
    }



    List<Group> getAllGroupsForPartner(int partnerId) throws Exception{
        String sql = """
                    SELECT t1.*,
                           (SELECT name from counties where county_id=t1.county_id)  as county,
                           (SELECT name from sub_counties where sub_county_id=t1.sub_county_id) as sub_county,
                           (SELECT name from wards where ward_id=t1.ward_id) as ward,
                           (SELECT count(id) FROM users WHERE group_id = t1.id) as total_members ,
                           (SELECT first_name from users WHERE id=t1.created_by) as created_by_name
                    FROM `groups` as t1  WHERE id IN (SELECT group_id FROM users 
                    WHERE patner_id = ?)  AND deleted_at is null
                    """;
        return  jdbcTemplateOne.query(sql,(rs,i)->setGroup(rs),partnerId);
    }

    private Group setGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getInt("id"));
        group.setGroupName(rs.getString("group_name"));
        group.setCounty(rs.getString("county"));
        group.setSubCounty(rs.getString("sub_county"));
        group.setWard(rs.getString("ward"));
        group.setStageLat(rs.getDouble("stage_latitude"));
        group.setStageLong(rs.getDouble("stage_longitude"));
        group.setChairName(rs.getString("chair_name"));
        group.setChairPhone(rs.getString("chair_phone"));
        group.setTresName(rs.getString("tres_name"));
        group.setTresPhone(rs.getString("tres_phone"));
        group.setSecName(rs.getString("sec_name"));
        group.setSecPhone(rs.getString("sec_phone"));
        group.setCreatedAt(rs.getString("created_at"));
        group.setOperatingHours(rs.getString("operating_hours"));
        group.setAccessRoads(rs.getString("access_roads"));
        group.setAdditionalNotes(rs.getString("additional_notes"));
        group.setTotalMembers(rs.getInt("total_members"));
        group.setLocationDataUpdated(rs.getBoolean("location_data_updated"));
        group.setCreatedBy(rs.getString("created_by_name"));
        return group;
    }

    int createNewGroup(@RequestBody NewGroupRequest group) throws Exception{

        String sql = """
                    INSERT INTO `groups`(
                        county_id, sub_county_id, ward_id, group_name, stage_latitude, stage_longitude,
                        chair_name, chair_phone, tres_name, tres_phone, sec_name, sec_phone, created_by,
                        expected_total_members, operating_hours, access_roads, additional_notes, appID,location_data_updated, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,1, NOW())
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, group.getCountyId());
            ps.setLong(2, group.getSubCountyId());
            ps.setLong(3, group.getWardId());
            ps.setString(4, group.getGroupName());
            ps.setDouble(5, group.getStageLat());
            ps.setDouble(6, group.getStageLong());
            ps.setString(7, group.getChairName());
            ps.setString(8, group.getChairPhone());
            ps.setString(9, group.getTresName());
            ps.setString(10, group.getTresPhone());
            ps.setString(11, group.getSecName());
            ps.setString(12, group.getSecPhone());
            ps.setInt(13, group.getCreatedBy());
            ps.setInt(14, group.getExpectedTotalMembers());
            ps.setString(15, group.getOperatingHours());
            ps.setString(16, group.getAccessRoads());
            ps.setString(17, group.getAdditionalNotes());
            ps.setInt(18, 3);
            return ps;
        }, keyHolder);

        int groupId = keyHolder.getKey().intValue();

        //upload photos for the created group
        group.getStagePhotos().forEach(photo -> {
            try {
                uploadStagePhotos(photo,"STAGE_PHOTO", groupId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });



        return groupId;
    }

    public String uploadStagePhotos(MultipartFile file, String fileType, int groupId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        String UPLOAD_DIR = BASE_UPLOAD_DIR + "stagePhotos";
        String publicUrl = "https://jiinue.misierraltd.com/storage/stagePhotos/"+uniqueFilename;

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);

        // save file to db
        return saveStagePhotoToDB(fileType,uniqueFilename,groupId,UPLOAD_DIR,publicUrl);
    }

    private String saveStagePhotoToDB(String docType,String docName, int groupId,String docPath, String publicUrl) throws Exception{

        String sql = "INSERT INTO stage_photos(doc_type, doc_name, group_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,docType,docName,groupId,docPath,publicUrl);
        return publicUrl;

    }


    public Object updateGroupLocationDetails(GroupLocationUpdate groupLocationUpdate) throws Exception{

        String sql = """
                UPDATE `groups` SET county_id=?, sub_county_id=?, ward_id=?, location_data_updated=true WHERE id=?
                """;

        return jdbcTemplateOne.update(sql,
                groupLocationUpdate.getCountyId(),
                groupLocationUpdate.getSubCountyId(),
                groupLocationUpdate.getWardId(),
                groupLocationUpdate.getGroupId());
    }

    public Group getGroupDetails(int groupId) {
        String sql = """
                SELECT t1.*,(SELECT name from counties where county_id=t1.county_id)  as county,
                       (SELECT name from sub_counties where sub_county_id=t1.sub_county_id) as sub_county,
                       (SELECT name from wards where ward_id=t1.ward_id) as ward,
                       (SELECT count(id) FROM users WHERE group_id = t1.id) as total_members,
                       (SELECT first_name from users WHERE id=t1.created_by) as created_by_name
                       FROM `groups` as t1 WHERE id = ? AND deleted_at is null  ORDER BY created_at DESC
                """;
        return jdbcTemplateOne.queryForObject(sql , (rs,i)-> setGroup(rs),groupId);
    }

    public List<String> getGroupPhotos(int groupId) {

        String sql = """
                SELECT * FROM stage_photos WHERE group_id=?
                """;

        return jdbcTemplateOne.query(sql,(rs,i)-> rs.getString("public_url"),groupId);
    }
}
