package razchexlitiel.cim.block.entity.hive;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import razchexlitiel.cim.api.hive.HiveNetworkManager;
import razchexlitiel.cim.api.hive.HiveNetworkMember;
import razchexlitiel.cim.block.entity.ModBlockEntities;
import razchexlitiel.cim.entity.mobs.DepthWormEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DepthWormNestBlockEntity extends BlockEntity implements HiveNetworkMember {
    private UUID networkId;
    private final List<CompoundTag> storedWorms = new ArrayList<>();
    private int releaseCooldown = 0;

    public DepthWormNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DEPTH_WORM_NEST.get(), pos, state);
    }

    // === HiveNetworkMember ===
    @Override
    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public void setNetworkId(UUID id) {
        this.networkId = id;
        setChanged();
    }

    public boolean isFull() {
        return storedWorms.size() >= 3; // лимит только по количеству червей
    }
    public void addWorm(DepthWormEntity worm) {
        if (isFull()) return;
        CompoundTag tag = new CompoundTag();
        worm.save(tag);
        storedWorms.add(tag);
        worm.discard();

        if (!level.isClientSide && networkId != null) {
            HiveNetworkManager manager = HiveNetworkManager.get(level);
            if (manager != null) manager.updateWormCount(networkId, worldPosition, 1);
        }
        setChanged();
    }

    public void addWormTag(CompoundTag tag) {
        storedWorms.add(tag);
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DepthWormNestBlockEntity blockEntity) {
        if (level.isClientSide) return;

        if (blockEntity.releaseCooldown > 0) blockEntity.releaseCooldown--;

        if (level.getGameTime() % 20 == 0 && !blockEntity.storedWorms.isEmpty()) {
            boolean hasEnemy = !level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(pos).inflate(10), // было 20
                    e -> e.isAlive() && e.deathTime <= 0 &&
                            !(e instanceof DepthWormEntity) &&
                            !(e instanceof Player p && (p.isCreative() || p.isSpectator()))
            ).isEmpty();

            if (hasEnemy && blockEntity.releaseCooldown <= 0) {
                blockEntity.releaseWormsAndNotify();
                blockEntity.releaseCooldown = 400;
            }
        }
    }

    public void releaseWormsAndNotify() {
        int count = storedWorms.size();
        if (!level.isClientSide && networkId != null) {
            HiveNetworkManager manager = HiveNetworkManager.get(level);
            if (manager != null) manager.updateWormCount(networkId, worldPosition, -count);
        }
        releaseWorms();
    }

    public void releaseWorms() {
        if (this.level == null || this.level.isClientSide) return;

        // ВАЖНО: Получаем ID сущности из вашего ModEntities (замените на свой, если он другой)

        String entityId = "cim:depth_worm";

        for (CompoundTag wormTag : this.storedWorms) {
            // 1. Добавляем ID типа сущности, иначе loadEntityRecursive вернет null
            wormTag.putString("id", entityId);

            // 2. Очищаем старый UUID, чтобы не было конфликтов при спавне
            wormTag.remove("UUID");
            wormTag.remove("UUIDMost");
            wormTag.remove("UUIDLeast");

            Entity entity = EntityType.loadEntityRecursive(wormTag, level, (e) -> {
                // Устанавливаем позицию (центр блока, где было гнездо)
                BlockPos spawnPos = findSpawnPos(this.worldPosition);
                e.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                        level.random.nextFloat() * 360F, 0);

                // Генерируем абсолютно новый UUID для "новой" жизни червя
                e.setUUID(UUID.randomUUID());

                return e;
            });

            if (entity != null) {
                // Добавляем в мир
                level.addFreshEntity(entity);
            }
        }

        this.storedWorms.clear();
        this.setChanged();
    }

    private BlockPos findSpawnPos(BlockPos nestPos) {
        for (Direction direction : Direction.values()) {
            BlockPos relative = nestPos.relative(direction);
            if (level.getBlockState(relative).isAir()) {
                return relative;
            }
        }
        return nestPos.above();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (networkId != null) tag.putUUID("NetworkId", networkId);
        ListTag list = new ListTag();
        list.addAll(storedWorms);
        tag.put("StoredWorms", list);
    }

    // В классе DepthWormNestBlockEntity
    // В DepthWormNestBlockEntity
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            if (this.networkId == null) {
                this.networkId = UUID.randomUUID(); // Ядро генерирует ID
                this.setChanged();
            }
            // Обязательно регистрируем ядро в менеджере!
            HiveNetworkManager.get(this.level).addNode(this.networkId, this.worldPosition);
        }
    }




}