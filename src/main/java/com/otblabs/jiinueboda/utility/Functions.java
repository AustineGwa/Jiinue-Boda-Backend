package com.otblabs.jiinueboda.utility;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Functions {

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
}
