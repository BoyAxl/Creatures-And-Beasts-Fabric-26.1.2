package com.cgessinger.creaturesandbeasts.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class CNBRegistrySupplier<T> implements Supplier<T> {
    private T value;

    @Override
    public T get() {
        return Objects.requireNonNull(this.value, "Registry entry has not been registered yet");
    }

    void set(T value) {
        this.value = value;
    }
}
