[![Basura](https://github.com/gavenda/studio-helper/actions/workflows/basura.yml/badge.svg)](https://github.com/gavenda/studio-helper/actions/workflows/basura.yml)
[![Vivy](https://github.com/gavenda/studio-helper/actions/workflows/vivy.yml/badge.svg)](https://github.com/gavenda/studio-helper/actions/workflows/vivy.yml)

# Studio Helper Bots
An all-in-one repository for the Discord server I am making bots in.

## Bot List
- [Vivy](bot/vivy) - A modern music bot
- [Basura](bot/basura) - An AniList bot
- [Lumi](bot/lumi) - A management bot

## Extension List
All the bots above are just extensions woven together, here are the extensions that are used in the bots above:
- [about](extension/about) - Simple about application command that displays running OS and version
- [administration](extension/anilist) - Utility for server administration
- [anilist](extension/anilist) - AniList integration, lookup anime/manga media easily
- [aniradio](extension/aniradio) - Simply plays a stream from [listen.moe](https://listen.moe)
- [announcer](extension/announcer) - Announces a bundled music file whenever someone joins the bot's voice channel
- [automove](extension/automove) - Auto moves a user whenever they self mute or deafened and vice-versa
- [counter](extension/counter) - Counter for literally anything, no database, everything is stored in a json file
- [moderation](extension/moderation) - Utilities for server moderation
- [music](extension/music) - Fully featured music extension, uses lava link to play music
- [utility](extension/utility) - Simple utilities for server members

### Framework
All the bots are made using [Kord](https://github.com/kordlib/kord) using the [Kord Extensions](https://github.com/Kord-Extensions/kord-extensions) framework.

### Docker
Some bots can be easily deployed via docker. You can customize their translations yourself since the framework made it
easy to do so.

### Note
All bots are made to be on a bleeding edge, meaning whatever Discord says the new standard, it will follow. Hence all the
bots here utilizes application commands, messages, and the new components for messages.