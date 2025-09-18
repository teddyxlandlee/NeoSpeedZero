package xland.mcmod.neospeedzero.command;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xland.mcmod.neospeedzero.record.manager.RecordManager;

import java.util.UUID;

public sealed interface RecordReference permits
        RecordLookup, RecordLookup.Definite, RecordLookup.Failure, RecordLookup.PlayerBased {
    Either<UUID, Component> parse(RecordManager manager);

    static RecordReference of(String s) {
        return RecordLookup.of(s);
    }

    static RecordReference ofPlayer(ServerPlayer targetPlayer) {
        return RecordLookup.ofPlayer(targetPlayer);
    }
}
