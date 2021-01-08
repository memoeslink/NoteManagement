package com.app.memoeslink.notemanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.TouchDelegate;
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
import java.util.Arrays;
import java.util.List;

public class AccountScreen extends BaseActivity {
    LinearLayout creationLayout;
    LinearLayout deletionLayout;
    LinearLayout updateLayout;
    LinearLayout otherLayout;
    LinearLayout linkLayout;
    EditText usernameInput;
    EditText nameInput;
    EditText lastNameInput;
    EditText passwordInput;
    EditText repeatPasswordInput;
    EditText emailInput;
    CheckBox adminCheckBox;
    ImageButton imageButton;
    Button createButton;
    Button deleteButton;
    Button updateButton;
    Button backButton;
    Button otherCreateButton;
    Button otherBackButton;
    ImageView profileImage;
    TextView title;
    TextView imageRemover;
    TextView passwordMatchText;
    TextView link;
    ImagePickerCallback imagePickerCallback;
    Bitmap image = null;
    boolean inManagementMode = false;
    boolean targetAuthorized = false;
    boolean[] validFields = {false, false, false};
    String password = "";
    String repeatPassword = "";
    String comesFrom = "";
    Long id = 0L;
    DatabaseConnection db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        creationLayout = findViewById(R.id.account_creationLayout);
        deletionLayout = findViewById(R.id.account_deletionLayout);
        updateLayout = findViewById(R.id.account_updateLayout);
        otherLayout = findViewById(R.id.account_otherLayout);
        linkLayout = findViewById(R.id.account_linkLayout);
        usernameInput = findViewById(R.id.account_usernameEditText);
        nameInput = findViewById(R.id.account_nameEditText);
        lastNameInput = findViewById(R.id.account_lastNameEditText);
        passwordInput = findViewById(R.id.account_passwordEditText);
        passwordInput.setTypeface(Typeface.DEFAULT);
        repeatPasswordInput = findViewById(R.id.account_repeatPasswordEditText);
        repeatPasswordInput.setTypeface(Typeface.DEFAULT);
        emailInput = findViewById(R.id.account_emailEditText);
        adminCheckBox = findViewById(R.id.account_adminCheckBox);
        imageButton = findViewById(R.id.account_imagePickerButton);
        createButton = findViewById(R.id.account_createButton);
        deleteButton = findViewById(R.id.account_deleteButton);
        updateButton = findViewById(R.id.account_updateButton);
        backButton = findViewById(R.id.account_backButton);
        otherCreateButton = findViewById(R.id.account_otherCreateButton);
        otherBackButton = findViewById(R.id.account_otherBackButton);
        profileImage = findViewById(R.id.account_profileImageView);
        title = findViewById(R.id.account_title);
        imageRemover = findViewById(R.id.account_removeImageOption);
        imageRemover.setTextColor(ContextCompat.getColor(this, R.color.url_color));
        imageRemover.setPaintFlags(imageRemover.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        passwordMatchText = findViewById(R.id.account_passwordMatchText);
        link = findViewById(R.id.account_logIn);
        link.setTextColor(ContextCompat.getColor(this, R.color.url_color));
        link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //Open database connection
        db = new DatabaseConnection(this);

        //Retrieve Intent extras
        Intent intent = getIntent();

        if (intent.hasExtra("comesFrom"))
            comesFrom = intent.getStringExtra("comesFrom");

        if (intent.hasExtra("isInManagementMode"))
            inManagementMode = intent.getBooleanExtra("isInManagementMode", false);

        if (intent.hasExtra("identifier"))
            id = intent.getLongExtra("identifier", 0L);

        if (intent.hasExtra("isTargetAuthorized"))
            targetAuthorized = intent.getBooleanExtra("isTargetAuthorized", false);

        //Enable or set views
        if (comesFrom.equals(HomeScreen.getClassName())) {
            title.setText(R.string.account_other_title);
            usernameInput.setEnabled(false);
        }

        if (comesFrom.equals(ManagementScreen.getClassName()) && inManagementMode) {
            title.setText(R.string.account_management_title);

            if (preferences.getLong("profile_id", 0L) != 1L) {
                if (targetAuthorized && preferences.getLong("profile_id", 0L) != id) {
                    usernameInput.setEnabled(false);
                    nameInput.setEnabled(false);
                    lastNameInput.setEnabled(false);
                    passwordInput.setVisibility(View.GONE);
                    repeatPasswordInput.setVisibility(View.GONE);
                    emailInput.setEnabled(false);
                    adminCheckBox.setEnabled(false);
                    imageButton.setEnabled(false);
                } else {
                    if (preferences.getLong("profile_id", 0L) == id)
                        adminCheckBox.setEnabled(false);
                    deleteButton.setEnabled(true);
                }
            } else {
                if (preferences.getLong("profile_id", 0L) != id)
                    deleteButton.setEnabled(true);
                else
                    adminCheckBox.setEnabled(false);
            }
        }

        if (comesFrom.equals(HomeScreen.getClassName()) || intent.hasExtra("notASingleUser"))
            adminCheckBox.setEnabled(false);

        if (intent.hasExtra("notASingleUser"))
            adminCheckBox.setChecked(intent.getBooleanExtra("notASingleUser", false));

        //Change view visibility
        if (comesFrom.equals(LoginScreen.getClassName())) {
            adminCheckBox.setVisibility(View.GONE);
            linkLayout.setVisibility(View.VISIBLE);
        }

        if (comesFrom.equals(HomeScreen.getClassName()) || (comesFrom.equals(ManagementScreen.getClassName())) && inManagementMode) {
            creationLayout.setVisibility(View.GONE);
            deletionLayout.setVisibility(View.VISIBLE);
            updateLayout.setVisibility(View.VISIBLE);
        }

        if (comesFrom.equals(ManagementScreen.getClassName()) && !inManagementMode) {
            creationLayout.setVisibility(View.GONE);
            otherLayout.setVisibility(View.VISIBLE);
        }

        //Declare ImagePicker
        imagePicker = new ImagePicker(AccountScreen.this);

        imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> images) {
                String path = images.get(0).getOriginalPath();
                path = path.replace("external_files", Environment.getExternalStorageDirectory().toString());

                if (Methods.checkIfFileExists(path)) {
                    try {
                        File file = new File(path);
                        long length = file.length() / (1024 * 1024);

                        if (length < 1) {
                            image = BitmapFactory.decodeFile(path);
                            profileImage.setImageBitmap(image);
                            imageRemover.setVisibility(View.VISIBLE);
                        } else {
                            Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_image_size_error), true, false, Status.ERROR, getString(R.string.status_error));
                            clearImage();
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_image_size_error), true, false, Status.ERROR, getString(R.string.status_error));
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
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validFields[0] = Methods.checkIfMatchesRegex(s.toString(), Methods.USERNAME_REGEX);
                usernameInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_asterisk, 0, validFields[0] ? R.drawable.ic_tick_mark : R.drawable.ic_question, 0);
                createButtonEnabler();
            }
        });

        usernameInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (usernameInput.getRight() - usernameInput.getCompoundDrawables()[DrawablePosition.RIGHT.getValue()].getBounds().width())) {
                    Methods.showToast(AccountScreen.this, getString(R.string.field_username), true, true);
                    return true;
                }
            }
            return false;
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validFields[1] = Methods.checkIfMatchesRegex(s.toString(), Methods.PASSWORD_REGEX);
                password = s.toString();
                passwordInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_asterisk, 0, validFields[1] ? R.drawable.ic_tick_mark : R.drawable.ic_question, 0);
                createButtonEnabler();
            }
        });

        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordInput.getRight() - passwordInput.getCompoundDrawables()[DrawablePosition.RIGHT.getValue()].getBounds().width())) {
                    Methods.showToast(AccountScreen.this, getString(R.string.field_password), true, true);
                    return true;
                }
            }
            return false;
        });

        repeatPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validFields[2] = Methods.checkIfMatchesRegex(s.toString(), Methods.PASSWORD_REGEX);
                repeatPassword = s.toString();
                repeatPasswordInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_asterisk, 0, validFields[2] ? R.drawable.ic_tick_mark : R.drawable.ic_question, 0);
                createButtonEnabler();
            }
        });

        repeatPasswordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (repeatPasswordInput.getRight() - repeatPasswordInput.getCompoundDrawables()[DrawablePosition.RIGHT.getValue()].getBounds().width())) {
                    Methods.showToast(AccountScreen.this, getString(R.string.field_password), true, true);
                    return true;
                }
            }
            return false;
        });

        imageButton.setOnClickListener(v -> {
            if (isImageSelectionAllowed())
                imagePicker.pickImage();
        });

        createButton.setOnClickListener(v -> saveUser(false));

        otherCreateButton.setOnClickListener(v -> saveUser(false));

        updateButton.setOnClickListener(v -> saveUser(true));

        deleteButton.setOnClickListener(v -> {
            if (preferences.getLong("profile_id", 0L) == id) {
                new AlertDialog.Builder(AccountScreen.this)
                        .setTitle(R.string.alert_delete_account)
                        .setMessage(R.string.alert_delete_account_description)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_delete, (dialogInterface, i) -> {
                            if (deleteUser(id))
                                Methods.logOff(AccountScreen.this, false);
                        }).create().show();
            } else {
                new AlertDialog.Builder(AccountScreen.this)
                        .setTitle(R.string.alert_delete_account)
                        .setMessage(R.string.alert_delete_other_account_description)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_delete, (dialogInterface, i) -> deleteUser(id)).create().show();
            }
        });

        backButton.setOnClickListener(v -> {
            if (comesFrom.equals(HomeScreen.getClassName())) {
                Intent i = new Intent(AccountScreen.this, HomeScreen.class);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(AccountScreen.this, ManagementScreen.class);
                startActivity(i);
                finish();
            }
        });

        otherBackButton.setOnClickListener(v -> {
            Intent i = new Intent(AccountScreen.this, ManagementScreen.class);
            startActivity(i);
            finish();
        });

        imageRemover.setOnClickListener(v -> clearImage());

        link.setOnClickListener(v -> {
            Intent i = new Intent(AccountScreen.this, LoginScreen.class);
            startActivity(i);
            finish();
        });

        //Expand link view area
        final View linkArea = (View) link.getParent();

        linkArea.post(() -> {
            final Rect rect = new Rect();
            linkArea.getHitRect(rect);
            rect.left -= 20;
            rect.top -= 20;
            rect.right += 20;
            rect.bottom += 20;
            linkArea.setTouchDelegate(new TouchDelegate(rect, link));
        });

        //Set fields data
        setUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                if (imagePicker == null) {
                    imagePicker = new ImagePicker(AccountScreen.this);
                    imagePicker.setImagePickerCallback(imagePickerCallback);
                }
                imagePicker.submit(data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (comesFrom.equals(HomeScreen.getClassName())) {
            Intent i = new Intent(AccountScreen.this, HomeScreen.class);
            startActivity(i);
            finish();
        } else if (comesFrom.equals(ManagementScreen.getClassName())) {
            Intent i = new Intent(AccountScreen.this, ManagementScreen.class);
            startActivity(i);
            finish();
        } else
            super.onBackPressed();
    }

    public void createButtonEnabler() {
        if (Arrays.asList(validFields).contains(false)) {
            createButton.setEnabled(false);
            updateButton.setEnabled(false);
            otherCreateButton.setEnabled(false);
            passwordMatchText.setVisibility(View.GONE);
        } else if (password.equals(repeatPassword)) {
            createButton.setEnabled(true);
            updateButton.setEnabled(true);
            otherCreateButton.setEnabled(true);
            passwordMatchText.setText(R.string.account_passwords_match);
            passwordMatchText.setTextColor(ContextCompat.getColor(this, R.color.light_green));
            passwordMatchText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_void, 0, R.drawable.ic_tick_mark, 0);
            passwordMatchText.setVisibility(View.VISIBLE);
        } else {
            createButton.setEnabled(false);
            updateButton.setEnabled(false);
            otherCreateButton.setEnabled(false);
            passwordMatchText.setText(R.string.account_passwords_not_match);
            passwordMatchText.setTextColor(ContextCompat.getColor(this, R.color.light_red));
            passwordMatchText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_void, 0, R.drawable.ic_cross, 0);
            passwordMatchText.setVisibility(View.VISIBLE);
        }
    }

    public void saveUser(boolean isUpdating) {
        String username = usernameInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        boolean isAdmin = adminCheckBox.isChecked();
        boolean proceed = false;

        if (isUpdating) {
            if (db.verifyUser(username) && db.selectUser(username).get(0).getId() != id)
                Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_username_used), true, false, Status.ERROR, getString(R.string.status_error));
            else {
                if (db.updateUser(id, username, name, lastName, password, email, isAdmin, image)) {
                    List<User> users = db.selectUser(username);

                    if (comesFrom.equals(HomeScreen.getClassName()))
                        Methods.storeUserData(this, users);
                    else if (comesFrom.equals(ManagementScreen.getClassName()) && id == users.get(0).getId())
                        Methods.storeUserData(this, users);
                    Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_user_updated, username), true, false, Status.SUCCESS, getString(R.string.status_success));
                    proceed = true;
                } else
                    Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_user_not_updated), true, false, Status.ERROR, getString(R.string.status_error));
            }
        } else {
            if (db.countUsers() >= 100)
                Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_max_user_limit), true, false, Status.ERROR, getString(R.string.status_error));
            else {
                if (db.verifyUser(username))
                    Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_username_used), true, false, Status.ERROR, getString(R.string.status_error));
                else {
                    if (db.insertUser(username, name, lastName, password, email, isAdmin, image)) {
                        Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_user_saved), true, false, Status.SUCCESS, getString(R.string.status_success));
                        proceed = true;
                    } else
                        Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_user_not_saved), true, false, Status.ERROR, getString(R.string.status_error));
                }
            }
        }

        if (proceed)
            closeActivity();
    }

    public boolean deleteUser(Long id) {
        if (db.deleteUser(id)) {
            Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_user_deleted), true, false, Status.SUCCESS, getString(R.string.status_success));
            closeActivity();
            return true;
        } else {
            Methods.showSystemToast(AccountScreen.this, getString(R.string.toast_user_not_deleted), true, false, Status.ERROR, getString(R.string.status_error));
            return false;
        }
    }

    public void clearImage() {
        image = null;
        profileImage.setImageDrawable(null);
        imageRemover.setVisibility(View.GONE);
    }

    public void setUserData() {
        if (comesFrom.equals(HomeScreen.getClassName())) {
            if (preferences.contains("profile_id")) {
                id = preferences.getLong("profile_id", 0L);
                String password = db.getUserPassword(id);
                passwordInput.setText(password);
                repeatPasswordInput.setText(password);
            } else
                id = 0L;

            if (preferences.contains("profile_username"))
                usernameInput.setText(preferences.getString("profile_username", "?"));

            if (preferences.contains("profile_name"))
                nameInput.setText(preferences.getString("profile_name", ""));

            if (preferences.contains("profile_lastName"))
                lastNameInput.setText(preferences.getString("profile_lastName", ""));

            if (preferences.contains("profile_email"))
                emailInput.setText(preferences.getString("profile_email", ""));

            if (preferences.contains("profile_isAdmin"))
                adminCheckBox.setChecked(preferences.getBoolean("profile_isAdmin", false));

            if (preferences.contains("profile_image")) {
                image = Methods.getByteArrayAsBitmap(Methods.getStringAsByteArray(preferences.getString("profile_image", null)));
                profileImage.setImageBitmap(image);

                if (image != null)
                    imageRemover.setVisibility(View.VISIBLE);
            }
        } else if (comesFrom.equals(ManagementScreen.getClassName()) && inManagementMode) {
            Long currentUserId = preferences.getLong("profile_id", 0L);
            List<User> users = db.selectUser(id);

            if (users.size() > 0) {
                usernameInput.setText(users.get(0).getUsername());
                nameInput.setText(users.get(0).getName());
                lastNameInput.setText(users.get(0).getLastName());
                emailInput.setText(users.get(0).getEmail());
                adminCheckBox.setChecked(users.get(0).isAdmin());
                image = Methods.getByteArrayAsBitmap(users.get(0).getImage());
                profileImage.setImageBitmap(image);

                if (currentUserId == 1L) {
                    passwordInput.setText(users.get(0).getPassword());
                    repeatPasswordInput.setText(users.get(0).getPassword());
                } else {
                    if (!targetAuthorized || preferences.getLong("profile_id", 0L) == id) {
                        passwordInput.setText(users.get(0).getPassword());
                        repeatPasswordInput.setText(users.get(0).getPassword());
                    }
                }

                if (image != null && (!targetAuthorized || currentUserId == 1L))
                    imageRemover.setVisibility(View.VISIBLE);
            }
        }
    }

    public void closeActivity() {
        Intent i;

        if (comesFrom.equals(MainActivity.getClassName())) {
            i = new Intent(AccountScreen.this, LoginScreen.class);
            startActivity(i);
        } else if (comesFrom.equals(LoginScreen.getClassName())) {
            i = new Intent(AccountScreen.this, LoginScreen.class);
            startActivity(i);
        } else if (comesFrom.equals(HomeScreen.getClassName())) {
            i = new Intent(AccountScreen.this, HomeScreen.class);
            startActivity(i);
        } else if (comesFrom.equals(ManagementScreen.getClassName())) {
            i = new Intent(AccountScreen.this, ManagementScreen.class);
            startActivity(i);
        }
        finish();
    }
}
