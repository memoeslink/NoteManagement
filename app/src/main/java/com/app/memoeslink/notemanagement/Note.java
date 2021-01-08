package com.app.memoeslink.notemanagement;

public class Note {
    private Long id;
    private String title;
    private String date;
    private String description;
    private String username;
    private byte[] image;
    private boolean personal;

    public Note() {
        id = 0L;
        title = null;
        date = null;
        description = null;
        username = null;
        image = null;
        personal = false;
    }

    public Note(Long id, String title, String date, String description, String username, byte[] image, boolean personal) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.username = username;
        this.image = image;
        this.personal = personal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }
}
