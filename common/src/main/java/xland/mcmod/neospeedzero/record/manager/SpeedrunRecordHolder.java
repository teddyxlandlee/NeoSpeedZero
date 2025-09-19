package xland.mcmod.neospeedzero.record.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xland.mcmod.neospeedzero.record.SpeedrunRecord;

public record SpeedrunRecordHolder(SpeedrunRecord record, SpeedrunPlayerInfo info) {
    public static final Codec<SpeedrunRecordHolder> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    SpeedrunRecord.CODEC.fieldOf("record").forGetter(SpeedrunRecordHolder::record),
                    SpeedrunPlayerInfo.MAP_CODEC.forGetter(SpeedrunRecordHolder::info)
            ).apply(instance, SpeedrunRecordHolder::new)
    );
}
