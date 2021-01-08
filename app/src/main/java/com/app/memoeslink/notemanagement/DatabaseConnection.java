package com.app.memoeslink.notemanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection extends SQLiteOpenHelper {
    private SQLiteDatabase db;

    //Database tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_NOTES = "notes";

    //Table fields
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_FIRST_NAME = "firstName";
    private static final String FIELD_LAST_NAME = "lastName";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_IS_ADMIN = "isAdmin";
    private static final String FIELD_IMAGE_PATH = "imagePath";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_NOTE_DATE = "noteDate";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_IS_PRIVATE = "isPrivate";

    //Database information
    private static final String DB_NAME = "notes.sqlite";
    private static final int DB_VERSION = 1;

    //Other
    private static final String ID_PREFIX = "id_";
    private static final String IDENTIFIER_PREFIX = "#id = ";
    Context context;

    public DatabaseConnection(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        db = getWritableDatabase();
    }

    //Create tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + ID_PREFIX + TABLE_USERS + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1, "
                + FIELD_USERNAME + " TEXT, "
                + FIELD_FIRST_NAME + " TEXT, "
                + FIELD_LAST_NAME + " TEXT, "
                + FIELD_PASSWORD + " TEXT, "
                + FIELD_EMAIL + " TEXT, "
                + FIELD_IS_ADMIN + " INTEGER, "
                + FIELD_IMAGE_PATH + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_NOTES + "("
                + ID_PREFIX + TABLE_NOTES + " INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 1, "
                + FIELD_TITLE + " TEXT, "
                + FIELD_NOTE_DATE + " TEXT, "
                + FIELD_DESCRIPTION + " TEXT, "
                + ID_PREFIX + TABLE_USERS + " INTEGER, "
                + FIELD_IMAGE_PATH + " TEXT, "
                + FIELD_IS_PRIVATE + " INTEGER, "
                + "FOREIGN KEY (" + ID_PREFIX + TABLE_USERS + ") REFERENCES " + TABLE_USERS + "(" + ID_PREFIX + TABLE_USERS + "))");
    }

    //Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop older table if exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public Long countUsers() {
        return countRows(TABLE_USERS);
    }

    public boolean verifyUser(String username) {
        try {
            boolean userExists;
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + FIELD_USERNAME + "='" + username + "'", null);
            userExists = c.getCount() > 0 ? true : false;
            c.close();
            return userExists;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUser(String username, String password) {
        try {
            boolean userExists;
            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + FIELD_USERNAME + "='" + username + "' AND " + FIELD_PASSWORD + "='" + password + "'", null);
            userExists = c.getCount() > 0 ? true : false;
            c.close();
            return userExists;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserPassword(Long id) {
        String s = "";

        try {
            Cursor c = db.rawQuery("SELECT " + FIELD_PASSWORD + " FROM " + TABLE_USERS + " WHERE " + ID_PREFIX + TABLE_USERS + "=" + id, null);

            if (c.moveToFirst())
                s = c.getString(0);
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            s = "";
        } finally {
            return s;
        }
    }

    public List<User> selectUser(Long id) {
        String userIdentifier = IDENTIFIER_PREFIX + (id == null ? "" : id);
        return selectUser(userIdentifier);
    }

    public List<User> selectUser(String... username) {
        List<User> users = new ArrayList<>();
        String retrievedUsername = "";

        try {
            if (username != null)
                retrievedUsername = username[0];
        } catch (Exception e) {
            e.printStackTrace();
            retrievedUsername = "";
        }

        try {
            Cursor c;
            byte[] image;

            if (retrievedUsername.contains(IDENTIFIER_PREFIX))
                c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + ID_PREFIX + TABLE_USERS + "=" + retrievedUsername.replace(IDENTIFIER_PREFIX, ""), null);
            else if (retrievedUsername.equals(""))
                c = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
            else
                c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + FIELD_USERNAME + "='" + retrievedUsername + "'", null);

            if (c.moveToFirst()) {
                int n = 0;

                do {
                    if (n <= Integer.MAX_VALUE) {
                        image = retrievedUsername.equals("") ? null : Methods.getBitmapAsByteArray(Methods.openImage(c.getString(c.getColumnIndex(FIELD_IMAGE_PATH))));
                        users.add(new User(c.getLong(c.getColumnIndex(ID_PREFIX + TABLE_USERS)), c.getString(c.getColumnIndex(FIELD_USERNAME)), c.getString(c.getColumnIndex(FIELD_FIRST_NAME)), c.getString(c.getColumnIndex(FIELD_LAST_NAME)), c.getString(c.getColumnIndex(FIELD_PASSWORD)), c.getString(c.getColumnIndex(FIELD_EMAIL)), c.getInt(c.getColumnIndex(FIELD_IS_ADMIN)) == 1 ? true : false, image));
                        n++;
                    }
                } while (c.moveToNext());
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            users.clear();
        } finally {
            return users;
        }
    }

    public boolean insertUser(String username, String name, String lastName, String password, String email, boolean isAdmin, Bitmap image) {
        ContentValues values = new ContentValues();
        String imagePath;

        //Save profile image into app directory
        if (image != null)
            imagePath = Methods.saveImage(context, image, username);
        else
            imagePath = "";

        //Define fields to set
        values.put(FIELD_USERNAME, username);
        values.put(FIELD_FIRST_NAME, name);
        values.put(FIELD_LAST_NAME, lastName);
        values.put(FIELD_PASSWORD, password);
        values.put(FIELD_EMAIL, email);
        values.put(FIELD_IS_ADMIN, isAdmin ? 1 : 0);
        values.put(FIELD_IMAGE_PATH, imagePath);

        //Insert into table
        try {
            db.insert(TABLE_USERS, null, values);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(Long id, String username, String name, String lastName, String password, String email, boolean isAdmin, Bitmap image) {
        ContentValues values = new ContentValues();
        String imagePath;

        //Save or update profile image into app directory
        if (image != null) {
            List<User> users = selectUser(id);

            //Delete profile image if username is different
            if (users.size() > 0) {
                if (username != users.get(0).getUsername())
                    Methods.deleteImage(context, users.get(0).getUsername());
            }

            imagePath = Methods.saveImage(context, image, username);
        } else {
            imagePath = "";

            //Delete profile image if exists
            Methods.deleteImage(context, username);
        }

        //Define fields to update
        values.put(FIELD_USERNAME, username);
        values.put(FIELD_FIRST_NAME, name);
        values.put(FIELD_LAST_NAME, lastName);
        values.put(FIELD_PASSWORD, password);
        values.put(FIELD_EMAIL, email);
        values.put(FIELD_IS_ADMIN, isAdmin ? 1 : 0);
        values.put(FIELD_IMAGE_PATH, imagePath);

        //Update entry
        try {
            db.update(TABLE_USERS, values, ID_PREFIX + TABLE_USERS + "=" + id, null);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(Long id) {
        String username;

        try {
            username = selectUser(id).get(0).getUsername();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            username = "";
        }

        //Delete entry
        try {
            String condition = ID_PREFIX + TABLE_USERS + "=?";
            String[] value = new String[]{String.valueOf(id)};
            db.delete(TABLE_USERS, condition, value);

            //Delete profile image if exists
            Methods.deleteImage(context, username);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Long countNotes() {
        return countRows(TABLE_NOTES);
    }

    public List<Note> selectNote(Long... noteValues) {
        List<Note> notes = new ArrayList<>();
        Long retrievedId = 0L;
        Long userType = 0L;

        try {
            if (noteValues != null)
                retrievedId = noteValues[0];
        } catch (Exception e) {
            e.printStackTrace();
            retrievedId = 0L;
        }

        try {
            if (noteValues != null)
                userType = noteValues[1];
        } catch (Exception e) {
            e.printStackTrace();
            userType = 0L;
        }

        try {
            String condition;

            if (userType == 0L) {
                if (retrievedId == 0L)
                    condition = "";
                else
                    condition = " WHERE " + TABLE_NOTES + "." + ID_PREFIX + TABLE_NOTES + "=" + retrievedId;
            } else if (userType == 1L)
                condition = " WHERE (" + FIELD_IS_ADMIN + "=" + 1 + " AND " + TABLE_NOTES + "." + FIELD_IS_PRIVATE + "=" + 0 + ") OR " + TABLE_USERS + "." + FIELD_IS_ADMIN + "=" + 0 + " OR " + TABLE_NOTES + "." + ID_PREFIX + TABLE_USERS + "=" + retrievedId;
            else if (userType == 2L)
                condition = " WHERE " + TABLE_NOTES + "." + FIELD_IS_PRIVATE + "=" + 0 + " OR " + TABLE_NOTES + "." + ID_PREFIX + TABLE_USERS + "=" + retrievedId;
            else
                condition = "";
            Cursor c = db.rawQuery("SELECT " + TABLE_NOTES + "." + ID_PREFIX + TABLE_NOTES + ", " + TABLE_NOTES + "." + FIELD_TITLE + ", " + TABLE_NOTES + "." + FIELD_NOTE_DATE + ", " + TABLE_NOTES + "." + FIELD_DESCRIPTION + ", " + TABLE_USERS + "." + FIELD_USERNAME + ", " + TABLE_NOTES + "." + FIELD_IMAGE_PATH + ", " + TABLE_NOTES + "." + FIELD_IS_PRIVATE + " FROM " + TABLE_USERS + " INNER JOIN " + TABLE_NOTES + " ON " + TABLE_USERS + "." + ID_PREFIX + TABLE_USERS + "=" + TABLE_NOTES + "." + ID_PREFIX + TABLE_USERS + condition + " ORDER BY " + TABLE_NOTES + "." + ID_PREFIX + TABLE_NOTES + " DESC", null);
            byte[] image;

            if (c != null && c.moveToFirst()) {
                System.out.println();
                int n = 0;

                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    if (n <= Integer.MAX_VALUE) {
                        image = retrievedId == 0 ? null : Methods.getBitmapAsByteArray(Methods.openImage(c.getString(c.getColumnIndex(TABLE_NOTES + "." + FIELD_IMAGE_PATH))));
                        notes.add(new Note(c.getLong(c.getColumnIndex(TABLE_NOTES + "." + ID_PREFIX + TABLE_NOTES)), c.getString(c.getColumnIndex(TABLE_NOTES + "." + FIELD_TITLE)), c.getString(c.getColumnIndex(TABLE_NOTES + "." + FIELD_NOTE_DATE)), c.getString(c.getColumnIndex(TABLE_NOTES + "." + FIELD_DESCRIPTION)), c.getString(c.getColumnIndex(TABLE_USERS + "." + FIELD_USERNAME)), image, c.getInt(c.getColumnIndex(FIELD_IS_PRIVATE)) == 1 ? true : false));
                        n++;
                    }
                }
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            notes.clear();
        } finally {
            return notes;
        }
    }

    public boolean insertNote(String title, String description, Long idUser, Bitmap image, boolean isPrivate) {
        ContentValues values = new ContentValues();
        String imagePath;

        //Get current date/time
        String dateTime = Methods.getCurrentDateTimeString();
        String imgDateTime = Methods.getImgDateTimeString();

        //Save profile image into app directory
        if (image != null)
            imagePath = Methods.saveImage(context, image, imgDateTime);
        else
            imagePath = "";

        //Define fields to set
        values.put(FIELD_TITLE, title);
        values.put(FIELD_NOTE_DATE, dateTime);
        values.put(FIELD_DESCRIPTION, description);
        values.put(ID_PREFIX + TABLE_USERS, idUser);
        values.put(FIELD_IMAGE_PATH, imagePath);
        values.put(FIELD_IS_PRIVATE, isPrivate ? 1 : 0);

        //Insert into table
        try {
            db.insert(TABLE_NOTES, null, values);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateNote(Long id, String title, String dateTime, String description, Bitmap image, Boolean isPrivate) {
        ContentValues values = new ContentValues();
        String imgDateTime = dateTime.replaceAll("[:-]", "_");
        imgDateTime = imgDateTime.replaceAll("\\s+", "");
        String imagePath;

        //Save profile image into app directory
        if (image != null)
            imagePath = Methods.saveImage(context, image, imgDateTime);
        else {
            imagePath = "";

            //Delete image if exists
            Methods.deleteImage(context, imgDateTime);
        }

        //Define fields to update
        values.put(FIELD_TITLE, title);
        values.put(FIELD_NOTE_DATE, dateTime);
        values.put(FIELD_DESCRIPTION, description);
        values.put(FIELD_IMAGE_PATH, imagePath);
        values.put(FIELD_IS_PRIVATE, isPrivate ? 1 : 0);

        //Update entry
        try {
            db.update(TABLE_NOTES, values, ID_PREFIX + TABLE_NOTES + "=" + id, null);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNote(Long id) {
        String imgDateTime;
        List<Note> notes = selectNote(id);

        if (notes.size() > 0) {
            imgDateTime = notes.get(0).getDate().replaceAll("[:-]", "_");
            imgDateTime = imgDateTime.replaceAll("\\s+", "");
        } else
            imgDateTime = "";

        //Delete entry
        try {
            String condition = ID_PREFIX + TABLE_NOTES + "=?";
            String[] value = new String[]{String.valueOf(id)};
            db.delete(TABLE_NOTES, condition, value);

            //Delete profile image if exists
            Methods.deleteImage(context, imgDateTime);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Long countRows(String table) {
        Long count = -1L;

        try {
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
            c.moveToFirst();
            count = c.getLong(0);
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            return count;
        }
    }
}
