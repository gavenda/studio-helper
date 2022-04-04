CREATE TABLE anime_airing_schedule (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  discord_guild_id BIGINT NOT NULL,
  media_id BIGINT NOT NULL
);

ALTER TABLE "guild" ADD COLUMN notification_channel_id BIGINT NOT NULL DEFAULT -1