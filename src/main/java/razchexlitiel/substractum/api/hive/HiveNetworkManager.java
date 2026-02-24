package razchexlitiel.substractum.api.hive;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import razchexlitiel.substractum.block.entity.hive.DepthWormNestBlockEntity;

import java.util.*;

public class HiveNetworkManager {
    // Новое: Хранилище всех узлов для AI червей и слияния сетей
    private final Map<UUID, Set<BlockPos>> networkNodes = new HashMap<>();

    // Старое: Для совместимости с твоей системой HiveNetwork и сохранениями
    private final Map<UUID, HiveNetwork> networks = new HashMap<>();
    private final Map<BlockPos, UUID> posToNetwork = new HashMap<>();

    public boolean hasNests(UUID netId) {
        HiveNetwork net = networks.get(netId);
        return net != null && !net.wormCounts.isEmpty();
    }

    // Регистрация узла (почвы или гнезда)
    public void addNode(UUID networkId, BlockPos pos) {
        networkNodes.computeIfAbsent(networkId, k -> new HashSet<>()).add(pos.immutable());
    }

    // Слияние сетей: masterId поглощает targetId
    public void mergeNetworks(UUID masterId, UUID targetId, Level level) {
        if (masterId.equals(targetId)) return;

        Set<BlockPos> targetNodes = networkNodes.remove(targetId);
        if (targetNodes != null) {
            for (BlockPos p : targetNodes) {
                BlockEntity be = level.getBlockEntity(p);
                if (be instanceof HiveNetworkMember member) {
                    member.setNetworkId(masterId);
                    this.addNode(masterId, p);
                    be.setChanged();
                }
            }
        }

        // Слияние в старой системе networks (если используется для логики)
        HiveNetwork targetNet = networks.remove(targetId);
        if (targetNet != null) {
            HiveNetwork masterNet = networks.computeIfAbsent(masterId, HiveNetwork::new);
            // Здесь должна быть логика переноса данных из targetNet в masterNet
        }
    }

    // Удаление узла с проверкой уровня для валидации
    public void removeNode(UUID networkId, BlockPos pos, Level level) {
        Set<BlockPos> nodes = networkNodes.get(networkId);
        if (nodes != null) {
            nodes.remove(pos);
            if (nodes.isEmpty()) {
                networkNodes.remove(networkId);
                networks.remove(networkId);
            } else {
                // Если узлы остались, проверяем, не нужно ли распустить сеть
                validateNetwork(networkId, level);
            }
        }
        posToNetwork.remove(pos);
    }

    // Проверка: если в сети нет ни одного Ядра, вся почва обнуляется
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

    public boolean hasFreeNest(UUID networkId, Level level) {
        Set<BlockPos> nodes = networkNodes.get(networkId);
        if (nodes == null) return false;

        for (BlockPos p : nodes) {
            if (level.getBlockEntity(p) instanceof DepthWormNestBlockEntity nest) {
                if (!nest.isFull()) return true;
            }
        }
        return false;
    }

    public boolean addWormToNetwork(UUID networkId, CompoundTag wormData, BlockPos entryPos, Level level) {
        Set<BlockPos> nodes = networkNodes.get(networkId);
        if (nodes == null) return false;

        for (BlockPos pos : nodes) {
            if (level.getBlockEntity(pos) instanceof DepthWormNestBlockEntity nest) {
                if (!nest.isFull()) {
                    nest.addWormTag(wormData);
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

    // === СЕРИАЛИЗАЦИЯ (Исправлено: теперь восстанавливает networkNodes) ===
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
        posToNetwork.clear();
        networkNodes.clear();
        ListTag networksList = tag.getList("Networks", 10);
        for (int i = 0; i < networksList.size(); i++) {
            HiveNetwork net = HiveNetwork.fromNBT(networksList.getCompound(i));
            networks.put(net.id, net);
            for (BlockPos p : net.members) {
                posToNetwork.put(p, net.id);
                // Восстанавливаем узлы для работы AI
                this.addNode(net.id, p);
            }
        }
    }

    public static final Capability<HiveNetworkManager> HIVE_NETWORK_MANAGER = CapabilityManager.get(new CapabilityToken<>(){});

    public static HiveNetworkManager get(Level level) {
        return level.getCapability(HIVE_NETWORK_MANAGER).orElse(null);
    }
}