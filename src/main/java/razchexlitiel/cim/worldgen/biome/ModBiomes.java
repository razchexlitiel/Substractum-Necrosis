package razchexlitiel.cim.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
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
import razchexlitiel.cim.worldgen.ModPlacedFeatures;

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

        BiomeDefaultFeatures.addDefaultCarversAndLakes(biomeBuilder); // Пещеры и каньоны
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeBuilder); // Аметисты
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeBuilder); // Спавнеры
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder); // Диорит, андезит, гранит
        BiomeDefaultFeatures.addDefaultOres(biomeBuilder); // ВСЕ РУДЫ (уголь, железо, алмазы и т.д.)
        BiomeDefaultFeatures.addDefaultSoftDisks(biomeBuilder); // Глина и песок в воде

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.GIANT_SEQUOIA_PLACED_KEY);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)  // Дождь разрешен
                .temperature(0.7F)       // Комфортная, слегка прохладная температура (как в горах)
                .downfall(0.8F)          // Высокая влажность (идеально для секвой)

                // Визуальные эффекты: Делаем красиво
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(3579307)
                        .waterFogColor(336947)
                        .skyColor(0x78A7FF)           // Ясное, чистое голубое небо
                        .fogColor(0xC0D8FF)           // Легкий, свежий утренний туман
                        .grassColorOverride(0x3E8E38) // Насыщенный, сочный зеленый цвет травы
                        .foliageColorOverride(0x334C15)
                        // Добавляем спокойную музыку из тайги для атмосферы
                        .backgroundMusic(Musics.createGameMusic(net.minecraft.sounds.SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA))
                        .build())
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }
}
