package razchexlitiel.substractum.main;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
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
        ModItems.ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
}