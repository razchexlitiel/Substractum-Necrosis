package razchexlitiel.cim.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.block.basic.ModBlocks;
import razchexlitiel.cim.item.ModItems;

public class ModLangProvider extends LanguageProvider {

    protected final String locale;

    public ModLangProvider(PackOutput output, String locale) {
        super(output, CrustalIncursionMod.MOD_ID, locale);
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
        add("tooltip.cim.detminer.line1", "Mines blocks in explosion radius");
        add("tooltip.cim.detminer.line2", "Completely harmless to entities");
    }

    private void addRussian() {
        // Блоки и предметы
        add(ModBlocks.DET_MINER.get(), "Шахтёрский заряд");
        add(ModItems.RANGE_DETONATOR.get(), "Детонатор дальнего действия");

        // Вкладка креатива
        add("creativetab.snm_weapons_tab", "Substractum: Арсенал");

        // Тултипы
        add("tooltip.cim.detminer.line1", "Добывает блоки в радиусе взрыва");
        add("tooltip.cim.detminer.line2", "Не наносит урон при подрыве");
    }
}