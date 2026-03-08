package com.otblabs.jiinueboda.assets.tracking.trackerExcel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class BulkSimCardNumberExtractor {
    public void extractNumbers() {
        String inputFolderPath = "data/xls_inputs"; // Change to your folder
        String outputPath = "combined_sim_cards_output.xlsx";

        try (Workbook outputWorkbook = new XSSFWorkbook()) {
            Sheet outputSheet = outputWorkbook.createSheet("Sim Cards");

            // Write header
            Row header = outputSheet.createRow(0);
            header.createCell(0).setCellValue("phone");
            header.createCell(1).setCellValue("amount");

            File folder = new File(inputFolderPath);
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));



            if (files == null || files.length == 0) {
                System.out.println("No .xlsx files found in folder: " + inputFolderPath);
                return;
            }

            int outputRowNum = 1;

            for (File file : files) {
                try (InputStream inp = new FileInputStream(file);
                     Workbook inputWorkbook = new XSSFWorkbook(inp)
                ) {

                    Sheet inputSheet = inputWorkbook.getSheetAt(0);

                    // Find "Sim Card" column
                    Row headerRow = inputSheet.getRow(1);
                    int simCardCol = -1;
                    for (Cell cell : headerRow) {
                        if (cell.getStringCellValue().trim().equalsIgnoreCase("Sim Card")) {
                            simCardCol = cell.getColumnIndex();
                            break;
                        }
                    }

                    if (simCardCol == -1) {
                        System.out.println("Sim Card column not found in: " + file.getName());
                        continue;
                    }

                    for (int i = 2; i <= inputSheet.getLastRowNum(); i++) {
                        Row row = inputSheet.getRow(i);
                        if (row != null) {
                            Cell simCardCell = row.getCell(simCardCol);
                            if (simCardCell != null && !simCardCell.toString().trim().isEmpty()) {
                                String simCard = simCardCell.toString().trim();
                                if (simCard.startsWith("0")) {
                                    simCard = "254" + simCard.substring(1);
                                }

                                Row newRow = outputSheet.createRow(outputRowNum++);
                                newRow.createCell(0).setCellValue(simCard); // Insert modified value
                                newRow.createCell(1).setCellValue(5);       // Amount column
                            }
                        }
                    }


                    System.out.println("Processed: " + file.getName());
                }
            }

            // Save combined output
            try (OutputStream fileOut = new FileOutputStream(outputPath)) {
                outputWorkbook.write(fileOut);
            }

            System.out.println("All Sim Card data combined and exported to: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
