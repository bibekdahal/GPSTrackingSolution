package com.frobi.gpstrackingsolution.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static MainActivity m_mainActivity;
    public static void SetMainActivity(MainActivity mainActivity) { m_mainActivity=mainActivity; }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        bindPreferenceSummaryToValue(findPreference("interval"));
        bindPreferenceSummaryToValue(findPreference("emergencyno1"));
        bindPreferenceSummaryToValue(findPreference("emergencyno2"));
        bindPreferenceSummaryToValue(findPreference("emergencyno3"));
        bindPreferenceSummaryToValue(findPreference("emergencyno4"));
    }

    private Preference.OnPreferenceChangeListener prefListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            preference.setSummary(stringValue);
            return true;
        }
    };

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("interval")) {
            m_mainActivity.RestartService();
        }
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(prefListener);
        prefListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    public static long GetLongSetting(String key, long defaultValue, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        long val;
        try {
            val = sharedPref.getLong(key, defaultValue);
        } catch(Exception e1) {
            String str = sharedPref.getString(key, Long.toString(defaultValue));
            try {
                val = Long.parseLong(str);
            } catch (Exception e) {
                val = 0;
            }
        }
        return val;
    }

    public static int GetIntSetting(String key, int defaultValue, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int val;
        try {
            val = sharedPref.getInt(key, defaultValue);
        } catch(Exception e1) {
            String str = sharedPref.getString(key, Integer.toString(defaultValue));
            try {
                val = Integer.parseInt(str);
            } catch (Exception e) {
                val = 0;
            }
        }
        return val;
    }

    public static boolean GetBoolSetting(String key, boolean defaultValue, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }
}
