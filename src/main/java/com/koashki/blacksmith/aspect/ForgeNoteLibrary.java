package com.koashki.blacksmith.aspect;

import com.koashki.blacksmith.core.BlacksmithMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ForgeNoteLibrary {
    public record NoteEntry(ResourceLocation id, String title, String clueText) {}

    private static final Map<ResourceLocation, NoteEntry> NOTES = new LinkedHashMap<>();

    static {
        add(BlacksmithMod.id("note_phantom_cut"),
                "Margin Scrap: The Second Shadow",
                "\"A sharp flint edge bites once, yet brass gears remember the motion. Fold a ticking timepiece with a keen blade and the air itself cuts a heartbeat later.\"");
        add(BlacksmithMod.id("note_thunderstep"),
                "Charred Page: Walking the Arc",
                "\"Copper drinks the storm; pearls of the End drink distance. Bound together at white heat, the blade tore me through the air in a streak of blue lightning.\"");
        add(BlacksmithMod.id("note_shatterwave"),
                "Cracked Sketch: The Singing Glass",
                "\"The purple crystal sang when struck, yet shattered at the edge. When I folded a temple bell's bronze into its brittle heart, the resonance broke the foe's armor instead.\"");
        add(BlacksmithMod.id("note_seismic_rebound"),
                "Soot-Stained Note: Heavy Bounce",
                "\"Iron's weight drags a hammer down, yet bog-slime refuses to stay still. Quench them as one and the ground throws back everything it strikes.\"");
        add(BlacksmithMod.id("note_blood_burst"),
                "Warning Fragment: The Hungry Spark",
                "\"Never bind carrion and sulfur without steadying the anvil. The metal drinks life from the fallen, then erupts — volatile, ravenous, and terrifying.\"");
        add(BlacksmithMod.id("note_forged_detonation"),
                "Smudge of Ash: Crucible Thunder",
                "\"When blaze-fire meets raw kinetic force inside a heavy billet, a clean strike does not merely cleave — it detonates like a furnace bursting its seams.\"");
    }

    private ForgeNoteLibrary() {}

    private static void add(ResourceLocation id, String title, String clueText) {
        NOTES.put(id, new NoteEntry(id, title, clueText));
    }

    public static Optional<NoteEntry> get(ResourceLocation id) {
        return Optional.ofNullable(NOTES.get(id));
    }

    public static List<NoteEntry> all() {
        return List.copyOf(NOTES.values());
    }

    public static NoteEntry randomNote(RandomSource random) {
        List<NoteEntry> entries = all();
        return entries.get(random.nextInt(entries.size()));
    }
}
