package xland.mcmod.neospeedzero.record.manager;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface NeoSpeedServer {
    RecordManager ns0$recordManager();

    static NeoSpeedServer of(MinecraftServer server) {
        return (NeoSpeedServer) server;
    }
}
