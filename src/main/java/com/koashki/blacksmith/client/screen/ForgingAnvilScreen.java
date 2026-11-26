package com.koashki.blacksmith.client.screen;

import com.koashki.blacksmith.component.BlacksmithProficiencyData;
import com.koashki.blacksmith.core.ModAttachments;
import com.koashki.blacksmith.item.ForgedArchetype;
import com.koashki.blacksmith.menu.ForgingAnvilMenu;
import com.koashki.blacksmith.network.WorkstationActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ForgingAnvilScreen extends AbstractContainerScreen<ForgingAnvilMenu> {
    private int chosenArchetypeIndex = 0;
    private double clientElapsedTicks = 0.0;
    private int lastSyncedStep = -1;

    public ForgingAnvilScreen(ForgingAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.literal("Form"), btn ->
                        chosenArchetypeIndex = (chosenArchetypeIndex + 1) % ForgedArchetype.values().length)
                .bounds(leftPos + 82, topPos + 19, 32, 18)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Forge"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "start_forge", chosenArchetypeIndex)))
                .bounds(leftPos + 116, topPos + 19, 24, 18)
                .build());

        addRenderableWidget(Button.builder(Component.literal("Light"), btn -> sendStrike(ForgedArchetype.StrikeKind.LIGHT))
                .bounds(leftPos + 8, topPos + 74, 38, 16)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Heavy"), btn -> sendStrike(ForgedArchetype.StrikeKind.HEAVY))
                .bounds(leftPos + 48, topPos + 74, 38, 16)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Stab"), btn -> sendStrike(ForgedArchetype.StrikeKind.STABILIZE))
                .bounds(leftPos + 88, topPos + 74, 36, 16)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Quench"), btn -> sendStrike(ForgedArchetype.StrikeKind.QUENCH))
                .bounds(leftPos + 126, topPos + 74, 42, 16)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!menu.isSessionActive() || menu.isAwaitingOverForge()) {
            clientElapsedTicks = 0.0;
            lastSyncedStep = -1;
            return;
        }
        int step = menu.currentStep();
        int serverElapsed = menu.stepElapsedTicks();
        if (step != lastSyncedStep || Math.abs(clientElapsedTicks - serverElapsed) > 2.5) {
            clientElapsedTicks = serverElapsed;
            lastSyncedStep = step;
        } else {
            clientElapsedTicks += 1.0;
        }
    }

    private void sendStrike(ForgedArchetype.StrikeKind kind) {
        if (menu.isAwaitingOverForge()) {
            PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "over_forge",
                    kind == ForgedArchetype.StrikeKind.QUENCH ? 0 : 1));
            return;
        }
        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "strike", kind.ordinal()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (menu.isSessionActive()) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                sendStrike(ForgedArchetype.StrikeKind.LIGHT);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_D) {
                sendStrike(ForgedArchetype.StrikeKind.HEAVY);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_S) {
                sendStrike(ForgedArchetype.StrikeKind.STABILIZE);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_SPACE) {
                sendStrike(ForgedArchetype.StrikeKind.QUENCH);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFF23252A);
        gui.renderOutline(x, y, imageWidth, imageHeight, 0xFF5D6472);

        // Workstation slots
        drawSlotBox(gui, x + 16, y + 20);
        drawSlotBox(gui, x + 38, y + 20);
        drawSlotBox(gui, x + 60, y + 20);
        drawSlotBox(gui, x + 144, y + 20);

        // Player inventory divider + full 36-slot grid
        gui.fill(x + 6, y + 94, x + imageWidth - 6, y + 95, 0xFF474D59);
        drawPlayerInventorySlots(gui, x + 8, y + 106);

        // Minigame Rhythm Bar
        int barX = x + 12;
        int barY = y + 53;
        int barW = 152;
        int barH = 14;
        gui.fill(barX, barY, barX + barW, barY + barH, 0xFF111317);
        gui.renderOutline(barX, barY, barW, barH, 0xFF666D7A);

        if (menu.isSessionActive()) {
            float center = menu.targetCenter();
            float width = menu.targetWidth();

            int goodStart = barX + Math.round((center - width * 0.60f) * barW);
            int goodEnd = barX + Math.round((center + width * 0.60f) * barW);
            gui.fill(Math.max(barX + 1, goodStart), barY + 1, Math.min(barX + barW - 1, goodEnd), barY + barH - 1, 0xFFCC9922);

            int perfStart = barX + Math.round((center - width * 0.28f) * barW);
            int perfEnd = barX + Math.round((center + width * 0.28f) * barW);
            gui.fill(Math.max(barX + 1, perfStart), barY + 1, Math.min(barX + barW - 1, perfEnd), barY + barH - 1, 0xFF44EE66);

            // Smooth sub-frame interpolation at full native FPS
            int period = menu.cursorPeriodTicks();
            double smoothElapsed = menu.isAwaitingOverForge() ? clientElapsedTicks : (clientElapsedTicks + partialTick);
            double phase = (smoothElapsed % period) / (double) period;
            float smoothCursor = (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2.0));

            int cursorX = barX + Math.round(smoothCursor * (barW - 3));
            gui.fill(cursorX, barY - 2, cursorX + 3, barY + barH + 2, 0xFFFFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        gui.drawString(font, title, 8, 6, 0xFFE6ECF5, false);

        if (minecraft != null && minecraft.player != null) {
            BlacksmithProficiencyData prof = minecraft.player.getData(ModAttachments.PROFICIENCY);
            gui.drawString(font, "Lv." + prof.level(), 140, 6, 0xFFFFAA00, false);
        }

        if (menu.isAwaitingOverForge()) {
            gui.drawString(font, "Score " + menu.previewScore() + "! Heavy=Over-Forge | Quench=Keep", 8, 41, 0xFFFFCC33, false);
        } else if (menu.isSessionActive()) {
            String prompt = "Next: " + menu.expectedStrikeKind().name()
                    + " (" + (menu.currentStep() + 1) + "/" + menu.totalSteps() + ")  Score: " + menu.previewScore();
            gui.drawString(font, prompt, 8, 41, 0xFF88DDFF, false);
        } else {
            ForgedArchetype form = ForgedArchetype.values()[chosenArchetypeIndex];
            gui.drawString(font, "Form: " + form.displayName() + "  [A/D/S/Space]", 8, 41, 0xFFAAB4C4, false);
        }

        gui.drawString(font, playerInventoryTitle, 8, 96, 0xFF9EA8B8, false);
    }

    private void drawPlayerInventorySlots(GuiGraphics gui, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBox(gui, left + col * 18, top + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBox(gui, left + col * 18, top + 58);
        }
    }

    private void drawSlotBox(GuiGraphics gui, int sx, int sy) {
        gui.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF15171B);
        gui.renderOutline(sx - 1, sy - 1, 18, 18, 0xFF5D6472);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }
}
