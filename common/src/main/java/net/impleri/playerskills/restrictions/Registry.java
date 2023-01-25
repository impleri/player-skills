package net.impleri.playerskills.restrictions;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Registry<T extends AbstractRestriction<?>> {
    private final Map<ResourceLocation, List<T>> registry = new HashMap<>();

    public List<T> entries() {
        return registry.values().stream().flatMap(Collection::stream).toList();
    }

    public void clear() {
        registry.clear();
    }

    public List<T> find(ResourceLocation name) {
        @Nullable List<T> restrictions = registry.get(name);

        return (restrictions != null) ? restrictions.stream().toList() : new ArrayList<>();
    }

    public void add(ResourceLocation name, T restriction) {
        List<T> restrictions = find(name);
        restrictions.add(restriction);

        registry.put(name, restrictions);
    }
}
