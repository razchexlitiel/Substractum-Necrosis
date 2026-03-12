package com.cim.api.hive;


import com.cim.block.basic.ModBlocks;
import com.cim.block.entity.hive.HiveSoilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.cim.block.entity.hive.DepthWormNestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class HiveNetwork {
    public final UUID id;
    public final Set<BlockPos> members = new HashSet<>();
    public final Map<BlockPos, Integer> wormCounts = new HashMap<>(); // только для гнёзд
    public int killsPool = 0; // Наша валюта
    private long lastFedTime = 0;
    public HiveNetwork(UUID id) { this.id = id; }

    public void addMember(BlockPos pos, boolean isNest) {
        members.add(pos);
        if (isNest) wormCounts.put(pos, 0);
    }
    public void removeMember(BlockPos pos) {
        members.remove(pos);
        wormCounts.remove(pos);
    }
    public boolean isNest(Level level, BlockPos pos) {
        // Проверяем: 1. Блок в списке сети. 2. Блок реально существует в мире. 3. Это блок ГНЕЗДА.
        return members.contains(pos) &&
                level.isLoaded(pos) &&
                level.getBlockState(pos).is(ModBlocks.DEPTH_WORM_NEST.get());
    }


    public int getTotalWorms() {
        return wormCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean addWorm(Level level, CompoundTag wormTag, BlockPos sourcePos) {
        // 1. Проверяем, есть ли вообще гнезда в этой сети
        if (wormCounts.isEmpty()) {
            System.out.println("[HiveNetwork] ОШИБКА: В сети " + id + " нет зарегистрированных гнезд!");
            return false;
        }

        BlockPos bestNest = null;
        int minCount = Integer.MAX_VALUE;

        // 2. Ищем самое свободное гнездо (лимит 3 червя)
        for (BlockPos nestPos : wormCounts.keySet()) {
            BlockEntity be = level.getBlockEntity(nestPos);
            if (be instanceof DepthWormNestBlockEntity nest) {
                int currentCount = wormCounts.getOrDefault(nestPos, 0);
                if (currentCount < 3 && currentCount < minCount) {
                    minCount = currentCount;
                    bestNest = nestPos;
                }
            }
        }

        // 3. Если нашли место — закидываем червя
        if (bestNest != null) {
            DepthWormNestBlockEntity nestBE = (DepthWormNestBlockEntity) level.getBlockEntity(bestNest);
            nestBE.addWormTag(wormTag); // Метод в твоем NestBE для хранения NBT
            wormCounts.put(bestNest, wormCounts.get(bestNest) + 1);
            System.out.println("[HiveNetwork] УСПЕХ: Червь направлен в гнездо " + bestNest);
            return true;
        }

        System.out.println("[HiveNetwork] ВАРНИНГ: Все гнезда в сети " + id + " переполнены!");
        return false;
    }

    public void update(Level level) {
        if (level.isClientSide) return;

        // Сделаем проверку чаще (раз в 2 секунды) для теста
        if (level.getGameTime() % 40 == 0) {
            // ЛОГ №1: Видим ли мы вообще тики?
            System.out.println("[Hive Tick] Проверка сети " + this.id + " | Очки: " + killsPool + " | Узлов: " + members.size());

            if (members.isEmpty()) {
                System.out.println("[Hive Tick] ОШИБКА: Список узлов пуст! Улей не знает, где он находится.");
            }

            makeDecisions(level);
        }
    }


    private void makeDecisions(Level level) {
        if (killsPool <= 0 || members.isEmpty()) return;

        List<BlockPos> nodes = new ArrayList<>(this.members);
        Collections.shuffle(nodes);

        for (BlockPos pos : nodes) {
            if (!level.isLoaded(pos)) continue;
            BlockEntity be = level.getBlockEntity(pos);

            // --- ЛОГИКА ГНЕЗДА ---
            if (be instanceof DepthWormNestBlockEntity nest) {
                // 1. Хил (1 очко)
                if (killsPool >= 1 && nest.hasInjuredWorms()) {
                    if (nest.healOneWorm()) {
                        this.killsPool -= 1;
                        System.out.println("[Hive] Вылечен червь в " + pos);
                        return;
                    }
                }
                // 2. Рождение (10 очков)
                if (killsPool >= 10 && nest.getStoredWormsCount() < 3) {
                    spawnNewWorm(nest, pos);
                    this.killsPool -= 10;
                    System.out.println("[Hive] Рожден новый червь в " + pos);
                    return;
                }
            }

            // --- ЛОГИКА ЭКСПАНСИИ (Для любого узла сети) ---
            if (killsPool >= 5) {
                for (Direction dir : Direction.values()) {
                    BlockPos target = pos.relative(dir); // ТА САМАЯ СТРОЧКА
                    BlockState state = level.getBlockState(target);

                    // Проверяем: не бедрок, не воздух, не само гнездо и не почва
                    if (!state.isAir() &&
                            !state.is(ModBlocks.HIVE_SOIL.get()) &&
                            !state.is(ModBlocks.DEPTH_WORM_NEST.get()) &&
                            state.getDestroySpeed(level, target) >= 0) {

                        level.setBlockAndUpdate(target, ModBlocks.HIVE_SOIL.get().defaultBlockState());
                        this.killsPool -= 5;
                        System.out.println("[Hive] Экспансия: почва захватила " + target);
                        return;
                    }
                }
            }
        }
    }



    // Вспомогательный метод для чистоты кода
    private void spawnNewWorm(DepthWormNestBlockEntity nest, BlockPos pos) {
        CompoundTag newWorm = new CompoundTag();
        newWorm.putFloat("Health", 15.0F);
        newWorm.putInt("Kills", 0);
        nest.addWormTag(newWorm);
        this.updateWormCount(pos, 1);
        this.killsPool -= 10;
        System.out.println("[Hive] Рожден новый червь в " + pos + ". Остаток очков: " + killsPool);
    }


    public void updateWormCount(BlockPos nestPos, int delta) {
        wormCounts.merge(nestPos, delta, Integer::sum);
    }


    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putInt("KillsPool", this.killsPool); // ДОБАВЛЕНО
        tag.putLong("LastFed", this.lastFedTime); // ДОБАВЛЕНО

        ListTag membersList = new ListTag();
        for (BlockPos p : members) {
            CompoundTag pTag = NbtUtils.writeBlockPos(p);
            pTag.putBoolean("IsNest", wormCounts.containsKey(p));
            if (wormCounts.containsKey(p)) {
                pTag.putInt("WormCount", wormCounts.get(p));
            }
            membersList.add(pTag);
        }
        tag.put("Members", membersList);
        return tag;
    }

    public static HiveNetwork fromNBT(CompoundTag tag) {
        UUID id = tag.getUUID("Id");
        HiveNetwork net = new HiveNetwork(id);

        // Сначала читаем общие данные сети (вне цикла!)
        net.killsPool = tag.getInt("KillsPool");
        net.lastFedTime = tag.getLong("LastFed");

        ListTag membersList = tag.getList("Members", 10);
        for (int i = 0; i < membersList.size(); i++) {
            CompoundTag pTag = membersList.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(pTag);
            boolean isNest = pTag.getBoolean("IsNest");

            net.members.add(pos);
            if (isNest) {
                net.wormCounts.put(pos, pTag.getInt("WormCount"));
            }
        }
        return net;
    }


    private void die(Level level) {
        System.out.println("[HiveNetwork] КРИТИЧЕСКАЯ ОШИБКА: Сеть " + id + " погибла от голода!");

        // 1. Проходим по всем зарегистрированным блокам колонии
        for (BlockPos pos : new HashSet<>(members)) {
            BlockState state = level.getBlockState(pos);

            // Заменяем блоки на мертвые версии
            if (state.is(ModBlocks.DEPTH_WORM_NEST.get())) {
                level.setBlockAndUpdate(pos, ModBlocks.DEPTH_WORM_NEST_DEAD.get().defaultBlockState());
            } else if (state.is(ModBlocks.HIVE_SOIL.get())) {
                level.setBlockAndUpdate(pos, ModBlocks.HIVE_SOIL_DEAD.get().defaultBlockState());
            }

            // Очищаем привязку блоков в менеджере, чтобы они не считались частью сети
            HiveNetworkManager manager = HiveNetworkManager.get(level);
            if (manager != null) {
                manager.removeNode(this.id, pos, level);
            }
        }

        // 2. Полностью удаляем данные о червях и очках
        members.clear();
        wormCounts.clear();
        killsPool = 0;
    }

}