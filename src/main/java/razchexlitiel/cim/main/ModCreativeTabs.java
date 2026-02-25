package razchexlitiel.cim.main;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.cim.item.ModItems;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrustalIncursionMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CIM_WEAPONS_TAB = CREATIVE_MODE_TABS.register("cim_weapons_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RANGE_DETONATOR.get())) // Иконка вкладки
                    .title(Component.translatable("creativetab.cim_weapons_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RANGE_DETONATOR.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> CIM_NATURE_TAB = CREATIVE_MODE_TABS.register("cim_nature_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DEPTH_WORM_SPAWN_EGG.get())) // Иконка вкладки
                    .title(Component.translatable("creativetab.cim_nature_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DEPTH_WORM_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
