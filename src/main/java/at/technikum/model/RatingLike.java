package at.technikum.model;

public class RatingLike {
    private int userId;
    private int ratingId;

    public RatingLike(int userId, int ratingId) {
        this.userId = userId;
        this.ratingId = ratingId;
    }

    public int getUserId() {
        return userId;
    }

    public int getRatingId() {
        return ratingId;
    }
}
