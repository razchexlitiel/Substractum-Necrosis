package razchexlitiel.substractum.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.substractum.main.SubstractumMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SubstractumMod.MOD_ID);

    // Пример твоего первого предмета
    public static final RegistryObject<Item> NECROTIC_FRAGMENT = ITEMS.register("necrotic_fragment",
            () -> new Item(new Item.Properties()));
}