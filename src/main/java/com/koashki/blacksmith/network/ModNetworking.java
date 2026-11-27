package com.koashki.blacksmith.network;

import com.koashki.blacksmith.blockentity.BlastForgeBlockEntity;
import com.koashki.blacksmith.blockentity.ForgingAnvilBlockEntity;
import com.koashki.blacksmith.blockentity.SynthesisChamberBlockEntity;
import com.koashki.blacksmith.core.BlacksmithMod;
import com.koashki.blacksmith.core.ModAttachments;
import com.koashki.blacksmith.item.ForgedArchetype;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
public final class ModNetworking {
    private ModNetworking() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(WorkstationActionPayload.TYPE, WorkstationActionPayload.STREAM_CODEC, ModNetworking::handleWorkstationAction);
        registrar.playToServer(MasterpieceNamePayload.TYPE, MasterpieceNamePayload.STREAM_CODEC, ModNetworking::handleMasterpieceName);
        registrar.playToClient(ProficiencySyncPayload.TYPE, ProficiencySyncPayload.STREAM_CODEC, ModNetworking::handleProficiencySync);
        registrar.playToClient(LedgerSyncPayload.TYPE, LedgerSyncPayload.STREAM_CODEC, ModNetworking::handleLedgerSync);
    }

    private static void handleWorkstationAction(WorkstationActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.distanceToSqr(payload.pos().getCenter()) > 64.0) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(payload.pos());
            if (be instanceof BlastForgeBlockEntity forge) {
                switch (payload.action()) {
                    case "bellows" -> forge.pumpBellows(player);
                    case "vent" -> forge.ventHeat(player);
                    case "pour_alloy" -> forge.pourMoltenAlloy(player);
                }
            } else if (be instanceof ForgingAnvilBlockEntity anvil) {
                switch (payload.action()) {
                    case "start_forge" -> {
                        ForgedArchetype[] values = ForgedArchetype.values();
                        anvil.startSession(player, values[Math.clamp(payload.arg(), 0, values.length - 1)]);
                    }
                    case "strike" -> {
                        ForgedArchetype.StrikeKind[] kinds = ForgedArchetype.StrikeKind.values();
                        anvil.handleStrike(player, kinds[Math.clamp(payload.arg(), 0, kinds.length - 1)]);
                        syncProficiency(player);
                    }
                    case "over_forge" -> {
                        anvil.decideOverForge(player, payload.arg() != 0);
                        syncProficiency(player);
                    }
                    case "lattice_click" -> {
                        anvil.handleLatticeClick(player, payload.arg());
                        syncProficiency(player);
                    }
                    case "lattice_quench" -> {
                        anvil.bailOutLattice(player);
                        syncProficiency(player);
                    }
                }
            } else if (be instanceof SynthesisChamberBlockEntity chamber) {
                switch (payload.action()) {
                    case "ascend_splice" -> chamber.executeAscendOrSplice(player);
                    case "extract_essence" -> chamber.executeExtractEssence(player);
                }
            }
        });
    }

    private static void handleMasterpieceName(MasterpieceNamePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (player.distanceToSqr(payload.pos().getCenter()) > 64.0) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(payload.pos());
            if (be instanceof ForgingAnvilBlockEntity anvil) {
                anvil.renameMasterpiece(player, payload.customName());
            }
        });
    }

    private static void handleProficiencySync(ProficiencySyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                context.player().setData(ModAttachments.PROFICIENCY, payload.proficiency());
            }
        });
    }

    private static void handleLedgerSync(LedgerSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                context.player().setData(ModAttachments.DISCOVERED_BLUEPRINTS, payload.ledger());
            }
        });
    }

    public static void syncProficiency(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ProficiencySyncPayload(player.getData(ModAttachments.PROFICIENCY)));
    }

    public static void syncLedger(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new LedgerSyncPayload(player.getData(ModAttachments.DISCOVERED_BLUEPRINTS)));
    }

    @EventBusSubscriber(modid = BlacksmithMod.MOD_ID)
    public static final class LoginSync {
        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                syncProficiency(player);
                syncLedger(player);
            }
        }
    }
}
