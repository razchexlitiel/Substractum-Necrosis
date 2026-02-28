package razchexlitiel.cim.main;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import razchexlitiel.cim.api.energy.EnergyNetworkManager;
import razchexlitiel.cim.api.hive.HiveNetworkManagerProvider;
import razchexlitiel.cim.block.basic.ModBlocks;
import razchexlitiel.cim.block.entity.ModBlockEntities;
import razchexlitiel.cim.capability.ModCapabilities;
import razchexlitiel.cim.client.config.ModConfigKeybindHandler;
import razchexlitiel.cim.entity.ModEntities;
import razchexlitiel.cim.entity.mobs.DepthWormEntity;
import razchexlitiel.cim.entity.weapons.turrets.TurretLightEntity;
import razchexlitiel.cim.item.fekal_electric.ModBatteryItem;
import razchexlitiel.cim.menu.ModMenuTypes;
import razchexlitiel.cim.network.ModPacketHandler;
import razchexlitiel.cim.sound.ModSounds;
import software.bernie.geckolib.GeckoLib;

import razchexlitiel.cim.item.ModItems;

import java.util.List;

@Mod(CrustalIncursionMod.MOD_ID)
public class CrustalIncursionMod {
    public static final String MOD_ID = "cim";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CrustalIncursionMod() {
        LOGGER.info("Initializing Crustal Incursion...");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeTabs.register(modEventBus);
        GeckoLib.initialize();

        ModBlocks.register(modEventBus); // 1. Сначала блоки
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ServerLevel level = event.getServer().overworld(); // или через все миры
            EnergyNetworkManager.get(level).tick();
        }
    }

    @SubscribeEvent
    public static void onServerWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            EnergyNetworkManager.get(serverLevel).rebuildAllNetworks();
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Логгирование для отладки
        LOGGER.info("Building creative tab contents for: " + event.getTabKey());

        if (event.getTab() == ModCreativeTabs.CIM_TECH_TAB.get()) {

            event.accept(ModItems.SCREWDRIVER.get());
            event.accept(ModBlocks.SHAFT_IRON);
            event.accept(ModBlocks.MOTOR_ELECTRO);
            event.accept(ModBlocks.WIND_GEN_FLUGER);
            event.accept(ModBlocks.GEAR_PORT);
            event.accept(ModBlocks.ADDER);
            event.accept(ModBlocks.STOPPER);
            event.accept(ModBlocks.TACHOMETER);
            event.accept(ModBlocks.ROTATION_METER);

            event.accept(ModItems.CREATIVE_BATTERY);
            event.accept(ModBlocks.MACHINE_BATTERY);
            event.accept(ModBlocks.MACHINE_BATTERY_LITHIUM);
            event.accept(ModBlocks.WIRE_COATED);
            event.accept(ModBlocks.SWITCH);
            event.accept(ModBlocks.CONVERTER_BLOCK);

            List<RegistryObject<Item>> batteriesToAdd = List.of(
                    ModItems.BATTERY,
                    ModItems.BATTERY_ADVANCED,
                    ModItems.BATTERY_LITHIUM,
                    ModItems.BATTERY_TRIXITE
            );

            for (RegistryObject<Item> batteryRegObj : batteriesToAdd) {
                Item item = batteryRegObj.get();
                if (item instanceof ModBatteryItem batteryItem) {
                    ItemStack emptyStack = new ItemStack(batteryItem);
                    event.accept(emptyStack);
                    ItemStack chargedStack = new ItemStack(batteryItem);
                    ModBatteryItem.setEnergy(chargedStack, batteryItem.getCapacity());
                    event.accept(chargedStack);
                }
            }
        }


        if (event.getTab() == ModCreativeTabs.CIM_WEAPONS_TAB.get()) {

            event.accept(ModItems.DETONATOR);
            event.accept(ModItems.MULTI_DETONATOR);
            event.accept(ModItems.RANGE_DETONATOR);
            event.accept(ModBlocks.DET_MINER);
            event.accept(ModItems.TURRET_CHIP);
            event.accept(ModItems.TURRET_LIGHT_CREATIVE_PLACER);
            event.accept(ModItems.MACHINEGUN);
            event.accept(ModBlocks.TURRET_LIGHT_PLACER);
            event.accept(ModItems.AMMO_TURRET);
            event.accept(ModItems.AMMO_TURRET_HOLLOW);
            event.accept(ModItems.AMMO_TURRET_PIERCING);
            event.accept(ModItems.AMMO_TURRET_FIRE);
            event.accept(ModItems.AMMO_TURRET_RADIO);

        }
        if (event.getTab() == ModCreativeTabs.CIM_NATURE_TAB.get()) {

            event.accept(ModItems.DEPTH_WORM_SPAWN_EGG);
            event.accept(ModBlocks.DEPTH_WORM_NEST);
            event.accept(ModBlocks.HIVE_SOIL);

        }
    }

    // Метод регистрации атрибутов (здоровье, урон и т.д.)
    private void entityAttributeEvent(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(ModEntities.DEPTH_WORM.get(), DepthWormEntity.createAttributes().build());
        event.put(ModEntities.TURRET_LIGHT.get(), TurretLightEntity.createAttributes().build());
        event.put(ModEntities.TURRET_LIGHT_LINKED.get(), TurretLightEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        // Проверка, чтобы не прикрепить дважды
        if (!event.getObject().isClientSide) {
            event.addCapability(new ResourceLocation("cim", "hive_network_manager"),
                    new HiveNetworkManagerProvider());
        }
    }

}