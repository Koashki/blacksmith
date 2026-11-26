# Blacksmith (NeoForge 1.21.1)

An AI-assisted Minecraft Java mod built around a **Universal Creation Engine** — active blacksmithing, freeform material infusion, emergent skill composition, dynamic item grading, and material synthesis inspired by *Overgeared* and *Pick Me Up, Infinite Gacha*.

## Tech Stack & Target Environment
* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge (`21.1.255` via ModDevGradle `2.0.148`)
* **Java Version:** JDK 21
* **Data Persistence:** NeoForge 1.21.1 Data Components (`DataComponentType`) for item grades, molten heat, forged composition, composed abilities, and synthesis stars, plus `AttachmentType` (`copyOnDeath`) for player Blacksmith Proficiency and discovered blueprints.

---

## Core Mechanics

### 1. Universal Creation Engine (*Create Anything From Anything*)
Instead of rigid 1-to-1 recipes for fixed vanilla gear, any item in Minecraft (or other mods) carries forgeable DNA:
* **4-Dimensional Datapack-Driven Aspects (`data/<ns>/blacksmith/aspects/`):**
  * **Physical / Elemental:** *Thermal, Cryo, Voltaic, Kinetic, Resonant, Radiant, Abyssal, Caustic*
  * **Structural:** *Razor, Dense, Elastic, Brittle, Featherweight, Adaptive*
  * **Conceptual:** *Spatial, Temporal, Siphoning, Volatile, Harmonic, Execution, Phasing, Ego*
  * **Delivery / Behavior:** *Storing, Tethering, Splitting, Returning, Seeking*
* **Universal Fallback Analyzer:** Curated item mappings are loaded from JSON datapacks, while any unmapped vanilla or modded item automatically derives Aspects, Instability, and tint colors from its properties (burn time, blast resistance, food/potion data, rarity, etc.). Nothing is ever unforgeable.
* **Emergent Aspect Reactions & Skill Grammar (`[Trigger] + [Delivery] + [Effect]`):**
  * Combining complementary Aspects triggers emergent Reactions such as **Phantom Cut** (*Temporal + Razor*), **Thunderstep** (*Voltaic + Spatial*), **Shatterwave** (*Resonant + Brittle*), **Seismic Rebound** (*Dense + Elastic*), **Blood-Burst** (*Siphoning + Volatile*), and **Forged Detonation** (*Thermal + Kinetic*).
  * Unstabilized **Instability** can imprint *Pick Me Up*-style **Flaws/Quirks** (*Recoil*, *Hunger Drain*, *Overheat*).
* **Modular Forged Archetypes:** Shape molten alloys into **Sword**, **Greatsword**, **Dagger**, **Spear**, or **War Hammer** with dynamic procedural tinting derived from the infused materials.

### 2. Active Forging, Grading & Proficiency Breakthrough (*Overgeared*)
* **Early-Game Progression:** Wood & Stone tools remain in the Crafting Table. Base metal progression starts with an affordable **Stone Forging Hammer**, **Blast Forge** (Furnace + Smooth Stone + 3 Iron), and **Forging Anvil** (Smooth Stone + 7 Iron), with a `replaceVanillaMetalRecipes` server config toggle. *(Initial material scope focuses on **Iron & Gold**; Diamond/Netherite and expanded material packs are designed as datapack/follow-up expansions.)*
* **Blast Forge (Heat & Bellows):**
  * Combine a Base Metal + up to **4 Wildcard Items** and heat toward the optimal window (`Optimal Temp: 850°C – 950°C`).
  * Pump the **Bellows** (GUI button or shift-right-click) for controlled `+60°C` bursts, then **Pour** a glowing `Molten Alloy Billet`.
* **Forging Anvil (Cadence Minigame + Over-Forge Gamble):**
  * Select a weapon form and execute a server-authoritative strike cadence (**Light Tap** `A`, **Heavy Strike** `D`, **Stabilize** `S`, **Quench** `Space`) within a shrinking target zone.
  * **Over-Forge Gamble:** On a strong base forge (`70+` score), choose to **Quench & Keep** safely or **Over-Forge** in a faster bonus round to push for higher tiers at the risk of cracking your score.
* **Item Rating Tiers (`blacksmith:item_grade`) & Breakthrough Roll:**
  * **Normal (Gray):** `1.0x` base stats (failed attempts never destroy your materials).
  * **Rare (Blue):** `+15%` Attack Damage / Armor / Mining Speed.
  * **Epic (Purple):** `+35%` stats + `1` composed ability / reaction.
  * **Unique (Gold):** `+60%` stats + `2` abilities + custom emissive Gold tooltip border.
  * **Legendary (Orange):** `+100%` stats + `3` abilities + server-wide broadcast & sound.
  * **Mythic (Red):** `+125%` stats + `4` abilities + server-wide broadcast & sound.
  * **Blacksmith Proficiency:** Every forge grants Blacksmith XP. Hitting a `97–100` score guarantees at least **Unique** and triggers a **Breakthrough Roll** into **Legendary / Mythic** whose probability scales with your Blacksmith Proficiency level (and *Refining Dust* catalysts).

### 3. Material Synthesis, Ability Splicing & Essence Extraction (*Pick Me Up*)
* **Synthesis Chamber Block:**
  * Accepts `1` **Base Creation** + up to `4` **Catalysts** (sacrificed forged creations, *Refining Dust*, *Aspect Essence*, or wildcard items) and displays live **Success Probability (%)** before committing.
* **Mechanic Loops:**
  * **Star Ascension (`★1` to `★5`) & Ability Splicing:** Upgrading Star Rank boosts stats (`+8%` per star) and can splice unique abilities from sacrificed creations onto your main weapon.
  * **Essence Extraction:** Dismantling unwanted or Normal-grade creations yields *Refining Dust* and *Aspect Essence* preserving their extracted Aspects.

---

## Package Architecture (`com.koashki.blacksmith`)

* `core/` — Mod entrypoint, `DeferredRegister` registries (`ModBlocks`, `ModItems`, `ModBlockEntities`, `ModMenus`, `ModDataComponents`, `ModAttachments`, `ModCreativeTabs`), and `ModConfigs`.
* `component/` — Immutable 1.21.1 `DataComponentType` records (`GradeTier`, `ItemGradeData`, `ForgingHeatData`, `SynthesisStarData`, `ForgedCompositionData`, `ComposedAbilitiesData`) and player `AttachmentType` records (`BlacksmithProficiencyData`, `DiscoveredBlueprintsData`).
* `aspect/` — 4-dimensional datapack-driven Aspect registry, universal item fallback analyzer, emergent Reaction resolver, and `SkillComposer`.
* `trait/` — `GradingEngine` (proficiency Breakthrough Roll & Masterpiece broadcasts), `StatScalingHandler` (`ItemAttributeModifierEvent`), and `AbilityExecutionHandler`.
* `block/` & `blockentity/` — `BlastForgeBlock`, `ForgingAnvilBlock`, `SynthesisChamberBlock`, and their ticking/syncing `BlockEntity` classes.
* `item/` — `ForgedArchetype`, `ForgedWeaponItem`, `MoltenAlloyItem`, `ForgingHammerItem`, `RefiningDustItem`, `AspectEssenceItem`.
* `menu/` & `client/` — `AbstractContainerMenu` implementations, procedural GUI screens (`BlastForgeScreen`, `ForgingAnvilScreen`, `SynthesisChamberScreen`), dynamic `ItemColor` tinting, and emissive tooltip renderers.
* `network/` — Server-authoritative `CustomPacketPayload` records (`WorkstationActionPayload`, `MasterpieceNamePayload`, `ProficiencySyncPayload`).
* `datagen/` — NeoForge `DataGenerator` providers for blockstates, item models, recipes, loot tables, tags, and `en_us.json`.

---

## Development Commands

```powershell
# Generate blockstates, models, recipes, loot tables, tags, and en_us.json
.\gradlew.bat runData

# Compile and package build/libs/blacksmith-0.1.0.jar
.\gradlew.bat build

# Launch Minecraft 1.21.1 client with the mod loaded
.\gradlew.bat runClient
```

---

## AI Coding Conventions for This Repo
1. **1.21.1 Data Components Only:** Never use `ItemStack#getTag()` or `getOrCreateTag()`. Always define strongly typed `DataComponentType` records with `Codec` and `StreamCodec`.
2. **Server-Authoritative Minigames:** The client renders the forging minigame bar, but hit timing and final `ItemGrade` calculation must be validated on the server via `CustomPacketPayload`.
3. **Data Generation (`runData`):** Generate blockstates, item models, loot tables, tags, and custom forging/synthesis recipes via NeoForge `DataGenerator` rather than hand-writing JSONs.
