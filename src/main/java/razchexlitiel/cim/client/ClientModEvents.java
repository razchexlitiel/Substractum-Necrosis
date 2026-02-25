package razchexlitiel.cim.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import razchexlitiel.cim.client.gecko.bullets.TurretBulletRenderer;
import razchexlitiel.cim.client.gecko.mobs.DepthWormRenderer;
import razchexlitiel.cim.client.overlay.OverlayAmmoHud;
import razchexlitiel.cim.client.overlay.gui.GUIMachineBattery;
import razchexlitiel.cim.config.ModConfigKeybindHandler;
import razchexlitiel.cim.entity.ModEntities;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.menu.ModMenuTypes;

@Mod.EventBusSubscriber(modid = CrustalIncursionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        MenuScreens.register(ModMenuTypes.MACHINE_BATTERY_MENU.get(), GUIMachineBattery::new);

    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {

        EntityRenderers.register(ModEntities.TURRET_BULLET.get(), TurretBulletRenderer::new);
        EntityRenderers.register(ModEntities.DEPTH_WORM.get(), DepthWormRenderer::new);

    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.RELOAD_KEY);
        event.register(ModKeyBindings.UNLOAD_KEY);
        ModConfigKeybindHandler.onRegisterKeyMappings(event);
        CrustalIncursionMod.LOGGER.info("Registered key mappings.");
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {

    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "ammo_hud", OverlayAmmoHud.HUD_AMMO);
    }

}