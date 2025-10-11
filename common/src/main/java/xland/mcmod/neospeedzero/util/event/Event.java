package xland.mcmod.neospeedzero.util.event;

import java.util.function.Function;

@SuppressWarnings("unused")
public sealed interface Event<T, I> permits Events {
    static <T, I> Event<T, I> of(Function<? super Iterable<? extends T>, ? extends I> merger) {
        return new Events<>(merger);
    }

    I invoker();
    
    void register(T listener);

    void unregister(T listener);
    
    boolean isRegistered(T listener);
    
    void clearListeners();
}
