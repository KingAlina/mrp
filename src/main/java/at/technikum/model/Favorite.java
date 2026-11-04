package at.technikum.model;

public class Favorite {
    private int userId;
    private int mediaId;

    public Favorite(int userId, int mediaId) {
        this.userId = userId;
        this.mediaId = mediaId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMediaId() {
        return mediaId;
    }
}
