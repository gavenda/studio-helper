CREATE TABLE user_locale (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  discord_id BIGINT NOT NULL,
  locale VARCHAR(20) NOT NULL DEFAULT 'en-US',
  UNIQUE(discord_id)
);
