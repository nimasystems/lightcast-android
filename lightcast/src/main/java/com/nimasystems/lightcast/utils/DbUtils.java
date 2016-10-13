package com.nimasystems.lightcast.utils;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DbUtils {

    public static final String NULL = "NULL";

    public static String addSlashes(String str) {
        return DatabaseUtils.sqlEscapeString(str);
    }

    public static String DateToSql(Date date) {
        if (date == null) {
            return null;
        }
        CharSequence c = DateFormat.format(DateTimeUtils.FORMAT_SQL_LONG,
                date.getTime());
        String ret = c.toString();
        //noinspection ConstantConditions
        ret = (ret == null) ? NULL : ret;
        return ret;
    }

    public static Date SqlStringToDate(String strDate) {
        if (StringUtils.isNullOrEmpty(strDate)) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat(DateTimeUtils.FORMAT_SQL_LONG, Locale.US);

        try {
            return format.parse(strDate);
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    public static long getLastInsertedId(SQLiteDatabase db) {
        final String MY_QUERY = "SELECT last_insert_rowid()";
        Cursor cur = db.rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        int ID = cur.getInt(0);
        cur.close();
        return ID;
    }
}
