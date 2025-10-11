package xland.mcmod.neospeedzero.util.event;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Function;

final class Events<T, I> implements Event<T, I> {
    private final Function<? super Iterable<? extends T>, ? extends I> merger;
    private final LinkedHashSet<T> registry = new LinkedHashSet<>();

    Events(Function<? super Iterable<? extends T>, ? extends I> merger) {
        Objects.requireNonNull(merger, "merger cannot be null");
        this.merger = merger;
    }

    @Override
    public I invoker() {
        return this.merger.apply(Collections.unmodifiableCollection(registry));
    }

    @Override
    public void register(T listener) {
        registry.addLast(listener);
    }

    @Override
    public void unregister(T listener) {
        registry.remove(listener);
    }

    @Override
    public void clearListeners() {
        registry.clear();
    }

    @Override
    public boolean isRegistered(T listener) {
        return registry.contains(listener);
    }
}
