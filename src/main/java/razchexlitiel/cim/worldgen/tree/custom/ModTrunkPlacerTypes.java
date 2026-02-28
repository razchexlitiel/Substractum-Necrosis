package razchexlitiel.cim.worldgen.tree.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import razchexlitiel.cim.main.CrustalIncursionMod; // Импорт твоего главного класса

// Размещать в: src/main/java/razchexlitiel/cim/worldgen/tree/custom/ModTrunkPlacerTypes.java
public class ModTrunkPlacerTypes {

    // 1. Создаем реестр для типов стволов
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPES =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, CrustalIncursionMod.MOD_ID);

    // 2. Регистрируем наш кастомный тип ствола
    // Мы передаем сюда CODEC, который мы заботливо подготовили в классе GiantSequoiaTrunkPlacer
    public static final RegistryObject<TrunkPlacerType<razchexlitiel.cim.worldgen.tree.custom.GiantSequoiaTrunkPlacer>> GIANT_SEQUOIA_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("giant_sequoia_trunk_placer",
                    () -> new TrunkPlacerType<>(razchexlitiel.cim.worldgen.tree.custom.GiantSequoiaTrunkPlacer.CODEC));

    // 3. Метод для подключения к главной шине
    public static void register(IEventBus eventBus) {
        TRUNK_PLACER_TYPES.register(eventBus);
    }
}