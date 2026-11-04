package at.technikum.repository;

import at.technikum.model.Media;

import java.util.List;

public interface MediaRepository {
    void save(Media media);
    Media findById(int id);
    List<Media> findAll();
    void delete(int id);
}