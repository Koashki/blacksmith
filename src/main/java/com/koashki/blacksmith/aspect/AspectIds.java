package com.koashki.blacksmith.aspect;

import com.koashki.blacksmith.core.BlacksmithMod;
import net.minecraft.resources.ResourceLocation;

public final class AspectIds {
    private AspectIds() {}

    // Physical / Elemental Aspects
    public static final ResourceLocation THERMAL = BlacksmithMod.id("thermal");
    public static final ResourceLocation CRYO = BlacksmithMod.id("cryo");
    public static final ResourceLocation VOLTAIC = BlacksmithMod.id("voltaic");
    public static final ResourceLocation KINETIC = BlacksmithMod.id("kinetic");
    public static final ResourceLocation RESONANT = BlacksmithMod.id("resonant");
    public static final ResourceLocation RADIANT = BlacksmithMod.id("radiant");
    public static final ResourceLocation ABYSSAL = BlacksmithMod.id("abyssal");
    public static final ResourceLocation CAUSTIC = BlacksmithMod.id("caustic");

    // Structural Aspects
    public static final ResourceLocation RAZOR = BlacksmithMod.id("razor");
    public static final ResourceLocation DENSE = BlacksmithMod.id("dense");
    public static final ResourceLocation ELASTIC = BlacksmithMod.id("elastic");
    public static final ResourceLocation BRITTLE = BlacksmithMod.id("brittle");
    public static final ResourceLocation FEATHERWEIGHT = BlacksmithMod.id("featherweight");
    public static final ResourceLocation ADAPTIVE = BlacksmithMod.id("adaptive");

    // Conceptual Aspects
    public static final ResourceLocation SPATIAL = BlacksmithMod.id("spatial");
    public static final ResourceLocation TEMPORAL = BlacksmithMod.id("temporal");
    public static final ResourceLocation SIPHONING = BlacksmithMod.id("siphoning");
    public static final ResourceLocation VOLATILE = BlacksmithMod.id("volatile");
    public static final ResourceLocation HARMONIC = BlacksmithMod.id("harmonic");
    public static final ResourceLocation EXECUTION = BlacksmithMod.id("execution");
    public static final ResourceLocation PHASING = BlacksmithMod.id("phasing");
    public static final ResourceLocation EGO = BlacksmithMod.id("ego");

    // Delivery / Behavior Aspects
    public static final ResourceLocation STORING = BlacksmithMod.id("storing");
    public static final ResourceLocation TETHERING = BlacksmithMod.id("tethering");
    public static final ResourceLocation SPLITTING = BlacksmithMod.id("splitting");
    public static final ResourceLocation RETURNING = BlacksmithMod.id("returning");
    public static final ResourceLocation SEEKING = BlacksmithMod.id("seeking");

    // Triggers
    public static final ResourceLocation TRIGGER_ON_HIT = BlacksmithMod.id("on_hit");
    public static final ResourceLocation TRIGGER_ON_CRIT = BlacksmithMod.id("on_crit");
    public static final ResourceLocation TRIGGER_ON_RIGHT_CLICK = BlacksmithMod.id("on_right_click");
    public static final ResourceLocation TRIGGER_ON_KILL = BlacksmithMod.id("on_kill");

    // Deliveries
    public static final ResourceLocation DELIVERY_TARGET = BlacksmithMod.id("target");
    public static final ResourceLocation DELIVERY_AOE_SHOCKWAVE = BlacksmithMod.id("aoe_shockwave");
    public static final ResourceLocation DELIVERY_CHAIN_ARC = BlacksmithMod.id("chain_arc");
    public static final ResourceLocation DELIVERY_BLINK_DASH = BlacksmithMod.id("blink_dash");
    public static final ResourceLocation DELIVERY_DELAYED_ECHO = BlacksmithMod.id("delayed_echo");
    public static final ResourceLocation DELIVERY_SELF_BUFF = BlacksmithMod.id("self_buff");

    // Effects
    public static final ResourceLocation EFFECT_IGNITE = BlacksmithMod.id("ignite");
    public static final ResourceLocation EFFECT_FROST = BlacksmithMod.id("frost");
    public static final ResourceLocation EFFECT_LIGHTNING = BlacksmithMod.id("lightning");
    public static final ResourceLocation EFFECT_LAUNCH = BlacksmithMod.id("launch");
    public static final ResourceLocation EFFECT_SONIC_STUN = BlacksmithMod.id("sonic_stun");
    public static final ResourceLocation EFFECT_POISON = BlacksmithMod.id("poison");
    public static final ResourceLocation EFFECT_LIFESTEAL = BlacksmithMod.id("lifesteal");
    public static final ResourceLocation EFFECT_EXPLODE = BlacksmithMod.id("explode");
    public static final ResourceLocation EFFECT_ARMOR_SHRED = BlacksmithMod.id("armor_shred");
    public static final ResourceLocation EFFECT_HASTE = BlacksmithMod.id("haste");
    public static final ResourceLocation EFFECT_VOID_PULL = BlacksmithMod.id("void_pull");

    // Emergent Reactions
    public static final ResourceLocation REACTION_PHANTOM_CUT = BlacksmithMod.id("phantom_cut");
    public static final ResourceLocation REACTION_THUNDERSTEP = BlacksmithMod.id("thunderstep");
    public static final ResourceLocation REACTION_SHATTERWAVE = BlacksmithMod.id("shatterwave");
    public static final ResourceLocation REACTION_SEISMIC_REBOUND = BlacksmithMod.id("seismic_rebound");
    public static final ResourceLocation REACTION_BLOOD_BURST = BlacksmithMod.id("blood_burst");
    public static final ResourceLocation REACTION_FORGED_DETONATION = BlacksmithMod.id("forged_detonation");

    // Instability Quirks
    public static final ResourceLocation QUIRK_RECOIL = BlacksmithMod.id("recoil");
    public static final ResourceLocation QUIRK_HUNGER_DRAIN = BlacksmithMod.id("hunger_drain");
    public static final ResourceLocation QUIRK_OVERHEAT = BlacksmithMod.id("overheat");
}
