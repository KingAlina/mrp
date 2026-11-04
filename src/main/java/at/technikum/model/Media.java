package at.technikum.model;

import java.util.List;


public class Media {
    private int id;
    private int creatorId;

    private String title;
    private String description;
    private MediaType mediaType;
    private int releaseYear;
    private List<Genre> genres;
    private int ageRestriction;

    public Media(int creatorId, String title, String description, MediaType mediaType, int releaseYear, List<Genre> genres, int ageRestriction) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.ageRestriction = ageRestriction;
    }

    public int getId() {
        return id;
    }

    public int getCreatorId() {
        return creatorId;
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }
}
