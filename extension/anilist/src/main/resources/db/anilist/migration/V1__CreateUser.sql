CREATE TABLE anilist_user (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	discord_id BIGINT NOT NULL,
	discord_guild_id BIGINT NOT NULL,
	anilist_id BIGINT NOT NULL,
	anilist_username VARCHAR(255) NOT NULL,
	UNIQUE(discord_id, discord_guild_id, anilist_id)
);