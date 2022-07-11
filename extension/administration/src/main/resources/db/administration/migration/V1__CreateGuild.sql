CREATE TABLE guild (
  discord_guild_id BIGINT PRIMARY KEY NOT NULL,
  welcome_channel_id BIGINT,
  leave_channel_id BIGINT
);
