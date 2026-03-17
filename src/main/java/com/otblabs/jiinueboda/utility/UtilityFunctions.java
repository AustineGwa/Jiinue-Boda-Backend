package com.otblabs.jiinueboda.utility;

import jakarta.servlet.http.HttpServletRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.Enumeration;

public class UtilityFunctions {

    public static String getCurrentTimestamp() {
        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.of("Africa/Nairobi")).toInstant());
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) { return null; }

        // 1. Trim leading and trailing spaces
        phoneNumber = phoneNumber.trim();

        // 2. Remove all internal spaces
        phoneNumber = phoneNumber.replaceAll("\\s+", "");

        // 3. Remove leading '+'
        if (phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.substring(1);
        }

        // 4. Replace leading '0' with '254'
        if (phoneNumber.startsWith("0")) {
            phoneNumber = "254" + phoneNumber.substring(1);
        }

        // 5. If 10 digits, append 254 at the beginning
        if (phoneNumber.length() == 10) {
            phoneNumber = "254" + phoneNumber;
        }

        return phoneNumber;
    }

    public static String convertDateFormat(Date date){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return  dateFormat.format(date);
    }

    public static DayOfWeek getToday(){
        return LocalDateTime.now().atZone(ZoneId.of("Africa/Nairobi")).getDayOfWeek();
    }

    public static int getCurrentYear(){
        return Year.now().getValue();
    }


    public static int getCurrentMonth(){
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();

        // Get the current month as an integer (1-12)
        int currentMonthValue = currentMonth.getValue();
        return currentMonthValue;

        // Get the current month name
//        String currentMonthName = currentMonth.name();
    }

    public static void logHeaders(HttpServletRequest request) {
        System.out.println("Request Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
    }

    public static Date formatDateTime(String datetimevalue) throws ParseException {
        return new SimpleDateFormat("yyyyMMddHHmmss").parse(datetimevalue);
    }
}
