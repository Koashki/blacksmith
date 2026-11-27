package com.koashki.blacksmith.client.renderer;

import com.koashki.blacksmith.block.ForgingAnvilBlock;
import com.koashki.blacksmith.blockentity.ForgingAnvilBlockEntity;
import com.koashki.blacksmith.component.ItemGradeData;
import com.koashki.blacksmith.core.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ForgingAnvilRenderer implements BlockEntityRenderer<ForgingAnvilBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ForgingAnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            ForgingAnvilBlockEntity be,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack stack = be.displayStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.025, 0.5);

        Direction facing = be.getBlockState().hasProperty(ForgingAnvilBlock.FACING)
                ? be.getBlockState().getValue(ForgingAnvilBlock.FACING)
                : Direction.NORTH;
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.scale(0.62f, 0.62f, 0.62f);

        // Glow emissively on the anvil if the billet is hot or the output is a Masterpiece
        ItemGradeData grade = stack.get(ModDataComponents.ITEM_GRADE.get());
        boolean emissive = stack.has(ModDataComponents.FORGING_HEAT.get())
                || (grade != null && grade.tier().hasEmissiveBorder());
        int light = emissive ? LightTexture.FULL_BRIGHT : packedLight;

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, packedOverlay, poseStack, bufferSource, be.getLevel(), 0);
        poseStack.popPose();
    }
}
