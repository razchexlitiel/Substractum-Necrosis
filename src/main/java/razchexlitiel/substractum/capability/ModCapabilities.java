package razchexlitiel.substractum.capability;


import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import razchexlitiel.substractum.api.hive.HiveNetworkManager;

public class ModCapabilities {
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {

        event.register(HiveNetworkManager.class);
    }
}