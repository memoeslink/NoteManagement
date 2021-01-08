package com.app.memoeslink.notemanagement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

public class EventScreen extends BaseActivity {
    protected SharedPreferences.OnSharedPreferenceChangeListener listener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.75f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        //Set listener to SharedPreferences
        listener = (prefs, key) -> {
            if (key.equals("temp_eventIsVisible")) finish();
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onBackPressed() {
    }
}
