package com.koashki.blacksmith.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfigs {
    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
        SERVER = new Server(serverBuilder);
        SERVER_SPEC = serverBuilder.build();

        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    private ModConfigs() {}

    public static final class Server {
        public final ModConfigSpec.BooleanValue replaceVanillaMetalRecipes;
        public final ModConfigSpec.DoubleValue optimalTempMinC;
        public final ModConfigSpec.DoubleValue optimalTempMaxC;
        public final ModConfigSpec.BooleanValue broadcastLegendaryForging;
        public final ModConfigSpec.DoubleValue baseLegendaryBreakthroughChance;
        public final ModConfigSpec.DoubleValue maxLegendaryBreakthroughChance;
        public final ModConfigSpec.DoubleValue baseMythicBreakthroughChance;
        public final ModConfigSpec.DoubleValue maxMythicBreakthroughChance;
        public final ModConfigSpec.IntValue maxProficiencyLevel;

        Server(ModConfigSpec.Builder builder) {
            builder.push("progression");
            replaceVanillaMetalRecipes = builder
                    .comment("If true, vanilla Iron and Gold tool/weapon/armor crafting table recipes are disabled in favor of the Forging Anvil.")
                    .define("replaceVanillaMetalRecipes", true);
            builder.pop();

            builder.push("forging");
            optimalTempMinC = builder
                    .comment("Minimum optimal forging temperature in Celsius.")
                    .defineInRange("optimalTempMinC", 850.0, 200.0, 2000.0);
            optimalTempMaxC = builder
                    .comment("Maximum optimal forging temperature in Celsius.")
                    .defineInRange("optimalTempMaxC", 950.0, 300.0, 2500.0);
            broadcastLegendaryForging = builder
                    .comment("If true, forging a Legendary or Mythic creation broadcasts a server-wide announcement and sound.")
                    .define("broadcastLegendaryForging", true);
            builder.pop();

            builder.push("proficiency");
            maxProficiencyLevel = builder
                    .comment("Maximum Blacksmith Proficiency level.")
                    .defineInRange("maxProficiencyLevel", 50, 10, 200);
            baseLegendaryBreakthroughChance = builder
                    .comment("Base Legendary breakthrough chance (0.0 - 1.0) on a 97-100 forging score at Level 1.")
                    .defineInRange("baseLegendaryBreakthroughChance", 0.05, 0.0, 1.0);
            maxLegendaryBreakthroughChance = builder
                    .comment("Maximum Legendary breakthrough chance (0.0 - 1.0) at max Blacksmith Proficiency.")
                    .defineInRange("maxLegendaryBreakthroughChance", 0.45, 0.0, 1.0);
            baseMythicBreakthroughChance = builder
                    .comment("Base Mythic breakthrough chance (0.0 - 1.0) on a 99-100 forging score at Level 1.")
                    .defineInRange("baseMythicBreakthroughChance", 0.01, 0.0, 1.0);
            maxMythicBreakthroughChance = builder
                    .comment("Maximum Mythic breakthrough chance (0.0 - 1.0) at max Blacksmith Proficiency.")
                    .defineInRange("maxMythicBreakthroughChance", 0.12, 0.0, 1.0);
            builder.pop();
        }
    }

    public static final class Client {
        public final ModConfigSpec.BooleanValue showEmissiveTooltipBorders;
        public final ModConfigSpec.BooleanValue showDebugAspectWeights;

        Client(ModConfigSpec.Builder builder) {
            builder.push("visuals");
            showEmissiveTooltipBorders = builder
                    .comment("Render custom colored/emissive tooltip borders for Unique, Legendary, and Mythic creations.")
                    .define("showEmissiveTooltipBorders", true);
            showDebugAspectWeights = builder
                    .comment("Display raw numeric aspect weights in item tooltips when holding Shift.")
                    .define("showDebugAspectWeights", false);
            builder.pop();
        }
    }
}
