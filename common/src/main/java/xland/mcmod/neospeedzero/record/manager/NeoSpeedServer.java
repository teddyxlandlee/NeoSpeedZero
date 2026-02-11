package xland.mcmod.neospeedzero.record.manager;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import xland.mcmod.neospeedzero.util.access.PlatformAccess;

@ApiStatus.NonExtendable
public interface NeoSpeedServer {
    RecordManager ns0$recordManager();

    static RecordManager getRecordManager(MinecraftServer server) {
        return PlatformAccess.wrap(server).ns0$recordManager();
    }
}
