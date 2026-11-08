# Tensura: Great Sage - Growth

Tensura: Great Sage - Growth adds three unlockable upgrades to the Great Sage
addon: **Analyze Aspectual Magic**, **Analyze Spiritual Magic** and **Analyze
Summoning Magic**. Each one teaches the Sage's analysis one more family of
Tensura magic, which the base addon cannot read at all - its chain is built for
the `Skill` branch only, and magic skills come back to it as unknown.

The Sage menu shows all three as their own rows with the level they need, the XP
they cost and whether you own them, and a click buys one there and then. Once a
family is unlocked its learned skills join the analysis screen like any other
tier, analysed through the addon's own messages, voice lines and timings.

## What it does

- **Three gated upgrades.** Aspectual Magic at Sage level 5, Spiritual Magic at
  8, Summoning Magic at 12 - each costing 1000 Sage XP. The gate is checked
  server-side, and the refusals read in the Sage's own voice: wrong level, not
  enough XP, or already unlocked.
- **Every family joins the analysis screen.** The skill browser gains one tier
  bucket per family, holding that family's learned skills with the normal
  analysable badges. A locked family stays listed, greyed out the way a sealed
  rank is, and clicking it refuses with the Sage's own sealed-rank line.
- **The unlock is real.** A locked family is refused by the server even if
  selected (`The Great Sage has not unlocked ... analysis yet`), and once bought
  it flows through the addon's own analysis machinery - its messages, voice
  lines, config-derived durations and the identical slot contract its `tick()`
  consumes.
- **The screens stay honest.** The unlock flags sync to the client on login and
  again immediately after each purchase, so the growth rows and the analysis
  screen update without a rejoin.
- **The progression stays in this mod's bucket.** The addon's node store is a
  final enum, so the three upgrades keep their own NBT, costs and level rules
  here rather than pretending to be addon nodes.

## Getting an unlock

Either click the family's row in the Sage menu (it buys the upgrade right
there), or run the command yourself:

```
/sagegrowth unlock aspectual
/sagegrowth unlock spiritual
/sagegrowth unlock summon
```

| Family | Sage level | Cost |
|---|---|---|
| Aspectual Magic | 5 | 1000 XP |
| Spiritual Magic | 8 | 1000 XP |
| Summoning Magic | 12 | 1000 XP |

The Sage's level and XP are the base addon's own progression; this mod only
spends them.

## Dependencies

| | Mod | Why |
|---|---|---|
| Required | [Great Sage - Tensura Reincarnated Addon](https://modrinth.com/mod/great-sage) 0.0.7.1 | The addon whose analysis this extends |
| Required | [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated) | The magic families being analysed |
| Required | [ManasCore](https://www.curseforge.com/minecraft/mc-mods/manascore) | Tensura's skill storage APIs |

Made for the **Tensura RPG** pack (Minecraft 1.21.1, NeoForge).

## Credits

- [Great Sage - Tensura Reincarnated Addon](https://modrinth.com/mod/great-sage)
  by dedek - the analysis, the Sage screens and the progression this grows.
- [Tensura: Reincarnated](https://www.curseforge.com/minecraft/mc-mods/tensura-reincarnated)
  by ManasMods - the magic families and their skills.
- [ManasCore](https://www.curseforge.com/minecraft/mc-mods/manascore) by
  ManasMods - the skill storage APIs.

This addon embeds no third-party assets, textures, models or code.

## License

GNU Lesser General Public License v2.1 - see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.

## AI disclosure

Parts of this project were written with the assistance of AI coding tools.
AI-assisted code, data and documentation ship only after human review by the
maintainers.
