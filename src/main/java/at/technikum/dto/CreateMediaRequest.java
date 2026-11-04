package at.technikum.dto;

import at.technikum.model.Genre;
import at.technikum.model.MediaType;

import java.util.List;

public class CreateMediaRequest {
    private int creatorId;
    private String title;
    private String description;
    private MediaType mediaType;
    private int releaseYear;
    private List<Genre> genres;
    private int ageRestriction;

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