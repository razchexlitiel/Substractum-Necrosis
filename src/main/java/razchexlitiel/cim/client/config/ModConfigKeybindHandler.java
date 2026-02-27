package razchexlitiel.cim.client.config;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import razchexlitiel.cim.network.ModPacketHandler;
import razchexlitiel.cim.network.packet.guns.PacketUnloadGun;

// УДАЛИЛИ АННОТАЦИЮ @Mod.EventBusSubscriber
public class ModConfigKeybindHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (ModKeyBindings.UNLOAD_KEY.consumeClick()) {
            ModPacketHandler.INSTANCE.sendToServer(new PacketUnloadGun());
        }
    }
}
