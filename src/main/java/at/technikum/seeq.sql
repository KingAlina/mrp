-- Reset database tables
DROP TABLE IF EXISTS media_genres;
DROP TABLE IF EXISTS media;
DROP TABLE IF EXISTS users;


-- USERS

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username TEXT UNIQUE NOT NULL,
                       password TEXT NOT NULL
);


-- MEDIA

CREATE TABLE media (
                       id SERIAL PRIMARY KEY,
                       creator_id INT NOT NULL,
                       title TEXT NOT NULL,
                       description TEXT,
                       media_type TEXT NOT NULL,
                       release_year INT NOT NULL,
                       age_restriction INT NOT NULL
);


-- MEDIA GENRES

CREATE TABLE media_genres (
                              media_id INT NOT NULL,
                              genre TEXT NOT NULL,

                              PRIMARY KEY (media_id, genre),

                              FOREIGN KEY (media_id)
                                  REFERENCES media(id)
                                  ON DELETE CASCADE
);


-- TEST USERS

INSERT INTO users (username, password)
VALUES
    ('alice', '1234'),
    ('bob', '1234');


-- TEST MEDIA

INSERT INTO media
(creator_id, title, description, media_type, release_year, age_restriction)
VALUES
    (1, 'Interstellar', 'Science fiction movie', 'MOVIE', 2014, 12),
    (2, 'Stranger Things', 'Mystery series', 'SERIES', 2016, 16),
    (1, 'The Witcher 3', 'Fantasy role-playing game', 'GAME', 2015, 18);


-- TEST GENRES

INSERT INTO media_genres (media_id, genre)
VALUES
    (1, 'SCIFI'),
    (1, 'ADVENTURE'),

    (2, 'MYSTERY'),
    (2, 'HORROR'),

    (3, 'FANTASY'),
    (3, 'ADVENTURE');