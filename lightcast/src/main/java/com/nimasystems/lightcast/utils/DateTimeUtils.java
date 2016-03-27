package com.nimasystems.lightcast.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@SuppressLint("Assert")
public class DateTimeUtils {

    public static final String FORMAT_SQL_LONG = "yyyy-MM-dd kk:mm:ss";

    public static String dateToSQLStringFormat(Date date) {
        return dateToString(date, FORMAT_SQL_LONG);
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static String dateToUTCSqliteString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        format.setTimeZone(tz);
        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date utcStringToDate(String dateStringInUtc) {
        if (dateStringInUtc == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        format.setTimeZone(tz);

        try {
            return format.parse(dateStringInUtc);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String dateTimeToLocaleStringFormat(Context context, Date date) {
        if (date == null || context == null) {
            return null;
        }

        java.text.DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);

        java.text.DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);

        String d = dateFormat.format(date) + " " + timeFormat.format(date);
        return d;
    }

    public static Integer daysBetweenDate(Date fromDateTime, Date toDateTime) {
        Calendar fromDay = Calendar.getInstance();
        fromDay.setTime(fromDateTime);

        Calendar toDay = Calendar.getInstance();
        toDay.setTime(toDateTime);

        Long diff = toDay.getTimeInMillis() - fromDay.getTimeInMillis();
        Long days = diff / (24 * 60 * 60 * 1000);

        return days.intValue();
    }

    public static String dateToLocaleStringFormat(Context context, Date date) {
        if (date == null || context == null) {
            return null;
        }

        java.text.DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        return dateFormat.format(date);
    }

    public static String dateToString(Date date, String format) {
        if (date == null || StringUtils.isNullOrEmpty(format)) {
            assert false : "invalid params";
            return null;
        }
        String ret = DateFormat.format(format, date).toString();
        return ret;
    }

    public static Date getDateFromUnixtime(int unixtime) {
        long time = unixtime * (long) 1000;
        Date date = new Date(time);
        return date;
    }

    public static long getUnixTimestamp(Date date, boolean toCurrentTimezone) {
        if (date == null) {
            assert false : "invalid params";
            return 0;
        }

        long unix_timestamp;

        if (toCurrentTimezone) {
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            int dstOffset = TimeZone.getDefault().getDSTSavings();
            unix_timestamp = (date.getTime() + gmtOffset + dstOffset) / 1000;
        } else {
            unix_timestamp = (date.getTime()) / 1000;
        }
        return unix_timestamp;
    }

    public static String getSystemDateFormat(Context context, String dateString) {
        if (context == null || StringUtils.isNullOrEmpty(dateString)) {
            assert false;
            return null;
        }
        java.text.DateFormat df = new SimpleDateFormat(FORMAT_SQL_LONG,
                Locale.US);
        Date date;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        if (date != null) {
            java.text.DateFormat dateFormat = android.text.format.DateFormat
                    .getDateFormat(context);
            return dateFormat.format(date) + " " + dateString.substring(11, 16);
        }
        return null;
    }

    public static long startOfDay(Date d) {
        return startOfDayMils(d, null);
    }

    public static long startOfDayMils(Date d, TimeZone tz) {
        Calendar cal = (tz != null ? Calendar.getInstance(tz) : Calendar
                .getInstance());
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long mils = cal.getTimeInMillis();
        return mils;
    }

    public static Date stringToDateTime(String dateString, Locale locale) {
        if (dateString == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                locale);

        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date stringToTime(String dateString, Locale locale) {
        if (dateString == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", locale);

        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date stringToDate(String dateString, Locale locale) {
        if (dateString == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);

        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public static String dateToSqliteString(Date date, Locale locale) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                locale);
        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static String dateTimeToStringWithTimezone(TimeZone timezone,
                                                      Date date, Locale locale) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                locale);
        format.setTimeZone(timezone);
        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static String dateToStringWithTimezone(TimeZone timezone, Date date,
                                                  Locale locale) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);
        format.setTimeZone(timezone);
        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static String dateToString(Date date, Locale locale) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);

        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static String timeToString(Date date, Locale locale) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", locale);

        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static String timeToString2(Date date, Locale locale) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", locale);

        try {
            String out = format.format(date);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getMinsFromDate(Date date) {

        Calendar lCal = Calendar.getInstance();
        lCal.setTime(date);

        Integer hours = lCal.get(Calendar.HOUR_OF_DAY);
        if (hours == 0) {
            hours = 12;
        }

        Integer mins = lCal.get(Calendar.MINUTE);

        Integer minutes = 60 * hours + mins;

        return minutes;
    }

    @SuppressLint("SimpleDateFormat")
    public static String dateFromTimezoneToUTC(String dateStr, String format,
                                               TimeZone serverTimezone) {

        SimpleDateFormat sourceFormat = new SimpleDateFormat(format);

        if (serverTimezone != null) {
            sourceFormat.setTimeZone(serverTimezone);
        }

        Date dateServerTimezone;
        String dateUTC;
        try {
            dateServerTimezone = sourceFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        SimpleDateFormat sourceFormat1 = new SimpleDateFormat(format);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        sourceFormat1.setTimeZone(tz);
        dateUTC = sourceFormat1.format(dateServerTimezone);

        return dateUTC;
    }

    @SuppressLint("SimpleDateFormat")
    public static String dateFromUTCTimeZoneToCustom(String dateStr,
                                                     String format) {

        SimpleDateFormat sourceFormat = new SimpleDateFormat(format);
        sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dateUTC;
        String date;
        try {
            dateUTC = sourceFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        SimpleDateFormat sourceFormat1 = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        sourceFormat1.setTimeZone(tz);
        date = sourceFormat1.format(dateUTC);

        return date;
    }

    @SuppressLint("SimpleDateFormat")
    public static String dateFromCustomTimeZoneToUTC(String dateStr,
                                                     String format) {

        SimpleDateFormat sourceFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        sourceFormat.setTimeZone(tz);
        Date dateUTC;
        String date;
        try {
            dateUTC = sourceFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        SimpleDateFormat sourceFormat1 = new SimpleDateFormat(format);
        sourceFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = sourceFormat1.format(dateUTC);

        return date;
    }

    @SuppressLint("SimpleDateFormat")
    public static String dateFromUTCTimeZoneToDefault(String dateStr,
                                                      String format, TimeZone tz) {

        SimpleDateFormat sourceFormat = new SimpleDateFormat(format);
        sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dateUTC;
        String date;
        try {
            dateUTC = sourceFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        SimpleDateFormat sourceFormat1 = new SimpleDateFormat(format);
        sourceFormat1.setTimeZone(tz);
        date = sourceFormat1.format(dateUTC);

        return date;
    }
}
