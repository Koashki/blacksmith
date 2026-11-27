package com.koashki.blacksmith.block;

import com.koashki.blacksmith.blockentity.BlastForgeBlockEntity;
import com.koashki.blacksmith.core.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import org.joml.Vector3f;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlastForgeBlock extends BaseEntityBlock {
    public static final MapCodec<BlastForgeBlock> CODEC = simpleCodec(BlastForgeBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BlastForgeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlastForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BLAST_FORGE.get(), BlastForgeBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlastForgeBlockEntity forge)) {
            return;
        }

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.6;
        double cz = pos.getZ() + 0.5;

        Direction facing = state.getValue(FACING);
        double fx = cx + facing.getStepX() * 0.54 + (random.nextDouble() - 0.5) * 0.35;
        double fy = cy + (random.nextDouble() - 0.5) * 0.25;
        double fz = cz + facing.getStepZ() * 0.54 + (random.nextDouble() - 0.5) * 0.35;

        // Diegetic tinted flame/aura particles reflecting the alloy's dominant Aspect
        int rgb = forge.dominantColorRgb();
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        level.addParticle(new DustParticleOptions(new Vector3f(r, g, b), 1.1f), fx, fy, fz, 0.0, 0.04, 0.0);
        level.addParticle(ParticleTypes.SMOKE, cx + (random.nextDouble() - 0.5) * 0.3, pos.getY() + 1.05, cz + (random.nextDouble() - 0.5) * 0.3, 0.0, 0.03, 0.0);

        // Instability sparks
        float instability = forge.previewInstability();
        if (instability >= 0.28f && random.nextFloat() < Math.min(0.85f, instability)) {
            level.addParticle(instability >= 0.50f ? ParticleTypes.LAVA : ParticleTypes.ELECTRIC_SPARK,
                    fx, fy + 0.1, fz,
                    (random.nextDouble() - 0.5) * 0.12, 0.08 + random.nextDouble() * 0.06, (random.nextDouble() - 0.5) * 0.12);
        }

        // Subtle diegetic audio cues
        if (random.nextFloat() < 0.14f) {
            String aspect = forge.dominantAspectPath();
            switch (aspect) {
                case "resonant", "harmonic" ->
                        level.playLocalSound(cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35f, 0.85f + random.nextFloat() * 0.3f, false);
                case "voltaic", "storing" ->
                        level.playLocalSound(cx, cy, cz, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.25f, 1.6f + random.nextFloat() * 0.3f, false);
                case "spatial", "abyssal", "phasing" ->
                        level.playLocalSound(cx, cy, cz, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.20f, 1.3f, false);
                case "temporal" ->
                        level.playLocalSound(cx, cy, cz, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.22f, 1.8f, false);
                case "caustic", "siphoning" ->
                        level.playLocalSound(cx, cy, cz, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.30f, 1.1f, false);
                default ->
                        level.playLocalSound(cx, cy, cz, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.55f, 0.9f + random.nextFloat() * 0.2f, false);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlastForgeBlockEntity forge) {
                if (player.isShiftKeyDown()) {
                    forge.pumpBellows(serverPlayer);
                } else {
                    serverPlayer.openMenu(forge, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
