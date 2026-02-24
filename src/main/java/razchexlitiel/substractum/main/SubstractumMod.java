package razchexlitiel.substractum.main;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import razchexlitiel.substractum.api.hive.HiveNetworkManagerProvider;
import razchexlitiel.substractum.block.basic.ModBlocks;
import razchexlitiel.substractum.block.entity.ModBlockEntities;
import razchexlitiel.substractum.capability.ModCapabilities;
import razchexlitiel.substractum.entity.ModEntities;
import razchexlitiel.substractum.entity.mobs.DepthWormEntity;
import razchexlitiel.substractum.network.ModPacketHandler;
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

        ModBlocks.register(modEventBus); // 1. Сначала блоки
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.register(modEventBus);
        modEventBus.addListener(this::entityAttributeEvent);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        registerCapabilities(modEventBus);
    }
    private void registerCapabilities(IEventBus modEventBus) {
        modEventBus.addListener(ModCapabilities::register);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPacketHandler.register();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Логгирование для отладки
        LOGGER.info("Building creative tab contents for: " + event.getTabKey());

        if (event.getTab() == ModCreativeTabs.SNM_WEAPONS_TAB.get()) {

            event.accept(ModItems.RANGE_DETONATOR);
            event.accept(ModBlocks.DET_MINER);
            event.accept(ModItems.MACHINEGUN);
            event.accept(ModItems.AMMO_TURRET);
            event.accept(ModItems.AMMO_TURRET_HOLLOW);
            event.accept(ModItems.AMMO_TURRET_PIERCING);
            event.accept(ModItems.AMMO_TURRET_FIRE);
            event.accept(ModItems.AMMO_TURRET_RADIO);

        }
        if (event.getTab() == ModCreativeTabs.SNM_NATURE_TAB.get()) {

            event.accept(ModItems.DEPTH_WORM_SPAWN_EGG);
            event.accept(ModBlocks.DEPTH_WORM_NEST);
            event.accept(ModBlocks.HIVE_SOIL);

        }




    }

    // Метод регистрации атрибутов (здоровье, урон и т.д.)
    private void entityAttributeEvent(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(ModEntities.DEPTH_WORM.get(), DepthWormEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        // Проверка, чтобы не прикрепить дважды
        if (!event.getObject().isClientSide) {
            event.addCapability(new ResourceLocation("substractum", "hive_network_manager"),
                    new HiveNetworkManagerProvider());
        }
    }

}