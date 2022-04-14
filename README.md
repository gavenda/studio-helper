[![Basura](https://github.com/gavenda/studio-helper/actions/workflows/basura.yml/badge.svg)](https://github.com/gavenda/studio-helper/actions/workflows/basura.yml)
[![Vivy](https://github.com/gavenda/studio-helper/actions/workflows/vivy.yml/badge.svg)](https://github.com/gavenda/studio-helper/actions/workflows/vivy.yml)
[![Parrot](https://github.com/gavenda/studio-helper/actions/workflows/parrot.yml/badge.svg)](https://github.com/gavenda/studio-helper/actions/workflows/parrot.yml)

# Studio Helper Bots
An all-in-one repository for the Discord server I am making bots in.

## Bot List
- [Vivy](bot/vivy) - A modern music bot
- [Basura](bot/basura) - An AniList bot
- [Chupa](bot/chupa) - A simple counter bot

### Framework
All the bots are made using [Kord](https://github.com/kordlib/kord) using the [Kord Extensions](https://github.com/Kord-Extensions/kord-extensions) framework.

### Docker
Some bots can be easily deployed via docker. You can customize their translations yourself since the framework made it
easy to do so.

### Note
All bots are made to be on a bleeding edge, meaning whatever Discord says the new standard, it will follow. Hence all the
bots here utilizes application commands, messages, and the new components for messages.