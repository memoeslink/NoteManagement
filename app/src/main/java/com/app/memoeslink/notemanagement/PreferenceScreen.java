package com.app.memoeslink.notemanagement;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.preference.PreferenceFragmentCompat;

public class PreferenceScreen extends BaseActivity {
    AlertDialog alertDialog;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        //Define AlertDialog
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alert_restart));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(getString(R.string.alert_restart_description));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.text_ok), (dialogInterface, i) -> {
            Methods.restartApplication(PreferenceScreen.this);
            dialogInterface.dismiss();
        });

        //Set listeners
        listener = (prefs, key) -> {
            if (key.equals("setting_language")) {
                alertDialog.show();
            }
        };
        settings.registerOnSharedPreferenceChangeListener(listener);
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }

    public void backMethod(View v) {
        finish();
    }
}