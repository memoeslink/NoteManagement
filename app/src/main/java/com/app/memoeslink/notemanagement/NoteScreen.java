package com.app.memoeslink.notemanagement;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.File;
import java.util.List;

public class NoteScreen extends BaseActivity {
    LinearLayout creationLayout;
    LinearLayout managementLayout;
    EditText titleInput;
    EditText descriptionInput;
    ImageButton imageButton;
    Button createButton;
    Button deleteButton;
    Button updateButton;
    Button backButton;
    Button otherBackButton;
    ImageView titleImage;
    ImageView imageView;
    TextView title;
    TextView imageRemover;
    CheckBox privacyCheckBox;
    ImagePicker imagePicker;
    ImagePickerCallback imagePickerCallback;
    Bitmap image = null;
    boolean inManagementMode = false;
    boolean targetAuthorized = true;
    boolean isButtonEnablingAllowed = false;
    boolean validTitle = false;
    Long id = 0L;
    Long idNote = 0L;
    String targetUsername = "";
    String dateTime = "";
    DatabaseConnection db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        creationLayout = findViewById(R.id.note_creationLayout);
        managementLayout = findViewById(R.id.note_managementLayout);
        titleInput = findViewById(R.id.note_titleEditText);
        descriptionInput = findViewById(R.id.note_descriptionEditText);
        imageButton = findViewById(R.id.note_imagePickerButton);
        createButton = findViewById(R.id.note_createButton);
        deleteButton = findViewById(R.id.note_deleteButton);
        updateButton = findViewById(R.id.note_updateButton);
        backButton = findViewById(R.id.note_backButton);
        otherBackButton = findViewById(R.id.note_otherBackButton);
        titleImage = findViewById(R.id.note_titleImageView);
        imageView = findViewById(R.id.note_imageView);
        title = findViewById(R.id.note_title);
        imageRemover = findViewById(R.id.note_removeImageOption);
        imageRemover.setTextColor(ContextCompat.getColor(this, R.color.url_color));
        imageRemover.setPaintFlags(imageRemover.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privacyCheckBox = findViewById(R.id.note_privacyCheckbox);

        //Open database connection
        db = new DatabaseConnection(this);

        //Retrieve Intent extras
        Intent intent = getIntent();

        if (intent.hasExtra("isInManagementMode"))
            inManagementMode = intent.getBooleanExtra("isInManagementMode", false);

        if (intent.hasExtra("targetUsername")) {
            targetUsername = intent.getStringExtra("targetUsername");
            List<User> users = db.selectUser(targetUsername);

            if (users.size() > 0) {
                id = users.get(0).getId();
                targetAuthorized = users.get(0).isAdmin();
            }
        }

        if (intent.hasExtra("noteIdentifier"))
            idNote = intent.getLongExtra("noteIdentifier", 0L);

        //Enable or set views
        if (inManagementMode) {
            titleImage.setImageResource(R.drawable.ic_manage_note);
            title.setText(R.string.note_edition_title);

            if (preferences.getLong("profile_id", 0L) != 1L) {
                if (preferences.getLong("profile_id", 0L) == id) {
                    deleteButton.setEnabled(true);
                    isButtonEnablingAllowed = true;
                } else {
                    if (preferences.getBoolean("profile_isAdmin", false) || targetAuthorized || (!preferences.getBoolean("profile_isAdmin", false) && preferences.getLong("profile_id", 0L) != id)) {
                        titleInput.setEnabled(false);
                        descriptionInput.setEnabled(false);
                        imageButton.setEnabled(false);
                        privacyCheckBox.setEnabled(false);
                        isButtonEnablingAllowed = false;
                    } else {
                        deleteButton.setEnabled(true);
                        isButtonEnablingAllowed = true;
                    }
                }
            } else {
                deleteButton.setEnabled(true);
                isButtonEnablingAllowed = true;
            }
            creationLayout.setVisibility(View.GONE);
            managementLayout.setVisibility(View.VISIBLE);
        } else
            isButtonEnablingAllowed = true;

        //Declare ImagePicker
        imagePicker = new ImagePicker(NoteScreen.this);

        imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                String path = images.get(0).getOriginalPath();

                if (Methods.checkIfFileExists(path)) {
                    try {
                        File file = new File(path);
                        long length = file.length() / (1024 * 1024);

                        if (length < 1) {
                            image = BitmapFactory.decodeFile(path);
                            imageView.setImageBitmap(image);
                            imageRemover.setVisibility(View.VISIBLE);
                        } else {
                            Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_image_size_error), true, false, Status.ERROR, getString(R.string.status_error));
                            clearImage();
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_image_size_error), true, false, Status.ERROR, getString(R.string.status_error));
                        clearImage();
                    }
                }
            }

            @Override
            public void onError(String message) {
                // Do error handling
            }
        };
        imagePicker.setImagePickerCallback(imagePickerCallback);

        //Set listeners
        titleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validTitle = !s.toString().trim().isEmpty();
                titleInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_asterisk, 0, validTitle ? R.drawable.ic_tick_mark : R.drawable.ic_question, 0);

                if (isButtonEnablingAllowed) {
                    if (validTitle) {
                        createButton.setEnabled(true);
                        updateButton.setEnabled(true);
                    } else {
                        createButton.setEnabled(false);
                        updateButton.setEnabled(false);
                    }
                }
            }
        });

        titleInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (titleInput.getRight() - titleInput.getCompoundDrawables()[DrawablePosition.RIGHT.getValue()].getBounds().width())) {
                    Methods.showToast(NoteScreen.this, getString(R.string.field_title), true, true);
                    return true;
                }
            }
            return false;
        });

        imageButton.setOnClickListener(v -> {
            if (isImageSelectionAllowed())
                imagePicker.pickImage();
        });

        createButton.setOnClickListener(v -> saveNote(false));

        updateButton.setOnClickListener(v -> saveNote(true));

        deleteButton.setOnClickListener(v -> deleteNote(idNote));

        backButton.setOnClickListener(v -> finish());

        otherBackButton.setOnClickListener(v -> finish());

        imageRemover.setOnClickListener(v -> clearImage());

        //Set fields data
        setNoteData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                if (imagePicker == null) {
                    imagePicker = new ImagePicker(NoteScreen.this);
                    imagePicker.setImagePickerCallback(imagePickerCallback);
                }
                imagePicker.submit(data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void saveNote(boolean isUpdating) {
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        boolean proceed = false;

        if (isUpdating) {
            if (db.updateNote(idNote, title.trim(), dateTime, description.trim(), image, privacyCheckBox.isChecked())) {
                Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_note_updated), true, false, Status.SUCCESS, getString(R.string.status_success));
                proceed = true;
            } else
                Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_note_not_updated), true, false, Status.ERROR, getString(R.string.status_error));
        } else {
            if (db.insertNote(title.trim(), description.trim(), id, image, privacyCheckBox.isChecked())) {
                Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_note_saved), true, false, Status.SUCCESS, getString(R.string.status_success));
                proceed = true;
            } else
                Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_note_not_saved), true, false, Status.ERROR, getString(R.string.status_error));
        }

        if (proceed)
            finish();
    }

    public void deleteNote(Long id) {
        if (db.deleteNote(id)) {
            Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_note_deleted), true, false, Status.ERROR, getString(R.string.status_error));
            finish();
        } else
            Methods.showSystemToast(NoteScreen.this, getString(R.string.toast_note_not_deleted), true, false, Status.ERROR, getString(R.string.status_error));
    }

    public void clearImage() {
        image = null;
        imageView.setImageDrawable(null);
        imageRemover.setVisibility(View.GONE);
    }

    public void setNoteData() {
        if (inManagementMode) {
            List<Note> notes = db.selectNote(idNote);

            if (notes.size() > 0) {
                titleInput.setText(notes.get(0).getTitle());
                descriptionInput.setText(notes.get(0).getDescription());
                dateTime = notes.get(0).getDate();
                image = Methods.getByteArrayAsBitmap(notes.get(0).getImage());
                imageView.setImageBitmap(image);

                if (image != null && (!targetAuthorized || preferences.getLong("profile_id", 0L) == 1L || preferences.getLong("profile_id", 0L) == id))
                    imageRemover.setVisibility(View.VISIBLE);
                privacyCheckBox.setChecked(notes.get(0).isPersonal());
            }
        }
    }
}
