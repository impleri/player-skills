package net.impleri.playerskills.restrictions;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.impleri.playerskills.utils.RegistrationType;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * In order to use
 *
 * @param <T>
 */
public abstract class AbstractRestrictionBuilder<T extends AbstractRestriction<?>> extends BuilderBase<T> {
    @HideFromJS
    private final MinecraftServer server;
    @HideFromJS
    public Predicate<Player> condition = (Player player) -> true;

    @HideFromJS
    public final List<ResourceLocation> includeDimensions = new ArrayList<>();

    @HideFromJS
    public final List<ResourceLocation> excludeDimensions = new ArrayList<>();

    @HideFromJS
    public final List<ResourceLocation> includeBiomes = new ArrayList<>();

    @HideFromJS
    public final List<ResourceLocation> excludeBiomes = new ArrayList<>();

    @HideFromJS
    public AbstractRestrictionBuilder(ResourceLocation id, @Nullable MinecraftServer server) {
        super(id);
        this.server = server;
    }

    @HideFromJS
    public AbstractRestrictionBuilder(ResourceLocation id) {
        this(id, null);
    }

    @RemapForJS("if")
    public AbstractRestrictionBuilder<T> condition(Predicate<PlayerDataJS> consumer) {
        this.condition = (Player player) -> player != null && consumer.test(new PlayerDataJS(player));

        return this;
    }

    public AbstractRestrictionBuilder<T> unless(Predicate<PlayerDataJS> consumer) {
        this.condition = (Player player) -> player == null || !consumer.test(new PlayerDataJS(player));

        return this;
    }

    @HideFromJS
    private void ifInDimensionsNamespaced(String namespace, Consumer<ResourceLocation> callback) {
        if (server == null) {
            ConsoleJS.SERVER.error("Attempted to add a dimension-based restriction using a mod that hasn't updated to PlayerSills 1.7.0");
            return;
        }

        server.levelKeys().stream()
                .map(ResourceKey::location)
                .filter(location -> location.getNamespace().equals(namespace))
                .forEach(callback);
    }

    @HideFromJS
    private void ifDimension(String dimensionName, Consumer<ResourceLocation> callback) {
        RegistrationType<Level> registrationType = new RegistrationType<>(dimensionName, net.minecraft.core.Registry.DIMENSION_REGISTRY);
        registrationType.ifNamespace(namespace -> ifInDimensionsNamespaced(namespace, callback));
        // Note: Dimensions do not have tags, so we cannot use the #tag selector on dimensions
        registrationType.ifName(callback);
    }

    public AbstractRestrictionBuilder<T> inDimension(String dimension) {
        ifDimension(dimension, this.includeDimensions::add);

        return this;
    }

    public AbstractRestrictionBuilder<T> notInDimension(String dimension) {
        ifDimension(dimension, this.excludeDimensions::add);

        return this;
    }

    @HideFromJS
    private void ifInBiomesNamespaced(String namespace, Consumer<ResourceLocation> callback) {
        BuiltinRegistries.BIOME.entrySet().stream()
                .map(Map.Entry::getKey)
                .map(ResourceKey::location)
                .filter(location -> location.getNamespace().equals(namespace))
                .forEach(callback);
    }

    @HideFromJS
    private void ifInBiomesTagged(TagKey<Biome> tag, Consumer<ResourceLocation> callback) {
        BuiltinRegistries.BIOME.getTags()
                .filter(pair -> pair.getFirst().equals(tag))
                .map(Pair::getSecond)
                .flatMap(HolderSet.ListBacked::stream)
                .map(biomeHolder -> biomeHolder.unwrapKey().orElseGet(() -> null))
                .filter(Objects::nonNull)
                .map(ResourceKey::location)
                .forEach(callback);
    }

    @HideFromJS
    private void ifBiome(String dimensionName, Consumer<ResourceLocation> callback) {
        RegistrationType<Biome> registrationType = new RegistrationType<>(dimensionName, Registry.BIOME_REGISTRY);

        registrationType.ifNamespace(namespace -> ifInBiomesNamespaced(namespace, callback));
        registrationType.ifTag(tag -> ifInBiomesTagged(tag, callback));
        registrationType.ifName(callback);
    }

    public AbstractRestrictionBuilder<T> inBiome(String biome) {
        ifBiome(biome, this.includeDimensions::add);

        return this;
    }

    public AbstractRestrictionBuilder<T> notInBiome(String biome) {
        ifBiome(biome, this.excludeDimensions::add);

        return this;
    }
}
