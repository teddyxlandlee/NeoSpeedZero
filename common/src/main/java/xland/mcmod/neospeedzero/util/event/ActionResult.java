package xland.mcmod.neospeedzero.util.event;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface ActionResult permits ActionResultImpl {
    static ActionResult interruptSuccess() {
        return ActionResultImpl.INTERRUPT_SUCCESS;
    }

    static ActionResult interruptFailure() {
        return ActionResultImpl.INTERRUPT_FAILURE;
    }

    static ActionResult pass() {
        return ActionResultImpl.PASS;
    }

    static ActionResult of(@Nullable Boolean triState) {
        if (triState == null) return pass();
        return triState ? interruptSuccess() : interruptFailure();
    }

    @Nullable Boolean asTriState();

    default boolean interrupts() {
        return asTriState() != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean getResult(boolean defaultValue) {
        Boolean nullable = asTriState();
        return nullable == null ? defaultValue : nullable;
    }
}
