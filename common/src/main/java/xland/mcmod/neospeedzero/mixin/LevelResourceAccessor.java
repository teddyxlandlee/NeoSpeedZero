package xland.mcmod.neospeedzero.mixin;

import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelResource.class)
public interface LevelResourceAccessor {
    @Invoker("<init>")
    static LevelResource ns0$create(String x) {
        throw new AssertionError("Mixin " + x);
    }
}
