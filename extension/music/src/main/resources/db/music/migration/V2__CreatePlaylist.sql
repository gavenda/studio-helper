CREATE TABLE music_playlist
(
    playlist_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    discord_user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL
);
