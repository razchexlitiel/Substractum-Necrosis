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

    public static final RegistryObject<CreativeModeTab> SUBSTRACTUM_TAB = CREATIVE_MODE_TABS.register("substractum_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.NECROTIC_FRAGMENT.get())) // Иконка вкладки
                    .title(Component.translatable("creativetab.substractum_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.NECROTIC_FRAGMENT.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
