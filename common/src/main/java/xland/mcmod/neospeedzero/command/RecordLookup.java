package xland.mcmod.neospeedzero.command;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
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
            case 0 -> Either.right(Component.translatable("message.neospeedzero.record.not_found", toString()));
            case 1 -> Either.left(uuids.getFirst());
            default -> Either.right(Component.translatable("message.neospeedzero.record.ambiguous", toString()));
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
            if (!s.matches("^#[0-9A-Fa-f]{4}$")) {
                return new Failure(Component.translatable("message.neospeedzero.record.invalid_ref", s));
            }

            s = s.substring(1).toLowerCase(Locale.ROOT);
            return new RecordLookup(s);
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
                    : Either.right(Component.translatable(
                            "message.neospeedzero.record.stop.absent", player.getDisplayName()
                    ));
        }
    }
}
