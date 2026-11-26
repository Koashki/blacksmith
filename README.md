# Blacksmith (NeoForge 1.21.1)

An AI-assisted Minecraft Java mod implementing active blacksmithing, dynamic item grading, and material synthesis inspired by *Overgeared* and *Pick Me Up, Infinite Gacha*.

## Tech Stack & Target Environment
* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge (Latest 1.21.1 MDK)
* **Java Version:** JDK 21
* **Data Persistence:** NeoForge Data Components (`DataComponentType`) for item ratings, synthesis tiers, and custom attributes (replacing legacy NBT tags).

---

## Core Mechanics

### 1. Active Forging & Item Rating (*Overgeared*)
Standard crafting recipes are replaced or augmented by an interactive **Forging Anvil** workstation.
* **Forging Minigame (GUI + World Interaction):**
  * Players heat ingots/alloys in a **Blast Forge** to an optimal temperature window (`Optimal Temp: 850°C - 950°C`).
  * On the **Forging Anvil**, players execute a rhythm/timing strike sequence (e.g., *Light Tap*, *Heavy Strike*, *Quench*) within a shrinking target zone.
* **Item Rating Tiers (Data Component: `blacksmith:item_grade`):**
  * **Normal (Gray):** Base stats (1.0x multiplier).
  * **Rare (Blue):** +15% Attack Damage / Armor / Mining Speed.
  * **Epic (Purple):** +35% stats + 1 random minor trait (e.g., *Keen Edge*, *Featherweight*).
  * **Unique (Gold):** +60% stats + 1 active/passive skill trigger + custom emissive tooltip border.
  * **Legendary / Mythic (Orange/Red):** +100% stats + unique weapon skill + world-wide chat/sound broadcast upon creation (requires near-perfect minigame score + high-tier materials).

### 2. Material Synthesis & Soul Infusion (*Pick Me Up*)
Inspired by *Pick Me Up*'s Synthesis Altar where low-tier gear, monster cores, and scrap are sacrificed to ascend items or roll new blueprints.
* **Synthesis Chamber Block:**
  * Accepts 1 **Base Item** + up to 4 **Catalyst Materials** (mob drops, enchanted books, or lower-grade forged weapons).
  * Displays calculated **Success Probability (%)** and potential **Ascension Output** before committing.
* **Mechanic Loops:**
  * **Star Ascension (`★1` to `★5`):** Fusing duplicate or same-tier weapons increases the item's Star Rank, unlocking higher stat caps and extra socket slots.
  * **Essence Extraction:** Dismantling failed/Normal-grade items yields *Refining Dust* and *Trait Shards*, ensuring failed forging attempts still contribute to progression.

---

## Package Architecture (`com.<yourname>.blacksmith`)

* `core/` — Mod initialization, DeferredRegisters, Creative Tab, and Configs.
* `component/` — Custom `DataComponentType` records (`ItemGradeData`, `SynthesisStarData`, `ForgingHeatData`).
* `block/` & `blockentity/` — `ForgingAnvilBlock`, `BlastForgeBlock`, `SynthesisChamberBlock`, and their syncing `BlockEntity` classes.
* `menu/` & `client/screen/` — `AbstractContainerMenu` and custom GUI screens (including the Forging Minigame canvas and Synthesis Altar UI).
* `network/` — `CustomPacketPayload` records for client-to-server minigame hit validation and synthesis execution (server-authoritative to prevent packet spoofing).
* `recipe/` — Custom JSON recipe types (`forging` and `synthesis`) so recipes are fully datapack-compatible.

---

## AI Coding Conventions for This Repo
1. **1.21.1 Data Components Only:** Never use `ItemStack#
