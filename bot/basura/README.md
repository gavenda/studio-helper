# Basura

A Discord bot purely based on the open source code
of [Annie May](https://github.com/AlexanderColen/Annie-May-Discord-Bot).

Highly consider this project a love child or bastard of **Annie**.

Consider this gateway version as deprecated. As the [serverless version](https://github.com/gavenda/basura-cloudflare-worker) is much more scalable compared to this.
The only difference is that it can no longer stream anime radio from [Listen.MOE](https://listen.moe/).

## Can I contribute to the code?

Feel free to fork the code. You are free to host your own instance.

## Get me up and running!

In a simple docker compose configuration:

```yaml
version: '3'
services:
  bot:
    image: 'gavenda/basura:latest'
    environment:
      TOKEN: <your bot token>
      ANILIST_DB_USER: basura
      ANILIST_DB_PASS: basura
      ANILIST_DB_URL: jdbc:postgresql://database:5432/basura
  database:
    image: 'postgres:latest'
    restart: always
    ports:
      - '5432:5432'
    environment:
      POSTGRES_USER: basura
      POSTGRES_PASSWORD: basura
      POSTGRES_DB: basura
```
