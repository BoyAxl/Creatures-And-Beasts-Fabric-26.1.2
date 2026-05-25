# Creatures and Beasts - Fabric 26.1.x Port

Unofficial Fabric port of **Creatures and Beasts** for **Minecraft 26.1.x**.

This repository keeps the spirit of the original Forge mod while moving the codebase to the current Fabric toolchain, Fabric API, and Geckolib 5.

## ✨ Status

- 🧵 Loader: **Fabric**
- ⛏️ Minecraft: **26.1.x**
- 🦎 Geckolib: **5.5.1**
- ☕ Java: **25+**
- 🧪 State: playable test port, still being verified in-game

The port is built against the **26.1** baseline and declares compatibility with the **26.1.x** line, including **26.1**, **26.1.1**, and **26.1.2**.

## 🌿 What Works

- 🐾 Core entity registration, spawning, attributes, animations, renderers, and spawn eggs
- 🎒 Sporeling backpack behavior, including carrying and dropping tamed sporelings
- 🐣 Little Grebe breeding/passenger behavior
- 👑 Flower crown and glowing flower crown armor rendering
- ⚔️ Cinder sword/item fixes for modern item components
- 🍳 Cinder furnace screen/container port
- 🧪 Loot tables, recipes, item tags, model data, sprites, sounds, and Geckolib assets under the Fabric namespace

## 🛠️ Build

```bash
./gradlew build
```

The Fabric jar is generated at:

```text
fabric/build/libs/CreaturesAndBeasts-Fabric-1.0.1+26.1.x.jar
```

## 🧡 Credits

All original mod concept, assets, mobs, and gameplay belong to the **Creatures and Beasts** team.

- Original authors: **Joosh**, **CGessinger**, **HellionGames**, **BluSpring**
- Contributor credit from the original metadata: **AzureDoom**
- Spawn egg textures adapted from the **Modded Omelet** resource pack by **L2**
- Original project/source reference: <https://github.com/bonsaistudi0s/Creatures_And_Beasts-Forge>

This Fabric port is an unofficial community maintenance effort by **BoyAxl / Axl**, built for personal testing and compatibility work.

## 🤖 AI Assistance

This port was prepared with help from **OpenAI Codex** as a coding assistant.

AI assistance was used for:

- Comparing the Fabric port against the original Forge 1.19.2 jar behavior
- Translating original loader-specific flows into Fabric APIs
- Updating rendering and animation paths for Geckolib 5
- Debugging in-game issues from logs and runtime testing

All changes were reviewed and tested locally during the porting process.

## 📦 Reference Files

Local reference jars used during development are intentionally **not included** in this repository.

The `reference/` folder is ignored because it may contain original mod/dependency jars used only for comparison while porting.

## ⚠️ Disclaimer

This is **not an official Creatures and Beasts release**.

Please support and credit the original authors. If an official modern Fabric release becomes available, prefer that version.
