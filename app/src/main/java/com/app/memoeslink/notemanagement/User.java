package com.app.memoeslink.notemanagement;

public class User {
    private long id;
    private String username;
    private String name;
    private String lastName;
    private String password;
    private String email;
    private boolean admin;
    private byte[] image;

    public User() {
        id = 0L;
        username = null;
        name = null;
        lastName = null;
        password = null;
        email = null;
        admin = false;
        setImage(null);
    }

    public User(long id, String username, String name, String lastName, String password, String email, boolean admin, byte[] image) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.admin = admin;
        this.setImage(image);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
