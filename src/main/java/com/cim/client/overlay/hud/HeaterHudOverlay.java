package com.cim.client.overlay.hud;

import com.cim.main.CrustalIncursionMod; // Проверь правильность импорта своего главного класса мода
import com.cim.multiblock.industrial.HeaterBlockEntity;
import com.cim.multiblock.system.IMultiblockPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Регистрируем класс только для клиента
@Mod.EventBusSubscriber(modid = CrustalIncursionMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeaterHudOverlay {

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        // Отрисовываем наш ХУД сразу после ванильного прицела (CROSSHAIR)
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Смотрим, куда направлен взгляд игрока
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity be = mc.level.getBlockEntity(pos);

            // Если смотрим на невидимую часть - достаем главный контроллер
            if (be instanceof IMultiblockPart part && part.getControllerPos() != null) {
                be = mc.level.getBlockEntity(part.getControllerPos());
            }

            // Если контроллер - это Нагреватель, рисуем красоту!
            if (be instanceof HeaterBlockEntity heater) {
                GuiGraphics graphics = event.getGuiGraphics();
                Font font = mc.font;

                int heat = heater.getHeatLevel();
                int maxHeat = heater.getMaxHeat();

                // Вычисляем процент нагрева, чтобы менять цвет текста
                float heatPercent = (float) heat / maxHeat;
                // Серый -> Оранжевый -> Красный
                int color = heatPercent > 0.8f ? 0xFF5555 : (heatPercent > 0.4f ? 0xFFAA00 : 0xAAAAAA);

                String text = "Температура: " + heat + " / " + maxHeat + " °C";

                // Координаты: чуть правее и ниже центра экрана (прицела)
                int screenWidth = event.getWindow().getGuiScaledWidth();
                int screenHeight = event.getWindow().getGuiScaledHeight();
                int x = screenWidth / 2 + 15;
                int y = screenHeight / 2 + 10;

                // Для стиля "Create" можно нарисовать полупрозрачную черную подложку:
                int textWidth = font.width(text);
                graphics.fill(x - 2, y - 2, x + textWidth + 2, y + font.lineHeight + 1, 0x80000000);

                // Рисуем сам текст с тенью
                graphics.drawString(font, text, x, y, color, true);
            }
        }
    }
}