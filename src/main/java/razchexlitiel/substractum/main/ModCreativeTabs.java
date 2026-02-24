package razchexlitiel.substractum.main;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.substractum.item.ModItems;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SubstractumMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SNM_WEAPONS_TAB = CREATIVE_MODE_TABS.register("snm_weapons_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RANGE_DETONATOR.get())) // Иконка вкладки
                    .title(Component.translatable("creativetab.snm_weapons_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RANGE_DETONATOR.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> SNM_NATURE_TAB = CREATIVE_MODE_TABS.register("snm_nature_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DEPTH_WORM_SPAWN_EGG.get())) // Иконка вкладки
                    .title(Component.translatable("creativetab.snm_nature_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DEPTH_WORM_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
