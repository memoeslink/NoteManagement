package com.app.memoeslink.notemanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

public class ManagementScreen extends BaseActivity {
    RelativeLayout userLayout;
    RelativeLayout messageLayout;
    Button createAccountButton;
    Button backButton;
    boolean recentlyCreated = true;
    DatabaseConnection db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        userLayout = findViewById(R.id.management_userLayout);
        messageLayout = findViewById(R.id.management_messageLayout);
        createAccountButton = findViewById(R.id.management_createAccountButton);
        backButton = findViewById(R.id.management_backButton);

        //Open database connection
        db = new DatabaseConnection(this);

        //Retrieve users from database
        getUsers();

        //Set listeners
        createAccountButton.setOnClickListener(v -> {
            Intent i = new Intent(ManagementScreen.this, AccountScreen.class);
            i.putExtra("comesFrom", getClassName());
            i.putExtra("isInManagementMode", false);
            startActivity(i);
            finish();
        });

        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        editor.putString("temp_currentScreen", getClassName()).commit();

        if (!recentlyCreated) {
            getUsers();
        } else recentlyCreated = false;
    }

    public void getUsers() {
        if (db.countUsers() > 0L) {
            //Remove all views
            userLayout.removeAllViews();

            //Get all users
            final List<User> users = db.selectUser();

            //Create a list of only user names
            String[] userNames = new String[users.size()];

            //Get contact names
            for (int c = 0; c < users.size(); c++) {
                if ((users.get(c).getName().equals("") || users.get(c).getName().isEmpty()) && (users.get(c).getLastName().equals("") || users.get(c).getLastName().isEmpty()))
                    userNames[c] = users.get(c).getUsername();
                else
                    userNames[c] = users.get(c).getUsername() + " (" + users.get(c).getName() + (users.get(c).getName().isEmpty() || users.get(c).getLastName().isEmpty() ? "" : " ") + users.get(c).getLastName() + ")";
            }

            //Define ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, userNames);
            ListView userListView = new ListView(this);
            userListView.setAdapter(adapter);
            userListView.setVerticalFadingEdgeEnabled(true);
            userListView.setFadingEdgeLength(10);
            userListView.setScrollbarFadingEnabled(false);

            //Define onClickListener
            userListView.setOnItemClickListener((parent, view, position, id) -> {
                Intent i = new Intent(getBaseContext(), AccountScreen.class);
                i.putExtra("comesFrom", getClassName());
                i.putExtra("isInManagementMode", true);
                i.putExtra("identifier", users.get(position).getId());
                i.putExtra("isTargetAuthorized", users.get(position).isAdmin());
                startActivity(i);
            });

            //Add ListView to layout
            userLayout.addView(userListView);

            messageLayout.setVisibility(View.GONE);
            userLayout.setVisibility(View.VISIBLE);
        } else {
            userLayout.setVisibility(View.GONE);
            messageLayout.setVisibility(View.VISIBLE);
        }
    }

    public static String getClassName() {
        return ManagementScreen.class.getSimpleName();
    }
}
