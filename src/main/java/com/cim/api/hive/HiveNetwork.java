package com.cim.api.hive;


import com.cim.block.basic.ModBlocks;
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
        if (lastFedTime == 0) lastFedTime = level.getGameTime();

        // 1. Потребление (раз в 24000 тиков)
        if (level.getGameTime() - lastFedTime >= 24000) {
            if (killsPool > 0) {
                killsPool--;
                lastFedTime = level.getGameTime();
                System.out.println("[Hive] Налог уплачен. Остаток: " + killsPool);
            } else {
                die(level);
                return;
            }
        }

        // 2. Принятие решений (каждые 5 секунд / 100 тиков)
        // Убрал проверку % 200, чтобы он чаще проверял возможность развития
        if (level.getGameTime() % 100 == 0 && killsPool > 0) {
            makeDecisions(level);
        }
    }

    private void makeDecisions(Level level) {
        if (this.members == null || this.members.isEmpty()) {
            System.out.println("[Hive Error] Сеть " + id + " не видит своих блоков! Траты невозможны.");
            return;
        }

        List<BlockPos> nodes = new ArrayList<>(this.members);
        Collections.shuffle(nodes);

        for (BlockPos pos : nodes) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof DepthWormNestBlockEntity nest)) continue;

            // ЛОГИКА 1: Рождение (Приоритет, если червей мало)
            if (killsPool >= 10 && nest.getStoredWormsCount() < 3) {
                spawnNewWorm(nest, pos);
                return; // Одно действие за цикл
            }

            // ЛОГИКА 2: Лечение (Если есть раненые)
            if (killsPool >= 1 && nest.hasInjuredWorms()) {
                if (nest.healOneWorm()) {
                    killsPool--;
                    return;
                }
            }

            // ЛОГИКА 3: Экспансия (Почва)
            // Улей строит почву, только если у него больше 15 очков (заначка)
            if (killsPool > 15) {
                for (Direction dir : Direction.values()) {
                    BlockPos target = pos.relative(dir);
                    if (level.isEmptyBlock(target)) {
                        level.setBlockAndUpdate(target, ModBlocks.HIVE_SOIL.get().defaultBlockState());
                        killsPool -= 2;
                        System.out.println("[Hive] Укрепление структуры в " + target);
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
        System.out.println("[Hive] Рожден новый червь в " + pos + ". Остаток очков: " + this.killsPool);
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