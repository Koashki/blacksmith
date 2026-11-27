package com.koashki.blacksmith.client.screen;

import com.koashki.blacksmith.aspect.AspectReactionDefinition;
import com.koashki.blacksmith.aspect.AspectRegistryManager;
import com.koashki.blacksmith.aspect.AspectTextUtil;
import com.koashki.blacksmith.aspect.ForgeNoteLibrary;
import com.koashki.blacksmith.aspect.ItemAspectMapping;
import com.koashki.blacksmith.component.DiscoveredBlueprintsData;
import com.koashki.blacksmith.core.ModAttachments;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SmithsLedgerScreen extends Screen {
    private static final int BOOK_W = 244;
    private static final int BOOK_H = 196;

    private int activeTab = 0; // 0 = Materials, 1 = Reactions, 2 = Forge Notes, 3 = Legends
    private int page = 0;

    public SmithsLedgerScreen() {
        super(Component.translatable("item.blacksmith.smiths_ledger"));
    }

    @Override
    protected void init() {
        int left = (width - BOOK_W) / 2;
        int top = (height - BOOK_H) / 2;

        addRenderableWidget(Button.builder(Component.literal("Materials"), b -> switchTab(0))
                .bounds(left + 8, top + 22, 55, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Reactions"), b -> switchTab(1))
                .bounds(left + 66, top + 22, 55, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Notes"), b -> switchTab(2))
                .bounds(left + 124, top + 22, 55, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Legends"), b -> switchTab(3))
                .bounds(left + 182, top + 22, 54, 16).build());

        addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> page = Math.max(0, page - 1))
                .bounds(left + 12, top + BOOK_H - 24, 52, 16).build());
        addRenderableWidget(Button.builder(Component.literal("Next >"), b -> page++)
                .bounds(left + BOOK_W - 64, top + BOOK_H - 24, 52, 16).build());
    }

    private void switchTab(int tab) {
        this.activeTab = tab;
        this.page = 0;
    }

    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gui, mouseX, mouseY, partialTick);
        int left = (width - BOOK_W) / 2;
        int top = (height - BOOK_H) / 2;

        // Warm parchment journal background (drawn after 1.21.1 world blur so it stays razor-sharp)
        gui.fill(left, top, left + BOOK_W, top + BOOK_H, 0xFF2C241E);
        gui.fill(left + 5, top + 5, left + BOOK_W - 5, top + BOOK_H - 5, 0xFFF2E6CE);
        gui.renderOutline(left, top, BOOK_W, BOOK_H, 0xFF5A4331);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        int left = (width - BOOK_W) / 2;
        int top = (height - BOOK_H) / 2;

        gui.drawCenteredString(font, "Smith's Ledger", left + BOOK_W / 2, top + 9, 0xFF3E2A1B);

        DiscoveredBlueprintsData ledger = (minecraft != null && minecraft.player != null)
                ? minecraft.player.getData(ModAttachments.DISCOVERED_BLUEPRINTS)
                : DiscoveredBlueprintsData.EMPTY;

        int contentX = left + 14;
        int contentY = top + 46;
        int maxWidth = BOOK_W - 28;

        if (activeTab == 0) {
            renderMaterialsTab(gui, ledger, contentX, contentY, maxWidth);
        } else if (activeTab == 1) {
            renderReactionsTab(gui, ledger, contentX, contentY, maxWidth);
        } else if (activeTab == 2) {
            renderNotesTab(gui, ledger, contentX, contentY, maxWidth);
        } else {
            renderLegendsTab(gui, ledger, contentX, contentY, maxWidth);
        }
    }

    private void renderLegendsTab(GuiGraphics gui, DiscoveredBlueprintsData ledger, int x, int y, int maxW) {
        List<String> legends = ledger.masterpieceHistory();
        if (legends.isEmpty()) {
            drawWrapped(gui, "No Masterpieces inscribed yet. Roll a Legendary or Mythic Breakthrough on the Forging Anvil and inscribe its name to immortalize it here.", x, y, maxW, 0xFF5C4736);
            return;
        }
        int perPage = 6;
        int maxPage = Math.max(0, (legends.size() - 1) / perPage);
        page = Math.clamp(page, 0, maxPage);

        int start = page * perPage;
        int end = Math.min(legends.size(), start + perPage);
        int cursorY = y;
        for (int i = start; i < end; i++) {
            gui.drawString(font, "★ " + legends.get(i), x, cursorY, 0xFF8C3A00, false);
            cursorY += 16;
        }
        gui.drawCenteredString(font, (page + 1) + " / " + (maxPage + 1), x + maxW / 2, y + 124, 0xFF5C4736);
    }

    private void renderMaterialsTab(GuiGraphics gui, DiscoveredBlueprintsData ledger, int x, int y, int maxW) {
        List<ResourceLocation> items = ledger.discoveredItems();
        if (items.isEmpty()) {
            drawWrapped(gui, "No materials charted yet. Forge with any item or assay gear via Essence Extraction to record its Aspects here.", x, y, maxW, 0xFF5C4736);
            return;
        }
        int perPage = 4;
        int maxPage = Math.max(0, (items.size() - 1) / perPage);
        page = Math.clamp(page, 0, maxPage);

        int start = page * perPage;
        int end = Math.min(items.size(), start + perPage);
        int cursorY = y;
        for (int i = start; i < end; i++) {
            ResourceLocation id = items.get(i);
            Item item = BuiltInRegistries.ITEM.get(id);
            ItemAspectMapping map = AspectRegistryManager.analyzeItem(new ItemStack(item));
            String name = item.getDescription().getString();
            String aspects = map.aspects().entrySet().stream()
                    .map(e -> AspectTextUtil.prettify(e.getKey()) + " " + e.getValue())
                    .collect(Collectors.joining(", "));
            gui.drawString(font, "• " + name, x, cursorY, 0xFF2B1C10, false);
            gui.drawString(font, "  " + aspects, x, cursorY + 10, 0xFF6B4E35, false);
            cursorY += 28;
        }
        gui.drawCenteredString(font, (page + 1) + " / " + (maxPage + 1), x + maxW / 2, y + 124, 0xFF5C4736);
    }

    private void renderReactionsTab(GuiGraphics gui, DiscoveredBlueprintsData ledger, int x, int y, int maxW) {
        List<AspectReactionDefinition> all = new ArrayList<>(AspectRegistryManager.allReactions());
        int perPage = 4;
        int maxPage = Math.max(0, (all.size() - 1) / perPage);
        page = Math.clamp(page, 0, maxPage);

        int start = page * perPage;
        int end = Math.min(all.size(), start + perPage);
        int cursorY = y;
        for (int i = start; i < end; i++) {
            AspectReactionDefinition def = all.get(i);
            if (ledger.hasReaction(def.id())) {
                gui.drawString(font, "⚡ " + AspectTextUtil.prettify(def.id()), x, cursorY, 0xFF8C4600, false);
                String formula = AspectTextUtil.prettify(def.aspectA()) + " + " + AspectTextUtil.prettify(def.aspectB()) + " → " + AspectTextUtil.prettify(def.effect());
                gui.drawString(font, "  " + formula, x, cursorY + 10, 0xFF4A3728, false);
            } else {
                gui.drawString(font, "⚡ ???", x, cursorY, 0xFF7D6B5D, false);
                gui.drawString(font, "  Undiscovered Reaction", x, cursorY + 10, 0xFF8C7B6E, false);
            }
            cursorY += 28;
        }
        gui.drawCenteredString(font, (page + 1) + " / " + (maxPage + 1), x + maxW / 2, y + 124, 0xFF5C4736);
    }

    private void renderNotesTab(GuiGraphics gui, DiscoveredBlueprintsData ledger, int x, int y, int maxW) {
        List<ResourceLocation> found = ledger.discoveredNotes();
        if (found.isEmpty()) {
            drawWrapped(gui, "No Torn Forge Notes transcribed yet. Search dungeons, mineshafts, bastions, or trade with Weaponsmiths to uncover lost master-smith clues.", x, y, maxW, 0xFF5C4736);
            return;
        }
        int maxPage = Math.max(0, found.size() - 1);
        page = Math.clamp(page, 0, maxPage);

        ResourceLocation noteId = found.get(page);
        ForgeNoteLibrary.get(noteId).ifPresent(entry -> {
            gui.drawString(font, entry.title(), x, y, 0xFF7A3E00, false);
            drawWrapped(gui, entry.clueText(), x, y + 16, maxW, 0xFF3E2D20);
        });
        gui.drawCenteredString(font, (page + 1) + " / " + (maxPage + 1), x + maxW / 2, y + 124, 0xFF5C4736);
    }

    private void drawWrapped(GuiGraphics gui, String text, int x, int y, int maxW, int color) {
        List<FormattedCharSequence> lines = font.split(Component.literal(text), maxW);
        int cy = y;
        for (FormattedCharSequence line : lines) {
            gui.drawString(font, line, x, cy, color, false);
            cy += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
