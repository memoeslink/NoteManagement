package com.app.memoeslink.notemanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeScreen extends BaseActivity {
    LinearLayout profileLayout;
    LinearLayout selectorLayout;
    LinearLayout noteCreationLayout;
    LinearLayout noteFilterLayout;
    RelativeLayout notesLayout;
    RelativeLayout messageLayout;
    View separator;
    ImageView imageView;
    ImageView noteCreationImageView;
    ImageView noteFilterImageView;
    TextView userInfo;
    Bitmap image;
    boolean recentlyCreated = true;
    boolean listenerDisabled = false;
    boolean admin;
    long userId;
    List<Note> notes;
    AlertDialog alertDialog;
    DatabaseConnection db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        profileLayout = findViewById(R.id.home_profileLayout);
        separator = findViewById(R.id.home_separator);
        selectorLayout = findViewById(R.id.home_selectorLayout);
        noteCreationLayout = findViewById(R.id.home_noteCreationLayout);
        noteFilterLayout = findViewById(R.id.home_noteFilterLayout);
        notesLayout = findViewById(R.id.home_notesLayout);
        messageLayout = findViewById(R.id.home_messageLayout);
        imageView = findViewById(R.id.home_profileImageView);
        noteCreationImageView = findViewById(R.id.home_noteCreationImageView);
        noteFilterImageView = findViewById(R.id.home_noteFilterImageView);
        userInfo = findViewById(R.id.home_profileTextView);

        //Open database connection
        db = new DatabaseConnection(this);

        //Get profile information and fill views
        fillProfile();

        //Set image
        if (preferences.getBoolean("preference_filterNotes", false))
            noteFilterImageView.setImageResource(R.drawable.ic_individual);
        else
            noteFilterImageView.setImageResource(R.drawable.ic_group);

        //Retrieve notes from database
        getNotes();

        //Set listeners
        selectorLayout.setOnClickListener(v -> {
            Intent i = new Intent(HomeScreen.this, AccountScreen.class);
            i.putExtra("comesFrom", getClassName());
            startActivity(i);
            finish();
        });

        noteCreationLayout.setOnClickListener(view -> {
            Intent i = new Intent(HomeScreen.this, NoteScreen.class);
            i.putExtra("isInManagementMode", false);
            i.putExtra("targetUsername", preferences.getString("profile_username", ""));
            startActivity(i);
        });

        noteFilterLayout.setOnClickListener(view -> {
            if (preferences.getBoolean("preference_filterNotes", false)) {
                noteFilterImageView.setImageResource(R.drawable.ic_group);
                editor.putBoolean("preference_filterNotes", false).commit();
            } else {
                noteFilterImageView.setImageResource(R.drawable.ic_individual);
                editor.putBoolean("preference_filterNotes", true).commit();
            }
            getNotes();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        editor.putString("temp_currentScreen", getClassName()).commit();

        if (!recentlyCreated) {
            fillProfile();
            getNotes();
        } else recentlyCreated = false;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_exit_app)
                .setMessage(R.string.alert_exit_app_description)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_exit, (arg0, arg1) -> {
                    HomeScreen.super.onBackPressed();
                    System.exit(0);
                }).create().show();
    }

    public void fillProfile() {
        if (preferences.contains("profile_image")) {
            int dominantColor;

            if (preferences.getString("profile_image", null) != null) {
                try {
                    image = Methods.getByteArrayAsBitmap(Methods.getStringAsByteArray(preferences.getString("profile_image", null)));
                    imageView.setImageBitmap(image);
                    dominantColor = Methods.getDominantColor(image);
                    profileLayout.setBackgroundColor(ColorUtils.setAlphaComponent(dominantColor, 100));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    imageView.setImageResource(R.drawable.ic_anonymous);
                    profileLayout.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(getBaseContext(), R.color.header_color), 100));
                    dominantColor = ContextCompat.getColor(this, R.color.header_color);
                }
            } else {
                imageView.setImageResource(R.drawable.ic_anonymous);
                profileLayout.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(getBaseContext(), R.color.header_color), 100));
                dominantColor = ContextCompat.getColor(this, R.color.header_color);
            }
            separator.setBackgroundColor(Methods.getContrastColor(dominantColor, Color.TRANSPARENT, Color.WHITE));
        }

        if (preferences.contains("profile_username"))
            userInfo.setText(preferences.getString("profile_username", "?"));

        if (preferences.contains("profile_id"))
            userId = preferences.getLong("profile_id", -1);

        if (preferences.contains("profile_isAdmin"))
            admin = preferences.getBoolean("profile_isAdmin", false);
        else
            admin = false;
    }

    public void getNotes() {
        noteFilterLayout.setEnabled(false);
        noteFilterLayout.setClickable(false);

        //Set grayscale to ImageView
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        noteFilterImageView.setColorFilter(cf);

        if (db.countNotes() > 0L) {
            //Remove all views
            notesLayout.removeAllViews();

            //Get notes
            if (userId == 1)
                notes = db.selectNote();
            else if (admin) {
                notes = db.selectNote(userId, 1L);
            } else
                notes = db.selectNote(userId, 2L);

            //Filter notes
            if (preferences.getBoolean("preference_filterNotes", false)) {
                List<Note> filteredNotes = new ArrayList<>();
                String currentUsername = preferences.getString("profile_username", "");

                for (int n = 0; n < notes.size(); n++) {
                    if (notes.get(n).getUsername().equals(currentUsername))
                        filteredNotes.add(notes.get(n));
                }
                notes = filteredNotes;
            }

            //Define ListView
            ArrayAdapter<Note> adapter = new CustomListAdapter(this, R.layout.custom_list, notes);
            ListView noteListView = new ListView(this);
            noteListView.setAdapter(adapter);
            noteListView.setVerticalFadingEdgeEnabled(true);
            noteListView.setFadingEdgeLength(10);
            noteListView.setScrollbarFadingEnabled(false);

            //Define onClickListener
            noteListView.setOnItemClickListener((parent, view, position, id) -> {
                if (!listenerDisabled) {
                    listenerDisabled = true;
                    long idNote = notes.get(position).getId();
                    String description;
                    Bitmap noteImage;

                    if (!settings.getBoolean("setting_compactView", false)) {
                        description = notes.get(position).getDescription().trim().isEmpty() || notes.get(position).getDescription() == null ? "<[[void\\]>" : notes.get(position).getDescription();

                        Intent i = new Intent(getBaseContext(), NoteViewScreen.class);
                        Gson gson = new Gson();
                        String json = gson.toJson(notes.get(position)); //Convert object to JSON

                        //Try to retrieve image from the note
                        try {
                            noteImage = Methods.getByteArrayAsBitmap(db.selectNote(idNote).get(0).getImage());
                        } catch (Exception e) {
                            noteImage = null;
                        }
                        i.putExtra("note", json);
                        i.putExtra("description", description);
                        i.putExtra("noteImage", noteImage);
                        startActivity(i);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                listenerDisabled = false;
                            }
                        }, 1000);
                    } else {
                        description = notes.get(position).getDescription().trim().isEmpty() || notes.get(position).getDescription() == null ? "<font color=\"#ABA9E3\"><i>" + getString(R.string.text_unavailable_description) + "</i></font>" : notes.get(position).getDescription();

                        //Define AlertDialog
                        LayoutInflater layoutInflater = LayoutInflater.from(HomeScreen.this);
                        View v = layoutInflater.inflate(R.layout.alert_note, null);
                        v.setScrollbarFadingEnabled(false);

                        TextView textview = v.findViewById(R.id.alert_noteTextView);
                        textview.setText(Methods.fromHtml(getString(R.string.note_body, notes.get(position).getTitle(), "<font color=\"#8C8ACC\"><b>" + notes.get(position).getUsername() + "</b></font> " + Methods.generateIdeogram(notes.get(position).getUsername()), notes.get(position).getDate(), description)));
                        ImageView imageView = v.findViewById(R.id.alert_noteImageView);

                        //Try to retrieve image from the note
                        try {
                            noteImage = Methods.getByteArrayAsBitmap(db.selectNote(idNote).get(0).getImage());

                            if (noteImage == null)
                                imageView.setImageResource(R.drawable.ic_missing_image);
                            else
                                imageView.setImageBitmap(noteImage);
                        } catch (Exception e) {
                            imageView.setImageResource(R.drawable.ic_missing_image);
                        }

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeScreen.this);
                        alertDialogBuilder.setTitle(getString(R.string.alert_note) + " (ID: " + idNote + ")");
                        alertDialogBuilder.setView(v);
                        alertDialogBuilder.setPositiveButton(R.string.action_manage, (dialogInterface, id1) -> {
                            Intent i = new Intent(getBaseContext(), NoteScreen.class);
                            i.putExtra("isInManagementMode", true);
                            i.putExtra("targetUsername", notes.get(position).getUsername());
                            i.putExtra("noteIdentifier", notes.get(position).getId());
                            startActivity(i);
                        });
                        alertDialogBuilder.setOnDismissListener(dialog -> listenerDisabled = false);
                        alertDialogBuilder.setNeutralButton(R.string.text_ok, null);
                        alertDialog = alertDialogBuilder.create();

                        if (!alertDialog.isShowing())
                            alertDialog.show();
                    }
                }
            });

            //Add ListView to layout
            notesLayout.addView(noteListView);

            if (notes.size() > 0) {
                messageLayout.setVisibility(View.GONE);
                notesLayout.setVisibility(View.VISIBLE);
            } else {
                notesLayout.setVisibility(View.GONE);
                messageLayout.setVisibility(View.VISIBLE);
            }
            noteFilterImageView.setColorFilter(null);
            noteFilterLayout.setEnabled(true);
            noteFilterLayout.setClickable(true);
        } else {
            notesLayout.setVisibility(View.GONE);
            messageLayout.setVisibility(View.VISIBLE);
        }
    }

    public static String getClassName() {
        return HomeScreen.class.getSimpleName();
    }
}
