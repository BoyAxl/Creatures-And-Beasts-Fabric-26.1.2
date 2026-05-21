package com.cgessinger.creaturesandbeasts.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CNBDeferredRegister<T> {
    private final String namespace;
    private final Registry<T> registry;
    private final List<Entry<? extends T>> entries = new ArrayList<>();

    private CNBDeferredRegister(String namespace, Registry<T> registry) {
        this.namespace = namespace;
        this.registry = registry;
    }

    public static <T> CNBDeferredRegister<T> create(String namespace, Registry<T> registry) {
        return new CNBDeferredRegister<>(namespace, registry);
    }

    public <E extends T> CNBRegistrySupplier<E> register(String path, Supplier<E> factory) {
        CNBRegistrySupplier<E> supplier = new CNBRegistrySupplier<>();
        this.entries.add(new Entry<>(path, factory, supplier));
        return supplier;
    }

    public void register() {
        for (Entry<? extends T> entry : this.entries) {
            this.register(entry);
        }
    }

    public List<CNBRegistrySupplier<? extends T>> entries() {
        List<CNBRegistrySupplier<? extends T>> suppliers = new ArrayList<>();
        for (Entry<? extends T> entry : this.entries) {
            suppliers.add(entry.supplier());
        }
        return suppliers;
    }

    private <E extends T> void register(Entry<E> entry) {
        E value = entry.factory().get();
        Registry.register(this.registry, Identifier.fromNamespaceAndPath(this.namespace, entry.path()), value);
        entry.supplier().set(value);
    }

    private record Entry<T>(String path, Supplier<T> factory, CNBRegistrySupplier<T> supplier) {
    }
}
