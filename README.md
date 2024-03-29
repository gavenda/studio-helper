## NOTE: No longer being maintained, the massive maintenance I have to do every new discord update for Kord is just insane. I'll be moving my bots to simply use discord.js or cloudflare workers if I have no need for the gateway.

[![Vivy](https://github.com/gavenda/studio-helper/actions/workflows/vivy.yml/badge.svg)](https://github.com/gavenda/studio-helper/actions/workflows/vivy.yml)

# Studio Helper Bots

An all-in-one repository for the Discord server I am making bots in.

## Bot List

- [Vivy](bot/vivy) - A modern music bot
- [Basura](bot/basura) - An AniList bot

## Extension List

All the bots above are just extensions woven together, here are the extensions that are used in the bots above:

- [about](extension/about) - Simple about application command that displays running OS and version
- [administration](extension/anilist) - Utility for server administration
- [anilist](extension/anilist) - AniList integration, lookup anime/manga media easily
- [announcer](extension/announcer) - Announces a bundled music file whenever someone joins the bot's voice channel
- [automove](extension/automove) - Auto moves a user whenever they self mute or deafened and vice-versa
- [counter](extension/counter) - Counter for literally anything, no database, everything is stored in a json file
- [information](extension/information) - Shows information for emoji, user, etc.
- [listenmoe](extension/listenmoe) - Simply plays a stream from [listen.moe](https://listen.moe)
- [moderation](extension/moderation) - Utilities for server moderation
- [music](extension/music) - Fully featured music extension, uses lava link to play music
- [utility](extension/utility) - Simple utilities for server members

### Framework

All the bots are made using [Kord](https://github.com/kordlib/kord) using
the [Kord Extensions](https://github.com/Kord-Extensions/kord-extensions) framework.

### Docker

Some bots can be easily deployed via docker. You can customize their translations yourself since the framework made it
easy to do so.

### Note

All bots are made to be on a bleeding edge, meaning whatever Discord says the new standard, it will follow. Hence all
the
bots here utilizes application commands, messages, and the new components for messages.
