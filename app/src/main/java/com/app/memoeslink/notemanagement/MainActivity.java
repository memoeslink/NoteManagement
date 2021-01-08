package com.app.memoeslink.notemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.Locale;

public class MainActivity extends BaseActivity {
    boolean loggingOff = false;
    DatabaseConnection db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Open database connection
        db = new DatabaseConnection(this);

        //Set settings if not exist
        if (!preferences.contains("preference_filterNotes"))
            editor.putBoolean("preference_filterNotes", false).commit();

        if (!settings.contains("setting_compactView"))
            settings.edit().putBoolean("setting_compactView", false).commit();

        if (!settings.contains("setting_language"))
            settings.edit().putString("setting_language", Locale.getDefault().getLanguage()).commit();

        //Delete temporary preferences
        if (preferences.contains("temp_eventIsVisible"))
            editor.remove("temp_eventIsVisible").commit();

        //Retrieve Intent extras
        Intent intent = getIntent();

        if (intent.hasExtra("isLoggingOff"))
            loggingOff = intent.getBooleanExtra("isLoggingOff", false);

        //Remove temporary preferences
        if (preferences.contains("temp_currentScreen"))
            editor.remove("temp_currentScreen").commit();

        if (loggingOff) {
            Intent i = new Intent(MainActivity.this, LoginScreen.class);
            startActivity(i);
            finish();
        } else {
            new Handler().postDelayed(() -> {
                Intent i;

                if (db.countUsers() <= 0L) {
                    Methods.deleteDirectory(MainActivity.this);
                    i = new Intent(MainActivity.this, AccountScreen.class);
                    i.putExtra("comesFrom", getClassName());
                    i.putExtra("notASingleUser", true);
                } else {
                    if (preferences.contains("profile_username"))
                        i = new Intent(MainActivity.this, HomeScreen.class);
                    else
                        i = new Intent(MainActivity.this, LoginScreen.class);
                }
                startActivity(i);
                finish();
            }, 2000);
        }
    }

    public static String getClassName() {
        return MainActivity.class.getSimpleName();
    }
}
