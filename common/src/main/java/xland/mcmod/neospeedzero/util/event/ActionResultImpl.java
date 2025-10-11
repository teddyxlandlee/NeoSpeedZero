package xland.mcmod.neospeedzero.util.event;

import org.jetbrains.annotations.Nullable;

enum ActionResultImpl implements ActionResult {
    INTERRUPT_SUCCESS(Boolean.TRUE),
    INTERRUPT_FAILURE(Boolean.FALSE),
    PASS(null),
    ;

    @Override
    public @Nullable Boolean asTriState() {
        return asTriState;
    }

    private final Boolean asTriState;

    ActionResultImpl(Boolean asTriState) {
        this.asTriState = asTriState;
    }
}
