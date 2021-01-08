package com.app.memoeslink.notemanagement;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class LoginScreen extends BaseActivity {
    EditText usernameInput;
    EditText passwordInput;
    Button loginButton;
    TextView link;
    String username = null;
    String password = null;
    DatabaseConnection db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameInput = findViewById(R.id.login_usernameEditText);
        passwordInput = findViewById(R.id.login_passwordEditText);
        passwordInput.setTypeface(Typeface.DEFAULT);
        loginButton = findViewById(R.id.login_loginButton);
        link = findViewById(R.id.login_createAccount);
        link.setTextColor(ContextCompat.getColor(this, R.color.url_color));
        link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //Open database connection
        db = new DatabaseConnection(this);

        //Set filters
        passwordInput.setFilters(new InputFilter[]{Methods.defineSpaceFilter()});

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
                username = s.toString();
                loginButtonEnabler();
            }
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
                password = s.toString();
                loginButtonEnabler();
            }
        });

        loginButton.setOnClickListener(v -> logIn());

        link.setOnClickListener(v -> {
            Intent i = new Intent(LoginScreen.this, AccountScreen.class);
            i.putExtra("comesFrom", getClassName());
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
    }

    public void loginButtonEnabler() {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
            loginButton.setEnabled(false);
        else
            loginButton.setEnabled(true);
    }

    public void logIn() {
        if (db.verifyUser(username, password)) {
            List<User> users = db.selectUser(username);

            if (users == null || users.size() == 0)
                Methods.showSystemToast(LoginScreen.this, getString(R.string.toast_login_fail), true, false, Status.ERROR, getString(R.string.status_error));
            else {
                Methods.storeUserData(this, users);
                Methods.showSystemToast(LoginScreen.this, getString(R.string.toast_login_successful, username), true, false, Status.SUCCESS, getString(R.string.status_success));
                Intent i = new Intent(LoginScreen.this, HomeScreen.class);
                startActivity(i);
                finish();
            }

        } else
            Methods.showSystemToast(LoginScreen.this, getString(R.string.toast_login_error), true, false, Status.ERROR, getString(R.string.status_error));
    }

    public static String getClassName() {
        return LoginScreen.class.getSimpleName();
    }
}
