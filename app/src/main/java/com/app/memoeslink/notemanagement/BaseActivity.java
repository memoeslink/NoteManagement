package com.app.memoeslink.notemanagement;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.kbeanie.multipicker.api.ImagePicker;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    AlertDialog alertDialog;
    ImagePicker imagePicker;
    SharedPreferences preferences;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(getString(R.string.app_preferences), Context.MODE_PRIVATE);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        //Define AlertDialog
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View v = layoutInflater.inflate(R.layout.alert_about, null);

        TextView textview = (TextView) v.findViewById(R.id.alert_aboutTextView);
        textview.setText(Methods.fromHtml(getString(R.string.about)));
        textview.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_about);
        alertDialogBuilder.setView(v);
        alertDialogBuilder.setNeutralButton(R.string.text_ok, null);
        alertDialog = alertDialogBuilder.create();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem homeOption = menu.findItem(R.id.menu_home);
        MenuItem managementOption = menu.findItem(R.id.menu_manageUsers);
        MenuItem logOffOption = menu.findItem(R.id.menu_logOff);
        homeOption.setVisible(false);
        managementOption.setVisible(false);
        logOffOption.setVisible(false);

        if (preferences.contains("profile_username")) {
            if (preferences.getString("profile_username", null) != null)
                logOffOption.setVisible(true);

            if (!preferences.getString("temp_currentScreen", "").equals(HomeScreen.getClassName()))
                homeOption.setVisible(true);

            if (preferences.getBoolean("profile_isAdmin", false) && !preferences.getString("temp_currentScreen", "").equals(ManagementScreen.getClassName()))
                managementOption.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        switch (item.getItemId()) {
            case R.id.menu_home:
                editor.putString("temp_currentScreen", HomeScreen.getClassName()).commit();
                i = new Intent(getApplicationContext(), HomeScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menu_manageUsers:
                editor.putString("temp_currentScreen", ManagementScreen.getClassName()).commit();
                i = new Intent(getApplicationContext(), ManagementScreen.class);
                startActivity(i);
                return true;
            case R.id.menu_settings:
                i = new Intent(getApplicationContext(), PreferenceScreen.class);
                startActivity(i);
                return true;
            case R.id.menu_logOff:
                Methods.logOff(this, true);
                return true;
            case R.id.menu_about:
                alertDialog.show();
                return true;
            case R.id.menu_exit:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        invalidateOptionsMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED && imagePicker != null)
            imagePicker.pickImage();
    }

    @Override
    protected void attachBaseContext(Context context) {
        context = wrap(context);
        super.attachBaseContext(context);
    }

    public ContextWrapper wrap(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String language = settings.getString("setting_language", Locale.getDefault().getLanguage());

        if (!language.equals(Locale.ENGLISH.getLanguage()) && !language.equals("es"))
            language = Locale.ENGLISH.getLanguage();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);
        } else {
            configuration.setLocale(locale);
            context = context.createConfigurationContext(configuration);
        }
        return new ContextWrapper(context);
    }

    public boolean isImageSelectionAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                return false;
            }
        }
        return true;
    }
}
