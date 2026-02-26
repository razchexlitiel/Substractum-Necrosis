package razchexlitiel.cim.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import razchexlitiel.cim.block.entity.ModBlockEntities;
import razchexlitiel.cim.client.gecko.block.rotation.MotorElectroRenderer;
import razchexlitiel.cim.client.gecko.block.rotation.ShaftIronRenderer;
import razchexlitiel.cim.client.gecko.block.rotation.WindGenFlugerRenderer;
import razchexlitiel.cim.client.gecko.entity.bullets.TurretBulletRenderer;
import razchexlitiel.cim.client.gecko.entity.mobs.DepthWormRenderer;
import razchexlitiel.cim.client.loader.ProceduralWireLoader;
import razchexlitiel.cim.client.overlay.gui.GUIMotorElectro;
import razchexlitiel.cim.client.overlay.hud.OverlayAmmoHud;
import razchexlitiel.cim.client.overlay.gui.GUIMachineBattery;
import razchexlitiel.cim.client.renderer.ClientRenderHandler;
import razchexlitiel.cim.config.ModConfigKeybindHandler;
import razchexlitiel.cim.entity.ModEntities;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.menu.ModMenuTypes;

@Mod.EventBusSubscriber(modid = CrustalIncursionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {

        MinecraftForge.EVENT_BUS.register(ClientRenderHandler.class);


        MenuScreens.register(ModMenuTypes.MACHINE_BATTERY_MENU.get(), GUIMachineBattery::new);
        MenuScreens.register(ModMenuTypes.MOTOR_ELECTRO_MENU.get(), GUIMotorElectro::new);
        BlockEntityRenderers.register(ModBlockEntities.MOTOR_ELECTRO_BE.get(), MotorElectroRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.SHAFT_IRON_BE.get(), ShaftIronRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.WIND_GEN_FLUGER_BE.get(), WindGenFlugerRenderer::new);
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {

        EntityRenderers.register(ModEntities.TURRET_BULLET.get(), TurretBulletRenderer::new);
        EntityRenderers.register(ModEntities.DEPTH_WORM.get(), DepthWormRenderer::new);

    }
    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterGeometryLoaders event) {
        event.register("procedural_wire", new ProceduralWireLoader());
    }
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.RELOAD_KEY);
        event.register(ModKeyBindings.UNLOAD_KEY);
        ModConfigKeybindHandler.onRegisterKeyMappings(event);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {

    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "ammo_hud", OverlayAmmoHud.HUD_AMMO);
    }

}