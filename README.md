# Blacksmith (NeoForge 1.21.1)

An AI-assisted Minecraft Java mod built around a **Universal Creation Engine** — active blacksmithing, freeform material infusion, diegetic discovery, emergent skill composition, dynamic item grading, Mythic Living Ego weapons, and material synthesis inspired by *Overgeared* and *Pick Me Up, Infinite Gacha*.

## Tech Stack & Target Environment
* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge (`21.1.255` via ModDevGradle `2.0.148`)
* **Java Version:** JDK 21
* **Data Persistence:** NeoForge 1.21.1 Data Components (`DataComponentType`) for item grades, molten heat, forged composition, composed abilities, synthesis stars, forge notes, and living egos, plus `AttachmentType` (`copyOnDeath`) for player Blacksmith Proficiency and the Smith's Ledger.

---

## Core Mechanics

### 1. Universal Creation Engine (*Create Anything From Anything*)
Instead of rigid 1-to-1 recipes for fixed vanilla gear, any item in Minecraft (or other mods) carries forgeable DNA:
* **4-Dimensional Datapack-Driven Aspects (`data/<ns>/blacksmith/aspects/`):**
  * **Physical / Elemental:** *Thermal, Cryo, Voltaic, Kinetic, Resonant, Radiant, Abyssal, Caustic*
  * **Structural:** *Razor, Dense, Elastic, Brittle, Featherweight, Adaptive*
  * **Conceptual:** *Spatial, Temporal, Siphoning, Volatile, Harmonic, Execution, Phasing, Ego*
  * **Delivery / Behavior:** *Storing, Tethering, Splitting, Returning, Seeking*
* **Universal Fallback Analyzer:** Curated starter item mappings are loaded from JSON datapacks, while any unmapped vanilla or modded item automatically derives Aspects, Instability, and tint colors from its properties (burn time, blast resistance, food/potion data, rarity, etc.). Nothing is ever unforgeable.
* **Emergent Aspect Reactions & Skill Grammar (`[Trigger] + [Delivery] + [Effect]`):**
  * Combining complementary Aspects triggers emergent Reactions such as **Phantom Cut** (*Temporal + Razor*), **Thunderstep** (*Voltaic + Spatial*), **Shatterwave** (*Resonant + Brittle*), **Seismic Rebound** (*Dense + Elastic*), **Blood-Burst** (*Siphoning + Volatile*), and **Forged Detonation** (*Thermal + Kinetic*).
  * Unstabilized **Instability** can imprint *Pick Me Up*-style **Flaws/Quirks** (*Recoil*, *Hunger Drain*, *Overheat*).
* **10 Modular Forged Archetypes (Custom 3-Layer 16×16 Pixel Art):**
  * **Weapons:** *Sword, Greatsword, Dagger, Spear, War Hammer*
  * **Survival Tools & Shield:** *Pickaxe, Axe, Shovel, Hoe, Reactive Shield* (blocking triggers composed abilities back at attackers)
  * Every forged creation renders with dynamic multi-layer tinting (`layer0` base metal, `layer1` infused runes/accent, `layer2` untinted wood/leather handle).

### 2. Diegetic Discovery Loop (*No Spoiler Numbers*)
* **Diegetic Blast Forge:** Read your alloy while heating through physical **Aspect-tinted flames**, **erratic electric/lava sparks** as instability rises, and **subtle audio cues** (amethyst chimes, crackles, portal hums, ticks, hisses).
* **Smith's Ledger (`Book + Iron Nugget`):** A 4-tab parchment journal (**Materials**, **Reactions**, **Notes**, **Legends**) that permanently records every material you forge with or assay via Essence Extraction, every Reaction you unlock, every Torn Forge Note you read, and every named Masterpiece you inscribe. Holding `Shift` over a discovered material reveals its recorded Aspects; uncharted materials stay clean.
* **Torn Forge Notes:** 6 poetic master-smith journal scraps hinting at hidden Reactions — found in **Simple Dungeon**, **Abandoned Mineshaft**, and **Bastion Treasure** chests, or traded by **Apprentice Weaponsmiths**.

### 3. Active Forging, Over-Forge Lattice, Grading & Living Ego (*Overgeared* + *Pick Me Up*)
* **Early-Game Survival Progression:** Wood & Stone tools remain in the Crafting Table. Base metal progression starts with an affordable **Stone Forging Hammer**, **Blast Forge** (Furnace + Smooth Stone + 3 Iron), and **Forging Anvil** (Smooth Stone + 7 Iron). Vanilla Iron/Gold tool, weapon, and shield Crafting Table recipes are routed through the Forging Anvil when `replaceVanillaMetalRecipes = true` in `blacksmith-server.toml`.
* **Blast Forge (Heat, Bellows & Vent):**
  * Combine a Base material + up to **4 Wildcard Items** and balance temperature inside the `850°C – 950°C` optimal band using **Bellows** (`+60°C`) and **Vent** (`-80°C`), then **Pour** a glowing `Molten Alloy Billet`.
* **Forging Anvil (3D Workpiece Renderer + Smooth Cadence + Over-Forge Modal):**
  * Displays your glowing billet or finished creation flat on top of the anvil via a 3D `BlockEntityRenderer`.
  * Execute a smooth, frame-interpolated, server-authoritative strike cadence (**Light** `A`, **Heavy** `D`, **Stabilize** `S`, **Quench** `Space`) within a shrinking target zone.
  * **Over-Forge Gamble (Pop-Out 3×3 Molten Lattice):** On a `70+` base forge, push your luck in a dedicated, centered *Pick Me Up*-inspired 3×3 cross-flip spatial puzzle (`6` moves, `12s` cooling timer) for a massive score & Breakthrough boost — or risk fracturing your score.
* **Item Rating Tiers (`blacksmith:item_grade`), Proficiency Breakthrough & Masterpiece Naming:**
  * **Normal (Gray):** `1.0x` base stats (failed attempts never destroy your materials).
  * **Rare (Blue):** `+15%` Attack Damage / Armor / Mining Speed.
  * **Epic (Purple):** `+35%` stats + `1` composed ability / reaction.
  * **Unique (Gold):** `+60%` stats + `2` abilities + custom emissive Gold tooltip border.
  * **Legendary (Orange):** `+100%` stats + `3` abilities + Masterpiece Naming modal + server-wide broadcast & sound.
  * **Mythic (Red):** `+125%` stats + `4` abilities + Masterpiece Naming modal + server-wide broadcast + **Living Ego**.
  * **Blacksmith Proficiency:** Every forge grants Blacksmith XP. A `97–100` score triggers a **Breakthrough Roll** into **Legendary / Mythic** scaling with your Proficiency level and *Refining Dust* catalysts.
  * **Mythic Living Ego (`blacksmith:ego_data`):** Mythic creations awaken with one of 4 temperaments (*Proud, Fierce, Serene, Mischievous*), level from `Lv.1` to `Lv.10` via kills (`+3%` bonus stats per level), and whisper atmospheric lines in your action bar.

### 4. Material Synthesis, Ability Splicing & Essence Extraction (*Pick Me Up*)
* **Synthesis Chamber Block:**
  * Accepts `1` **Base Creation** + up to `4` **Catalysts** (sacrificed creations, *Refining Dust*, *Aspect Essence*, or wildcard items) and displays live **Success Probability (%)** before committing.
  * **Star Ascension (`★1` to `★5`) & Ability Splicing:** Ascends star rank (`+8%` stats per star) and splices unique abilities from sacrificed creations onto your main gear.
  * **Essence Extraction:** Dismantles unwanted creations into *Refining Dust* and *Aspect Essence* while assaying their materials into your **Smith's Ledger**.

---

## Package & Resource Architecture (`com.koashki.blacksmith`)

* `core/` — Mod entrypoint, `DeferredRegister` registries (`ModBlocks`, `ModItems`, `ModBlockEntities`, `ModMenus`, `ModDataComponents`, `ModAttachments`, `ModLootModifiers`, `ModCreativeTabs`), and `ModConfigs`.
* `component/` — Immutable 1.21.1 `DataComponentType` records (`GradeTier`, `ItemGradeData`, `ForgingHeatData`, `SynthesisStarData`, `ForgedCompositionData`, `ComposedAbilitiesData`, `ForgeNoteData`, `EgoData`) and player `AttachmentType` records (`BlacksmithProficiencyData`, `DiscoveredBlueprintsData`).
* `aspect/` — 4-dimensional datapack-driven Aspect registry, universal item fallback analyzer, emergent Reaction resolver, `SkillComposer`, `ForgeNoteLibrary`, and `AspectTextUtil`.
* `trait/` — `GradingEngine`, `StatScalingHandler`, `AbilityExecutionHandler`, and `EgoHandler`.
* `block/` & `blockentity/` — `BlastForgeBlock`, `ForgingAnvilBlock`, `SynthesisChamberBlock`, and their ticking/syncing `BlockEntity` classes.
* `item/` — `ForgingHammerItem`, plus organized subpackages `item/forged/` (weapons, tools, shield), `item/material/` (`MoltenAlloyItem`, `RefiningDustItem`, `AspectEssenceItem`), and `item/lore/` (`SmithsLedgerItem`, `TornForgeNoteItem`).
* `recipe/` — `VanillaRecipeOverrideHandler` (`replaceVanillaMetalRecipes` config toggle).
* `loot/` — `AddForgeNoteLootModifier` (dungeon/mineshaft/bastion chests) and `ModVillagerTrades` (Apprentice Weaponsmith).
* `menu/` & `client/` — `AbstractContainerMenu` implementations, `client/screen/` GUIs (`BlastForgeScreen`, `ForgingAnvilScreen`, `SynthesisChamberScreen`, `SmithsLedgerScreen`), `client/renderer/ForgingAnvilRenderer` (3D BER), layered `ItemColor` tinting, and emissive tooltip borders.
* `network/` — Server-authoritative `CustomPacketPayload` records (`WorkstationActionPayload`, `MasterpieceNamePayload`, `ProficiencySyncPayload`, `LedgerSyncPayload`).
* `datagen/` — NeoForge `DataGenerator` providers for blockstates, item models, recipes, loot tables, tags, global loot modifiers, and `en_us.json`.
* `resources/assets/blacksmith/textures/item/` — Categorized 16×16 pixel-art textures under `weapon/`, `tool/`, `hammer/`, `material/`, and `lore/`.

---

## Development Commands

```powershell
# Generate blockstates, models, recipes, loot tables, tags, loot modifiers, and en_us.json
.\gradlew.bat runData

# Compile and package build/libs/blacksmith-0.1.0.jar
.\gradlew.bat build

# Launch Minecraft 1.21.1 client with the mod loaded
.\gradlew.bat runClient
```

---

## AI Coding Conventions for This Repo
1. **1.21.1 Data Components Only:** Never use `ItemStack#getTag()` or `getOrCreateTag()`. Always define strongly typed `DataComponentType` records with `Codec` and `StreamCodec`.
2. **Server-Authoritative Minigames:** The client renders the forging minigame bar and Over-Forge lattice, but hit timing, lattice moves, and final `ItemGrade` calculation must be validated on the server via `CustomPacketPayload`.
3. **Data Generation (`runData`):** Generate blockstates, item models, loot tables, tags, global loot modifiers, and recipes via NeoForge `DataGenerator` rather than hand-writing JSONs.
