package com.sleepstream.checkkeeper.qrmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRManager {

    public String resultQR="";
    public String FN;
    public String FD;
    public String FP;
    public String date="";
    public String time="";
    public String totalSum;
    private String ErrCode;

    public QRManager(String resultQR) {
        if(resultQR != null) {
            this.resultQR = resultQR;
            parseQR();
        }
    }



    private void parseQR() {

        try {
            //parse Date
            int dateIndexStart = resultQR.indexOf("t=");
            if (dateIndexStart > -1) {
                int dateIdexEnd = resultQR.indexOf("T", dateIndexStart);
                if (dateIdexEnd > -1) {
                    date = resultQR.substring(dateIndexStart + 2, dateIdexEnd);
                    if(date.length()>=8) {
                        SimpleDateFormat oldDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                        SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date dateNew = oldDateFormat.parse(date);
                        date = newDateFormat.format(dateNew);
                    }
                    else date = "";
                } else date = "";
            } else date = "";

            //parse Time
            int timeIndexStart = resultQR.indexOf("T");
            if (timeIndexStart > -1) {
                int timeIndexEnd = resultQR.indexOf("&s=");
                if(timeIndexEnd >-1) {
                    time = resultQR.substring(timeIndexStart + 1, timeIndexEnd);
                    if(time.length()>3) {
                        int length = time.length();
                        //add zero if time without seconds, ?hours?
                        if (length < 6) {
                            String tm = "000000";
                            time = time + tm.substring(0, 6 - length);
                        }

                        SimpleDateFormat oldDateFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
                        SimpleDateFormat newDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        Date timeNew = oldDateFormat.parse(time);
                        time = newDateFormat.format(timeNew);
                    }
                    else
                        time = "";
                }
                else time = "";
            } else time = "";

            //parse totalSum
            int tSumIndexStart = resultQR.indexOf("&s=");
            if (tSumIndexStart > -1) {
                int tSumIndexEnd = resultQR.indexOf("&fn=");
                if(tSumIndexEnd>-1) {
                    totalSum = resultQR.substring(tSumIndexStart + 3, tSumIndexEnd);
                }
                else totalSum = "";
            } else totalSum = "";
            //parse fn
            int fnIndexStart = resultQR.indexOf("&fn=");
            if (fnIndexStart > -1) {
                int fnIdexEnd = resultQR.indexOf("&i=", fnIndexStart);
                if (fnIdexEnd > -1) {
                    FN = resultQR.substring(fnIndexStart + 4, fnIdexEnd);
                } else FN = "";
            } else FN = "";
            //parse fd
            int fdIndexStart = resultQR.indexOf("&i=");
            if (fdIndexStart > -1) {
                int fdIndexEnd = resultQR.indexOf("&fp=", fdIndexStart);
                if (fdIndexEnd > -1) {
                    FD = resultQR.substring(fdIndexStart + 3, fdIndexEnd);
                } else FD = "";
            } else FD = "";
            //parse fp
            int fpIndexStart = resultQR.indexOf("&fp=");
            if (fdIndexStart > -1) {
                int fpIndexEnd = resultQR.indexOf("&n=", fdIndexStart);
                if (fpIndexEnd > -1) {
                    FP = resultQR.substring(fpIndexStart + 4, fpIndexEnd);
                } else FP = "";
            } else FP = "";
        }
        catch(Exception ex)
        {
            throw new Error("Error parsing QRcode\n" + ex.toString());
        }
    }
}
