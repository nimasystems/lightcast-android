package com.nimasystems.lightcast.utils;

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
        CharSequence c = DateFormat.format("yyyy-MM-dd kk:mm:ss",
                date.getTime());
        String ret = c.toString();
        ret = (ret == null) ? NULL : ret;
        return ret;
    }

    public static Date SqlStringToDate(String strDate) {
        if (StringUtils.isNullOrEmpty(strDate)) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.US);

        try {
            Date date = format.parse(strDate);
            return date;
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    public static long getLastInsertedId(SQLiteDatabase db) {
        long lastIns = 0;

        if (db == null) {
            return lastIns;
        }

        String query = "SELECT last_insert_rowid()";
        lastIns = Integer.parseInt(db.rawQuery(query, null).toString());

        return lastIns;
    }
}
