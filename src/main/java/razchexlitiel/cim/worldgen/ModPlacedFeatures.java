package razchexlitiel.cim.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import razchexlitiel.cim.main.CrustalIncursionMod;

import java.util.List;

// Размещать в: src/main/java/razchexlitiel/cim/worldgen/ModPlacedFeatures.java
public class ModPlacedFeatures {

    // 1. Уникальный ключ для размещенной структуры
    public static final ResourceKey<PlacedFeature> GIANT_SEQUOIA_PLACED_KEY = registerKey("giant_sequoia_placed");

    // 2. Сборка (DataGen)
    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        // Получаем доступ к реестру уже собранных "чертежей"
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Регистрируем размещение нашей секвойи
        register(context, GIANT_SEQUOIA_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.GIANT_SEQUOIA_KEY),
                // PlacementUtils.treePlacement - это удобный ванильный метод, который сам добавляет:
                // 1) Поиск поверхности, 2) Проверку, что под деревом земля/трава, 3) Проверку биома
                List.of(
                        RarityFilter.onAverageOnceEvery(5),                     // 1. Редкость (1 раз на 5 чанков)
                        InSquarePlacement.spread(),                             // 2. Случайный сдвиг по X и Z внутри чанка
                        SurfaceWaterDepthFilter.forMaxDepth(0),                 // 3. Запрещаем спавн в воде
                        PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,                   // 4. Ищем самый верхний твердый блок
                        PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING), // 5. Проверка: может ли тут стоять саженец (чтобы не спавнилось на камне/льду)
                        BiomeFilter.biome()                                     // 6. Строгое правило: только в нашем биоме
                ));
    }

    // --- Вспомогательные методы ---
    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(CrustalIncursionMod.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
