package com.gmail.jorgegilcavazos.ballislife.util;

public final class Utilities {

    public static String getPeriodString(String periodValue, String periodName) {
        if (periodValue.equals("")) {
            return "";
        }
        int period = Integer.parseInt(periodValue);
        int overtimePeriod = period - 4;
        if (period <= 4) {
            return period + " " + periodName;
        } else {
            return overtimePeriod + "OT";
        }
    }

    public static String getStreamableShortcodeFromUrl(String url) {
        final String streamableUrl = "streamable.com/";

        int i = url.indexOf(streamableUrl);

        if (i == -1) {
            return null;
        }

        return url.substring(i + streamableUrl.length());
    }
}
