package razchexlitiel.substractum.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import razchexlitiel.substractum.main.SubstractumMod;

@Mod.EventBusSubscriber(modid = SubstractumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        // Сюда будем вписывать рендереры для мобов из Smogline
        // Пример: event.registerEntityRenderer(ModEntities.MY_MOB.get(), MyMobRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Если в Smogline была кастомная броня (не GeckoLib), слои регистрируются тут
    }
}