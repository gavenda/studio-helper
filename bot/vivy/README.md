# Vivy

My mission is to make everyone happy by singing.

## Let me sing?
In a simple docker compose configuration:
```yaml
version: '3'
services:
  bot:
    image: 'gavenda/vivy:latest'
    environment:
      TOKEN: <your bot token>
      SPOTIFY_CLIENT_ID: <optional spotify client id>
      SPOTIFY_CLIENT_SECRET: <optional spotify client secret>
      MUSIC_DB_USER: vivy
      MUSIC_DB_PASS: vivy
      MUSIC_DB_URL: jdbc:postgresql://database:5432/vivy
      LINK_NODES: ws://lava.link
      LINK_PASSWORDS: vivy
  database:
    image: 'postgres:latest'
    restart: always
    ports:
      - '5432:5432'
    environment:
      POSTGRES_USER: vivy
      POSTGRES_PASSWORD: vivy
      POSTGRES_DB: vivy
```
## Running as a service
An example [systemd unit file](systemd/vivy.service) is included in the repository.
