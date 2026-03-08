package com.otblabs.jiinueboda.reconciliations;


import com.otblabs.jiinueboda.integrations.momo.mpesa.hashing.MssidHash;
import com.otblabs.jiinueboda.reconciliations.models.MpesaTransaction;
import com.otblabs.jiinueboda.reconciliations.models.UtilityMpesaTransaction;
import com.otblabs.jiinueboda.integrations.momo.mpesa.MpesaTransactionsService;
import com.otblabs.jiinueboda.integrations.momo.mpesa.pull.PullRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MissingPaymentService {

    private final JdbcTemplate jdbcTemplateOne;
    private final MpesaTransactionsService mpesaTransactionsService;
    private final MssidHash mssidHash;

    public MissingPaymentService(JdbcTemplate jdbcTemplateOne, MpesaTransactionsService mpesaTransactionsService, MssidHash mssidHash) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.mpesaTransactionsService = mpesaTransactionsService;
        this.mssidHash = mssidHash;
    }



    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public List<UtilityMpesaTransaction> parseUtilityAccountFile(String filePath) throws IOException {
        List<String> lines = readFile(filePath);
        return parseTransactions(lines);
    }

    private List<String> readFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<UtilityMpesaTransaction> parseTransactions(List<String> lines) {
        List<UtilityMpesaTransaction> transactions = new ArrayList<>();

        // Skip header lines and column headers (first 7 lines)
        for (int i = 7; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",", -1);
            if (parts.length >= 13) {
                UtilityMpesaTransaction transaction = new UtilityMpesaTransaction();

                transaction.setReceiptNo(parts[0].trim());
                transaction.setCompletionTime(parseDateTime(parts[1].trim()));
                transaction.setInitiationTime(parseDateTime(parts[2].trim()));
                transaction.setDetails(parts[3].trim());
                transaction.setTransactionStatus(parts[4].trim());
                transaction.setPaidIn(parseBigDecimal(parts[5].trim()));
                transaction.setWithdrawn(parseBigDecimal(parts[6].trim()));
                transaction.setBalance(parseBigDecimal(parts[7].trim()));
                transaction.setBalanceConfirmed(parseBoolean(parts[8].trim()));
                transaction.setReasonType(parts[9].trim());
                transaction.setOtherPartyInfo(parts[10].trim());
                transaction.setLinkedTransactionId(parts[11].trim());
                transaction.setAccountNo(parts[12].trim());

                transactions.add(transaction);
            }
        }

        return transactions;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return "true".equalsIgnoreCase(value.trim());
    }

    public List<String> processUtilityAccountCsv(MultipartFile file) throws IOException {
        List<String> receiptNumbers = new ArrayList<>();

        List<MpesaTransaction> transactionsList = new ArrayList<>();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip the first 6 rows (header information)
            for (int i = 0; i < 6; i++) {
                reader.readLine();
            }

            // Read the column headers
            String headerLine = reader.readLine();

            if (headerLine == null) {
                throw new IOException("CSV file is empty or malformed");
            }

            String[] headers = headerLine.split(",");
            int receiptColumnIndex = -1;
            int transactionDateTime = -1;
            int transactionAmount   =-1;
            int accountNumber = -1;


            // Find the index of the Receipt No. column
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals("Receipt No.")) {
                    receiptColumnIndex = i;
                    break;
                }else if(headers[i].trim().equals("Completion Time")){
                    transactionDateTime = i;
                    break;
                }else if(headers[i].trim().equals("Paid In")){
                    transactionAmount = i;
                    break;
                }else if(headers[i].trim().equals("A/C No.")){
                    accountNumber = i;
                    break;
                }
            }

            if (receiptColumnIndex == -1) {
                throw new IOException("CSV file does not contain 'Receipt No.' column");
            }

            // Process data rows and extract receipt numbers
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");
                if (values.length > receiptColumnIndex) {
                    String receiptNumber = values[receiptColumnIndex].trim();
                    if (!receiptNumber.isEmpty()) {
                        receiptNumbers.add(receiptNumber);
                    }
                }
            }
        }

        return receiptNumbers;
    }

    List<String> getAndUpdateMissingMpesaTransactions(List<UtilityMpesaTransaction> allTransactions) {
        if (allTransactions == null || allTransactions.isEmpty()) {
            return new ArrayList<>();
        }

        // Extract all transaction IDs for batch lookup
        Set<String> transactionIds = allTransactions.stream()
                .map(transaction -> transaction.getReceiptNo().trim())
                .collect(Collectors.toSet());

        // Single query to check existing transactions
        String existingSql = "SELECT TransID FROM mpesa_c2b WHERE TransID IN (" +
                String.join(",", Collections.nCopies(transactionIds.size(), "?")) + ")";

        Set<String> existingIds = new HashSet<>(
                jdbcTemplateOne.queryForList(existingSql, String.class, transactionIds.toArray())
        );

        // Process only missing transactions
        List<MpesaTransaction> missingTransactions = allTransactions.stream()
                .filter(transaction -> !existingIds.contains(transaction.getReceiptNo().trim()))
                .map(this::convertToMpesaTransaction)
                .collect(Collectors.toList());

        // Batch insert missing transactions
        if (!missingTransactions.isEmpty()) {
            batchInsertMissingTransactions(missingTransactions);
        }

        return missingTransactions.stream()
                .map(MpesaTransaction::getTransactionId)
                .collect(Collectors.toList());

    }

    private MpesaTransaction convertToMpesaTransaction(UtilityMpesaTransaction transaction) {
        MpesaTransaction mpesaTransaction = new MpesaTransaction();
        mpesaTransaction.setFirstName(transaction.getOtherPartyInfo().trim());
        mpesaTransaction.setTransactionId(transaction.getReceiptNo().trim());
        mpesaTransaction.setTransactionTime(transaction.getCompletionTime().toString().trim());
        mpesaTransaction.setTransactionAmount(String.valueOf(transaction.getPaidIn()).trim());
        mpesaTransaction.setLoanAccount(transaction.getAccountNo().trim());
        return mpesaTransaction;
    }

    private void batchInsertMissingTransactions(List<MpesaTransaction> transactions) {
        String sql = """
        INSERT INTO mpesa_c2b(TransactionType,TransID,TransTime,TransAmount,BusinessShortCode,BillRefNumber,is_manual,InvoiceNumber,FirstName,appId,created_at,updated_at)
        VALUES ('Pay Bill',?,?,?,'4125097',?,0,null,null,3,NOW(),NOW())
        """;

        List<Object[]> batchArgs = transactions.stream()
                .map(tx -> new Object[]{
                        tx.getTransactionId().trim(),
                        parseTransactionTime(tx.getTransactionTime()),
                        tx.getTransactionAmount(),
                        tx.getLoanAccount()
                })
                .collect(Collectors.toList());

        jdbcTemplateOne.batchUpdate(sql, batchArgs);
    }

    private Timestamp parseTransactionTime(String timeString) {
        try {
            // Handle ISO format like "2025-07-01T00:01:30"
            if (timeString.contains("T")) {
                LocalDateTime localDateTime = LocalDateTime.parse(timeString);
                return Timestamp.valueOf(localDateTime);
            }
            // Handle other formats if needed
            else {
                // Add other format parsing here if you have different formats
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.parse(timeString, formatter);
                return Timestamp.valueOf(localDateTime);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse transaction time: " + timeString, e);
        }
    }

    private MpesaTransaction setMpesaTransaction(ResultSet rs) throws SQLException {
        MpesaTransaction mpesaTransaction = new MpesaTransaction();
        mpesaTransaction.setFirstName(rs.getString("FirstName"));
        mpesaTransaction.setTransactionId(rs.getString("TransID"));
        mpesaTransaction.setTransactionTime(rs.getString("TransAmount"));
        mpesaTransaction.setTransactionAmount(rs.getString("TransAmount"));
        mpesaTransaction.setLoanAccount(rs.getString("BillRefNumber"));
        return mpesaTransaction;
    }

    public Object getMissingTransactionsFromPullData(String startData, String endDate)  throws Exception{

		PullRequest pullRequest = new PullRequest();
		pullRequest.setStartDate(startData);
		pullRequest.setEndDate(endDate);

        return mpesaTransactionsService.requestPullData(pullRequest,3);

    }
}
