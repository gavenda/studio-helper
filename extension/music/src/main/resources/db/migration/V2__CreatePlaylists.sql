CREATE TABLE playlist
(
    playlist_id bigserial NOT NULL,
    discord_user_id BIGINT NOT NULL,
    name character varying NOT NULL,
    PRIMARY KEY (playlist_id)
);

CREATE SEQUENCE playlist_song_playlist_song_id_seq;
CREATE TABLE playlist_song
(
    playlist_song_id BIGINT NOT NULL DEFAULT NEXTVAL('playlist_song_playlist_song_id_seq'),
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
ALTER SEQUENCE playlist_song_playlist_song_id_seq RESTART WITH 1000;
