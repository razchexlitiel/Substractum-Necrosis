package razchexlitiel.cim.network;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import razchexlitiel.cim.main.CrustalIncursionMod;
import razchexlitiel.cim.network.packet.*;

public class ModPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CrustalIncursionMod.MOD_ID, "main_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;


        INSTANCE.registerMessage(id++, UpdateBatteryC2SPacket.class, UpdateBatteryC2SPacket::toBytes, UpdateBatteryC2SPacket::new, (msg, ctx) -> msg.handle(ctx));

        INSTANCE.registerMessage(id++,
                razchexlitiel.cim.network.packet.PacketSyncEnergy.class,
                razchexlitiel.cim.network.packet.PacketSyncEnergy::encode,
                razchexlitiel.cim.network.packet.PacketSyncEnergy::decode,
                razchexlitiel.cim.network.packet.PacketSyncEnergy::handle
        );

        INSTANCE.registerMessage(id++,
                PacketReloadGun.class,
                PacketReloadGun::toBytes,
                PacketReloadGun::new,
                PacketReloadGun::handle
        );

        // === ДОБАВЬТЕ ЭТОТ ПАКЕТ ДЛЯ СТРЕЛЬБЫ ===
        INSTANCE.registerMessage(id++,
                PacketShoot.class,
                PacketShoot::toBytes,
                PacketShoot::new,
                PacketShoot::handle
        );

        INSTANCE.registerMessage(id++,
                PacketUnloadGun.class,
                PacketUnloadGun::toBytes,
                PacketUnloadGun::new,
                PacketUnloadGun::handle
        );

        INSTANCE.registerMessage(id++,
                PacketToggleMotor.class,
                PacketToggleMotor::encode,
                PacketToggleMotor::new,
                PacketToggleMotor::handle
        );

        INSTANCE.registerMessage(id++,
                PacketToggleMotorMode.class,
                PacketToggleMotorMode::encode,
                PacketToggleMotorMode::new,
                PacketToggleMotorMode::handle
        );

    }
}
