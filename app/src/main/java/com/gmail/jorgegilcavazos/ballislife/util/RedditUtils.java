package com.gmail.jorgegilcavazos.ballislife.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Html;
import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.features.shared.FullCardViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.shared.PostListViewHolder;

import java.util.ArrayList;
import java.util.List;

public final class RedditUtils {
    private final static String TAG = "RedditUtils";

    public final static String LIVE_GT_TYPE = "LIVE_GAME_THREAD";
    public final static String POST_GT_TYPE = "POST_GAME_THREAD";

    /**
     * Parses a given /r/NBA flair into a readable friendly string.
     * @param flair usually formatted as "Flair {cssClass='Celtics1', text='The Truth'}"
     * @return friendly string, e.g. "The Truth", or empty string if flair was null or not valid.
     */
    public static String parseNbaFlair(String flair) {
        final int EXPECTED_SECTIONS = 5;
        if (flair == null) {
            return "";
        }

        String[] sections = flair.split("'");
        if (sections.length == EXPECTED_SECTIONS) {
            return sections[sections.length - 2];
        }
        return "";
    }

    /**
     * Given a list of {@link GameThreadSummary}, a couple of teams and a type (LIVE or POST). Finds
     * and returns the id of the reddit thread for the corresponding game thread or
     * post game thread.
     */
    public static String findGameThreadId(List<GameThreadSummary> threadList,
                                          String type,
                                          String homeTeamAbbr,
                                          String awayTeamAbbr) {
        if (threadList == null) {
            return "";
        }

        String homeTeamFullName = null;
        String awayTeamFullName = null;

        for (TeamName teamName : TeamName.values()) {
            if (teamName.toString().equals(homeTeamAbbr)) {
                homeTeamFullName = teamName.getTeamName();
            }
            if (teamName.toString().equals(awayTeamAbbr)) {
                awayTeamFullName = teamName.getTeamName();
            }
        }

        if (homeTeamFullName == null || awayTeamFullName == null) {
            return "";
        }

        List<GameThreadSummary> matchingThreads = new ArrayList<>();

        for (GameThreadSummary thread : threadList) {
            String capsTitle = thread.getTitle().toUpperCase();

            // Usually formatted as "GAME THREAD: Cleveland Cavaliers @ San Antonio Spurs".
            switch (type) {
                case LIVE_GT_TYPE:
                    if (capsTitle.contains("GAME THREAD") && !capsTitle.contains("POST")
                            && titleContainsTeam(capsTitle, homeTeamFullName)
                            && titleContainsTeam(capsTitle, awayTeamFullName)) {
                        matchingThreads.add(thread);
                    }
                    break;
                case POST_GT_TYPE:
                    if ((capsTitle.contains("POST GAME THREAD")
                            || capsTitle.contains("POST-GAME THREAD"))
                            && titleContainsTeam(capsTitle, homeTeamFullName)
                            && titleContainsTeam(capsTitle, awayTeamFullName)) {
                        matchingThreads.add(thread);
                    }
                    break;
            }
        }

        int maxComments = -1;
        String bestThreadId = "";
        for (GameThreadSummary thread : matchingThreads) {
            if (thread.getNum_comments() > maxComments) {
                maxComments = thread.getNum_comments();
                bestThreadId = thread.getId();
            }
        }

        return bestThreadId;
    }

    public static CharSequence bindSnuDown(String rawHtml) {
        rawHtml = rawHtml.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ")
                .replace("</li>", "<br>").replaceAll("<li.*?>", "•").replace("<p>", "<div>")
                .replace("</p>","</div>");
        rawHtml = rawHtml.substring(0, rawHtml.lastIndexOf("\n") );

        return trim(Html.fromHtml(noTrailingwhiteLines(rawHtml)));
    }

    public static CharSequence trim(CharSequence s) {
        int start = 0;
        int end = s.length();
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    public static String noTrailingwhiteLines(String text) {
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * Checks that the title contains at least the team name, e.g "Spurs".
     */
    public static boolean titleContainsTeam(String title, String fullTeamName) {
        String capsTitle = title.toUpperCase();
        String capsTeam = fullTeamName.toUpperCase(); // Ex. "SAN ANTONIO SPURS".
        String capsName = capsTeam.substring(capsTeam.lastIndexOf(" ") + 1); // Ex. "SPURS".
        return capsTitle.contains(capsName);
    }

    public static void setUpvotedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentUpvoted));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentUpvoted));
    }

    public static void setDownvotedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentDownvoted));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentDownvoted));
    }

    public static void setNoVoteColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentNeutral));
    }

    public static void setUpvotedColors(Context context, final PostListViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentUpvoted));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentUpvoted));
    }

    public static void setDownvotedColors(Context context, final PostListViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentDownvoted));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentDownvoted));
    }

    public static void setNoVoteColors(Context context, final PostListViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentNeutral));
    }

    public static void setSavedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnSave.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.amber));
    }

    public static void setUnsavedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnSave.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
    }

    public static int getTeamLogo(String subreddit) {
        switch (subreddit) {
            case Constants.SUB_ATL:
                return R.drawable.atl;
            case Constants.SUB_BKN:
                return R.drawable.bkn;
            case Constants.SUB_BOS:
                return R.drawable.bos;
            case Constants.SUB_CHA:
                return R.drawable.cha;
            case Constants.SUB_CHI:
                return R.drawable.chi;
            case Constants.SUB_CLE:
                return R.drawable.cle;
            case Constants.SUB_DAL:
                return R.drawable.dal;
            case Constants.SUB_DEN:
                return R.drawable.den;
            case Constants.SUB_DET:
                return R.drawable.det;
            case Constants.SUB_GSW:
                return R.drawable.gsw;
            case Constants.SUB_HOU:
                return R.drawable.hou;
            case Constants.SUB_IND:
                return R.drawable.ind;
            case Constants.SUB_LAC:
                return R.drawable.lac;
            case Constants.SUB_LAL:
                return R.drawable.lal;
            case Constants.SUB_MEM:
                return R.drawable.mem;
            case Constants.SUB_MIA:
                return R.drawable.mia;
            case Constants.SUB_MIL:
                return R.drawable.mil;
            case Constants.SUB_MIN:
                return R.drawable.min;
            case Constants.SUB_NOP:
                return R.drawable.nop;
            case Constants.SUB_NYK:
                return R.drawable.nyk;
            case Constants.SUB_OKC:
                return R.drawable.okc;
            case Constants.SUB_ORL:
                return R.drawable.orl;
            case Constants.SUB_PHI:
                return R.drawable.phi;
            case Constants.SUB_PHO:
                return R.drawable.phx;
            case Constants.SUB_POR:
                return R.drawable.por;
            case Constants.SUB_SAC:
                return R.drawable.sac;
            case Constants.SUB_SAS:
                return R.drawable.sas;
            case Constants.SUB_TOR:
                return R.drawable.tor;
            case Constants.SUB_UTA:
                return R.drawable.uta;
            case Constants.SUB_WAS:
                return R.drawable.was;
            default:
                return R.drawable.rnbasnoo;
        }
    }

    public static int getTeamSnoo(String subreddit) {
        switch (subreddit) {
            case Constants.SUB_ATL:
                return R.drawable.atl;
            case Constants.SUB_BKN:
                return R.drawable.bkn_snoo;
            case Constants.SUB_BOS:
                return R.drawable.bos_snoo;
            case Constants.SUB_CHA:
                return R.drawable.cha_snoo;
            case Constants.SUB_CHI:
                return R.drawable.chi_snoo;
            case Constants.SUB_CLE:
                return R.drawable.cle;
            case Constants.SUB_DAL:
                return R.drawable.dal_snoo;
            case Constants.SUB_DEN:
                return R.drawable.den_snoo;
            case Constants.SUB_DET:
                return R.drawable.det;
            case Constants.SUB_GSW:
                return R.drawable.gsw_snoo;
            case Constants.SUB_HOU:
                return R.drawable.hou_snoo;
            case Constants.SUB_IND:
                return R.drawable.ind_snoo;
            case Constants.SUB_LAC:
                return R.drawable.lac_snoo;
            case Constants.SUB_LAL:
                return R.drawable.lal_snoo;
            case Constants.SUB_MEM:
                return R.drawable.mem_snoo;
            case Constants.SUB_MIA:
                return R.drawable.mia_snoo;
            case Constants.SUB_MIL:
                return R.drawable.mil_snoo;
            case Constants.SUB_MIN:
                return R.drawable.min_snoo;
            case Constants.SUB_NOP:
                return R.drawable.nop;
            case Constants.SUB_NYK:
                return R.drawable.nyk_snoo;
            case Constants.SUB_OKC:
                return R.drawable.okc_snoo;
            case Constants.SUB_ORL:
                return R.drawable.orl_snoo;
            case Constants.SUB_PHI:
                return R.drawable.phi;
            case Constants.SUB_PHO:
                return R.drawable.phx_snoo;
            case Constants.SUB_POR:
                return R.drawable.por_snoo;
            case Constants.SUB_SAC:
                return R.drawable.sac_snoo;
            case Constants.SUB_SAS:
                return R.drawable.sas_snoo;
            case Constants.SUB_TOR:
                return R.drawable.tor;
            case Constants.SUB_UTA:
                return R.drawable.uta_snoo;
            case Constants.SUB_WAS:
                return R.drawable.was_snoo;
            default:
                return R.drawable.rnbasnoo;
        }
    }

    public static String formatScoreToDigits(int score) {
        Log.d(TAG, score + "");
        if (score < 1000) {
            Log.d(TAG, score + " < 1000");
            return String.valueOf(score);
        } else {
            double res = ((double) score) / 1000.0;
            if (res < 10) {
                return String.valueOf(res).substring(0, 3) + "k";
            } else {
                return String.valueOf(res).substring(0, 2) + "k";
            }
        }
    }
}
