package razchexlitiel.cim.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import razchexlitiel.cim.block.entity.hive.DepthWormNestBlockEntity;
import razchexlitiel.cim.block.entity.hive.HiveSoilBlockEntity;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.block.basic.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CrustalIncursionMod.MOD_ID);

    // ПРИМЕР: Некротический Алтарь (если такой был в cim)
    // .build(null) в 1.20.1 — это стандарт для создания типа

    public static final RegistryObject<BlockEntityType<DepthWormNestBlockEntity>> DEPTH_WORM_NEST =
            BLOCK_ENTITIES.register("depth_worm_nest",
                    () -> BlockEntityType.Builder.of(DepthWormNestBlockEntity::new,
                            ModBlocks.DEPTH_WORM_NEST.get()).build(null));

    public static final RegistryObject<BlockEntityType<HiveSoilBlockEntity>> HIVE_SOIL =
            BLOCK_ENTITIES.register("hive_soil", () ->
                    BlockEntityType.Builder.of(HiveSoilBlockEntity::new, ModBlocks.HIVE_SOIL.get())
                            .build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}