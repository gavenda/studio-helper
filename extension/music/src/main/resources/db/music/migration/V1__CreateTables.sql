CREATE TABLE guild (
  discord_guild_id BIGINT PRIMARY KEY NOT NULL,
  text_channel_id BIGINT,
  volume INT NOT NULL DEFAULT 100
);
