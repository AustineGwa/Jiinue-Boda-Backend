package com.otblabs.jiinueboda.mail.backups;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class TransactionsBackupService {

    private final JdbcTemplate  jdbcTemplateOne;

    public TransactionsBackupService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }


    List<TransactionResult> getAllTransactions(){
        String sql = """
                SELECT COMMAND_ID, PARTY_B, REMARKS, OCCASION, RESPONSE_CODE, RESPONSE_DESCRIPTION, CONVERSATION_ID, ORIGINATOR_CONVERSATION_ID,
                       RESULT_DESC, RESULT_TYPE, RESULT_CODE, TRANSACTION_ID, TRANSACTION_RECEIPT, RESULT_PARAMETERS, TRANSACTION_AMOUNT,
                       RECIEVER_NAME, B2C_WORKING_ACCOUNT_AVAILABLE_FUNDS, B2C_UTILITY_ACCOUNT_AVAILABLE_FUNDS, TRANSACTION_COMPLETED_DATETIME,
                       RECEIVER_PARTY_PUBLIC_NAME,  CREATED_AT
                FROM mpesa_b2c WHERE occasion IN (SELECT loanAccountMPesa FROM loans)
                """;

        return jdbcTemplateOne.query(sql,(rs,i) -> TransactionResultRowMapper(rs));
    }

    private TransactionResult TransactionResultRowMapper(ResultSet rs) throws SQLException {

        TransactionResult result = new TransactionResult();

        result.setCommandId(rs.getString("COMMAND_ID"));
        result.setPartyB(rs.getString("PARTY_B"));
        result.setRemarks(rs.getString("REMARKS"));
        result.setOccasion(rs.getString("OCCASION"));
        result.setResponseCode(rs.getString("RESPONSE_CODE"));
        result.setResponseDescription(rs.getString("RESPONSE_DESCRIPTION"));
        result.setConversationId(rs.getString("CONVERSATION_ID"));
        result.setOriginatorConversationId(rs.getString("ORIGINATOR_CONVERSATION_ID"));
        result.setResultDesc(rs.getString("RESULT_DESC"));
        result.setResultType(rs.getString("RESULT_TYPE"));
        result.setResultCode(rs.getString("RESULT_CODE"));
        result.setTransactionId(rs.getString("TRANSACTION_ID"));
        result.setTransactionReceipt(rs.getString("TRANSACTION_RECEIPT"));
        result.setResultParameters(rs.getString("RESULT_PARAMETERS"));
        result.setTransactionAmount(rs.getInt("TRANSACTION_AMOUNT"));
        result.setReceiverName(rs.getString("RECIEVER_NAME"));
        result.setB2cWorkingAccountAvailableFunds(rs.getInt("B2C_WORKING_ACCOUNT_AVAILABLE_FUNDS"));
        result.setB2cUtilityAccountAvailableFunds(rs.getInt("B2C_UTILITY_ACCOUNT_AVAILABLE_FUNDS"));
        result.setTransactionCompletedDatetime(rs.getString("TRANSACTION_COMPLETED_DATETIME"));
        result.setReceiverPartyPublicName(rs.getString("RECEIVER_PARTY_PUBLIC_NAME"));
        result.setCreatedAt( rs.getString("CREATED_AT"));

        return result;
    }


}
