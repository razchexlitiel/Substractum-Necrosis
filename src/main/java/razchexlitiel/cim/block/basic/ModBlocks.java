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
import razchexlitiel.cim.block.basic.explosives.DetMinerBlock;
import razchexlitiel.cim.block.basic.hive.DepthWormNestBlock;
import razchexlitiel.cim.block.basic.hive.HiveSoilBlock;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrustalIncursionMod.MOD_ID);

    public static final RegistryObject<Block> DET_MINER = registerBlock("det_miner",
            () -> new DetMinerBlock(BlockBehaviour.Properties.of()
                    .strength(0.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> DEPTH_WORM_NEST = registerBlock("depth_worm_nest",
            () -> new DepthWormNestBlock(BlockBehaviour.Properties.copy(Blocks.MUD).sound(SoundType.MUD)));

    public static final RegistryObject<Block> HIVE_SOIL = registerBlock("hive_soil",
            () -> new HiveSoilBlock(BlockBehaviour.Properties.copy(Blocks.MUD).sound(SoundType.MUD)));

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

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}