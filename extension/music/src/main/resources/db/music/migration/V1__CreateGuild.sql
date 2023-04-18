CREATE TABLE music_guild (
  discord_guild_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  text_channel_id BIGINT,
  last_message_id BIGINT,
  volume INT NOT NULL DEFAULT 100
);
