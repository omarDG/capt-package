package com.gemadec.cimr.Utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static String GetDateFormat(String datestring) {
        String finaldate = datestring;
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = fmt.parse(datestring);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat fmtOut = new SimpleDateFormat("yyMMdd");
        finaldate = fmtOut.format(date);
        Log.i("SecondstepActivity", "finaldate : "+finaldate);
        return finaldate;
    }
}
