package xland.mcmod.neospeedzero.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xland.mcmod.neospeedzero.record.manager.NeoSpeedServer;
import xland.mcmod.neospeedzero.record.manager.RecordManager;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin implements NeoSpeedServer {
    @Unique
    private final RecordManager ns0$recordManager = new RecordManager();

    @Override
    public RecordManager ns0$recordManager() {
        return ns0$recordManager;
    }
}
