package com.app.memoeslink.notemanagement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.TextAlignment;
import com.google.gson.Gson;

public class NoteViewScreen extends BaseActivity {
    Button manageButton;
    Button backButton;
    ImageView image;
    TextView title;
    DocumentView content;
    String description;
    Note note;
    Bitmap noteImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);
        manageButton = findViewById(R.id.noteView_manageButton);
        backButton = findViewById(R.id.noteView_backButton);
        image = findViewById(R.id.noteView_noteImage);
        title = findViewById(R.id.noteView_title);
        content = findViewById(R.id.noteView_content);
        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.85f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        //Retrieve Intent extras
        Intent intent = getIntent();

        if (intent.hasExtra("note")) {
            Gson gson = new Gson(); //Retrieve JSON and convert it to object
            note = gson.fromJson(intent.getStringExtra("note"), Note.class);
        }

        if (intent.hasExtra("description"))
            description = intent.getStringExtra("description");

        if (intent.hasExtra("noteImage"))
            noteImage = intent.getParcelableExtra("noteImage");

        //Set note and note image
        if (noteImage == null)
            image.setImageResource(R.drawable.ic_missing_image);
        else
            image.setImageBitmap(noteImage);
        title.setText(Methods.fromHtml(getString(R.string.note_header, note.getTitle(), "<font color=\"#8C8ACC\"><b>" + note.getUsername() + "</b></font> " + Methods.generateIdeogram(note.getUsername()), note.getDate())));

        if (description.equals("<[[void\\]>")) {
            content.setText(getString(R.string.text_unavailable_description));
            content.getDocumentLayoutParams().setTextAlignment(TextAlignment.CENTER);
        } else
            content.setText(description);

        //Set listeners
        manageButton.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), NoteScreen.class);
            i.putExtra("isInManagementMode", true);
            i.putExtra("targetUsername", note.getUsername());
            i.putExtra("noteIdentifier", note.getId());
            startActivity(i);
            finish();
        });

        backButton.setOnClickListener(v -> finish());
    }
}
