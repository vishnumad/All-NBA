package com.gmail.jorgegilcavazos.ballislife.features.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.login.LoginActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.TeamName;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.observers.DisposableCompletableObserver;

import static android.content.Context.MODE_PRIVATE;
import static com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl
        .REDDIT_AUTH_PREFS;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    // Should match string values in strings.xml
    public static final String KEY_PREF_CGA_TOPICS = "pref_cga_topics";
    public static final String KEY_PREF_START_TOPICS = "pref_start_topics";
    public static final String KEY_ENABLE_ALERTS = "pref_enable_alerts";
    public static final String KEY_STARTUP_FRAGMENT = "key_startup_fragment";
    public static final String STARTUP_FRAGMENT_GAMES = "0";
    public static final String STARTUP_FRAGMENT_RNBA = "1";
    public static final String STARTUP_FRAGMENT_HIGHLIGHTS = "2";
    private static final String TAG = "SettingsFragment";

    @Inject
    RedditAuthentication redditAuthentication;

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BallIsLifeApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            pickPreferenceObject(getPreferenceScreen().getPreference(i));
        }

        initListeners();
    }

    private void pickPreferenceObject(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            PreferenceCategory category = (PreferenceCategory) preference;
            for (int i = 0; i < category.getPreferenceCount(); i++) {
                pickPreferenceObject(category.getPreference(i));
            }
        } else {
            initSummary(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        switch (key) {
            case "teams_list":
                String abbrev = sharedPreferences.getString(key, null);
                preference.setSummary(getTeamName(abbrev));
                break;
            case KEY_STARTUP_FRAGMENT:
                String selectedStartup = sharedPreferences.getString(key, null);
                preference.setSummary(getStartupFragmentTextRes(selectedStartup));
                break;
            case "log_out_pref":
                preference.setTitle("Log in");
                break;
            case KEY_PREF_CGA_TOPICS:
                Set<String> newCgaTopics = sharedPreferences.getStringSet(key, null);
                String[] availableGameTopics = getResources()
                        .getStringArray(R.array.pref_cga_values);

                updateTopicSubscriptions(newCgaTopics, availableGameTopics);
                break;
            case KEY_PREF_START_TOPICS:
                Set<String> newStartTopics = sharedPreferences.getStringSet(key, null);
                String[] availableStartTopics = getResources()
                        .getStringArray(R.array.pref_start_values);

                updateTopicSubscriptions(newStartTopics, availableStartTopics);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        initLogInStatusText();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateTopicSubscriptions(Set<String> newTopics, String[] availableTopics) {
        if (newTopics != null) {
            for (String availableTopic : availableTopics) {
                if (newTopics.contains(availableTopic)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(availableTopic);
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(availableTopic);
                }
            }
        }
    }

    private String getTeamName(String abbreviation) {
        if (abbreviation != null) {
            if (abbreviation.equals("noteam")) {
                return "No team selected";
            }

            abbreviation = abbreviation.toUpperCase();

            if (Constants.NBA_MATERIAL_ENABLED) {
                for (TeamName teamName : TeamName.values()) {
                    if (teamName.toString().equals(abbreviation)) {
                        return teamName.getTeamName();
                    }
                }
            } else {
                return abbreviation;
            }
        }
        return "No team selected";
    }

    private int getStartupFragmentTextRes(String selectedValue) {
        switch (selectedValue) {
            case STARTUP_FRAGMENT_GAMES:
                return R.string.games_fragment_title;
            case STARTUP_FRAGMENT_RNBA:
                return R.string.reddit_nba_fragment_title;
            case STARTUP_FRAGMENT_HIGHLIGHTS:
                return R.string.highlights_fragment_title;
            default:
                throw new IllegalStateException("Invalid fragment startup value: " + selectedValue);
        }
    }

    private void initSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            if (listPreference.getKey().equals("teams_list")) {
                preference.setSummary(getTeamName(listPreference.getValue()));
            } else if (listPreference.getKey().equals(KEY_STARTUP_FRAGMENT)) {
                preference.setSummary(getStartupFragmentTextRes(listPreference.getValue()));
            }
        }
    }

    private void initLogInStatusText() {
        Preference logInStatusPref = findPreference("log_in_status_pref");
        if (redditAuthentication.isUserLoggedIn()) {
            logInStatusPref.setTitle(R.string.log_out);
            logInStatusPref.setSummary(String.format(getString(R.string.logged_as_user),
                    redditAuthentication.getRedditClient().getAuthenticatedUser()));

        } else {
            logInStatusPref.setTitle(R.string.log_in);
            logInStatusPref.setSummary(R.string.click_login);
        }
    }

    private void initListeners() {
        final SharedPreferences redditPrefs = getActivity()
                .getSharedPreferences(REDDIT_AUTH_PREFS, MODE_PRIVATE);

        Preference logInStatusPref = findPreference("log_in_status_pref");
        logInStatusPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (redditAuthentication.isUserLoggedIn()) {
                    redditAuthentication.deAuthenticateUser(redditPrefs)
                            .andThen(redditAuthentication.authenticate(redditPrefs))
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribeWith(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    initLogInStatusText();
                                    // TODO check view is attached
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });
                } else {
                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(loginIntent);
                }
                return false;
            }
        });
    }
}