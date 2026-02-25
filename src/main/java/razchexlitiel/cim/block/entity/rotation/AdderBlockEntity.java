package razchexlitiel.cim.block.entity.rotation;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import razchexlitiel.cim.api.rotation.RotationNetworkHelper;
import razchexlitiel.cim.api.rotation.RotationSource;
import razchexlitiel.cim.api.rotation.Rotational;
import razchexlitiel.cim.api.rotation.RotationalNode;
import razchexlitiel.cim.block.entity.ModBlockEntities;
import razchexlitiel.cim.block.basic.rotation.AdderBlock;


public class AdderBlockEntity extends BlockEntity implements RotationalNode {

    private long speed = 0;
    private long torque = 0;

    // Инициализируем нулями, чтобы не было null
    private RotationSource cachedSource = new RotationSource(0, 0);

    public AdderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADDER_BE.get(), pos, state);
    }

    // ========== Rotational ==========
    @Override public long getSpeed() { return speed; }
    @Override public long getTorque() { return torque; }
    @Override public void setSpeed(long speed) { this.speed = speed; updateCache(); setChanged(); sync(); invalidateNeighborCaches(); }
    @Override public void setTorque(long torque) { this.torque = torque; updateCache(); setChanged(); sync(); invalidateNeighborCaches(); }
    @Override public long getMaxSpeed() { return 0; }
    @Override public long getMaxTorque() { return 0; }

    // ========== RotationalNode ==========

    // ВАЖНО: Сумматор всегда отдает свою текущую скорость как "кешированный источник"
    @Override
    public RotationSource getCachedSource() {
        return cachedSource;
    }

    // ВАЖНО: Кеш сумматора всегда валиден, так как он сам источник
    @Override
    public boolean isCacheValid(long currentTime) {
        return true;
    }

    @Override
    public void setCachedSource(@Nullable RotationSource source, long gameTime) {
        // Игнорируем внешнюю установку кеша, мы сами управляем им
    }

    private void updateCache() {
        this.cachedSource = new RotationSource(speed, torque);
    }

    @Override
    public void invalidateCache() {
        // Пусто, чтобы избежать рекурсии. Мы сами обновляем соседей через tick.
    }

    private void invalidateNeighborCaches() {
        if (level == null || level.isClientSide) return;
        Direction facing = getBlockState().getValue(AdderBlock.FACING);
        Direction outputSide = facing.getOpposite();
        BlockPos neighborPos = worldPosition.relative(outputSide);
        if (level.getBlockEntity(neighborPos) instanceof RotationalNode node) {
            node.invalidateCache();
        }
    }

    // AdderBlockEntity.java
    @Override
    public boolean canProvideSource(@Nullable Direction fromDir) {
        if (fromDir == null) return false;

        BlockState state = getBlockState();
        Direction facing = state.getValue(AdderBlock.FACING);
        Direction outputSide = facing.getOpposite();

        // ВАЖНО: Мы отдаем энергию ТОЛЬКО тому блоку,
        // который пришел к нам СО СТОРОНЫ нашего выхода.
        // Если вал стоит сбоку, fromDir будет равен лево/право, и мы вернем false.
        return fromDir == outputSide;
    }

    @Override
    public Direction[] getPropagationDirections(@Nullable Direction fromDir) {
        // Сумматор — это конечная точка для входящих валов и начальная для выходящего.
        // Энергия не "течет" сквозь него транзитом, она пересчитывается в tick().
        return new Direction[0];
    }




    // ========== Тик ==========
    public static void tick(Level level, BlockPos pos, BlockState state, AdderBlockEntity be) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(AdderBlock.FACING); // Куда смотрит "лицо"
        Direction outputSide = facing.getOpposite();      // Выход (сзади)

        // Определяем боковые стороны (входы)
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        long totalSpeed = 0;
        long totalTorque = 0;

        // Опрашиваем ТОЛЬКО левую и правую стороны
        for (Direction dir : new Direction[]{left, right}) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null) continue;

            RotationSource src = null;
            // Проверяем, может ли сосед дать нам энергию в этом направлении
            if (neighbor instanceof RotationalNode node && node.canProvideSource(dir.getOpposite())) {
                if (neighbor instanceof Rotational rot) {
                    src = new RotationSource(rot.getSpeed(), rot.getTorque());
                }
            }

            // Если сосед не Rotational, пробуем глубокий поиск источника за ним
            if (src == null) {
                src = RotationNetworkHelper.findSource(neighbor, dir.getOpposite());
            }

            if (src != null) {
                totalSpeed += src.speed();
                totalTorque = Math.max(totalTorque, src.torque());
            }
        }

        // Логика обновления состояния (остается прежней)
        boolean changed = false;
        if (be.speed != totalSpeed) { be.speed = totalSpeed; changed = true; }
        if (be.torque != totalTorque) { be.torque = totalTorque; changed = true; }

        if (changed) {
            be.updateCache();
            be.setChanged();
            be.sync();
            be.invalidateNeighborCaches(); // Будим только того, кто на выходе
        }
    }

    // ========== NBT и синхронизация ==========
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Speed", speed);
        tag.putLong("Torque", torque);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        speed = tag.getLong("Speed");
        torque = tag.getLong("Torque");
        cachedSource = null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putLong("Speed", speed);
        tag.putLong("Torque", torque);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        speed = tag.getLong("Speed");
        torque = tag.getLong("Torque");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}