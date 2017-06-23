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
            return sections[3];
        }
        return "";
    }

    /**
     * Parses a given /r/NBA flair into a string of the css class.
     * @param flair usually formatted as "Flair {cssClass='Celtics1', text='The Truth'}"
     * @return CSS class string, e.g. "Celtics1", or empty string if flair was null or not valid.
     */
    public static String parseCssClassFromFlair(String flair) {
        final int EXPECTED_SECTIONS = 5;
        if (flair == null) {
            return "";
        }

        String[] sections = flair.split("'");
        if (sections.length == EXPECTED_SECTIONS) {
            return sections[1];
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

    public static int getFlairFromCss(String cssClass) {
        switch (cssClass) {
            case "76ers1":
                return R.drawable.phi;
            case "76ers2":
                return R.drawable.phi;
            case "76ers3":
                return R.drawable.phi;
            case "76ers4":
                return R.drawable.phi;
            case "76ers5":
                return R.drawable.phi;
            case "Bucks1":
                return R.drawable.mil;
            case "Bucks2":
                return R.drawable.mil;
            case "Bucks3":
                return R.drawable.mil;
            case "Bucks4":
                return R.drawable.mil;
            case "Bucks5":
                return R.drawable.mil;
            case "Bucks6":
                return R.drawable.mil;
            case "Bucks7":
                return R.drawable.mil;
            case "Bulls":
                return R.drawable.chi;
            case "Cavaliers1":
                return R.drawable.cle;
            case "Cavaliers2":
                return R.drawable.cle;
            case "Cavaliers3":
                return R.drawable.cle;
            case "Celtics1":
                return R.drawable.bos;
            case "Celtics2":
                return R.drawable.bos;
            case "Clippers":
                return R.drawable.lac;
            case "Clippers2":
                return R.drawable.lac;
            case "Clippers3":
                return R.drawable.lac;
            case "Clippers4":
                return R.drawable.lac;
            case "Grizzlies":
                return R.drawable.mem;
            case "Grizzlies2":
                return R.drawable.mem;
            case "VanGrizzlies":
                return R.drawable.mem;
            case "VanGrizzlies2":
                return R.drawable.mem;
            case "VanGrizzlies3":
                return R.drawable.mem;
            case "Hawks1":
                return R.drawable.atl;
            case "Hawks2":
                return R.drawable.atl;
            case "Hawks3":
                return R.drawable.atl;
            case "HawksSecond":
                return R.drawable.atl;
            case "Heat":
                return R.drawable.atl;
            case "Heat2":
                return R.drawable.atl;
            case "Heat3":
                return R.drawable.atl;
            case "Pelicans":
                return R.drawable.nop;
            case "Pelicans2":
                return R.drawable.nop;
            case "Pelicans3":
                return R.drawable.nop;
            case "Pelicans4":
                return R.drawable.nop;
            case "Pelicans5":
                return R.drawable.nop;
            case "Jazz1":
                return R.drawable.uta;
            case "Jazz2":
                return R.drawable.uta;
            case "Jazz3":
                return R.drawable.uta;
            case "Jazz4":
                return R.drawable.uta;
            case "Jazz5":
                return R.drawable.uta;
            case "Jazz6":
                return R.drawable.uta;
            case "Jazz7":
                return R.drawable.uta;
            case "Kings1":
                return R.drawable.sac;
            case "Kings2":
                return R.drawable.sac;
            case "Kings3":
                return R.drawable.sac;
            case "Kings4":
                return R.drawable.sac;
            case "Kings5":
                return R.drawable.sac;
            case "Kings6":
                return R.drawable.sac;
            case "Kings7":
                return R.drawable.sac;
            case "Kings8":
                return R.drawable.sac;
            case "Knicks1":
                return R.drawable.nyk;
            case "Knicks2":
                return R.drawable.nyk;
            case "Knicks3":
                return R.drawable.nyk;
            case "Knicks4":
                return R.drawable.nyk;
            case "Knicks5":
                return R.drawable.nyk;
            case "KnickerBockers":
                return R.drawable.nyk;
            case "Lakers1":
                return R.drawable.lal;
            case "Lakers2":
                return R.drawable.lal;
            case "Lakers3":
                return R.drawable.lal;
            case "MinnLakers":
                return R.drawable.lal;
            case "Magic1":
                return R.drawable.orl;
            case "Magic2":
                return R.drawable.orl;
            case "Magic3":
                return R.drawable.orl;
            case "Magic4":
                return R.drawable.orl;
            case "Mavs1":
                return R.drawable.dal;
            case "Mavs2":
                return R.drawable.dal;
            case "Mavs3":
                return R.drawable.dal;
            case "Nets1":
                return R.drawable.bkn;
            case "Nets2":
                return R.drawable.bkn;
            case "Nets3":
                return R.drawable.bkn;
            case "Nets4":
                return R.drawable.bkn;
            case "Nuggets1":
                return R.drawable.den;
            case "Nuggets2":
                return R.drawable.den;
            case "Nuggets3":
                return R.drawable.den;
            case "Nuggets4":
                return R.drawable.den;
            case "Pacers1":
                return R.drawable.ind;
            case "Pacers2":
                return R.drawable.ind;
            case "Pistons1":
                return R.drawable.det;
            case "Pistons2":
                return R.drawable.det;
            case "Pistons3":
                return R.drawable.det;
            case "Pistons4":
                return R.drawable.det;
            case "Raptors1":
                return R.drawable.tor;
            case "Raptors2":
                return R.drawable.tor;
            case "Raptors3":
                return R.drawable.tor;
            case "Raptors4":
                return R.drawable.tor;
            case "Raptors5":
                return R.drawable.tor;
            case "Raptors6":
                return R.drawable.tor;
            case "Raptors7":
                return R.drawable.tor;
            case "Raptors8":
                return R.drawable.tor;
            case "Raptors9":
                return R.drawable.tor;
            case "TorHuskies":
                return R.drawable.tor;
            case "Rockets1":
                return R.drawable.hou;
            case "Rockets2":
                return R.drawable.hou;
            case "Rockets3":
                return R.drawable.hou;
            case "SanDiegoRockets":
                return R.drawable.hou;
            case "Spurs1":
                return R.drawable.sas;
            case "Spurs2":
                return R.drawable.sas;
            case "Spurs3":
                return R.drawable.sas;
            case "Suns1":
                return R.drawable.phx;
            case "Suns2":
                return R.drawable.phx;
            case "Suns3":
                return R.drawable.phx;
            case "Suns4":
                return R.drawable.phx;
            case "Suns5":
                return R.drawable.phx;
            case "Suns6":
                return R.drawable.phx;
            case "Supersonics1":
                return R.drawable.sea;
            case "Supersonics2":
                return R.drawable.sea;
            case "Thunder":
                return R.drawable.okc;
            case "Timberwolves1":
                return R.drawable.min;
            case "Timberwolves2":
                return R.drawable.min;
            case "Timberwolves3":
                return R.drawable.min;
            case "Timberwolves4":
                return R.drawable.min;
            case "TrailBlazers1":
                return R.drawable.por;
            case "TrailBlazers2":
                return R.drawable.por;
            case "TrailBlazers3":
                return R.drawable.por;
            case "TrailBlazers4":
                return R.drawable.por;
            case "TrailBlazers5":
                return R.drawable.por;
            case "Warriors1":
                return R.drawable.gsw;
            case "Warriors2":
                return R.drawable.gsw;
            case "Warriors3":
                return R.drawable.gsw;
            case "Warriors4":
                return R.drawable.gsw;
            case "Wizards":
                return R.drawable.was;
            case "Wizards2":
                return R.drawable.was;
            case "Wizards3":
                return R.drawable.was;
            case "Wizards4":
                return R.drawable.was;
            case "Wizards5":
                return R.drawable.was;
            case "Wizards6":
                return R.drawable.was;
            case "ChaHornets":
                return R.drawable.cha;
            case "ChaHornets2":
                return R.drawable.cha;
            case "ChaHornets3":
                return R.drawable.cha;
            case "ChaHornets4":
                return R.drawable.cha;
            case "ChaHornets5":
                return R.drawable.cha;
            case "ChaHornets6":
                return R.drawable.cha;
            case "NBA":
                return R.drawable.nba;
            case "West":
                return R.drawable.west;
            case "East":
                return R.drawable.east;
        }
        return -1;
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

    // Should match pref_teams_list_values in strings.xml
    public static String getSubredditFromAbbr(String abbr) {
        switch (abbr) {
            case "atl":
                return Constants.SUB_ATL;
            case "bkn":
                return Constants.SUB_BKN;
            case "bos":
                return Constants.SUB_BOS;
            case "cha":
                return Constants.SUB_CHA;
            case "chi":
                return Constants.SUB_CHI;
            case "cle":
                return Constants.SUB_CLE;
            case "dal":
                return Constants.SUB_DAL;
            case "den":
                return Constants.SUB_DEN;
            case "det":
                return Constants.SUB_DET;
            case "gsw":
                return Constants.SUB_GSW;
            case "hou":
                return Constants.SUB_HOU;
            case "ind":
                return Constants.SUB_IND;
            case "lac":
                return Constants.SUB_LAC;
            case "lal":
                return Constants.SUB_LAL;
            case "mem":
                return Constants.SUB_MEM;
            case "mia":
                return Constants.SUB_MIA;
            case "mil":
                return Constants.SUB_MIL;
            case "min":
                return Constants.SUB_MIN;
            case "nop":
                return Constants.SUB_NOP;
            case "nyk":
                return Constants.SUB_NYK;
            case "okc":
                return Constants.SUB_OKC;
            case "orl":
                return Constants.SUB_ORL;
            case "phi":
                return Constants.SUB_PHI;
            case "phx":
                return Constants.SUB_PHO;
            case "por":
                return Constants.SUB_POR;
            case "sac":
                return Constants.SUB_SAC;
            case "sas":
                return Constants.SUB_SAS;
            case "tor":
                return Constants.SUB_TOR;
            case "uta":
                return Constants.SUB_UTA;
            case "was":
                return Constants.SUB_WAS;
            default:
                throw new IllegalStateException("Invalid abbreviation for favorite team: " + abbr);
        }
    }
}
