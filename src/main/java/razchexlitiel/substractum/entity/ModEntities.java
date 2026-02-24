package razchexlitiel.substractum.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.substractum.entity.mobs.DepthWormEntity;
import razchexlitiel.substractum.entity.weapons.bullets.TurretBulletEntity;
import razchexlitiel.substractum.main.SubstractumMod;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SubstractumMod.MOD_ID);

    public static final RegistryObject<EntityType<TurretBulletEntity>> TURRET_BULLET =
            ENTITY_TYPES.register("turret_bullet",
                    () -> EntityType.Builder.<TurretBulletEntity>of(TurretBulletEntity::new, MobCategory.MISC)
                            .sized(0.05f, 0.05f)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .setShouldReceiveVelocityUpdates(true)
                            .build("turret_bullet"));

    public static final RegistryObject<EntityType<DepthWormEntity>> DEPTH_WORM =
            ENTITY_TYPES.register("depth_worm",
                    () -> EntityType.Builder.of(DepthWormEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.6f) // Размер хитбокса (сделаем его поменьше для лучшего пути)
                            .build(new ResourceLocation(SubstractumMod.MOD_ID, "depth_worm").toString()));

}
