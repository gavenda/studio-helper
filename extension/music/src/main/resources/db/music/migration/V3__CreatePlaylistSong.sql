CREATE TABLE music_playlist_song
(
    playlist_song_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    playlist_id bigint NOT NULL,
    title VARCHAR(255) NOT NULL,
    uri VARCHAR(255)NOT NULL,
    identifier VARCHAR(255) NOT NULL,
);