package xland.mcmod.neospeedzero.command;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import xland.mcmod.neospeedzero.NeoSpeedTranslations;
import xland.mcmod.neospeedzero.record.manager.RecordManager;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

record RecordLookup(String prefix) implements RecordReference {
    @Override
    public Either<UUID, Component> parse(RecordManager manager) {
        List<UUID> uuids = manager.getAllRecordIds().stream()
                .filter(uuid -> uuid.toString().startsWith(prefix()))
                .toList();
        return switch (uuids.size()) {
            case 0 -> Either.right(NeoSpeedTranslations.RECORD_NOT_FOUND.createWithArgs(toString()));
            case 1 -> Either.left(uuids.getFirst());
            default -> Either.right(NeoSpeedTranslations.RECORD_LOOKUP_AMBIGUOUS.createWithArgs(toString()));
        };
    }

    @Override
    public @NotNull String toString() {
        return "#" + prefix();
    }

    static RecordReference of(String s) {
        try {
            return new Definite(UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            // not a definite reference
            if (!s.matches("^[0-9A-Fa-f]{4}$")) {
                return new Failure(NeoSpeedTranslations.RECORD_LOOKUP_INVALID_REFERENCE.createWithArgs(s));
            }

            return new RecordLookup(s.toLowerCase(Locale.ROOT));
        }
    }

    static RecordReference ofPlayer(ServerPlayer targetPlayer) {
        return new PlayerBased(targetPlayer);
    }

    record Definite(UUID uuid) implements RecordReference {
        @Override
        public Either<UUID, Component> parse(RecordManager manager) {
            // do not check whether it exists, so far
            // this check will be handled by RecordManager, in case of "ghost holders"
            return Either.left(uuid());
        }
    }

    record Failure(Component message) implements RecordReference {
        @Override
        public Either<UUID, Component> parse(RecordManager manager) {
            return Either.right(message());
        }
    }

    record PlayerBased(ServerPlayer player) implements RecordReference {
        @Override
        public Either<UUID, Component> parse(RecordManager manager) {
            UUID uuid = manager.findRecordIdByPlayer(player());
            return uuid != null
                    ? Either.left(uuid)
                    : Either.right(NeoSpeedTranslations.PLAYER_NOT_SPEEDRUNNING.createWithArgs(player.getDisplayName()));
        }
    }
}
