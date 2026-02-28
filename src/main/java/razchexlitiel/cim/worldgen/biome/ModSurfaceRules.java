package razchexlitiel.cim.worldgen.biome; // Поменяй на свой пакет

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import razchexlitiel.cim.block.basic.ModBlocks;
import razchexlitiel.cim.worldgen.biome.ModBiomes; // Твой класс с ключом биома

public class ModSurfaceRules {

    // Вспомогательный метод для удобства
    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static SurfaceRules.RuleSource makeRules() {
        // Проверяем, находимся ли мы в нашем биоме
        SurfaceRules.ConditionSource isSequoiaBiome = SurfaceRules.isBiome(ModBiomes.SEQUOIA_GROVE);

        // Правило для блоков: Подзол на самом верху, Земля под ним
        SurfaceRules.RuleSource sequoiaSurface = SurfaceRules.sequence(
                // Проверяем, что мы находимся выше пещер (на настоящей поверхности)
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, makeStateRule(ModBlocks.SEQUOIA_BIOME_MOSS.get())),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, makeStateRule(Blocks.DIRT))
                        )
                )
        );

        // Возвращаем итоговое правило: ЕСЛИ биом наш -> ПРИМЕНИТЬ правило
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(isSequoiaBiome, sequoiaSurface)
        );
    }
}
