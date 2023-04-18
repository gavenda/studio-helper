CREATE TABLE counter_guild_count (
  guild_count_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  discord_guild_id BIGINT NOT NULL,
  count_name VARCHAR NOT NULL,
  count_amount VARCHAR NOT NULL,
  last_user_id BIGINT NOT NULL,
  last_timestamp TIMESTAMP NOT NULL
);
