CREATE TABLE music_guild (
  discord_guild_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  text_channel_id BIGINT,
  last_message_id BIGINT,
  volume INT NOT NULL DEFAULT 100
);

CREATE TABLE music_playlist
(
    playlist_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    discord_user_id BIGINT NOT NULL,
    name character varying NOT NULL
);

CREATE TABLE music_playlist_song
(
    playlist_song_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    playlist_id bigint NOT NULL,
    title character varying NOT NULL,
    uri character varying NOT NULL,
    identifier character varying NOT NULL,
    PRIMARY KEY (playlist_song_id),
    CONSTRAINT fk_playlist_song FOREIGN KEY (playlist_id)
        REFERENCES playlist (playlist_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
);
