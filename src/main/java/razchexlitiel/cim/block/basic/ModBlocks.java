package razchexlitiel.cim.block.basic;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.cim.block.basic.energy.ConverterBlock;
import razchexlitiel.cim.block.basic.energy.MachineBatteryBlock;
import razchexlitiel.cim.block.basic.energy.SwitchBlock;
import razchexlitiel.cim.block.basic.energy.WireBlock;
import razchexlitiel.cim.block.basic.explosives.DetMinerBlock;
import razchexlitiel.cim.block.basic.hive.DepthWormNestBlock;
import razchexlitiel.cim.block.basic.hive.HiveSoilBlock;
import razchexlitiel.cim.block.basic.rotation.*;
import razchexlitiel.cim.item.fekal_electric.MachineBatteryBlockItem;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrustalIncursionMod.MOD_ID);


    public static final List<RegistryObject<Block>> BATTERY_BLOCKS = new ArrayList<>();
    public static final RegistryObject<Block> MACHINE_BATTERY = registerBattery("machine_battery", 1_000_000L);
    public static final RegistryObject<Block> MACHINE_BATTERY_LITHIUM = registerBattery("machine_battery_lithium", 50_000_000L);
    public static final RegistryObject<Block> DET_MINER = registerBlock("det_miner",
            () -> new DetMinerBlock(BlockBehaviour.Properties.of().strength(0.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CONVERTER_BLOCK = registerBlock("converter_block",
            () -> new ConverterBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final RegistryObject<Block> WIRE_COATED = registerBlock("wire_coated",
            () -> new WireBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));
    public static final RegistryObject<Block> SWITCH = registerBlock("switch",
            () -> new SwitchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> DEPTH_WORM_NEST = registerBlock("depth_worm_nest",
            () -> new DepthWormNestBlock(BlockBehaviour.Properties.copy(Blocks.MUD).sound(SoundType.MUD)));
    public static final RegistryObject<Block> HIVE_SOIL = registerBlock("hive_soil",
            () -> new HiveSoilBlock(BlockBehaviour.Properties.copy(Blocks.MUD).sound(SoundType.MUD)));

    public static final RegistryObject<Block> MOTOR_ELECTRO = BLOCKS.register("motor_electro",
            () -> new MotorElectroBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0f, 4.0f)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> WIND_GEN_FLUGER = BLOCKS.register("wind_gen_fluger",
            () -> new WindGenFlugerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0f, 4.0f)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));


    public static final RegistryObject<Block> SHAFT_IRON = BLOCKS.register("shaft_iron",
            () -> new ShaftIronBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.0f, 4.0f)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> GEAR_PORT = registerBlock("gear_port",
            () -> new GearPortBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(5.0f, 4.0f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> STOPPER = registerBlock("stopper",
            () -> new StopperBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(5.0f, 4.0f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ADDER = registerBlock("adder",
            () -> new AdderBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(5.0f, 4.0f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TACHOMETER = registerBlock("tachometer",
            () -> new TachometerBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(5.0f, 4.0f).requiresCorrectToolForDrops()));


    public static final RegistryObject<Block> ROTATION_METER = registerBlock("rotation_meter",
            () -> new RotationMeterBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(5.0f, 4.0f).requiresCorrectToolForDrops()));











    // Вспомогательный метод: регистрирует блок И предмет для него
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Регистрация предмета блока (чтобы он был в инвентаре)
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> registerBattery(String name, long capacity) {
        // 1. Регистрируем БЛОК
        RegistryObject<Block> batteryBlock = BLOCKS.register(name,
                () -> new MachineBatteryBlock(Block.Properties.of().strength(5.0f).requiresCorrectToolForDrops(), capacity));

        // 2. Регистрируем ПРЕДМЕТ (MachineBatteryBlockItem)
        ModItems.ITEMS.register(name,
                () -> new MachineBatteryBlockItem(batteryBlock.get(), new Item.Properties(), capacity));

        // 3. Добавляем в список для TileEntity
        BATTERY_BLOCKS.add(batteryBlock);

        return batteryBlock;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}