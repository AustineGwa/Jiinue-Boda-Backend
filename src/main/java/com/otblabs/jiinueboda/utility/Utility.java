package com.otblabs.jiinueboda.utility;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

public class Utility {

    public static String getCurrentTimestamp() {
        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.of("Africa/Nairobi")).toInstant());
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
    }

    public static String convertDateFormat(Date date){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return  dateFormat.format(date);
    }

    public static  DayOfWeek getToday(){
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
}

