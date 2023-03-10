# Basura

A Discord bot purely based on the open source code
of [Annie May](https://github.com/AlexanderColen/Annie-May-Discord-Bot).

Highly consider this project a love child or bastard of **Annie**.

To add this bot to your server,
click [here](https://discord.com/api/oauth2/authorize?client_id=870014073197170799&permissions=517543939136&scope=bot%20applications.commands)
.
I cannot guarantee 24/7 uptime as it is self-hosted and running a Raspberry Pi.

## What is Basura?

Basura literally means trash.

Trash includes isekai bullshit, power leveling xianxia, wuxia, otome politics,
and any that heightens your level of idiotness.

## What can it do?

It is purely based off what **Annie** can do (description also from **Annie**):

- Look up any anime/manga media from AniList via a search query.
- Display scores of users within each Discord server for the searched media.
- Search for users on AniList and display their stats and weab tendencies (did I forget to mention this is again, based
  on **Annie**?)
- Display media rankings based on season, season year, and format.

## I will just use Annie instead!

Go ahead. The only difference between this and **Annie** is that it uses interaction commands (or slash commands as most
people call it)

**Annie** has a lot better social interactions while this purely focuses on what everyone has watched, scored, and
mainly looking up trash.

I also highly advise you to use **Annie** as it has a much larger community.

## Why aren't you using Annie instead?

**Annie** broke for sometime and my discord server frequently uses it. I am the only capable developer there so I
checked out **Annie**'s code, port over its functionalities
in the language I am best at, and viola here we are.

## What are the commands?

We are using interaction commands. Hit your slash (/) key on the discord server this bot is on and it will tell you
everything you need to know.
Interaction commands in my opinion, should mitigate the necessary need for a help command.

## What are the future plans for this?

- Implement more ideas that **Annie** may come up (yes this is shamelessly a high priority, I highly respect the author
  for the incredible ideas)
- Localization per User/Server (this is already built on the code)
- Whatever my Discord server requests me to do that is weab related

## Where is MyAnimeList support?

Dear lord, of all things, the most toxic community I have ever went into. I can probably do this, once they have their
APIv2 sorted out. As of now it is almost impossible
to incorporate MAL without going hacky. As of this writing (09/12/2021) their authentication docs and endpoints are
still on TODO. Even if it works, I won't bother without a proper
documentation.

## Can I contribute to the code?

Feel free to fork the code. You are free to host your own instance.

## Get me up and running!

Rename `example.env` to `.env` and put your discord bot token.

```bash
./gradlew installDist
docker-compose build
docker-compose up
```