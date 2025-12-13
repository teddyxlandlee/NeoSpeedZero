package xland.mcmod.neospeedzero.record.manager;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;
import xland.mcmod.neospeedzero.NeoSpeedZero;
import xland.mcmod.neospeedzero.mixin.LevelResourceAccessor;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;
import xland.mcmod.neospeedzero.util.StreamIoUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RecordManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, UUID> playerToRecordMap = new LinkedHashMap<>();
    private final Map<UUID, SpeedrunRecordHolder> recordMap = new LinkedHashMap<>();

    private final MinecraftServer server;

    public RecordManager(MinecraftServer server) {
        this.server = server;
    }

    private void bindHost(SpeedrunRecord record, ServerPlayer host) {
        recordMap.put(record.recordId(), new SpeedrunRecordHolder(
                record, new SpeedrunPlayerInfo(host.getUUID())
        ));
    }

    private void registerTo(ServerPlayer player, SpeedrunRecord record) {
        playerToRecordMap.put(player.getUUID(), record.recordId());
    }

    public @Nullable Component startHosting(SpeedrunRecord record, ServerPlayer host) {
        if (playerToRecordMap.containsKey(host.getUUID())) {
            return Component.translatable(
                    "message.neospeedzero.record.start.started",
                    host.getDisplayName(),
                    record.snapshot()
            );
        }
        bindHost(record, host);
        registerTo(host, record);
        return null;
    }

    public @Nullable Component joinRecord(UUID recordId, ServerPlayer subPlayer) {
        UUID prevRecordId = findRecordIdByPlayer(subPlayer);
        if (prevRecordId != null) {
            SpeedrunRecordHolder holder = findRecordByUuid(prevRecordId);
            return Component.translatable(
                    "message.neospeedzero.record.start.started",
                    subPlayer.getDisplayName(),
                    holder == null ? "<???>" : holder.record().snapshot()
            );
        }

        var holder = findRecordByUuid(recordId);
        if (holder == null) return Component.translatable("message.neospeedzero.record.not_found", recordId.toString());
        holder.info().participants().add(subPlayer.getUUID());
        registerTo(subPlayer, holder.record());

        return null;
    }

    public @Nullable UUID findRecordIdByPlayer(ServerPlayer player) {
        return findRecordIdByPlayer(player.getUUID());
    }

    public @Nullable UUID findRecordIdByPlayer(UUID playerId) {
        return playerToRecordMap.get(playerId);
    }

    public @Nullable SpeedrunRecordHolder findRecordByPlayer(ServerPlayer player) {
        return findRecordByPlayer(player.getUUID());
    }

    public @Nullable SpeedrunRecordHolder findRecordByPlayer(UUID playerId) {
        @Nullable UUID recordId = findRecordIdByPlayer(playerId);
        return recordId == null ? null : recordMap.get(recordId);
    }

    public @Nullable SpeedrunRecordHolder findRecordByUuid(UUID recordId) {
        return recordMap.get(recordId);
    }

    public @UnmodifiableView Set<UUID> getAllRecordIds() {
        return Collections.unmodifiableSet(recordMap.keySet());
    }

    public PlayerRole getPlayerRole(@Nullable UUID recordId, ServerPlayer player) {
        var holder = recordId == null ? findRecordByPlayer(player) : findRecordByUuid(recordId);
        if (holder == null) return PlayerRole.NONE;
        return holder.info().getPlayerRole(player.getUUID());
    }

    public @Nullable SpeedrunRecordHolder endRecord(UUID recordId) {
        var holder = findRecordByUuid(recordId);
        if (holder == null) return null;
        endRecord(holder);
        return holder;
    }

    public void endRecord(SpeedrunRecordHolder holder) {
        // Remove all players
        playerToRecordMap.keySet().removeAll(holder.info().getAllPlayers());

        // Remove the record, and move it to history
        recordMap.remove(holder.record().recordId());
        saveHistoricalRecordAsync(holder);
    }

    public @Nullable SpeedrunRecordHolder leaveRecord(ServerPlayer player) {
        return leaveRecord(player.getUUID());
    }

    public @Nullable SpeedrunRecordHolder leaveRecord(UUID playerId) {
        var holder = findRecordByPlayer(playerId);
        if (holder == null) return null;
        return switch (holder.info().getPlayerRole(playerId)) {
            // The host quits, thus the record dies
            case HOST -> {
                endRecord(holder);
                yield holder;
            }
            // sub-player quits
            case PARTICIPANT -> {
                if (holder.info().participants().remove(playerId)) {
                    playerToRecordMap.remove(playerId);
                    yield holder;
                } else {
                    yield null;
                }
            }
            default -> null;
        };
    }

    @Contract(pure = true)
    @VisibleForTesting
    public Snapshot snapshot(boolean onServerSave) {
        return new Snapshot(
                new SaveInfo(onServerSave),
                recordMap.entrySet()
                        .stream()
                        .collect(Collectors.toUnmodifiableMap(
                                Map.Entry::getKey, e -> e.getValue().info()
                        )),
                playerToRecordMap,
                recordMap.values().stream().map(SpeedrunRecordHolder::record).collect(Collectors.toUnmodifiableSet())
        );
    }

    public record Snapshot(
            SaveInfo saveInfo,
            @Unmodifiable Map<UUID, SpeedrunPlayerInfo> infos,
            @Unmodifiable Map<UUID, UUID> playerToRecordIdMap,
            @Unmodifiable Set<SpeedrunRecord> records
    ) {
        private void loadTo(RecordManager manager) {
            Map<UUID, SpeedrunRecordHolder> recordMap = new LinkedHashMap<>();
            for (SpeedrunRecord record : records()) {
                UUID recordId = record.recordId();
                SpeedrunPlayerInfo info = infos().get(recordId);
                if (info == null) throw new NoSuchElementException("info is absent");

                recordMap.put(recordId, new SpeedrunRecordHolder(record, info));
            }

            manager.recordMap.clear();
            manager.recordMap.putAll(recordMap);
            manager.playerToRecordMap.clear();
            manager.playerToRecordMap.putAll(this.playerToRecordIdMap());
        }

        public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull Snapshot> STREAM_CODEC = StreamCodec.composite(
                SaveInfo.STREAM_CODEC, Snapshot::saveInfo,
                StreamIoUtil.ofMap(UUIDUtil.STREAM_CODEC, SpeedrunPlayerInfo.STREAM_CODEC), Snapshot::infos,
                StreamIoUtil.ofMap(UUIDUtil.STREAM_CODEC, UUIDUtil.STREAM_CODEC), Snapshot::playerToRecordIdMap,
                StreamIoUtil.ofJsonArrayString(SpeedrunRecord.CODEC, LinkedHashSet::new), Snapshot::records,
                Snapshot::new
        );
    }

//    private final ExecutorService historyExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Thread.Builder THREAD_BUILDER = Thread.ofVirtual().name("History-Record-Saver-", 1);

    private void saveHistoricalRecordAsync(SpeedrunRecordHolder holder) {
        THREAD_BUILDER.start(() -> {
            try {
                var ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
                CompoundTag compound = SpeedrunRecordHolder.CODEC.encodeStart(ops, holder)
                        .getOrThrow()
                        .asCompound()
                        .orElseThrow(() -> new IllegalStateException("Not a compound"));

                String recordIdString = holder.record().recordId().toString();
                Path path = this.serverDir()
                        .resolve("historical_records")
                        .resolve(recordIdString.substring(0, 2))
                        .resolve(recordIdString + ".dat");
                Files.createDirectories(path.getParent());

                NbtIo.writeCompressed(compound, path);
            } catch (Exception e) {
                LOGGER.error("Failed to save historical record {}", holder.record().recordId(), e);
            }
        });
    }


    @ApiStatus.Internal
    public void registerLegacyRecord(NeoSpeedPlayer player, SpeedrunRecord record) {
        startHosting(record, (ServerPlayer) player);
    }

    public void loadFromServer() {
        Path path = this.serverDir().resolve("PlayerRecords.dat");
        Snapshot snapshot;

        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                return;
            }
            byte[] buf;
            try (var input = new BufferedInputStream(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path))))) {
                buf = input.readAllBytes();
            }

            snapshot = Snapshot.STREAM_CODEC.decode(StreamIoUtil.byteBuf(buf, server.registryAccess()));
        } catch (Exception e) {
            LOGGER.error("Failed to load NeoSpeedZero player records", e);
            return;
        }
        snapshot.loadTo(this);
    }

    public void saveToServer() {
        Path path = this.serverDir().resolve("PlayerRecords.dat");
        Path backup = this.serverDir().resolve("PlayerRecords.dat_old");

        try {
            // onServerSave is currently unpredictable, so `false` it here
            RegistryFriendlyByteBuf byteBuf = StreamIoUtil.byteBuf(null, server.registryAccess());
            Snapshot.STREAM_CODEC.encode(byteBuf, snapshot(false));
            byte[] bytes = ByteBufUtil.getBytes(byteBuf);

            Files.createDirectories(path.getParent());
            Path tempFile = Files.createTempFile("PlayerRecords", "dat");
            try (var output = new BufferedOutputStream(new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(tempFile))))) {
                output.write(bytes);
            }
            net.minecraft.util.Util.safeReplaceFile(path, tempFile, backup);
        } catch (Exception e) {
            LOGGER.error("Failed to save NeoSpeed Zero player records", e);
        }
    }
    
    private static final LevelResource LEVEL_RESOURCE = LevelResourceAccessor.ns0$create(NeoSpeedZero.MOD_ID);
    
    private Path serverDir() {
        return this.server.getWorldPath(LEVEL_RESOURCE);
    }
}
