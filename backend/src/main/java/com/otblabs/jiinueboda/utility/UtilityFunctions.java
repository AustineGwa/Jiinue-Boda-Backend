package com.otblabs.jiinueboda.utility;

import jakarta.servlet.http.HttpServletRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class UtilityFunctions {

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
