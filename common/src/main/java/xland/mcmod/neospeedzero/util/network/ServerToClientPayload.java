package xland.mcmod.neospeedzero.util.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ServerToClientPayload extends CustomPacketPayload {
    @Environment(EnvType.CLIENT)
    void onClientReceive();
}
