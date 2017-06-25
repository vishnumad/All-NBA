package com.gmail.jorgegilcavazos.ballislife.util;

import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import net.dean.jraw.models.Submission;

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

    // Get data from real submission if available, otherwise used data from fake one.
    public static String getThumbnailToShowFromCustomSubmission(CustomSubmission customSubmission) {
        String thumbnail;
        String highResThumbnail;
        String thumbnailToShow;
        if (customSubmission.getSubmission() == null) {
            thumbnail = customSubmission.getThumbnail();
            highResThumbnail = customSubmission.getHighResThumbnail();
        } else {
            Submission submission = customSubmission.getSubmission();
            thumbnail = submission.getThumbnail();
            try {
                highResThumbnail = submission.getOEmbedMedia().getThumbnail().getUrl().toString();
            } catch (NullPointerException e) {
                highResThumbnail = null;
            }
        }

        // Show HD thumbnail over lower res version.
        return highResThumbnail != null ? highResThumbnail : thumbnail;
    }
}
