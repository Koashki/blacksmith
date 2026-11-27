package com.koashki.blacksmith.client.screen;

import com.koashki.blacksmith.blockentity.ForgingAnvilBlockEntity;
import com.koashki.blacksmith.component.BlacksmithProficiencyData;
import com.koashki.blacksmith.component.GradeTier;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.core.ModAttachments;
import com.koashki.blacksmith.core.ModDataComponents;
import com.koashki.blacksmith.item.forged.ForgedArchetype;
import com.koashki.blacksmith.menu.ForgingAnvilMenu;
import com.koashki.blacksmith.network.MasterpieceNamePayload;
import com.koashki.blacksmith.network.WorkstationActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ForgingAnvilScreen extends AbstractContainerScreen<ForgingAnvilMenu> {
    // Dedicated pop-out Over-Forge Molten Lattice modal dimensions
    private static final int MODAL_W = 168;
    private static final int MODAL_H = 212;
    private static final int LATTICE_CELL = 34;
    private static final int LATTICE_GAP = 5;

    // Masterpiece Naming modal dimensions
    private static final int NAME_MODAL_W = 188;
    private static final int NAME_MODAL_H = 104;

    private int chosenArchetypeIndex = 0;
    private double clientElapsedTicks = 0.0;
    private int lastSyncedStep = -1;
    private boolean namingModalActive = false;
    private boolean namingDismissedForCurrentOutput = false;

    private Button formBtn;
    private Button forgeBtn;
    private Button lightBtn;
    private Button heavyBtn;
    private Button stabBtn;
    private Button quenchBtn;
    private Button modalQuenchBtn;

    private EditBox masterpieceNameBox;
    private Button inscribeBtn;
    private Button skipNameBtn;

    public ForgingAnvilScreen(ForgingAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();

        formBtn = addRenderableWidget(Button.builder(Component.literal("Form"), btn ->
                        chosenArchetypeIndex = (chosenArchetypeIndex + 1) % ForgedArchetype.values().length)
                .bounds(leftPos + 82, topPos + 19, 32, 18)
                .build());

        forgeBtn = addRenderableWidget(Button.builder(Component.literal("Forge"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "start_forge", chosenArchetypeIndex)))
                .bounds(leftPos + 116, topPos + 19, 24, 18)
                .build());

        lightBtn = addRenderableWidget(Button.builder(Component.literal("Light"), btn -> sendStrike(ForgedArchetype.StrikeKind.LIGHT))
                .bounds(leftPos + 8, topPos + 74, 38, 16)
                .build());
        heavyBtn = addRenderableWidget(Button.builder(Component.literal("Heavy"), btn -> sendStrike(ForgedArchetype.StrikeKind.HEAVY))
                .bounds(leftPos + 48, topPos + 74, 38, 16)
                .build());
        stabBtn = addRenderableWidget(Button.builder(Component.literal("Stab"), btn -> sendStrike(ForgedArchetype.StrikeKind.STABILIZE))
                .bounds(leftPos + 88, topPos + 74, 36, 16)
                .build());
        quenchBtn = addRenderableWidget(Button.builder(Component.literal("Quench"), btn -> sendStrike(ForgedArchetype.StrikeKind.QUENCH))
                .bounds(leftPos + 126, topPos + 74, 42, 16)
                .build());

        int modalLeft = (width - MODAL_W) / 2;
        int modalTop = (height - MODAL_H) / 2;
        modalQuenchBtn = addRenderableWidget(Button.builder(Component.literal("Quench Early [Space]"), btn ->
                        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "lattice_quench", 0)))
                .bounds(modalLeft + 20, modalTop + MODAL_H - 26, MODAL_W - 40, 18)
                .build());
        modalQuenchBtn.visible = false;

        int nmLeft = (width - NAME_MODAL_W) / 2;
        int nmTop = (height - NAME_MODAL_H) / 2;
        masterpieceNameBox = addRenderableWidget(new EditBox(font, nmLeft + 14, nmTop + 42, NAME_MODAL_W - 28, 18, Component.literal("Masterpiece Name")));
        masterpieceNameBox.setMaxLength(32);
        masterpieceNameBox.visible = false;

        inscribeBtn = addRenderableWidget(Button.builder(Component.literal("Inscribe"), btn -> submitMasterpieceName())
                .bounds(nmLeft + 14, nmTop + 70, 76, 18)
                .build());
        inscribeBtn.visible = false;

        skipNameBtn = addRenderableWidget(Button.builder(Component.literal("Skip"), btn -> {
                    namingDismissedForCurrentOutput = true;
                    setNamingModalVisible(false);
                })
                .bounds(nmLeft + NAME_MODAL_W - 90, nmTop + 70, 76, 18)
                .build());
        skipNameBtn.visible = false;
    }

    private void submitMasterpieceName() {
        String text = masterpieceNameBox != null ? masterpieceNameBox.getValue().trim() : "";
        if (!text.isEmpty()) {
            PacketDistributor.sendToServer(new MasterpieceNamePayload(menu.blockPos(), text));
        }
        namingDismissedForCurrentOutput = true;
        setNamingModalVisible(false);
    }

    private void setNamingModalVisible(boolean visible) {
        namingModalActive = visible;
        if (masterpieceNameBox != null) {
            masterpieceNameBox.visible = visible;
            masterpieceNameBox.setFocused(visible);
            if (visible) {
                masterpieceNameBox.setValue("");
                setFocused(masterpieceNameBox);
            }
        }
        if (inscribeBtn != null) inscribeBtn.visible = visible;
        if (skipNameBtn != null) skipNameBtn.visible = visible;
    }

    private ItemStack outputStack() {
        return menu.slots.get(ForgingAnvilBlockEntity.SLOT_OUTPUT).getItem();
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        ItemStack out = outputStack();
        if (out.isEmpty()) {
            namingDismissedForCurrentOutput = false;
            if (namingModalActive) {
                setNamingModalVisible(false);
            }
        } else if (!menu.isSessionActive() && !menu.isLatticeActive() && !namingDismissedForCurrentOutput && !namingModalActive) {
            ItemGradeData grade = out.get(ModDataComponents.ITEM_GRADE.get());
            if (grade != null && grade.tier().isMasterpiece() && grade.customName().isEmpty()) {
                setNamingModalVisible(true);
            }
        }

        boolean lattice = menu.isLatticeActive();
        boolean modalBlocksBase = lattice || namingModalActive;
        if (formBtn != null) formBtn.visible = !modalBlocksBase;
        if (forgeBtn != null) forgeBtn.visible = !modalBlocksBase;
        if (lightBtn != null) lightBtn.visible = !modalBlocksBase;
        if (heavyBtn != null) heavyBtn.visible = !modalBlocksBase;
        if (stabBtn != null) stabBtn.visible = !modalBlocksBase;
        if (quenchBtn != null) quenchBtn.visible = !modalBlocksBase;
        if (modalQuenchBtn != null) modalQuenchBtn.visible = lattice;

        if (!menu.isSessionActive() || menu.isAwaitingOverForge() || lattice) {
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
        if (menu.isLatticeActive()) {
            if (kind == ForgedArchetype.StrikeKind.QUENCH) {
                PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "lattice_quench", 0));
            }
            return;
        }
        if (menu.isAwaitingOverForge()) {
            PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "over_forge",
                    kind == ForgedArchetype.StrikeKind.QUENCH ? 0 : 1));
            return;
        }
        PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "strike", kind.ordinal()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (namingModalActive) {
            if (masterpieceNameBox != null && masterpieceNameBox.mouseClicked(mouseX, mouseY, button)) {
                setFocused(masterpieceNameBox);
                return true;
            }
            if (button == 0) {
                if (inscribeBtn != null && inscribeBtn.isMouseOver(mouseX, mouseY)) {
                    inscribeBtn.onClick(mouseX, mouseY);
                    return true;
                }
                if (skipNameBtn != null && skipNameBtn.isMouseOver(mouseX, mouseY)) {
                    skipNameBtn.onClick(mouseX, mouseY);
                    return true;
                }
            }
            return true;
        }
        if (menu.isLatticeActive()) {
            if (button == 0) {
                if (modalQuenchBtn != null && modalQuenchBtn.isMouseOver(mouseX, mouseY)) {
                    modalQuenchBtn.onClick(mouseX, mouseY);
                    return true;
                }
                int cell = hoveredLatticeCell((int) mouseX, (int) mouseY);
                if (cell >= 0) {
                    PacketDistributor.sendToServer(new WorkstationActionPayload(menu.blockPos(), "lattice_click", cell));
                    return true;
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (namingModalActive) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitMasterpieceName();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                namingDismissedForCurrentOutput = true;
                setNamingModalVisible(false);
                return true;
            }
            if (masterpieceNameBox != null && masterpieceNameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            // Prevent 'E' inventory key from closing screen while typing a name
            return true;
        }
        if (menu.isSessionActive()) {
            if (keyCode == GLFW.GLFW_KEY_SPACE) {
                sendStrike(ForgedArchetype.StrikeKind.QUENCH);
                return true;
            }
            if (!menu.isLatticeActive()) {
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
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (namingModalActive && masterpieceNameBox != null) {
            return masterpieceNameBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private int latticeOriginX() {
        int gridSize = LATTICE_CELL * 3 + LATTICE_GAP * 2;
        return (width - gridSize) / 2;
    }

    private int latticeOriginY() {
        return (height - MODAL_H) / 2 + 38;
    }

    private int hoveredLatticeCell(int mouseX, int mouseY) {
        int gx = latticeOriginX();
        int gy = latticeOriginY();
        int stride = LATTICE_CELL + LATTICE_GAP;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int cx = gx + col * stride;
                int cy = gy + row * stride;
                if (mouseX >= cx && mouseX < cx + LATTICE_CELL && mouseY >= cy && mouseY < cy + LATTICE_CELL) {
                    return row * 3 + col;
                }
            }
        }
        return -1;
    }

    private static boolean isCrossNeighbor(int hoveredCell, int cell) {
        if (hoveredCell < 0) return false;
        if (hoveredCell == cell) return true;
        int hr = hoveredCell / 3;
        int hc = hoveredCell % 3;
        int r = cell / 3;
        int c = cell % 3;
        return Math.abs(hr - r) + Math.abs(hc - c) == 1;
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

        // Standard Minigame Rhythm Bar
        int barX = x + 12;
        int barY = y + 53;
        int barW = 152;
        int barH = 14;
        gui.fill(barX, barY, barX + barW, barY + barH, 0xFF111317);
        gui.renderOutline(barX, barY, barW, barH, 0xFF666D7A);

        if (menu.isSessionActive() && !menu.isLatticeActive()) {
            float center = menu.targetCenter();
            float widthVal = menu.targetWidth();

            int goodStart = barX + Math.round((center - widthVal * 0.60f) * barW);
            int goodEnd = barX + Math.round((center + widthVal * 0.60f) * barW);
            gui.fill(Math.max(barX + 1, goodStart), barY + 1, Math.min(barX + barW - 1, goodEnd), barY + barH - 1, 0xFFCC9922);

            int perfStart = barX + Math.round((center - widthVal * 0.28f) * barW);
            int perfEnd = barX + Math.round((center + widthVal * 0.28f) * barW);
            gui.fill(Math.max(barX + 1, perfStart), barY + 1, Math.min(barX + barW - 1, perfEnd), barY + barH - 1, 0xFF44EE66);

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
        } else if (menu.isSessionActive() && !menu.isLatticeActive()) {
            String prompt = "Next: " + menu.expectedStrikeKind().name()
                    + " (" + (menu.currentStep() + 1) + "/" + menu.totalSteps() + ")  Score: " + menu.previewScore();
            gui.drawString(font, prompt, 8, 41, 0xFF88DDFF, false);
        } else if (!menu.isLatticeActive()) {
            ForgedArchetype form = ForgedArchetype.values()[chosenArchetypeIndex];
            gui.drawString(font, "Form: " + form.displayName() + "  [A/D/S/Space]", 8, 41, 0xFFAAB4C4, false);
        }

        gui.drawString(font, playerInventoryTitle, 8, 96, 0xFF9EA8B8, false);
    }

    private void renderOverForgeModal(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 300.0f);

        gui.fill(0, 0, width, height, 0xCC0D0E12);

        int mx = (width - MODAL_W) / 2;
        int my = (height - MODAL_H) / 2;

        gui.fill(mx, my, mx + MODAL_W, my + MODAL_H, 0xFF1B191E);
        gui.renderOutline(mx, my, MODAL_W, MODAL_H, 0xFFFF9922);
        gui.renderOutline(mx + 2, my + 2, MODAL_W - 4, MODAL_H - 4, 0xFF5A3B1A);

        gui.drawCenteredString(font, "OVER-FORGE: MOLTEN LATTICE", mx + MODAL_W / 2, my + 10, 0xFFFFCC33);
        gui.drawCenteredString(font, "Align all 9 cells before cooling!", mx + MODAL_W / 2, my + 22, 0xFFB8C0CC);

        int gx = latticeOriginX();
        int gy = latticeOriginY();
        int stride = LATTICE_CELL + LATTICE_GAP;
        int mask = menu.latticeMask();
        int hovered = hoveredLatticeCell(mouseX, mouseY);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int cell = row * 3 + col;
                int cx = gx + col * stride;
                int cy = gy + row * stride;
                boolean tempered = ((mask >> cell) & 1) == 1;

                int fill = tempered ? 0xFFFFBB22 : 0xFF331B19;
                int core = tempered ? 0xFFFFF5B8 : 0xFF4F241C;
                gui.fill(cx, cy, cx + LATTICE_CELL, cy + LATTICE_CELL, fill);
                gui.fill(cx + 5, cy + 5, cx + LATTICE_CELL - 5, cy + LATTICE_CELL - 5, core);

                if (isCrossNeighbor(hovered, cell)) {
                    gui.renderOutline(cx, cy, LATTICE_CELL, LATTICE_CELL, 0xFF66EEFF);
                    gui.renderOutline(cx + 1, cy + 1, LATTICE_CELL - 2, LATTICE_CELL - 2, 0xAA66EEFF);
                } else {
                    gui.renderOutline(cx, cy, LATTICE_CELL, LATTICE_CELL, tempered ? 0xFFFFEE99 : 0xFF6E3B33);
                }
            }
        }

        int movesY = gy + stride * 3 + 4;
        gui.drawCenteredString(font, "Moves Remaining: " + menu.latticeMovesLeft(), mx + MODAL_W / 2, movesY, 0xFF88DDFF);

        int barX = mx + 18;
        int barY = movesY + 12;
        int barW = MODAL_W - 36;
        int barH = 9;
        gui.fill(barX, barY, barX + barW, barY + barH, 0xFF0E1014);
        gui.renderOutline(barX, barY, barW, barH, 0xFF6B5236);
        float ratio = Math.clamp(menu.latticeTicksLeft() / 240.0f, 0.0f, 1.0f);
        int fillW = Math.round(ratio * (barW - 2));
        int barColor = ratio > 0.35f ? 0xFFFFAA22 : 0xFFFF3333;
        gui.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, barColor);

        if (modalQuenchBtn != null && modalQuenchBtn.visible) {
            modalQuenchBtn.render(gui, mouseX, mouseY, partialTick);
        }

        gui.pose().popPose();
    }

    private void renderMasterpieceNamingModal(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 300.0f);

        gui.fill(0, 0, width, height, 0xCC0D0E12);

        int mx = (width - NAME_MODAL_W) / 2;
        int my = (height - NAME_MODAL_H) / 2;

        ItemGradeData grade = outputStack().get(ModDataComponents.ITEM_GRADE.get());
        boolean isMythic = grade != null && grade.tier() == GradeTier.MYTHIC;
        int borderColor = isMythic ? 0xFFFF3355 : 0xFFFF9922;
        String heading = isMythic ? "★ MYTHIC EGO AWAKENED ★" : "★ LEGENDARY MASTERPIECE ★";

        gui.fill(mx, my, mx + NAME_MODAL_W, my + NAME_MODAL_H, 0xFF1A161C);
        gui.renderOutline(mx, my, NAME_MODAL_W, NAME_MODAL_H, borderColor);
        gui.renderOutline(mx + 2, my + 2, NAME_MODAL_W - 4, NAME_MODAL_H - 4, 0xFF5A3B2A);

        gui.drawCenteredString(font, heading, mx + NAME_MODAL_W / 2, my + 12, borderColor);
        gui.drawCenteredString(font, "Inscribe a name for your creation:", mx + NAME_MODAL_W / 2, my + 26, 0xFFD0D6E0);

        if (masterpieceNameBox != null && masterpieceNameBox.visible) {
            masterpieceNameBox.render(gui, mouseX, mouseY, partialTick);
        }
        if (inscribeBtn != null && inscribeBtn.visible) {
            inscribeBtn.render(gui, mouseX, mouseY, partialTick);
        }
        if (skipNameBtn != null && skipNameBtn.visible) {
            skipNameBtn.render(gui, mouseX, mouseY, partialTick);
        }

        gui.pose().popPose();
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
        if (menu.isLatticeActive()) {
            renderBackground(gui, mouseX, mouseY, partialTick);
            renderOverForgeModal(gui, mouseX, mouseY, partialTick);
            return;
        }
        if (namingModalActive) {
            renderBackground(gui, mouseX, mouseY, partialTick);
            renderMasterpieceNamingModal(gui, mouseX, mouseY, partialTick);
            return;
        }
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }
}
