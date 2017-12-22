package com.gmail.jorgegilcavazos.ballislife.features.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.gopremium.GoPremiumActivity;
import com.gmail.jorgegilcavazos.ballislife.features.login.LoginActivity;
import com.gmail.jorgegilcavazos.ballislife.features.main.MainActivity;
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.TeamName;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.observers.DisposableCompletableObserver;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    // Should match string values in strings.xml
    public static final String KEY_PREF_CGA_TOPICS = "pref_cga_topics";
    public static final String KEY_PREF_START_TOPICS = "pref_start_topics";
    public static final String KEY_ENABLE_ALERTS = "pref_enable_alerts";
    public static final String KEY_STARTUP_FRAGMENT = "key_startup_fragment";
    public static final String KEY_YOUTUBE_IN_APP = "in_app_youtube";
    public static final String KEY_OPEN_BOX_SCORE_DEFAULT = "open_box_score_default";
    public static final String KEY_NO_SPOILERS_MODE = "no_spoilers_mode";
    public static final String KEY_DARK_THEME = "dark_theme";
    public static final String KEY_CHIPS_FOR_RNBA_ORIGINALS = "chips_for_rnba_originals";
    public static final String KEY_TRIPLE_DOUBLES = "triple_double_alert";
    public static final String KEY_QUADRUPLE_DOUBLES = "quadruple_double_alert";
    public static final String KEY_5_X_5 = "five_x_five_alert";
    public static final String KEY_APP_VERSION = "app_version";
    public static final String KEY_FEEDBACK = "feedback";
    public static final String STARTUP_FRAGMENT_GAMES = "0";
    public static final String STARTUP_FRAGMENT_RNBA = "1";
    public static final String STARTUP_FRAGMENT_HIGHLIGHTS = "2";

    @Inject RedditAuthentication redditAuthentication;
    @Inject BaseSchedulerProvider schedulerProvider;
    @Inject LocalRepository localRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BallIsLifeApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            pickPreferenceObject(getPreferenceScreen().getPreference(i));
        }

        // Set AppVersion number to Preference view
        Preference appVersion = findPreference(KEY_APP_VERSION);
        try {
            appVersion.setSummary(appVersion());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Feedback button listener to show dialog
        Preference feedback = findPreference(KEY_FEEDBACK);
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                feedbackDialog();
                return true;
            }
        });

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
        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
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
            case KEY_NO_SPOILERS_MODE:
                SwitchPreference noSpoilers = (SwitchPreference) preference;
                if (!settingsActivity.isPremium() && noSpoilers.isChecked()) {
                    noSpoilers.setChecked(false);
                    Intent intent = new Intent(getActivity(), GoPremiumActivity.class);
                    startActivity(intent);
                }
                break;
            case KEY_DARK_THEME:
                SwitchPreference darkTheme = (SwitchPreference) preference;
                if (darkTheme.isChecked()) {
                    localRepository.saveAppTheme(SwishTheme.DARK);
                } else {
                    localRepository.saveAppTheme(SwishTheme.LIGHT);
                }
                restartApp();
                break;
            case KEY_TRIPLE_DOUBLES:
                SwitchPreference tripDouble = (SwitchPreference) preference;
                if (!settingsActivity.isPremium() && tripDouble.isChecked()) {
                    tripDouble.setChecked(false);
                    Intent intent = new Intent(getActivity(), GoPremiumActivity.class);
                    startActivity(intent);
                    break;
                }
                if (tripDouble.isChecked()) {
                    FirebaseMessaging.getInstance()
                            .subscribeToTopic(Constants.TOPIC_TRIPLE_DOUBLES);
                } else {
                    FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic(Constants.TOPIC_TRIPLE_DOUBLES);
                }
                break;
            case KEY_QUADRUPLE_DOUBLES:
                SwitchPreference quadDoubles = (SwitchPreference) preference;
                if (!settingsActivity.isPremium() && quadDoubles.isChecked()) {
                    quadDoubles.setChecked(false);
                    Intent intent = new Intent(getActivity(), GoPremiumActivity.class);
                    startActivity(intent);
                    break;
                }
                if (quadDoubles.isChecked()) {
                    FirebaseMessaging.getInstance()
                            .subscribeToTopic(Constants.TOPIC_QUADRUPLE_DOUBLES);
                } else {
                    FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic(Constants.TOPIC_QUADRUPLE_DOUBLES);
                }
                break;
            case KEY_5_X_5:
                SwitchPreference fiveXFive = (SwitchPreference) preference;
                if (!settingsActivity.isPremium() && fiveXFive.isChecked()) {
                    fiveXFive.setChecked(false);
                    Intent intent = new Intent(getActivity(), GoPremiumActivity.class);
                    startActivity(intent);
                    break;
                }
                if (fiveXFive.isChecked()) {
                    FirebaseMessaging.getInstance()
                            .subscribeToTopic(Constants.TOPIC_5_X_5);
                } else {
                    FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic(Constants.TOPIC_5_X_5);
                }
                break;
        }
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 12345,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pendingIntent);
        }
        getActivity().finishAffinity();
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
        if (preference.getKey().equals(KEY_DARK_THEME) && preference instanceof SwitchPreference) {
            SwitchPreference darkTheme = (SwitchPreference) preference;
            if (localRepository.getAppTheme() == SwishTheme.DARK) {
                darkTheme.setChecked(true);
            } else {
                darkTheme.setChecked(false);
            }
        }
    }

    private void initLogInStatusText() {
        Preference logInStatusPref = findPreference("log_in_status_pref");
        if (localRepository.getUsername() != null) {
            logInStatusPref.setTitle(R.string.log_out);
            logInStatusPref.setSummary(String.format(getString(R.string.logged_as_user), localRepository.getUsername()));

        } else {
            logInStatusPref.setTitle(R.string.log_in);
            logInStatusPref.setSummary(R.string.click_login);
        }
    }

    private void initListeners() {
        Preference logInStatusPref = findPreference("log_in_status_pref");
        logInStatusPref.setOnPreferenceClickListener(preference -> {
            if (localRepository.getUsername() != null) {
                localRepository.saveUsername(null);
                redditAuthentication.deAuthenticateUser().andThen(redditAuthentication
                        .authenticate()).subscribeOn(schedulerProvider.io()).observeOn
                        (schedulerProvider.ui()).subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        initLogInStatusText();
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
        });
    }

    public String appVersion() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        String version = pInfo.versionName;
        return version;
    }

    public void feedbackDialog() {
        MaterialDialog feedbackDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.report_bug_or_suggest)
                .customView(R.layout.feedback_form_layout, false)
                .positiveText(R.string.send)
                .build();

        feedbackDialog.show();
    }
}