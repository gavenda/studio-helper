CREATE TABLE anilist_anime_airing_schedule (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	discord_guild_id BIGINT NOT NULL,
	media_id BIGINT NOT NULL,
	discord_user_id BIGINT NOT NULL
);
