package com.otblabs.jiinueboda.filemanagement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

@Service
public class FileManagementService {

    private final JdbcTemplate jdbcTemplateOne;
    private String BASE_UPLOAD_DIR = "/var/www/jiinue/public/storage/";
    private String UPLOAD_DIR;
    private String publicUrl;

    public FileManagementService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public String uploadClientAttachment(MultipartFile file, String fileType, int userId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        switch (fileType) {
            case "NATIONAL-ID" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "nationalIDUploads";
                publicUrl = "https://jiinue.misierraltd.com/storage/nationalIDUploads/"+uniqueFilename;
            }
            case "PASSPORT-PICTURE" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "passportPictureUploads";
                publicUrl = "https://jiinue.misierraltd.com/storage/passportPictureUploads/"+uniqueFilename;
            }
            case "KRA-PIN" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "kraPINUploads";
                publicUrl = "https://jiinue.misierraltd.com/storage/kraPINUploads/"+uniqueFilename;
            }
            case "DRIVING-LICENSE" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "drivingLicenseUploads";
                publicUrl = "https://jiinue.misierraltd.com/storage/drivingLicenseUploads/"+uniqueFilename;
            }
            default -> {
                return null;
            }
        }

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);

        // save file to db
        return saveFileEntryToDB(fileType,uniqueFilename,userId,UPLOAD_DIR,publicUrl);
    }

    public String uploadAssetAttachment(MultipartFile file, String fileType, int assetId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        switch (fileType) {
            case "ASSET-DOCUMENT" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "assetsUploads";
                publicUrl = "https://jiinue.misierraltd.com/storage/assetsUploads/"+uniqueFilename;
            }
            case "ASSET-DOCUMENT-IMAGES" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "assetsUploads";
                publicUrl = "https://jiinue.misierraltd.com/storage/assetsUploads/"+uniqueFilename;
            }
            case "EVAL-REPORT" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "evalReports";
                publicUrl = "https://jiinue.misierraltd.com/storage/evalReports/"+uniqueFilename;
            }
            case "CHARGED-LOGBOOK" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "chargedlogbooks";
                publicUrl = "https://jiinue.misierraltd.com/storage/chargedlogbooks/"+uniqueFilename;
            }
            case "COMMENT-ATTACHMENT-PROOFS" -> {
                UPLOAD_DIR = BASE_UPLOAD_DIR + "commentAttachments";
                publicUrl = "https://jiinue.misierraltd.com/storage/commentAttachments/"+uniqueFilename;
            }
            default -> {
                return null;
            }
        }

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);

        // save file to db
        return saveAssetAttachmentEntryToDB(fileType,uniqueFilename,assetId,UPLOAD_DIR,publicUrl);
    }

    public String uploadValuationForm(MultipartFile file, String fileType, int assetId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        if (fileType.equals("EVAL-REPORT")) {
            UPLOAD_DIR = BASE_UPLOAD_DIR + "evalReports";
            publicUrl = "https://jiinue.misierraltd.com/storage/evalReports/" + uniqueFilename;
        } else {
            return null;
        }

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);


        return saveEvalReportToDB(fileType,uniqueFilename,assetId,UPLOAD_DIR,publicUrl);

    }

    public String uploadChargedLogbbok(MultipartFile file, String fileType, int assetId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        if (fileType.equals("CHARGED-LOGBOOK")) {
            UPLOAD_DIR = BASE_UPLOAD_DIR + "chargedlogbooks";
            publicUrl = "https://jiinue.misierraltd.com/storage/chargedlogbooks/" + uniqueFilename;
        } else {
            return null;
        }

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);


        return saveChargedLogbookToDB(fileType,uniqueFilename,assetId,UPLOAD_DIR,publicUrl);

    }

    public int uploadLoanAgreement(MultipartFile file, String fileType, int userId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        if (fileType.equals("LOAN_AGREEMENT")) {
            UPLOAD_DIR = BASE_UPLOAD_DIR + "loanAgreements";
            publicUrl = "https://jiinue.misierraltd.com/storage/loanAgreements/" + uniqueFilename;
        } else {
            return 0;
        }

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);


        return saveLoanAgreementToDB(fileType,uniqueFilename,userId,UPLOAD_DIR,publicUrl);

    }

    private String saveEvalReportToDB(String docType,String docName, int assetId,String docPath, String publicUrl) throws Exception{
        String sql = "INSERT INTO asset_attachments(doc_type, doc_name, asset_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,docType,docName,assetId,docPath,publicUrl);
        return publicUrl;

    }

    private String saveChargedLogbookToDB(String docType,String docName, int assetId,String docPath, String publicUrl) throws Exception{
        String sql = "INSERT INTO asset_attachments(doc_type, doc_name, asset_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,docType,docName,assetId,docPath,publicUrl);
        return publicUrl;

    }

    private int saveLoanAgreementToDB(String docType,String docName, int userId,String docPath, String publicUrl) throws Exception{

        String sql = "INSERT INTO client_attachments(doc_type, doc_name, user_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplateOne.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, docType);
            ps.setString(2, docName);
            ps.setLong(3, userId);
            ps.setString(4, docPath);
            ps.setString(5, publicUrl);
            return ps;
        }, keyHolder);

        // Get the generated ID
        return keyHolder.getKey().intValue();


    }

    private String saveFileEntryToDB(String docType,String docName, int userId,String docPath, String publicUrl) throws Exception{
        String sql = "INSERT INTO client_attachments(doc_type, doc_name, user_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,docType,docName,userId,docPath,publicUrl);
        return publicUrl;

    }

    private String saveAssetAttachmentEntryToDB(String docType,String docName, int assetId,String docPath, String publicUrl) throws Exception{
        String sql = "INSERT INTO asset_attachments(doc_type, doc_name, asset_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,docType,docName,assetId,docPath,publicUrl);
        return publicUrl;

    }

    public String uploadCommentAttachment(MultipartFile file, String fileType, int commentId) throws Exception {

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename with UUID and timestamp
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + fileExtension;

        UPLOAD_DIR = BASE_UPLOAD_DIR + "commentAttachments";
        publicUrl = "https://jiinue.misierraltd.com/storage/commentAttachments/"+uniqueFilename;

        // Create directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file without replacing existing files
        Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation);

        // save file to db
        return saveCommentAttachmentEntryToDB(fileType,uniqueFilename,commentId,UPLOAD_DIR,publicUrl);
    }

    private String saveCommentAttachmentEntryToDB(String docType,String docName, int commentId,String docPath, String publicUrl) throws Exception{
        String sql = "INSERT INTO comment_attachments(doc_type, doc_name, asset_id, doc_path, public_url, created_at) VALUES(?,?,?,?,?,NOW())";
        jdbcTemplateOne.update(sql,docType,docName,commentId,docPath,publicUrl);
        return publicUrl;

    }


}
