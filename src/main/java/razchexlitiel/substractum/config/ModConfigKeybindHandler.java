package razchexlitiel.substractum.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import razchexlitiel.substractum.client.ModKeyBindings;
import razchexlitiel.substractum.main.SubstractumMod;
import razchexlitiel.substractum.network.ModPacketHandler;
import razchexlitiel.substractum.network.packet.PacketUnloadGun;

@Mod.EventBusSubscriber(modid = SubstractumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModConfigKeybindHandler {
    // Поменял категорию на substractum
    public static final String CATEGORY = "key.categories.substractum";

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.substractum.open_config",
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_0,
            CATEGORY
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
        // Не забудь зарегистрировать и UNLOAD_KEY, если он лежит в ModKeyBindings
        event.register(ModKeyBindings.UNLOAD_KEY);
    }

    // Обработка нажатий через InputEvent (более надежно для оружия)
    @Mod.EventBusSubscriber(modid = SubstractumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class GlobalKeyHandler {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                // Если нажата кнопка конфига
                while (OPEN_CONFIG.consumeClick()) {
                    Minecraft mc = Minecraft.getInstance();
                    // Здесь можно вывести сообщение или открыть твой кастомный GUI
                    if (mc.player != null) {
                        mc.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Config screen is disabled (Cloth Config removed)"));
                    }
                }

                // Обработка разрядки
                while (ModKeyBindings.UNLOAD_KEY.consumeClick()) {
                    ModPacketHandler.INSTANCE.sendToServer(new PacketUnloadGun());
                }
            }
        }
    }
}