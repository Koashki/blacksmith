package com.koashki.blacksmith.client.screen;

import com.koashki.blacksmith.menu.BlastForgeMenu;
import com.koashki.blacksmith.network.WorkstationActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class BlastForgeScreen extends AbstractContainerScreen<BlastForgeMenu> {
    public BlastForgeScreen(BlastForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Bellows"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "bellows", 0)))
                .bounds(leftPos + 54, topPos + 60, 52, 16)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Pour"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "pour_alloy", 0)))
                .bounds(leftPos + 114, topPos + 60, 50, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        // Dark forge panel background
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF2B2624);
        gui.renderOutline(x, y, imageWidth, imageHeight, 0xFF5A4E48);

        // Slot frames
        drawSlotBox(gui, x + 26, y + 53);
        drawSlotBox(gui, x + 26, y + 21);
        drawSlotBox(gui, x + 62, y + 17);
        drawSlotBox(gui, x + 82, y + 17);
        drawSlotBox(gui, x + 62, y + 37);
        drawSlotBox(gui, x + 82, y + 37);
        drawSlotBox(gui, x + 134, y + 33);

        // Temperature thermometer bar (left side)
        int barX = x + 10;
        int barY = y + 18;
        int barW = 8;
        int barH = 54;
        gui.fill(barX, barY, barX + barW, barY + barH, 0xFF141212);

        // Optimal 850-950°C band highlight
        int optTop = barY + barH - Math.round((950.0f / 1150.0f) * barH);
        int optBot = barY + barH - Math.round((850.0f / 1150.0f) * barH);
        gui.fill(barX - 1, optTop, barX + barW + 1, optBot, 0x9933CC55);

        float tempRatio = Math.clamp(menu.temperatureC() / 1150.0f, 0.0f, 1.0f);
        int fillH = Math.round(tempRatio * barH);
        int fillColor = (menu.temperatureC() >= 850 && menu.temperatureC() <= 950) ? 0xFF44EE66
                : (menu.temperatureC() > 950 ? 0xFFFF3333 : 0xFFFF8822);
        gui.fill(barX + 1, barY + barH - fillH, barX + barW - 1, barY + barH, fillColor);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 6, 0xFFE6D5C3, false);
        String tempLabel = menu.temperatureC() + "°C";
        int color = (menu.temperatureC() >= 850 && menu.temperatureC() <= 950) ? 0xFF55FF77 : 0xFFFFAA44;
        gui.drawString(font, tempLabel, 118, 18, color, false);
        gui.drawString(font, playerInventoryTitle, 8, 74, 0xFFB8A99A, false);
    }

    private void drawSlotBox(GuiGraphics gui, int sx, int sy) {
        gui.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF1A1716);
        gui.renderOutline(sx - 1, sy - 1, 18, 18, 0xFF6B5E57);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }
}
