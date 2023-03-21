package net.impleri.playerskills.restrictions;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class RestrictionsApi<T, R extends AbstractRestriction<T>> {
    protected final Predicate<R> emptyFilter = (R restriction) -> true;

    protected final Registry<R> registry;

    private final Field[] allRestrictionFields;

    private final Map<Player, List<R>> restrictionsCache = new HashMap<>();

    private final Map<Triple<UUID, ResourceLocation, ResourceLocation>, Map<T, T>> replacementCache = new HashMap<>();


    public RestrictionsApi(Registry<R> registry, Field[] fields) {
        this.registry = registry;
        this.allRestrictionFields = fields;

        SkillChangedEvent.EVENT.register(this::clearPlayerCache);
    }

    private Triple<UUID, ResourceLocation, ResourceLocation> createCacheKey(UUID player, ResourceLocation dimension, ResourceLocation biome) {
        return Triple.of(player, dimension, biome);
    }

    private Triple<UUID, ResourceLocation, ResourceLocation> createCacheKey(Player player, ResourceLocation dimension, ResourceLocation biome) {
        return createCacheKey(player.getUUID(), dimension, biome);
    }

    public void clearPlayerCache(SkillChangedEvent<?> event) {
        restrictionsCache.remove(event.getPlayer());
        clearReplacementCacheFor(event.getPlayer());
    }

    private void clearReplacementCacheFor(Player player) {
        var uuid = player.getUUID();

        replacementCache.keySet().stream()
                .filter(key -> key.getLeft().equals(uuid))
                .forEach(replacementCache::remove);
    }


    private Field getField(String name) {
        Optional<Field> found = Arrays.stream(allRestrictionFields)
                .filter(field -> field.getName().equals(name))
                .findFirst();

        return found.orElse(null);
    }

    private boolean getFieldValueFor(R restriction, String fieldName) {
        Field field = getField(fieldName);

        if (field != null) {
            try {
                // return the boolean value of the field
                return field.getBoolean(restriction);
            } catch (IllegalAccessException | IllegalArgumentException | NullPointerException |
                     ExceptionInInitializerError ignored) {
            }
        }

        // default to allow if we get some error when trying to access the field
        return true;
    }

    /**
     * Determine the ResourceLocation of the target
     */
    abstract protected ResourceLocation getTargetName(T target);

    /**
     * Create a Predicate using target to match with potential replacements registered to applicable restrictions.
     */
    abstract protected Predicate<T> createPredicateFor(T target);

    private Predicate<R> hasTarget() {
        return restriction -> restriction.target != null;
    }

    private Predicate<R> matchesPlayer(Player player) {
        return restriction -> restriction.condition.test(player);
    }

    private Predicate<R> matchesTarget(T target) {
        var isMatchingTarget = createPredicateFor(target);
        return restriction -> isMatchingTarget.test(restriction.target);
    }

    private Predicate<R> inIncludedDimension(@Nullable ResourceLocation dimension) {
        return restriction -> restriction.includeDimensions.size() == 0 || restriction.includeDimensions.contains(dimension);
    }

    private Predicate<R> notInExcludedDimension(@Nullable ResourceLocation dimension) {
        return restriction -> restriction.excludeDimensions.size() == 0 || !restriction.excludeDimensions.contains(dimension);
    }

    private Predicate<R> inIncludedBiome(@Nullable ResourceLocation biome) {
        return restriction -> restriction.includeBiomes.size() == 0 || restriction.includeBiomes.contains(biome);
    }

    private Predicate<R> notInExcludedBiome(@Nullable ResourceLocation biome) {
        return restriction -> restriction.excludeBiomes.size() == 0 || !restriction.excludeBiomes.contains(biome);
    }

    private List<R> populatePlayerRestrictions(Player player) {
        return registry.entries().stream()
                .filter(hasTarget())
                .filter(matchesPlayer(player))
                .toList();
    }

    private List<R> getRestrictionsFor(Player player) {
        return restrictionsCache.computeIfAbsent(player, this::populatePlayerRestrictions);
    }

    private Stream<R> getRestrictionsFor(Player player, T target, ResourceLocation dimension, ResourceLocation biome, Predicate<R> filter) {
        return getRestrictionsFor(player).stream()
                .filter(matchesTarget(target))
                .filter(inIncludedDimension(dimension))
                .filter(notInExcludedDimension(dimension))
                .filter(inIncludedBiome(biome))
                .filter(notInExcludedBiome(biome))
                .filter(filter);
    }

    private Stream<R> getReplacementsFor(Player player) {
        return getRestrictionsFor(player).stream()
                .filter(restriction -> restriction.replacement != null);
    }


    private Stream<R> getReplacementsFor(Player player, T target, ResourceLocation dimension, ResourceLocation biome, Predicate<R> filter) {
        return getRestrictionsFor(player, target, dimension, biome, filter)
                .filter(restriction -> restriction.replacement != null);
    }

    @NotNull
    private T getActualReplacement(Player player, T target, ResourceLocation dimension, ResourceLocation biome, Predicate<R> filter) {
        T replacement = target;
        boolean hasReplacement = true;

        // Recurse through replacements until we don't have one so that we can allow for cascading replacements
        while (hasReplacement) {
            var nextReplacement = getReplacementsFor(player, replacement, dimension, biome, filter)
                    .map(restriction -> restriction.replacement)
                    .findFirst();

            if (nextReplacement.isEmpty()) {
                hasReplacement = false;
            } else {
                replacement = nextReplacement.get();
            }
        }

        return replacement;
    }

    /**
     * Gets a count for all restrictions with replacements applicable to player.
     */
    public long countReplacementsFor(Player player) {
        return getReplacementsFor(player).count();
    }

    /**
     * Get replacement for target using restrictions applicable to player in the current dimension and biome. Will return target if no replacements found.
     */
    @NotNull
    public T getReplacementFor(@NotNull Player player, @NotNull T target, @NotNull ResourceLocation dimension, @NotNull ResourceLocation biome, @Nullable Predicate<R> filter) {
        var cacheKey = createCacheKey(player, dimension, biome);
        Map<T, T> playerCache = replacementCache.computeIfAbsent(cacheKey, (_player) -> new HashMap<>());

        if (playerCache.containsKey(target)) {
            return playerCache.get(target);
        }

        var actualFilter = filter == null ? emptyFilter : filter;
        var replacement = getActualReplacement(player, target, dimension, biome, actualFilter);

        playerCache.put(target, replacement);
        replacementCache.put(cacheKey, playerCache);

        return replacement;
    }

    @NotNull
    public T getReplacementFor(@NotNull Player player, @NotNull T target, @NotNull ResourceLocation dimension, @NotNull ResourceLocation biome) {
        return getReplacementFor(player, target, dimension, biome, null);
    }

    protected boolean canPlayer(Player player, T target, ResourceLocation dimension, ResourceLocation biome, @Nullable Predicate<R> filter, String fieldName, ResourceLocation resource) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on target {}}", fieldName, resource);
            return false;
        }

        var actualFilter = filter == null ? emptyFilter : filter;
        boolean hasRestrictions = getRestrictionsFor(player, target, dimension, biome, actualFilter)
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", resource, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }

    protected boolean canPlayer(Player player, T target, ResourceLocation dimension, ResourceLocation biome, String fieldName, ResourceLocation resource) {
        return canPlayer(player, target, dimension, biome, null, fieldName, resource);
    }

    @Deprecated
    private Stream<R> getRestrictionsFor(Player player, Predicate<T> isMatchingTarget, Predicate<R> filter) {
        return getRestrictionsFor(player).stream()
                .filter(restriction -> isMatchingTarget.test(restriction.target))
                .filter(filter);
    }


    @Deprecated
    private Stream<R> getReplacementsFor(Player player, Predicate<T> isMatchingTarget, Predicate<R> filter) {
        return getRestrictionsFor(player, isMatchingTarget, filter)
                .filter(restriction -> restriction.replacement != null);
    }

    @Deprecated
    @NotNull
    private T getActualReplacement(Player player, T target, Predicate<R> filter) {
        T replacement = target;
        boolean hasReplacement = true;

        // Recurse through replacements until we don't have one so that we can allow for cascading replacements
        while (hasReplacement) {
            var nextReplacement = getReplacementsFor(player, createPredicateFor(replacement), filter)
                    .map(restriction -> restriction.replacement)
                    .findFirst();

            if (nextReplacement.isEmpty()) {
                hasReplacement = false;
            } else {
                replacement = nextReplacement.get();
            }
        }

        return replacement;
    }


    /**
     * Get replacement for target using restrictions applicable to player. Will return target if no replacements found.
     *
     * @deprecated Use variant with dimension and biome paramaters
     */
    @Deprecated
    @NotNull
    public T getReplacementFor(@NotNull Player player, @NotNull T target, @Nullable Predicate<R> filter) {
        var actualFilter = filter == null ? emptyFilter : filter;
        return getActualReplacement(player, target, actualFilter);
    }

    /**
     * @deprecated Use getReplacementFor
     */
    @Deprecated
    public T getReplacement(Player player, T target) {
        return getReplacementFor(player, target, null);
    }

    /**
     * Internal method exposed for subclasses to implement expressive methods (e.g. BlockSkills.isHarvestable)
     */
    @Deprecated
    protected boolean canPlayer(Player player, Predicate<T> isMatchingTarget, Predicate<R> filter, String fieldName, ResourceLocation resource) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on target {}}", fieldName, resource);
            return false;
        }

        boolean hasRestrictions = getRestrictionsFor(player, isMatchingTarget, filter == null ? emptyFilter : filter)
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", resource, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }

    @Deprecated
    protected boolean canPlayer(Player player, Predicate<T> isMatchingTarget, String fieldName, ResourceLocation resource) {
        return canPlayer(player, isMatchingTarget, emptyFilter, fieldName, resource);
    }

    @Deprecated
    protected boolean canPlayer(Player player, T target, @Nullable Predicate<R> filter, String fieldName) {
        var targetName = getTargetName(target);
        Predicate<T> predicate = createPredicateFor(target);
        var actualFilter = filter == null ? emptyFilter : filter;
        T actualTarget = getReplacementFor(player, target, actualFilter);

        PlayerSkills.LOGGER.debug("Checking if {} ({}) is {}.", targetName, getTargetName(actualTarget), fieldName);

        return canPlayer(player, predicate, actualFilter, fieldName, targetName);
    }

    /**
     * @deprecated Use the more expressive methods available on subclasses (e.g. ItemSkills.isConsumable)
     */
    @Deprecated
    public boolean canPlayer(Player player, T target, String fieldName) {
        return canPlayer(player, target, null, fieldName);
    }

    /**
     * @deprecated Use the more expressive methods available on subclasses (e.g. ItemSkills.isConsumable)
     */
    @Deprecated
    public boolean canPlayer(Player player, ResourceLocation resourceName, String fieldName) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on {}", fieldName, resourceName);
            return false;
        }

        boolean hasRestrictions = registry.find(resourceName).stream()
                .filter(restriction -> restriction.condition.test(player)) // reduce to those whose condition matches the player
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", resourceName, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }
}
