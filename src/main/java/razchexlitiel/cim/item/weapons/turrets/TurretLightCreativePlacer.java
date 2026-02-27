package razchexlitiel.cim.item.weapons.turrets;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import razchexlitiel.cim.entity.ModEntities;
import razchexlitiel.cim.entity.mobs.DepthWormEntity;
import razchexlitiel.cim.entity.weapons.turrets.TurretLightEntity;

public class TurretLightCreativePlacer extends Item {
    public TurretLightCreativePlacer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            TurretLightEntity turret = ModEntities.TURRET_LIGHT.get().create(level);
            if (turret != null) {
                turret.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                level.addFreshEntity(turret);
                context.getItemInHand().shrink(1); // Тратим предмет
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.CONSUME;
    }
}