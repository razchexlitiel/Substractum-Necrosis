package com.smogline.main;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import software.bernie.geckolib.GeckoLib;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

@Mod(RefStrings.MODID)
public class MainRegistry {

    // Добавляем логгер для отладки
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "snm";



    private void registerCapabilities(IEventBus modEventBus) {
        modEventBus.addListener(ModCapabilities::register);
    }


    static {
        // Регистрируем конфиг до любых обращений к нему!
        ModClothConfig.register();
    }

    //ingot
    public MainRegistry(FMLJavaModLoadingContext context) {
        LOGGER.info("Initializing " + RefStrings.NAME);

        IEventBus modEventBus = context.getModEventBus();
        // ПРЯМАЯ РЕГИСТРАЦИЯ DEFERRED REGISTERS
        // Добавь эту:
        GeckoLib.initialize();

        MinecraftForge.EVENT_BUS.register(new CrateBreaker());
        MinecraftForge.EVENT_BUS.register(new MobGearHandler());
        ModBiomes.BIOMES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModExplosionParticles.PARTICLE_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModParticleTypes.PARTICLES.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModWorldGen.BIOME_MODIFIERS.register(modEventBus);
        ModEffects.register(modEventBus);
        ModRecipes.register(modEventBus);
        registerCapabilities(modEventBus);
        FogConfig.register();

        // ✅ ЭТА СТРОКА ДОЛЖНА БЫТЬ ПОСЛЕДНЕЙ!
        ModWorldGen.PROCESSORS.register(modEventBus);  // ✅ ОСТАВИ!

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        ModFluids.register(modEventBus);


        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ChunkRadiationManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new PlayerHandler());

        // >>> ДОБАВИТЬ ЭТУ СТРОКУ: <<<
        modEventBus.addListener(this::entityAttributeEvent);
        // Регистрация остальных систем resources
        // ModPacketHandler.register(); // Регистрация пакетов


        // Инстанцируем ClientSetup, чтобы его конструктор вызвал регистрацию на Forge Event Bus

        LOGGER.info("Radiation handlers registered. Using {}.", ModClothConfig.get().usePrismSystem ? "ChunkRadiationHandlerPRISM" : "ChunkRadiationHandlerSimple");
        LOGGER.info("Registered event listeners for Radiation System.");
        LOGGER.info("!!! MainRegistry: ClientSetup instance created, its Forge listeners should now be registered !!!");
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

            ModPacketHandler.register();
            ModHazards.registerHazards(); // Регистрация опасностей (радиация, биологическая опасность в будущем и тд)
            // MinecraftForge.EVENT_BUS.addListener(this::onRenderLevelStage);

            LOGGER.info("HazardSystem initialized successfully");
        });
    }

    @SubscribeEvent
    public void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        final ResourceLocation key = ResourceLocation.fromNamespaceAndPath(RefStrings.MODID, "chunk_radiation");

        // Проверяем, что capability еще не присоединен другим источником (стандартная мера предосторожности).
        if (!event.getCapabilities().containsKey(key)) {
            ChunkRadiationProvider provider = new ChunkRadiationProvider();
            event.addCapability(key, provider);

            // Добавляем слушатель для инвалидации LazyOptional.
            // Когда чанк выгружается, capability становится недействительным. Этот слушатель
            // позаботится о том, чтобы наш LazyOptional тоже был помечен как недействительный,
            // что помогает избежать утечек памяти.
            event.addListener(provider.getCapability(ChunkRadiationProvider.CHUNK_RADIATION_CAPABILITY)::invalidate);
        }
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

        // ТАЙМЕР ЗАКАНЧИВАЕТСЯ, ВЗРЫВЕМСЯ!
        if (event.getTab() == ModCreativeTabs.SMOGLINE_WEAPONS_TAB.get()) {

            event.accept(ModItems.GRENADE);
            event.accept(ModItems.GRENADEHE);
            event.accept(ModItems.GRENADEFIRE);
            event.accept(ModItems.GRENADESMART);
            event.accept(ModItems.GRENADESLIME);
            event.accept(ModItems.GRENADE_IF);
            event.accept(ModItems.GRENADE_IF_HE);
            event.accept(ModItems.GRENADE_IF_SLIME);
            event.accept(ModItems.GRENADE_IF_FIRE);
            event.accept(ModItems.GRENADE_NUC);

            event.accept(ModItems.DETONATOR);
            event.accept(ModItems.MULTI_DETONATOR);
            event.accept(ModItems.RANGE_DETONATOR);
            event.accept(ModItems.AIRSTRIKE_TEST);
            event.accept(ModItems.AIRSTRIKE_HEAVY);
            event.accept(ModItems.AIRSTRIKE_AGENT);
            event.accept(ModItems.AIRSTRIKE_NUKE);

            event.accept(ModItems.MACHINEGUN);
            event.accept(ModItems.AMMO_TURRET);
            event.accept(ModItems.AMMO_TURRET_HOLLOW);
            event.accept(ModItems.AMMO_TURRET_PIERCING);
            event.accept(ModItems.AMMO_TURRET_FIRE);
            event.accept(ModItems.AMMO_TURRET_RADIO);

            event.accept(ModBlocks.MINE_AP);
            event.accept(ModBlocks.MINE_FAT);
            event.accept(ModBlocks.AIRBOMB);
            event.accept(ModBlocks.BALEBOMB_TEST);
            event.accept(ModBlocks.C4);
            event.accept(ModBlocks.SMOKE_BOMB);
            event.accept(ModBlocks.DET_MINER);
            event.accept(ModBlocks.NUCLEAR_CHARGE);
            event.accept(ModBlocks.EXPLOSIVE_CHARGE);
            event.accept(ModBlocks.GIGA_DET);
            event.accept(ModBlocks.BARBED_WIRE_FIRE);
            event.accept(ModBlocks.BARBED_WIRE_POISON);
            event.accept(ModBlocks.BARBED_WIRE_RAD);
            event.accept(ModBlocks.BARBED_WIRE_WITHER);
            event.accept(ModBlocks.BARBED_WIRE);

            event.accept(ModItems.TURRET_CHIP);
            event.accept(ModBlocks.TURRET_LIGHT_PLACER);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientSetup.addTemplatesClient(event);
            });

          }

            if (event.getTab() == ModCreativeTabs.SMOGLINE_TECH_TAB.get()) {

                event.accept(ModItems.SCREWDRIVER.get());
                event.accept(ModBlocks.SHAFT_IRON);
                event.accept(ModBlocks.MOTOR_ELECTRO);
                event.accept(ModBlocks.WIND_GEN_FLUGER);
                event.accept(ModBlocks.GEAR_PORT);
                event.accept(ModBlocks.ADDER);
                event.accept(ModBlocks.STOPPER);
                event.accept(ModBlocks.TACHOMETER);
                event.accept(ModBlocks.ROTATION_METER);
                event.accept(ModBlocks.DEPTH_WORM_NEST);
                event.accept(ModBlocks.HIVE_SOIL);
                event.accept(ModItems.DEPTH_WORM_SPAWN_EGG);
            }


         // СТАНКИ
            if (event.getTab() == ModCreativeTabs.NTM_MACHINES_TAB.get()) {
            event.accept(ModBlocks.CRATE_IRON);
            event.accept(ModBlocks.CRATE_STEEL);
            event.accept(ModBlocks.ANVIL_IRON);
            event.accept(ModBlocks.ANVIL_LEAD);
            event.accept(ModBlocks.ANVIL_STEEL);
            event.accept(ModBlocks.ANVIL_DESH);
            event.accept(ModBlocks.ANVIL_FERROURANIUM);
            event.accept(ModBlocks.ANVIL_SATURNITE);
            event.accept(ModBlocks.ANVIL_BISMUTH_BRONZE);
            event.accept(ModBlocks.ANVIL_ARSENIC_BRONZE);
            event.accept(ModBlocks.ANVIL_SCHRABIDATE);
            event.accept(ModBlocks.ANVIL_DNT);
            event.accept(ModBlocks.ANVIL_OSMIRIDIUM);
            event.accept(ModBlocks.ANVIL_MURKY);
            event.accept(ModBlocks.PRESS);
            event.accept(ModBlocks.BLAST_FURNACE);
            event.accept(ModBlocks.BLAST_FURNACE_EXTENSION);
            event.accept(ModBlocks.SHREDDER);
            event.accept(ModBlocks.WOOD_BURNER);
            event.accept(ModBlocks.MACHINE_ASSEMBLER);
            event.accept(ModBlocks.ADVANCED_ASSEMBLY_MACHINE);
            event.accept(ModBlocks.ARMOR_TABLE);

            event.accept(ModBlocks.FLUID_TANK);
            event.accept(ModBlocks.MACHINE_BATTERY);
            event.accept(ModBlocks.MACHINE_BATTERY_LITHIUM);
            event.accept(ModBlocks.MACHINE_BATTERY_SCHRABIDIUM);
            event.accept(ModBlocks.MACHINE_BATTERY_DINEUTRONIUM);
            event.accept(ModBlocks.CONVERTER_BLOCK);

            event.accept(ModBlocks.WIRE_COATED);
            event.accept(ModBlocks.SWITCH);

            event.accept(ModItems.CREATIVE_BATTERY);

            List<RegistryObject<Item>> batteriesToAdd = List.of(
                    ModItems.BATTERY_POTATO,
                    ModItems.BATTERY,
                    ModItems.BATTERY_RED_CELL,
                    ModItems.BATTERY_RED_CELL_6,
                    ModItems.BATTERY_RED_CELL_24,
                    ModItems.BATTERY_ADVANCED,
                    ModItems.BATTERY_ADVANCED_CELL,
                    ModItems.BATTERY_ADVANCED_CELL_4,
                    ModItems.BATTERY_ADVANCED_CELL_12,
                    ModItems.BATTERY_LITHIUM,
                    ModItems.BATTERY_LITHIUM_CELL,
                    ModItems.BATTERY_LITHIUM_CELL_3,
                    ModItems.BATTERY_LITHIUM_CELL_6,
                    ModItems.BATTERY_SCHRABIDIUM,
                    ModItems.BATTERY_SCHRABIDIUM_CELL,
                    ModItems.BATTERY_SCHRABIDIUM_CELL_2,
                    ModItems.BATTERY_SCHRABIDIUM_CELL_4,
                    ModItems.BATTERY_SPARK,
                    ModItems.BATTERY_TRIXITE,
                    ModItems.BATTERY_SPARK_CELL_6,
                    ModItems.BATTERY_SPARK_CELL_25,
                    ModItems.BATTERY_SPARK_CELL_100,
                    ModItems.BATTERY_SPARK_CELL_1000,
                    ModItems.BATTERY_SPARK_CELL_2500,
                    ModItems.BATTERY_SPARK_CELL_10000,
                    ModItems.BATTERY_SPARK_CELL_POWER
            );

// 2. Проходимся по списку и добавляем 2 версии каждой
            for (RegistryObject<Item> batteryRegObj : batteriesToAdd) {
                Item item = batteryRegObj.get();

                // Проверка, что это ModBatteryItem
                if (item instanceof ModBatteryItem batteryItem) {
                    // Добавляем пустую батарею
                    ItemStack emptyStack = new ItemStack(batteryItem);
                    event.accept(emptyStack);

                    // Создаем заряженную батарею
                    ItemStack chargedStack = new ItemStack(batteryItem);
                    ModBatteryItem.setEnergy(chargedStack, batteryItem.getCapacity());
                    event.accept(chargedStack);

                    if (ModClothConfig.get().enableDebugLogging) {
                        LOGGER.debug("Added empty and charged variants of {} to creative tab",
                                batteryRegObj.getId());
                    }
                } else {
                    // На всякий случай, если в списке что-то не ModBatteryItem
                    event.accept(item);
                    LOGGER.warn("Item {} is not a ModBatteryItem, added as regular item",
                            batteryRegObj.getId());
                }
            }

            event.accept(ModItems.BLADE_STEEL);
            event.accept(ModItems.BLADE_TITANIUM);
            event.accept(ModItems.BLADE_ALLOY);
            event.accept(ModItems.BLADE_TEST);
            event.accept(ModItems.STAMP_STONE_FLAT);
            event.accept(ModItems.STAMP_STONE_PLATE);
            event.accept(ModItems.STAMP_STONE_WIRE);
            event.accept(ModItems.STAMP_STONE_CIRCUIT);
            event.accept(ModItems.STAMP_IRON_FLAT);
            event.accept(ModItems.STAMP_IRON_PLATE);
            event.accept(ModItems.STAMP_IRON_WIRE);
            event.accept(ModItems.STAMP_IRON_CIRCUIT);
            event.accept(ModItems.STAMP_IRON_9);
            event.accept(ModItems.STAMP_IRON_44);
            event.accept(ModItems.STAMP_IRON_50);
            event.accept(ModItems.STAMP_IRON_357);
            event.accept(ModItems.STAMP_STEEL_FLAT);
            event.accept(ModItems.STAMP_STEEL_PLATE);
            event.accept(ModItems.STAMP_STEEL_WIRE);
            event.accept(ModItems.STAMP_STEEL_CIRCUIT);
            event.accept(ModItems.STAMP_TITANIUM_FLAT);
            event.accept(ModItems.STAMP_TITANIUM_PLATE);
            event.accept(ModItems.STAMP_TITANIUM_WIRE);
            event.accept(ModItems.STAMP_TITANIUM_FLAT);
            event.accept(ModItems.STAMP_TITANIUM_PLATE);
            event.accept(ModItems.STAMP_TITANIUM_WIRE);
            event.accept(ModItems.STAMP_TITANIUM_CIRCUIT);
            event.accept(ModItems.STAMP_OBSIDIAN_FLAT);
            event.accept(ModItems.STAMP_OBSIDIAN_PLATE);
            event.accept(ModItems.STAMP_OBSIDIAN_WIRE);
            event.accept(ModItems.STAMP_OBSIDIAN_CIRCUIT);
            event.accept(ModItems.STAMP_DESH_FLAT);
            event.accept(ModItems.STAMP_DESH_PLATE);
            event.accept(ModItems.STAMP_DESH_WIRE);
            event.accept(ModItems.STAMP_DESH_CIRCUIT);
            event.accept(ModItems.STAMP_DESH_9);
            event.accept(ModItems.STAMP_DESH_44);
            event.accept(ModItems.STAMP_DESH_50);
            event.accept(ModItems.STAMP_DESH_357);

            event.accept(ModItems.TEMPLATE_FOLDER);

            event.accept(ModItems.FLUID_IDENTIFIER.get());

            // 2. Добавляем идентификаторы для ВСЕХ жидкостей в игре
            // Цикл проходит по реестру жидкостей Forge
            for (net.minecraft.world.level.material.Fluid fluid : net.minecraftforge.registries.ForgeRegistries.FLUIDS) {
                // Проверяем: жидкость не пустая И это "источник" (не течение)
                if (fluid != net.minecraft.world.level.material.Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState())) {

                    // Используем статический метод, который мы создали в ItemFluidIdentifier
                    // Убедись, что импортировал класс ItemFluidIdentifier
                    event.accept(com.smogline.item.custom.liquids.ItemFluidIdentifier.createStackFor(fluid));
                }
            }
            event.accept(ModBlocks.MOX1);
            event.accept(ModBlocks.MOX2);
            event.accept(ModBlocks.MOX3);
            event.accept(ModBlocks.SAND_ROUGH);

            event.accept(ModBlocks.FLUORITE_ORE);
            event.accept(ModBlocks.LIGNITE_ORE);
            event.accept(ModBlocks.TUNGSTEN_ORE);
            event.accept(ModBlocks.ASBESTOS_ORE);
            event.accept(ModBlocks.SULFUR_ORE);
            event.accept(ModBlocks.SEQUESTRUM_ORE);

            event.accept(ModBlocks.ALUMINUM_ORE);
            event.accept(ModBlocks.ALUMINUM_ORE_DEEPSLATE);
            event.accept(ModBlocks.TITANIUM_ORE);
            event.accept(ModBlocks.TITANIUM_ORE_DEEPSLATE);
            event.accept(ModBlocks.COBALT_ORE);
            event.accept(ModBlocks.COBALT_ORE_DEEPSLATE);
            event.accept(ModBlocks.THORIUM_ORE);
            event.accept(ModBlocks.THORIUM_ORE_DEEPSLATE);
            event.accept(ModBlocks.RAREGROUND_ORE);
            event.accept(ModBlocks.RAREGROUND_ORE_DEEPSLATE);
            event.accept(ModBlocks.BERYLLIUM_ORE);
            event.accept(ModBlocks.BERYLLIUM_ORE_DEEPSLATE);
            event.accept(ModBlocks.LEAD_ORE);
            event.accept(ModBlocks.LEAD_ORE_DEEPSLATE);
            event.accept(ModBlocks.CINNABAR_ORE);
            event.accept(ModBlocks.CINNABAR_ORE_DEEPSLATE);
            event.accept(ModBlocks.URANIUM_ORE_DEEPSLATE);

            event.accept(ModBlocks.RESOURCE_ASBESTOS.get());
            event.accept(ModBlocks.RESOURCE_BAUXITE.get());
            event.accept(ModBlocks.RESOURCE_HEMATITE.get());
            event.accept(ModBlocks.RESOURCE_LIMESTONE.get());
            event.accept(ModBlocks.RESOURCE_MALACHITE.get());
            event.accept(ModBlocks.RESOURCE_SULFUR.get());

            event.accept(ModItems.ALUMINUM_RAW);
            event.accept(ModItems.BERYLLIUM_RAW);
            event.accept(ModItems.COBALT_RAW);
            event.accept(ModItems.LEAD_RAW);
            event.accept(ModItems.THORIUM_RAW);
            event.accept(ModItems.TITANIUM_RAW);
            event.accept(ModItems.TUNGSTEN_RAW);
            event.accept(ModItems.URANIUM_RAW);


            event.accept(ModBlocks.GEYSIR_DIRT);
            event.accept(ModBlocks.GEYSIR_STONE);

            event.accept(ModBlocks.NUCLEAR_FALLOUT);
            event.accept(ModBlocks.SELLAFIELD_SLAKED);
            event.accept(ModBlocks.SELLAFIELD_SLAKED1);
            event.accept(ModBlocks.SELLAFIELD_SLAKED2);
            event.accept(ModBlocks.SELLAFIELD_SLAKED3);
            event.accept(ModBlocks.WASTE_LOG);
            event.accept(ModBlocks.WASTE_PLANKS);
            event.accept(ModBlocks.WASTE_GRASS);
            event.accept(ModBlocks.BURNED_GRASS);
            event.accept(ModBlocks.DEAD_DIRT);
            event.accept(ModBlocks.WASTE_LEAVES);

            event.accept(ModItems.STRAWBERRY);
            event.accept(ModBlocks.STRAWBERRY_BUSH);

            event.accept(ModBlocks.POLONIUM210_BLOCK);
// АВТОМАТИЧЕСКОЕ ДОБАВЛЕНИЕ ВСЕХ БЛОКОВ СЛИТКОВ
            for (ModIngots ingot : ModIngots.values()) {

                // !!! ВАЖНОЕ ИСПРАВЛЕНИЕ: ПРОВЕРКА НАЛИЧИЯ БЛОКА !!!
                if (ModBlocks.hasIngotBlock(ingot)) {

                    RegistryObject<Block> ingotBlock = ModBlocks.getIngotBlock(ingot);
                    if (ingotBlock != null) {
                        event.accept(ingotBlock.get());
                        if (ModClothConfig.get().enableDebugLogging) {
                            LOGGER.info("Added {} block to NTM Ores tab", ingotBlock.getId());
                        }
                    }
                }
            }


            event.accept(new ItemStack(ModItems.CINNABAR.get()));
            event.accept(new ItemStack(ModItems.FIRECLAY_BALL.get()));
            event.accept(new ItemStack(ModItems.SULFUR.get()));
            event.accept(new ItemStack(ModItems.SEQUESTRUM.get()));
            event.accept(new ItemStack(ModItems.LIGNITE.get()));
            event.accept(new ItemStack(ModItems.FLUORITE.get()));
            event.accept(new ItemStack(ModItems.RAREGROUND_ORE_CHUNK.get()));
            event.accept(new ItemStack(ModItems.FIREBRICK.get()));
            event.accept(new ItemStack(ModItems.WOOD_ASH_POWDER.get()));
            event.accept(new ItemStack(ModItems.CRUDE_OIL_BUCKET.get()));


            // ✅ СЛИТКИ
            for (ModIngots ingot : ModIngots.values()) {
                RegistryObject<Item> ingotItem = ModItems.getIngot(ingot);
                if (ingotItem != null && ingotItem.isPresent()) {
                    event.accept(new ItemStack(ingotItem.get()));
                }

            }

            // ✅ ModPowders
            for (ModPowders powder : ModPowders.values()) {
                RegistryObject<Item> powderItem = ModItems.getPowders(powder);
                if (powderItem != null && powderItem.isPresent()) {
                    event.accept(new ItemStack(powderItem.get()));
                }
            }

            // ✅ ОДИН ЦИКЛ ДЛЯ ВСЕХ ПОРОШКОВ ИЗ СЛИТКОВ (обычные + маленькие)
            for (ModIngots ingot : ModIngots.values()) {
                // Обычный порошок
                RegistryObject<Item> powder = ModItems.getPowder(ingot);
                if (powder != null && powder.isPresent()) {
                    event.accept(new ItemStack(powder.get()));
                }

                // Маленький порошок
                ModItems.getTinyPowder(ingot).ifPresent(tiny -> {
                    if (tiny != null && tiny.isPresent()) {
                        event.accept(new ItemStack(tiny.get()));
                    }
                });
            }
            // АВТОМАТИЧЕСКОЕ ДОБАВЛЕНИЕ ВСЕХ МОДИФИКАТОРОВ
            // 1. Получаем все зарегистрированные предметы
            List<RegistryObject<Item>> allModItems = ForgeRegistries.ITEMS.getEntries().stream()
                    .filter(entry -> entry.getKey().location().getNamespace().equals(RefStrings.MODID))
                    .map(entry -> RegistryObject.create(entry.getKey().location(), ForgeRegistries.ITEMS))
                    .collect(Collectors.toList());

            // 2. Проходимся по всем предметам и добавляем те, которые являются модификаторами
            for (RegistryObject<Item> itemObject : allModItems) {
                Item item = itemObject.get();
                if (item instanceof ItemArmorMod) {
                    event.accept(item);
                    if (ModClothConfig.get().enableDebugLogging) {
                        LOGGER.info("Automatically added Armor Mod [{}] to NTM Consumables tab", itemObject.getId());
                    }
                }
            }
            event.accept(ModItems.RADAWAY);
            //ЯЩИКИ
            event.accept(ModBlocks.FREAKY_ALIEN_BLOCK);
            event.accept(ModBlocks.CRATE);
            event.accept(ModBlocks.CRATE_LEAD);
            event.accept(ModBlocks.CRATE_METAL);
            event.accept(ModBlocks.CRATE_WEAPON);
            event.accept(ModBlocks.CRATE_CONSERVE);

            //ОСВЕЩЕНИЕ
            event.accept(ModBlocks.CAGE_LAMP);
            event.accept(ModBlocks.FLOOD_LAMP);



            event.accept(ModBlocks.DOOR_OFFICE);
            event.accept(ModBlocks.DOOR_BUNKER);
            event.accept(ModBlocks.METAL_DOOR);


            // БРОНЯ


            //СПЕЦ БРОНЯ
            event.accept(ModItems.SECURITY_HELMET);
            event.accept(ModItems.SECURITY_CHESTPLATE);
            event.accept(ModItems.SECURITY_LEGGINGS);
            event.accept(ModItems.SECURITY_BOOTS);



           /* //СИЛОВАЯ БРОНЯ
            event.accept(ModItems.AJR_HELMET);
            event.accept(ModItems.AJR_CHESTPLATE);
            event.accept(ModItems.AJR_LEGGINGS);
            event.accept(ModItems.AJR_BOOTS);*/

            //МЕЧИ

            event.accept(ModItems.STARMETAL_SWORD);

            //ТОПОРЫ

            event.accept(ModItems.STARMETAL_AXE);

            //КИРКИ

            event.accept(ModItems.STARMETAL_PICKAXE);

            //ЛОПАТЫ

            event.accept(ModItems.STARMETAL_SHOVEL);

            //МОТЫГИ

            event.accept(ModItems.STARMETAL_HOE);

            //СПЕЦ. ИНСТРУМЕНТЫ
            event.accept(ModItems.DEFUSER);
            event.accept(ModItems.CROWBAR);

            event.accept(ModItems.DOSIMETER);
            event.accept(ModItems.GEIGER_COUNTER);
            event.accept(ModBlocks.GEIGER_COUNTER_BLOCK);

            event.accept(ModItems.OIL_DETECTOR);
            event.accept(ModItems.DEPTH_ORES_SCANNER);


        }

    }
    // Метод регистрации атрибутов (здоровье, урон и т.д.)
    private void entityAttributeEvent(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(ModEntities.TURRET_LIGHT.get(), TurretLightEntity.createAttributes().build());
        event.put(ModEntities.TURRET_LIGHT_LINKED.get(), TurretLightEntity.createAttributes().build());
        event.put(ModEntities.DEPTH_WORM.get(), DepthWormEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onAttachCapabilitiesLevel(AttachCapabilitiesEvent<Level> event) {
        // Проверка, чтобы не прикрепить дважды
        if (!event.getObject().isClientSide) {
            event.addCapability(new ResourceLocation("smogline", "hive_network_manager"),
                    new HiveNetworkManagerProvider());
        }
    }

}

