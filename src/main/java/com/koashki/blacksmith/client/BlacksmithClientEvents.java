package com.koashki.blacksmith.client;

import com.koashki.blacksmith.aspect.AspectRegistryManager;
import com.koashki.blacksmith.aspect.ItemAspectMapping;
import com.koashki.blacksmith.client.renderer.ForgingAnvilRenderer;
import com.koashki.blacksmith.client.screen.BlastForgeScreen;
import com.koashki.blacksmith.client.screen.ForgingAnvilScreen;
import com.koashki.blacksmith.client.screen.SmithsLedgerScreen;
import com.koashki.blacksmith.client.screen.SynthesisChamberScreen;
import com.koashki.blacksmith.component.ComposedAbilitiesData;
import com.koashki.blacksmith.component.DiscoveredBlueprintsData;
import com.koashki.blacksmith.component.ForgedCompositionData;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.component.SynthesisStarData;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModAttachments;
import com.koashki.blacksmith.core.ModBlockEntities;
import com.koashki.blacksmith.core.ModConfigs;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.core.ModItems;
import com.koashki.blacksmith.core.ModMenus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.stream.Collectors;

public final class BlacksmithClientEvents {
    private BlacksmithClientEvents() {}

    public static void openLedgerScreen() {
        Minecraft.getInstance().setScreen(new SmithsLedgerScreen());
    }

    @EventBusSubscriber(modid = BlacksmithMod.MOD_ID, value = Dist.CLIENT)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.BLAST_FORGE.get(), BlastForgeScreen::new);
            event.register(ModMenus.FORGING_ANVIL.get(), ForgingAnvilScreen::new);
            event.register(ModMenus.SYNTHESIS_CHAMBER.get(), SynthesisChamberScreen::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.FORGING_ANVIL.get(), ForgingAnvilRenderer::new);
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                if (tintIndex >= 2) {
                    return 0xFFFFFFFF; // Untinted wooden/leather handle layer
                }
                ForgedCompositionData comp = stack.get(ModDataComponents.FORGED_COMPOSITION.get());
                if (comp == null) {
                    return tintIndex == 0 ? 0xFFD8D8DC : 0xFFFFAA33;
                }
                int rgb = tintIndex == 0 ? comp.primaryColor() : comp.accentColor();
                return 0xFF000000 | (rgb & 0x00FFFFFF);
            }, ModItems.FORGED_SWORD.get(), ModItems.FORGED_GREATSWORD.get(), ModItems.FORGED_DAGGER.get(),
                    ModItems.FORGED_SPEAR.get(), ModItems.FORGED_WAR_HAMMER.get(),
                    ModItems.FORGED_PICKAXE.get(), ModItems.FORGED_AXE.get(), ModItems.FORGED_SHOVEL.get(),
                    ModItems.FORGED_HOE.get(), ModItems.FORGED_SHIELD.get(),
                    ModItems.MOLTEN_ALLOY.get(), ModItems.ASPECT_ESSENCE.get());
        }
    }

    @EventBusSubscriber(modid = BlacksmithMod.MOD_ID, value = Dist.CLIENT)
    public static final class GameBusEvents {
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
            if (grade != null) {
                int bonusPct = Math.round(grade.tier().bonusMultiplier() * 100.0f);
                event.getToolTip().add(Component.literal("Grade: ").withStyle(ChatFormatting.GRAY)
                        .append(grade.tier().displayName())
                        .append(bonusPct > 0
                                ? Component.literal(" (+" + bonusPct + "% Stats)").withStyle(ChatFormatting.GREEN)
                                : Component.empty()));

                grade.forgedBy().ifPresent(smith ->
                        event.getToolTip().add(Component.literal("Forged by " + smith).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
            }

            SynthesisStarData stars = stack.get(ModDataComponents.SYNTHESIS_STAR.get());
            if (stars != null && stars.stars() > 0) {
                String filled = "★".repeat(stars.stars());
                String empty = "☆".repeat(SynthesisStarData.MAX_STARS - stars.stars());
                event.getToolTip().add(Component.literal("Ascension: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(filled).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(empty).withStyle(ChatFormatting.DARK_GRAY)));
            }

            ComposedAbilitiesData abilities = stack.get(ModDataComponents.COMPOSED_ABILITIES.get());
            if (abilities != null && !abilities.abilities().isEmpty()) {
                for (ComposedAbilitiesData.ComposedAbility ability : abilities.abilities()) {
                    String label = ability.reactionId()
                            .map(id -> "⚡ Reaction: " + prettify(id))
                            .orElse("◆ " + prettify(ability.trigger()) + " → " + prettify(ability.effect()));
                    event.getToolTip().add(Component.literal(label)
                            .withStyle(ability.reactionId().isPresent() ? ChatFormatting.GOLD : ChatFormatting.AQUA));
                }
                for (ResourceLocation quirk : abilities.quirks()) {
                    event.getToolTip().add(Component.literal("⚠ Instability Flaw: " + prettify(quirk)).withStyle(ChatFormatting.RED));
                }
            }

            // Show discovered material Aspects when holding Shift (silent for uncharted items)
            if (Screen.hasShiftDown() && grade == null && !stack.isEmpty() && event.getEntity() != null) {
                DiscoveredBlueprintsData ledger = event.getEntity().getData(ModAttachments.DISCOVERED_BLUEPRINTS);
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (ledger.hasItem(itemId)) {
                    ItemAspectMapping mapping = AspectRegistryManager.analyzeItem(stack);
                    String aspectList = mapping.aspects().keySet().stream()
                            .map(GameBusEvents::prettify)
                            .collect(Collectors.joining(", "));
                    event.getToolTip().add(Component.literal("Ledger Aspects: ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(aspectList).withStyle(ChatFormatting.YELLOW)));
                }
            }
        }

        @SubscribeEvent
        public static void onTooltipColor(RenderTooltipEvent.Color event) {
            if (!ModConfigs.CLIENT.showEmissiveTooltipBorders.get()) {
                return;
            }
            ItemGradeData grade = event.getItemStack().get(ModDataComponents.ITEM_GRADE.get());
            if (grade != null && grade.tier().hasEmissiveBorder()) {
                int rgb = grade.tier().colorRgb();
                event.setBorderStart(0xFF000000 | rgb);
                event.setBorderEnd(0xAA000000 | rgb);
            }
        }

        private static String prettify(ResourceLocation id) {
            String[] parts = id.getPath().split("_");
            StringBuilder sb = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty()) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
                }
            }
            return sb.toString();
        }
    }
}
