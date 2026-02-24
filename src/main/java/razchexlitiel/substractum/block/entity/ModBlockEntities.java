package razchexlitiel.substractum.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.substractum.main.SubstractumMod;
import razchexlitiel.substractum.block.basic.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SubstractumMod.MOD_ID);

    // ПРИМЕР: Некротический Алтарь (если такой был в Smogline)
    // .build(null) в 1.20.1 — это стандарт для создания типа
    /*
    public static final RegistryObject<BlockEntityType<NecroticAltarBlockEntity>> NECROTIC_ALTAR =
            BLOCK_ENTITIES.register("necrotic_altar", () ->
                    BlockEntityType.Builder.of(NecroticAltarBlockEntity::new,
                            ModBlocks.NECROTIC_ORE.get()).build(null));
    */

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}