package net.impleri.playerskills.integration.kubejs.api;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.utils.RegistrationType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract public class AbstractRegistrationEventJS<T, R extends AbstractRestriction<T>, B extends AbstractRestrictionBuilder<R>> extends ServerEventJS {
    private final ResourceKey<Registry<T>> registryName;

    private final Registry<T> registry;

    private final String type;

    protected final MinecraftServer server;

    public AbstractRegistrationEventJS(MinecraftServer s, String type, Registry<T> registry) {
        this.server = s;
        this.registry = registry;
        this.registryName = (ResourceKey<Registry<T>>) registry.key();
        this.type = type;
    }

    public void restrict(String resourceName, @NotNull Consumer<B> consumer) {
        RegistrationType<T> registrationType = new RegistrationType<T>(resourceName, registryName);

        registrationType.ifNamespace(namespace -> restrictNamespace(namespace, consumer));
        registrationType.ifName(name -> restrictOne(name, consumer));
        registrationType.ifTag(tag -> restrictTag(tag, consumer));
    }

    @HideFromJS
    abstract protected void restrictOne(ResourceLocation name, @NotNull Consumer<B> consumer);

    @HideFromJS
    protected void logRestrictionCreation(R restriction, ResourceLocation name) {
        var inBiomes = appendListInfo(restriction.includeBiomes, "in biomes");
        var notInBiomes = appendListInfo(restriction.excludeBiomes, "not in biomes");
        var inDimensions = appendListInfo(restriction.includeDimensions, "in dimensions");
        var notInDimensions = appendListInfo(restriction.excludeDimensions, "not in dimensions");
        var details = Stream.of(inBiomes, notInBiomes, inDimensions, notInDimensions)
                .filter(value -> value.length() > 0)
                .collect(Collectors.joining(", "));

        ConsoleJS.SERVER.info("Created " + type + " restriction for " + name + " " + details);
    }

    @HideFromJS
    private String appendListInfo(List<ResourceLocation> list, String description) {
        return list.isEmpty() ? "" : description + " " + list;
    }

    @HideFromJS
    private void restrictNamespace(String namespace, @NotNull Consumer<B> consumer) {
        ConsoleJS.SERVER.info("Creating " + type + " restrictions for namespace " + namespace);

        registry.keySet()
                .stream()
                .filter(name -> name.getNamespace().equals(namespace))
                .forEach(name -> restrictOne(name, consumer));
    }

    @HideFromJS
    abstract public Predicate<T> isTagged(TagKey<T> tag);

    @HideFromJS
    abstract public ResourceLocation getName(T resource);

    @HideFromJS
    private void restrictTag(TagKey<T> tag, @NotNull Consumer<B> consumer) {
        ConsoleJS.SERVER.info("Creating " + type + " restrictions for tag " + tag.location());

        registry.stream()
                .filter(isTagged(tag))
                .forEach(fluid -> restrictOne(getName(fluid), consumer));
    }
}
