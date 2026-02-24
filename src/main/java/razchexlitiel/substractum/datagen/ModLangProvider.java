package razchexlitiel.substractum.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import razchexlitiel.substractum.main.SubstractumMod;
import razchexlitiel.substractum.block.basic.ModBlocks;
import razchexlitiel.substractum.item.ModItems;

public class ModLangProvider extends LanguageProvider {

    protected final String locale;

    public ModLangProvider(PackOutput output, String locale) {
        super(output, SubstractumMod.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("ru_ru")) {
            addRussian();
        } else {
            addEnglish();
        }
    }

    private void addEnglish() {
        // Блоки и предметы
        add(ModBlocks.DET_MINER.get(), "Mining Charge");
        add(ModItems.RANGE_DETONATOR.get(), "Long-Range Detonator");

        // Вкладка креатива (SNM_WEAPONS_TAB)
        add("creativetab.snm_weapons_tab", "Substractum: Arsenal");

        // Тултипы
        add("tooltip.substractum.detminer.line1", "Mines blocks in explosion radius");
        add("tooltip.substractum.detminer.line2", "Completely harmless to entities");
    }

    private void addRussian() {
        // Блоки и предметы
        add(ModBlocks.DET_MINER.get(), "Шахтёрский заряд");
        add(ModItems.RANGE_DETONATOR.get(), "Детонатор дальнего действия");

        // Вкладка креатива
        add("creativetab.snm_weapons_tab", "Substractum: Арсенал");

        // Тултипы
        add("tooltip.substractum.detminer.line1", "Добывает блоки в радиусе взрыва");
        add("tooltip.substractum.detminer.line2", "Не наносит урон при подрыве");
    }
}