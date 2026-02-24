package razchexlitiel.substractum.main;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import razchexlitiel.substractum.block.basic.ModBlocks;
import razchexlitiel.substractum.entity.ModEntities;
import razchexlitiel.substractum.sound.ModSounds;
import software.bernie.geckolib.GeckoLib;

import razchexlitiel.substractum.item.ModItems;

@Mod(SubstractumMod.MOD_ID)
public class SubstractumMod {
    public static final String MOD_ID = "substractum";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SubstractumMod() {
        LOGGER.info("Initializing Substractum Necrosis...");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModCreativeTabs.register(modEventBus);
        GeckoLib.initialize();
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Логгирование для отладки
        LOGGER.info("Building creative tab contents for: " + event.getTabKey());

        if (event.getTab() == ModCreativeTabs.SNM_WEAPONS_TAB.get()) {

            event.accept(ModItems.RANGE_DETONATOR);
            event.accept(ModBlocks.DET_MINER);
            event.accept(ModItems.AMMO_TURRET);
            event.accept(ModItems.AMMO_TURRET_HOLLOW);
            event.accept(ModItems.AMMO_TURRET_PIERCING);
            event.accept(ModItems.AMMO_TURRET_FIRE);
            event.accept(ModItems.AMMO_TURRET_RADIO);

            



        }

    }

    // Метод регистрации атрибутов (здоровье, урон и т.д.)
    private void entityAttributeEvent(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
    }

}