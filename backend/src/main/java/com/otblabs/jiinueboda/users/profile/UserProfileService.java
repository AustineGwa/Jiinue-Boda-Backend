package com.otblabs.jiinueboda.users.profile;

import com.otblabs.jiinueboda.users.models.ProfileLoanStanding;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class UserProfileService {

    private final JdbcTemplate jdbcTemplateOne;

    public UserProfileService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }


    public List<KycDocument> getAllUserKycDocuments(int userId) {

        String sql = """
                SELECT * FROM client_attachments WHERE user_id=?
                """;
        return jdbcTemplateOne.query(sql, (rs,i)-> mapRowToKycDocs(rs),userId);
    }

    private KycDocument mapRowToKycDocs(ResultSet rs) throws SQLException {
        KycDocument kycDocument = new KycDocument();
        kycDocument.setDocId(rs.getInt("id"));
        kycDocument.setDocType(rs.getString("doc_type"));
        kycDocument.setPublicUrl(rs.getString("public_url"));
        kycDocument.setUploadedOn(rs.getString("created_at"));
        return kycDocument;
    }


    public List<ProfileData> getClientProfile(int userId) {
        return null;
    }

    public ProfileLoanStanding getProfileLoanStanding(int userId) {
        String sql = """
                select
                SUm(l.client_loan_total)as TotalLoan,
                SUM((client_loan_total - IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber)),0))) as LoanBalance,
                SUM(IFNULL((select sum(TransAmount) from mpesa_c2b m where (l.loanAccountMPesa = m.BillRefNumber)),0)) as TotalPaid
                from loans l
                inner join users u on u.id = l.userID
                where u.id =? AND l.disbursed_at is not null
                """;
        return jdbcTemplateOne.queryForObject(sql,(rs,i)->setProileLoanStanding(rs),userId);
    }

    private ProfileLoanStanding setProileLoanStanding(ResultSet rs) throws SQLException {
        ProfileLoanStanding profileLoanStanding = new ProfileLoanStanding();
        profileLoanStanding.setTotalLoan(rs.getInt("TotalLoan"));
        profileLoanStanding.setTotalPaid(rs.getInt("TotalPaid"));
        profileLoanStanding.setLoanBalance(rs.getInt("LoanBalance"));
        return profileLoanStanding;
    }

    public String getUserProfilePicture(int userId) {
        String sql = """
                SELECT public_url FROM client_attachments WHERE user_id = ? AND doc_type = 'PASSPORT-PICTURE'
                """;
        return jdbcTemplateOne.queryForObject(sql,(rs,i)-> rs.getString("public_url"),userId);
    }
}
