package at.technikum.model;

import java.time.LocalDateTime;

public class Rating {
    private int id;

    // belongs to exactly one user and mediaEntry
    private int userId;
    private int mediaId;

    private int value;
    private String comment;

    private int likes;
    private boolean confirmed;
    private LocalDateTime timeStamp;

    public Rating(int userId, int mediaId, int value, String comment) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.value = value;
        this.comment = comment;
        this.likes = 0;
        this.confirmed = false;
        this.timeStamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getMediaId() {
        return mediaId;
    }

    public int getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public int getLikes() {
        return likes;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }


}
