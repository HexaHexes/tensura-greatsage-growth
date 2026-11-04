# Tensura: Great Sage - Growth

A NeoForge mod for the **Tensura RPG** pack (MC 1.21.1) that grows the **Great
Sage** addon's abilities: three unlockable upgrades - Analyze Aspectual Magic,
Analyze Spiritual Magic and Analyze Summoning Magic - each teaching the Sage's
creature reading and console one more family of magic.

## What it does

Three growth upgrades for the Great Sage addon - **Analyze Aspectual Magic**,
**Analyze Spiritual Magic** and **Analyze Summoning Magic** - each unlocking
the Sage's analysis screen for one Tensura magic family, which the base addon
cannot analyse at all (its chain is built for the `Skill` branch only; magic
skills are refused as unknown).

- `/sagegrowth unlock <family>` spends Sage XP (aspectual: level 5, spiritual:
  8, summon: 12; 1000 XP each) - nodes live in this mod's progression bucket
  because the addon's own node store is a final enum.
- The unlocked family flows through the addon's own analysis machinery: its
  messages, voice lines, config-derived durations, and the identical slot
  contract its tick() consumes.
- Stack PRs 1-4 carry the implementation; issue #1 holds the research record.

## Dependencies

- [Great Sage - Tensura Reincarnated Addon](https://modrinth.com/mod/great-sage) 0.0.7.1
- [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated) 2.0.1.3
- [ManasCore](https://www.curseforge.com/minecraft/mc-mods/manascore) 4.0.0.2
