package com.smartstay.reports.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static final String OUTPUT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String OUTPUT_TIME_FORMAT = "hh:mm:ss a";
    public static final String OUTPUT_MONTH_FORMAT = "MMM YYYY";
    public static final String OUTPUT_DATE_MONTH_FORMAT = "dd MMM";

    public static String dateToString(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(date);
    }
    public static String dateToMonth(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_MONTH_FORMAT).format(date);
    }

    public static String dateToTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_TIME_FORMAT).format(date);
    }
    public static String dateToDateMonth(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_DATE_MONTH_FORMAT).format(date);
    }
}
