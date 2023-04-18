CREATE TABLE anilist_guild (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	discord_guild_id BIGINT NOT NULL,
	hentai BOOLEAN NOT NULL DEFAULT false,
	locale VARCHAR(20) NOT NULL DEFAULT 'en-US',
	notification_channel_id BIGINT NOT NULL DEFAULT -1
);
