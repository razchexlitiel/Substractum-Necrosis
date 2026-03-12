package com.cim.client.renderer;

import com.cim.block.entity.energy.ConnectorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ConnectorRenderer implements BlockEntityRenderer<ConnectorBlockEntity> {

    private static final int SEGMENTS = 16;
    private static final float WIRE_RADIUS = 0.03125f;
    private static final double SLACK = 1.03;
    private static final float R = 0.12f, G = 0.12f, B = 0.12f, A = 1.0f;

    public ConnectorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ConnectorBlockEntity animatable, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (animatable.getConnections().isEmpty()) return;

        Level level = animatable.getLevel();
        if (level == null) return;

        Vec3 startWorld = animatable.getWireAttachmentPoint();
        Vec3 renderOrigin = Vec3.atLowerCornerOf(animatable.getBlockPos());
        Vec3 start = startWorld.subtract(renderOrigin);

        poseStack.pushPose();

        // Рисуем провод для каждого подключения
        for (BlockPos otherPos : animatable.getConnections()) {
            // Чтобы провод не рисовался дважды (туда и обратно), рисуем только от блока с меньшими координатами
            if (animatable.getBlockPos().compareTo(otherPos) > 0) continue;

            BlockEntity otherBe = level.getBlockEntity(otherPos);
            if (!(otherBe instanceof ConnectorBlockEntity otherConnector)) continue;

            Vec3 endWorld = otherConnector.getWireAttachmentPoint();
            Vec3 end = endWorld.subtract(renderOrigin);

            // Передаем радиус из настроек блока
            renderWire(poseStack, bufferSource, start, end, packedLight, packedOverlay, animatable.getTier().wireRadius());
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ConnectorBlockEntity be) {
        return true; // Чтобы провод не исчезал, когда сам блок за спиной игрока
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    // ========== CATENARY (Формула провисания) ==========

    private record CatenaryData(boolean isVertical, double offsetX, double offsetY,
                                double scale, Vec3 delta, double horLength, Vec3 vecA) {
        Vec3 getPoint(double t) {
            if (isVertical) {
                return new Vec3(vecA.x + delta.x * t, vecA.y + delta.y * t, vecA.z + delta.z * t);
            }
            double x = vecA.x + delta.x * t;
            double y = vecA.y + scale * Math.cosh((t * horLength - offsetX) / scale) + offsetY;
            double z = vecA.z + delta.z * t;
            return new Vec3(x, y, z);
        }
    }

    private CatenaryData computeCatenary(Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double horLength = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        if (horLength < 0.05) {
            return new CatenaryData(true, 0, 0, 1, delta, 0, start);
        }

        double wireLength = delta.length() * SLACK;
        double l;
        {
            double goal = Math.sqrt(wireLength * wireLength - delta.y * delta.y) / horLength;
            double lower = 0, upper = 1;
            while (Math.sinh(upper) / upper < goal) {
                lower = upper;
                upper *= 2;
            }
            for (int i = 0; i < 20; i++) {
                double mid = (lower + upper) / 2.0;
                double val = Math.sinh(mid) / mid;
                if (val < goal) lower = mid;
                else if (val > goal) upper = mid;
                else break;
            }
            l = (lower + upper) / 2.0;
        }

        double scale = horLength / (2 * l);
        double offsetX = (horLength - scale * Math.log(
                (wireLength + delta.y) / (wireLength - delta.y))) * 0.5;
        double offsetY = -scale * Math.cosh((-offsetX) / scale);

        return new CatenaryData(false, offsetX, offsetY, scale, delta, horLength, start);
    }

    // ========== РЕНДЕР ПРОВОДА ==========

    private void renderWire(PoseStack poseStack, MultiBufferSource bufferSource,
                            Vec3 start, Vec3 end, int light, int overlay, float wireRadius) {

        CatenaryData catenary = computeCatenary(start, end);

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(
                        new ResourceLocation("minecraft", "textures/block/black_concrete.png")
                )
        );

        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        Vec3 prevPoint = catenary.getPoint(0);

        for (int i = 1; i <= SEGMENTS; i++) {
            double t = (double) i / SEGMENTS;
            Vec3 point = catenary.getPoint(t);

            Vec3 dir = point.subtract(prevPoint);
            double len = dir.length();
            if (len < 0.001) {
                prevPoint = point;
                continue;
            }
            dir = dir.scale(1.0 / len);

            Vec3 up = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 side = dir.cross(up).normalize().scale(WIRE_RADIUS);
            Vec3 upDir = dir.cross(side).normalize().scale(WIRE_RADIUS);

            emitQuad(matrix, normal, consumer, prevPoint, point, side, light, overlay);
            emitQuad(matrix, normal, consumer, prevPoint, point, side.scale(-1), light, overlay);
            emitQuad(matrix, normal, consumer, prevPoint, point, upDir, light, overlay);
            emitQuad(matrix, normal, consumer, prevPoint, point, upDir.scale(-1), light, overlay);

            prevPoint = point;
        }
    }

    private void emitQuad(Matrix4f mat, Matrix3f norm, VertexConsumer consumer,
                          Vec3 p1, Vec3 p2, Vec3 offset, int light, int overlay) {
        Vec3 a = p1.add(offset);
        Vec3 b = p1.subtract(offset);
        Vec3 c = p2.subtract(offset);
        Vec3 d = p2.add(offset);
        Vec3 n = offset.normalize();
        float nx = (float) n.x, ny = (float) n.y, nz = (float) n.z;

        vert(mat, norm, consumer, a, nx, ny, nz, 0, 0, light, overlay);
        vert(mat, norm, consumer, b, nx, ny, nz, 0, 1, light, overlay);
        vert(mat, norm, consumer, c, nx, ny, nz, 1, 1, light, overlay);
        vert(mat, norm, consumer, d, nx, ny, nz, 1, 0, light, overlay);
    }

    private void vert(Matrix4f mat, Matrix3f norm, VertexConsumer consumer,
                      Vec3 pos, float nx, float ny, float nz,
                      float u, float v, int light, int overlay) {
        consumer.vertex(mat, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(R, G, B, A)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(norm, nx, ny, nz)
                .endVertex();
    }
}
