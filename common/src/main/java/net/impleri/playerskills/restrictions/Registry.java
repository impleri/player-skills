package net.impleri.playerskills.restrictions;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public abstract class Registry<T extends AbstractRestriction<?>> {
    protected final ResourceLocation REGISTRY_KEY;

    protected final ResourceKey<net.minecraft.core.Registry<List<T>>> REGISTRY_RESOURCE;

    protected final MappedRegistry<List<T>> REGISTRY;

    public Registry(String modId) {
        REGISTRY_KEY = new ResourceLocation(modId, "restrictions");
        REGISTRY_RESOURCE = ResourceKey.createRegistryKey(REGISTRY_KEY);
        REGISTRY = new MappedRegistry<>(REGISTRY_RESOURCE, Lifecycle.stable(), null);
    }

    public List<T> entries() {
        return REGISTRY.stream().flatMap(Collection::stream).toList();
    }

    public List<T> find(ResourceLocation name) {
        @Nullable List<T> restrictions = REGISTRY.get(name);

        return (restrictions != null) ? restrictions.stream().toList() : new ArrayList<>();
    }

    public void add(ResourceLocation name, T restriction) {
        List<T> restrictions = find(name);
        var newRestrictions = Stream.concat(restrictions.stream(), Stream.of(restriction)).toList();

        REGISTRY.registerOrOverride(null, ResourceKey.create(REGISTRY_RESOURCE, name), newRestrictions, Lifecycle.stable());
    }
}
