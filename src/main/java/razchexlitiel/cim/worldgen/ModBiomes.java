package razchexlitiel.cim.worldgen;

import net.minecraft.world.level.levelgen.GenerationStep;
import razchexlitiel.cim.main.CrustalIncursionMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;

// Размещать в: src/main/java/com/smogline/worldgen/biome/ModBiomes.java
public class ModBiomes {

    // 1. Уникальный ключ для нашей красивой рощи
    public static final ResourceKey<Biome> SEQUOIA_GROVE = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(CrustalIncursionMod.MOD_ID, "sequoia_grove"));

    // 2. Метод Bootstrap для DataGen
    public static void bootstrap(BootstapContext<Biome> context) {
        context.register(SEQUOIA_GROVE, sequoiaGroveBiome(context));
    }

    // 3. Фабрика биома: Настраиваем красоту
    private static Biome sequoiaGroveBiome(BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();

        // Позже сюда мы добавим спавн нашей гигантской секвойи
        BiomeGenerationSettings.Builder biomeBuilder =
                new BiomeGenerationSettings.Builder(context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.GIANT_SEQUOIA_PLACED_KEY);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)  // Дождь разрешен
                .temperature(0.7F)       // Комфортная, слегка прохладная температура (как в горах)
                .downfall(0.8F)          // Высокая влажность (идеально для секвой)

                // Визуальные эффекты: Делаем красиво
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3F76E4)         // Классическая чистая вода
                        .waterFogColor(0x050533)      // Глубокий цвет под водой
                        .skyColor(0x78A7FF)           // Ясное, чистое голубое небо
                        .fogColor(0xC0D8FF)           // Легкий, свежий утренний туман
                        .grassColorOverride(0x3E8E38) // Насыщенный, сочный зеленый цвет травы
                        .foliageColorOverride(0x3E8E38)// Такой же цвет для ванильной листвы
                        // Добавляем спокойную музыку из тайги для атмосферы
                        .backgroundMusic(Musics.createGameMusic(net.minecraft.sounds.SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA))
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }
}
