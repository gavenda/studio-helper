CREATE TABLE anilist_user_locale (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	discord_id BIGINT NOT NULL,
	locale VARCHAR(20) NOT NULL DEFAULT 'en-US',
	UNIQUE(discord_id)
);
