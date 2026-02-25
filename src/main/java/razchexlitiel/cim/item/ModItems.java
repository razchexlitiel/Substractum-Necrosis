package razchexlitiel.cim.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.cim.block.basic.ModBlocks;
import razchexlitiel.cim.item.activators.RangeDetonatorItem;
import razchexlitiel.cim.item.fekal_electric.ItemCreativeBattery;
import razchexlitiel.cim.item.fekal_electric.ModBatteryItem;
import razchexlitiel.cim.item.guns.MachineGunItem;
import razchexlitiel.cim.item.mobs.DepthWormSpawnEggItem;
import razchexlitiel.cim.item.rotation.MotorElectroBlockItem;
import razchexlitiel.cim.item.rotation.ScrewdriverItem;
import razchexlitiel.cim.item.rotation.ShaftIronBlockItem;
import razchexlitiel.cim.item.rotation.WindGenFlugerBlockItem;
import razchexlitiel.cim.item.weapons.ammo.AmmoTurretItem;
import razchexlitiel.cim.main.CrustalIncursionMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CrustalIncursionMod.MOD_ID);

    public static final RegistryObject<Item> RANGE_DETONATOR = ITEMS.register("range_detonator",
            () -> new RangeDetonatorItem(new Item.Properties()));

    public static final RegistryObject<Item> DEPTH_WORM_SPAWN_EGG = ITEMS.register("depth_worm_spawn_egg",
            () -> new DepthWormSpawnEggItem(new Item.Properties()));

    public static final RegistryObject<Item> MACHINEGUN = ITEMS.register("machinegun",
            () -> new MachineGunItem(new Item.Properties()));

    public static final RegistryObject<Item> AMMO_TURRET = ITEMS.register("ammo_turret",
            () -> new AmmoTurretItem(new Item.Properties(), 4.0f, 3.0f, false));

    // Пробивной
    public static final RegistryObject<Item> AMMO_TURRET_PIERCING = ITEMS.register("ammo_turret_piercing",
            () -> new AmmoTurretItem(new Item.Properties(), 5.0f, 3.0f, true));

    // Пробивной
    public static final RegistryObject<Item> AMMO_TURRET_HOLLOW = ITEMS.register("ammo_turret_hollow",
            () -> new AmmoTurretItem(new Item.Properties(), 4.0f, 3.0f, false));

    // ОГНЕННЫЙ (убедись, что параметры правильные!)
    public static final RegistryObject<Item> AMMO_TURRET_FIRE = ITEMS.register("ammo_turret_fire",
            () -> new AmmoTurretItem(new Item.Properties(), 3.0f, 3.0f, false)); // Урон 3.0, не пробивает

    public static final RegistryObject<Item> AMMO_TURRET_RADIO = ITEMS.register("ammo_turret_radio",
            () -> new AmmoTurretItem(new Item.Properties(), 4.0f, 3.0f, false));

    public static final RegistryObject<Item> SCREWDRIVER = ITEMS.register("screwdriver",
            () -> new ScrewdriverItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(256))); // Прочность как у железных инструментов


    public static final RegistryObject<Item> MOTOR_ELECTRO_ITEM = ITEMS.register("motor_electro",
            () -> new MotorElectroBlockItem(ModBlocks.MOTOR_ELECTRO.get(),
                    new Item.Properties()));

    public static final RegistryObject<Item> WIND_GEN_FLUGER = ITEMS.register("wind_gen_fluger",
            () -> new WindGenFlugerBlockItem(ModBlocks.WIND_GEN_FLUGER.get(),
                    new Item.Properties()));


    // ---------- ВАЛ ЖЕЛЕЗНЫЙ (с GeoItem) ----------
    public static final RegistryObject<Item> SHAFT_IRON_ITEM = ITEMS.register("shaft_iron",
            () -> new ShaftIronBlockItem(ModBlocks.SHAFT_IRON.get(),
                    new Item.Properties()));

    //батарейки

    public static final RegistryObject<Item> CREATIVE_BATTERY = ITEMS.register("battery_creative",
            () -> new ItemCreativeBattery(
                    new Item.Properties()
            )
    );

    public static final RegistryObject<Item> BATTERY = ITEMS.register("battery",
            () -> new ModBatteryItem(
                    new Item.Properties(),
                    5000,
                    100,
                    100
            ));

    public static final RegistryObject<Item> BATTERY_ADVANCED = ITEMS.register("battery_advanced",
            () -> new ModBatteryItem(
                    new Item.Properties(),
                    20000,
                    500,
                    500
            ));

    // ========== ЛИТИЕВЫЕ БАТАРЕЙКИ (LITHIUM) ==========
    public static final RegistryObject<Item> BATTERY_LITHIUM = ITEMS.register("battery_lithium",
            () -> new ModBatteryItem(
                    new Item.Properties(),
                    250000,
                    1000,
                    1000
            ));


    public static final RegistryObject<Item> BATTERY_TRIXITE = ITEMS.register("battery_trixite",
            () -> new ModBatteryItem(
                    new Item.Properties(),
                    5000000,
                    40000,
                    200000
            ));


}