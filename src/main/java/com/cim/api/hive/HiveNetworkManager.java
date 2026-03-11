package com.cim.api.hive;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import com.cim.block.entity.hive.DepthWormNestBlockEntity;

import javax.annotation.Nullable;
import java.util.*;

public class HiveNetworkManager {
    private final Map<UUID, Set<BlockPos>> networkNodes = new HashMap<>();
    private final Map<UUID, HiveNetwork> networks = new HashMap<>();
    private final Map<BlockPos, UUID> posToNetwork = new HashMap<>();

    public boolean hasNests(UUID netId) {
        HiveNetwork net = networks.get(netId);
        return net != null && !net.wormCounts.isEmpty();
    }
    public void tick(Level level) {
        if (networks.isEmpty()) return;
        // Используем итератор, чтобы избежать ошибки при удалении сети во время смерти
        Iterator<HiveNetwork> it = networks.values().iterator();
        while (it.hasNext()) {
            it.next().update(level);
        }
    }
    public static HiveNetworkManager get(Level level) {
        return level.getCapability(HiveNetworkManagerProvider.HIVE_NETWORK_MANAGER).orElse(null);
    }


    public HiveNetwork getNetwork(UUID id) {
        if (id == null) return null;
        // Если сети нет в списке (например, после перезагрузки или нового UUID),
        // computeIfAbsent создаст её «на лету»
        return networks.computeIfAbsent(id, HiveNetwork::new);
    }

    public void addNode(UUID networkId, BlockPos pos) {
        // Получаем (или создаем) объект сети
        HiveNetwork network = getNetwork(networkId);

        // Добавляем блок в список members САМОЙ СЕТИ
        if (!network.members.contains(pos)) {
            network.members.add(pos.immutable());
            System.out.println("[Hive] Блок " + pos + " успешно привязан к сети " + networkId);
        }
    }



    public void mergeNetworks(UUID mainId, UUID secondId, Level level) {
        if (mainId.equals(secondId)) return;

        HiveNetwork mainNet = getNetwork(mainId);
        HiveNetwork secondNet = getNetwork(secondId);

        System.out.println("[Hive] Слияние сетей! " + secondId + " поглощена " + mainId);

        // 1. Переносим очки (не забываем про лимит 50)
        mainNet.killsPool = Math.min(50, mainNet.killsPool + secondNet.killsPool);

        // 2. Переносим все блоки из старой сети в новую
        for (BlockPos pos : secondNet.members) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HiveNetworkMember member) {
                member.setNetworkId(mainId); // Меняем ID в самом блоке
                if (!mainNet.members.contains(pos)) {
                    mainNet.members.add(pos);
                }
            }
        }

        // 3. Удаляем старую сеть из памяти менеджера
        networks.remove(secondId);
    }


    public void removeNode(UUID networkId, BlockPos pos, Level level) {
        Set<BlockPos> nodes = networkNodes.get(networkId);
        if (nodes != null) {
            nodes.remove(pos);
            if (nodes.isEmpty()) {
                networkNodes.remove(networkId);
                networks.remove(networkId);
            } else {
                validateNetwork(networkId, level);
            }
        }
        posToNetwork.remove(pos);
    }

    public void validateNetwork(UUID networkId, Level level) {
        if (level == null) return;
        Set<BlockPos> nodes = networkNodes.get(networkId);
        if (nodes == null) return;

        boolean hasNest = nodes.stream()
                .anyMatch(p -> level.getBlockEntity(p) instanceof DepthWormNestBlockEntity);

        if (!hasNest) {
            for (BlockPos p : nodes) {
                BlockEntity be = level.getBlockEntity(p);
                if (be instanceof HiveNetworkMember member) {
                    member.setNetworkId(null);
                    be.setChanged();
                }
            }
            networkNodes.remove(networkId);
            networks.remove(networkId);
        }
    }

    public BlockPos findNearestNest(Level level, BlockPos wormPos, double radius) {
        BlockPos closest = null;
        double minDistance = radius * radius;

        for (HiveNetwork network : networks.values()) {
            for (BlockPos pos : network.members) {
                // Передаем level в обновленный метод
                if (network.isNest(level, pos)) {
                    double dist = pos.distSqr(wormPos);
                    if (dist < minDistance) {
                        minDistance = dist;
                        closest = pos;
                    }
                }
            }
        }
        return closest;
    }


    public boolean hasFreeNest(UUID netId, Level level) {
        HiveNetwork network = getNetwork(netId);
        if (network == null) return false;

        for (BlockPos pos : network.members) {
            BlockEntity be = level.getBlockEntity(pos);
            // Проверяем, что это гнездо и в нем есть место (например, < 3 червей)
            if (be instanceof DepthWormNestBlockEntity nest && !nest.isFull()) {
                return true;
            }
        }
        return false;
    }
    public BlockPos findNearestNode(UUID networkId, Vec3 targetPos, Level level) {
        HiveNetwork network = getNetwork(networkId);
        if (network == null || network.members.isEmpty()) return null;

        BlockPos targetBlockPos = BlockPos.containing(targetPos);
        BlockPos closest = null;
        double minDistance = Double.MAX_VALUE;

        for (BlockPos pos : network.members) {
            // Проверяем расстояние от блока сети до врага
            double dist = pos.distSqr(targetBlockPos);
            if (dist < minDistance) {
                minDistance = dist;
                closest = pos;
            }
        }
        return closest;
    }

    public boolean addWormToNetwork(UUID networkId, CompoundTag wormData, BlockPos entryPos, Level level) {
        // 1. Берем сеть через наш правильный метод
        HiveNetwork network = getNetwork(networkId);
        if (network == null) return false;

        // 2. Ищем свободное гнездо среди МЕМБЕРОВ сети
        for (BlockPos pos : network.members) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DepthWormNestBlockEntity nest) {
                if (!nest.isFull()) {
                    nest.addWormTag(wormData);
                    // 3. ОБЯЗАТЕЛЬНО обновляем счетчик в мозге улья, чтобы он знал о новом черве
                    network.updateWormCount(pos, 1);
                    return true;
                }
            }
        }
        return false;
    }


    public void updateWormCount(UUID netId, BlockPos nestPos, int delta) {
        HiveNetwork net = networks.get(netId);
        if (net != null) net.updateWormCount(nestPos, delta);
    }
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag networksList = new ListTag();
        for (HiveNetwork net : networks.values()) {
            networksList.add(net.toNBT());
        }
        tag.put("Networks", networksList);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        networks.clear();
        ListTag networksList = tag.getList("Networks", 10);
        for (int i = 0; i < networksList.size(); i++) {
            HiveNetwork net = HiveNetwork.fromNBT(networksList.getCompound(i));
            networks.put(net.id, net);
            // Восстанавливаем связи для posToNetwork
            for (BlockPos pos : net.members) {
                posToNetwork.put(pos, net.id);
            }
        }
    }

    public static final Capability<HiveNetworkManager> HIVE_NETWORK_MANAGER = CapabilityManager.get(new CapabilityToken<>(){});

}