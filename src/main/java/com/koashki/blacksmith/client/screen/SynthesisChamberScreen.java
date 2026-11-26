package com.koashki.blacksmith.client.screen;

import com.koashki.blacksmith.menu.SynthesisChamberMenu;
import com.koashki.blacksmith.network.WorkstationActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class SynthesisChamberScreen extends AbstractContainerScreen<SynthesisChamberMenu> {
    public SynthesisChamberScreen(SynthesisChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 174;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Ascend / Splice"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "ascend_splice", 0)))
                .bounds(leftPos + 10, topPos + 72, 82, 16)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Extract"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "extract_essence", 0)))
                .bounds(leftPos + 100, topPos + 72, 66, 16)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF211A2B);
        gui.renderOutline(x, y, imageWidth, imageHeight, 0xFF6B4C9A);

        drawSlotBox(gui, x + 80, y + 35);
        drawSlotBox(gui, x + 44, y + 17);
        drawSlotBox(gui, x + 44, y + 53);
        drawSlotBox(gui, x + 116, y + 17);
        drawSlotBox(gui, x + 116, y + 53);
        drawSlotBox(gui, x + 148, y + 35);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 6, 0xFFE5D1FF, false);
        int chance = menu.successPercent();
        String rate = "Chance: " + chance + "%  (★" + menu.currentBaseStars() + ")";
        int color = chance >= 70 ? 0xFF55FF88 : (chance >= 40 ? 0xFFFFCC44 : 0xFFFF6666);
        gui.drawString(font, rate, 66, 56, color, false);
    }

    private void drawSlotBox(GuiGraphics gui, int sx, int sy) {
        gui.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF140F1C);
        gui.renderOutline(sx - 1, sy - 1, 18, 18, 0xFF7B5EA7);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }
}
