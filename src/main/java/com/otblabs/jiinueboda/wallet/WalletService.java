package com.otblabs.jiinueboda.wallet;

import com.otblabs.jiinueboda.wallet.models.TransactionApproval;
import com.otblabs.jiinueboda.wallet.models.UserTransaction;
import com.otblabs.jiinueboda.wallet.models.Wallet;
import com.otblabs.jiinueboda.wallet.models.WalletTransaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;

@Service
public class WalletService {

    private final JdbcTemplate jdbcTemplateOne;

    public WalletService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public Wallet getWalletForUser(int userId) throws Exception{
          String sqlUserWallet = "SELECT * FROM user_wallet WHERE user_id =?";
          return  jdbcTemplateOne.queryForObject(sqlUserWallet,(rs,i) -> {
              try {
                  return setWallet(rs);
              } catch (Exception e) {
                  throw new RuntimeException(e);
              }
          },userId);
    }

    public Wallet requestDebitWallet(UserTransaction userTransaction) throws Exception {
        String sqlDebit ="INSERT INTO request_deposit(user_id,wallet_account_number,amount,created_at) VALUES (?,?,?,NOW())";
        jdbcTemplateOne.update(sqlDebit,userTransaction.getUserId(),userTransaction.getAccountNumber(),userTransaction.getAmount());
        return getWalletForUser(userTransaction.getUserId());
    }

    public Wallet requestCreditWallet(UserTransaction userTransaction) throws Exception {
        String sqlDebit ="INSERT INTO request_withdraw(user_id,wallet_account_number,amount,created_at) VALUES (?,?,?,NOW())";
        jdbcTemplateOne.update(sqlDebit,userTransaction.getAmount(),userTransaction.getUserId());
        return getWalletForUser(userTransaction.getUserId());
    }

    public void createWalletTransaction(WalletTransaction walletTransaction){
        String sqlInsert = "INSERT INTO wallet_transactions (user_id, transaction_channel, transaction_reference,wallet_account_number,transaction_amount,created_at) " +
                "VALUES (?,?,?,?,?,NOW())";

        jdbcTemplateOne.update(sqlInsert,
                walletTransaction.getUserId(),
                walletTransaction.getTransactionChannel(),
                walletTransaction.getTransactionReference(),
                walletTransaction.getWalletAccountNumber(),
                walletTransaction.getTransactionAmount());
    }
    public void approveTransactions(TransactionApproval transactionApproval){
        String sqlApproveTransaction;
        if(transactionApproval.getTransactionType().equals("WITHDRAW")){
            sqlApproveTransaction = "UPDATE request_deposit SET approval_status=?, approved_by=?,approved_at=NOW(),updated_at=NOW(),is_completed=1," +
                    "completed_at=NOW() WHERE id=? AND user_id=? AND wallet_account_number=?";
        }else if(transactionApproval.getTransactionType().equals("DEPOSIT")){
            sqlApproveTransaction = "UPDATE request_deposit SET approval_status=?, approved_by=?,approved_at=NOW(),updated_at=NOW(),is_completed=1," +
                    "completed_at=NOW() WHERE id=? AND user_id=? AND wallet_account_number=?";
        }else{
            sqlApproveTransaction = "";
        }

        jdbcTemplateOne.update(sqlApproveTransaction,
                transactionApproval.isApproved(),
                transactionApproval.getApprovedBy(),
                transactionApproval.getRequestId(),
                transactionApproval.getUserId(),
                transactionApproval.getAccountNumber());
    }

    public Wallet debitWallet(UserTransaction userTransaction) throws Exception {
        String sqlDebit ="UPDATE user_wallet SET wallet_balance = wallet_balance + ? WHERE user_id = ?";
        jdbcTemplateOne.update(sqlDebit,userTransaction.getAmount(),userTransaction.getUserId());
        return getWalletForUser(userTransaction.getUserId());
    }

    public Wallet creditWallet(UserTransaction userTransaction) throws Exception {
        String sqlDebit ="UPDATE user_wallet SET wallet_balance = wallet_balance - ? WHERE user_id = ?";
        jdbcTemplateOne.update(sqlDebit,userTransaction.getAmount(),userTransaction.getUserId());
        return getWalletForUser(userTransaction.getUserId());
    }

    Wallet setWallet(ResultSet resultSet) throws Exception {
        Wallet wallet = new Wallet();
        wallet.setUserId(resultSet.getInt("user_id"));
        wallet.setWalletBalance(resultSet.getDouble("wallet_balance"));
        return wallet;
    }

    private boolean walletBalanceIsvalid(Wallet wallet) {
        String sql = "select SUM(transaction_amount)a as wallet_balance from wallet_transactions WHERE user_id=? AND is_verified=1 AND deleted_at is null";
        double transactionsTotal = jdbcTemplateOne.queryForObject(sql,(rs,i)-> rs.getDouble("wallet_balance"),wallet.getUserId());
        return transactionsTotal == wallet.getWalletBalance();
    }
}
