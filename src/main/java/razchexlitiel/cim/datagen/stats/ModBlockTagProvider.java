package razchexlitiel.cim.datagen.stats;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.block.basic.ModBlocks;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CrustalIncursionMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // --- ДЕФОЛТ: Все блоки мода ломаются железной киркой ---
        var pickaxeTag = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTag = tag(BlockTags.NEEDS_IRON_TOOL);

        ModBlocks.BLOCKS.getEntries().forEach(block -> {
            // Если ты НЕ добавил блок в исключения ниже, он попадает сюда
            pickaxeTag.add(block.get());
            ironTag.add(block.get());
        });

        // --- ИСКЛЮЧЕНИЯ (Например, топор или лопата) ---
        // tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.WASTE_LOG.get());
        // tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.WASTE_DIRT.get());
    }
}