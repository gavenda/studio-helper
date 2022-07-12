CREATE TABLE guild_count (
  guild_count_id BIGSERIAL PRIMARY KEY NOT NULL,
  discord_guild_id BIGINT NOT NULL,
  count_name VARCHAR NOT NULL,
  count_amount VARCHAR NOT NULL,
  last_user_id BIGINT NOT NULL,
  last_timestamp TIMESTAMPTZ NOT NULL
);
