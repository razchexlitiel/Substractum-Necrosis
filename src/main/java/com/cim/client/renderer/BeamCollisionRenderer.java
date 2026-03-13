package com.cim.client.renderer;

import com.cim.block.basic.ModBlocks;
import com.cim.block.entity.deco.BeamCollisionBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BeamCollisionRenderer implements BlockEntityRenderer<BeamCollisionBlockEntity> {

    public BeamCollisionRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BeamCollisionBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!blockEntity.isMaster()) return;

        Vec3 startPos = blockEntity.getStartPos();
        Vec3 endPos = blockEntity.getEndPos();
        if (startPos == null || endPos == null) return;

        BlockPos bePos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();

        double offsetX = startPos.x - bePos.getX();
        double offsetY = startPos.y - bePos.getY();
        double offsetZ = startPos.z - bePos.getZ();

        double dx = endPos.x - startPos.x;
        double dy = endPos.y - startPos.y;
        double dz = endPos.z - startPos.z;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance == 0) return;

        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        poseStack.pushPose();

        // Сдвигаем и поворачиваем базу
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.translate(-0.5, -0.5, 0);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BlockState beamState = ModBlocks.BEAM_BLOCK.get().defaultBlockState();

        // --- НОВАЯ ЛОГИКА ОТРИСОВКИ ОТРЕЗКОВ ---

        int fullBlocks = (int) distance; // Количество целых блоков
        float remainder = (float) (distance - fullBlocks); // Остаточный кусочек в конце

        // Цикл <= fullBlocks, чтобы отрисовать и целые, и остаток
        for (int i = 0; i <= fullBlocks; i++) {
            // Если остаток микроскопический, не рисуем лишний сегмент
            if (i == fullBlocks && remainder <= 0.001f) break;

            poseStack.pushPose();

            // Сдвигаем каждый следующий кусок на 1 блок вперед по оси Z
            poseStack.translate(0, 0, i);

            // Если это последний кусок, скейлим (плющим) только его
            if (i == fullBlocks) {
                poseStack.scale(1.0f, 1.0f, remainder);
            }

            // --- ДИНАМИЧЕСКОЕ ОСВЕЩЕНИЕ ---
            int segmentLight = packedLight;
            if (level != null) {
                // Высчитываем реальные координаты текущего куска в мире
                double ratio = (double) i / distance;
                double lerpX = startPos.x + dx * ratio;
                double lerpY = startPos.y + dy * ratio;
                double lerpZ = startPos.z + dz * ratio;

                BlockPos segmentPos = BlockPos.containing(lerpX, lerpY, lerpZ);
                // Получаем честный свет для этой точки (с учетом теней от деревьев, факелов и т.д.)
                segmentLight = LevelRenderer.getLightColor(level, segmentPos);
            }

            blockRenderer.renderSingleBlock(beamState, poseStack, buffer, segmentLight, packedOverlay,
                    net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.solid());

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BeamCollisionBlockEntity blockEntity) {
        // Обязательно true, иначе балка исчезнет, если отвернуться от Мастера
        return true;
    }
}