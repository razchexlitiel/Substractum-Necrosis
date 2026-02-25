package razchexlitiel.cim.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import razchexlitiel.cim.block.entity.energy.ConverterBlockEntity;
import razchexlitiel.cim.block.entity.energy.MachineBatteryBlockEntity;
import razchexlitiel.cim.block.entity.energy.SwitchBlockEntity;
import razchexlitiel.cim.block.entity.energy.WireBlockEntity;
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

    public static final RegistryObject<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY_BE =
            BLOCK_ENTITIES.register("machine_battery_be", () -> {
                // Превращаем список RegistryObject в массив Block[]
                Block[] validBlocks = ModBlocks.BATTERY_BLOCKS.stream()
                        .map(RegistryObject::get)
                        .toArray(Block[]::new);

                return BlockEntityType.Builder.<MachineBatteryBlockEntity>of(MachineBatteryBlockEntity::new, validBlocks)
                        .build(null);
            });

    public static final RegistryObject<BlockEntityType<WireBlockEntity>> WIRE_BE =
            BLOCK_ENTITIES.register("wire_be", () ->
                    BlockEntityType.Builder.<WireBlockEntity>of(WireBlockEntity::new, ModBlocks.WIRE_COATED.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<ConverterBlockEntity>> CONVERTER_BE =
            BLOCK_ENTITIES.register("converter_be",
                    () -> BlockEntityType.Builder.of(ConverterBlockEntity::new, ModBlocks.CONVERTER_BLOCK.get()).build(null));



    public static final RegistryObject<BlockEntityType<SwitchBlockEntity>> SWITCH_BE =
            BLOCK_ENTITIES.register("switch_be", () ->
                    BlockEntityType.Builder.of(SwitchBlockEntity::new, ModBlocks.SWITCH.get())
                            .build(null));




    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}