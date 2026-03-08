package com.otblabs.jiinueboda.integrations.momo.mpesa.hashing;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Service
public class MssidHash {

    private final JdbcTemplate jdbcTemplateOne;

    public MssidHash(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public  String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public  void generateNumbers(long start,long end) throws Exception {
//
//        for (long number = start; number <= end; number++) {
//            String hash = hashString(String.valueOf(number));
//            System.out.println("NUMBER "+number+ "hash "+hash);
//
//            insertRecordToDb(number,hash);
//
//        }
//
//        System.out.printf("... DONE ...");
//
//    }

//    public void generateNumbersSavetoFile(long start,long end) throws Exception {
//        int rowCount = 0;
//        String desktopPath = System.getProperty("user.home") + "\\Desktop\\numbers_2547.csv";
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(desktopPath))) {
//            for (long number = start; number <= end; number++) {
//                rowCount++;
//                String hash = hashString(String.valueOf(number));
//
//                System.out.println("NUMBER "+number+ "hash "+hash);
//
//                writer.write(number + "," + hash);
//                writer.newLine();
//            }
//
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void generateNumbers(long start, long end) {
        final int batchSize = 25_000;
        List<Object[]> batchArgs = new ArrayList<>(batchSize);

        long count = 0;

        for (long number = start; number <= end; number++) {
            String hash = hashString(String.valueOf(number));
            batchArgs.add(new Object[]{number, hash});
            count++;

            if (batchArgs.size() == batchSize) {
                insertBatch(batchArgs);
                batchArgs.clear();
                System.out.println("Inserted rows: " + count);
            }
        }

        // insert the last batch if not empty
        if (!batchArgs.isEmpty()) {
            insertBatch(batchArgs);
        }

        System.out.println("----- DONE -----");
    }

    private void insertBatch(List<Object[]> batchArgs) {
        StringBuilder sql = new StringBuilder("INSERT INTO mpesa_hash_table(number, hash) VALUES ");

        List<Object> params = new ArrayList<>(batchArgs.size() * 2);
        for (int i = 0; i < batchArgs.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("(?, ?)");
            params.add(batchArgs.get(i)[0]);
            params.add(batchArgs.get(i)[1]);
        }

        jdbcTemplateOne.update(sql.toString(), params.toArray());
    }


//    private void insertRecordToDb(long number, String hash) throws Exception{
//
//       String sql = """
//               INSERT INTO mpesa_hash_table(number, hash) values (?,?)
//               """;
//
//        jdbcTemplateOne.update(sql,number,hash);
//    }

    public boolean areHashesEqual(String hash1, String hash2) {
        return hash1.equals(hash2);
    }

    public static String formatPhoneNumber(String phoneNumber){
        if(phoneNumber == null ){return null;}
        if(phoneNumber.length() <11 && phoneNumber.startsWith("0")){
            return phoneNumber.replaceFirst("^0", "254");
        }

        if (phoneNumber.length() == 13 && phoneNumber.startsWith("+")){
            return phoneNumber.replaceFirst("^+", "");
        }else {
            return  phoneNumber;
        }

    }


    public void initDB() {
        try {
//            long start = 254700000000L;
//            long end = 254799999999L;

              long start = 254100000000L;
              long end = 254199999999L;
            generateNumbers(start,end);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }



}
