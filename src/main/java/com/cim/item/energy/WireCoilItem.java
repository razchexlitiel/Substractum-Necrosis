package com.cim.item.energy;

import com.cim.block.entity.energy.ConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WireCoilItem extends Item {

    public WireCoilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(clickedPos);

        if (!(be instanceof ConnectorBlockEntity connector)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Проверка: коннектор уже подключён?
        if (connector.isConnected()) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.cim.connector_already_connected"), true);
            }
            return InteractionResult.FAIL;
        }

        // Есть ли уже сохранённая первая точка?
        if (stack.hasTag() && stack.getTag().contains("FirstConnector")) {
            BlockPos firstPos = NbtUtils.readBlockPos(stack.getTag().getCompound("FirstConnector"));

            // Нельзя подключить к себе
            if (firstPos.equals(clickedPos)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.cim.same_connector"), true);
                }
                return InteractionResult.FAIL;
            }

            // Проверяем что первый коннектор ещё существует и свободен
            BlockEntity firstBe = level.getBlockEntity(firstPos);
            if (!(firstBe instanceof ConnectorBlockEntity firstConnector) || firstConnector.isConnected()) {
                // Первый коннектор пропал или уже занят — сбрасываем
                stack.removeTagKey("FirstConnector");
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.cim.first_connector_invalid"), true);
                }
                return InteractionResult.FAIL;
            }

            // Опционально: проверка максимальной дистанции
            double maxDistance = 32.0;
            if (firstPos.distSqr(clickedPos) > maxDistance * maxDistance) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("message.cim.too_far"), true);
                }
                return InteractionResult.FAIL;
            }

            // СОЕДИНЯЕМ!
            firstConnector.connectTo(clickedPos);
            connector.connectTo(firstPos);

            // Убираем NBT и тратим предмет
            stack.removeTagKey("FirstConnector");
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.cim.connected"), true);
            }
            return InteractionResult.SUCCESS;

        } else {
            // Запоминаем первую точку
            stack.getOrCreateTag().put("FirstConnector", NbtUtils.writeBlockPos(clickedPos));

            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("message.cim.first_point_set"), true);
            }
            return InteractionResult.SUCCESS;
        }
    }
}