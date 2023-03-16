package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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

    private final Map<Player, Map<T, T>> replacementCache = new HashMap<>();


    public RestrictionsApi(Registry<R> registry, Field[] fields) {
        this.registry = registry;
        this.allRestrictionFields = fields;

        SkillChangedEvent.EVENT.register(this::clearPlayerCache);
    }

    public void clearPlayerCache(SkillChangedEvent<?> event) {
        restrictionsCache.put(event.getPlayer(), new ArrayList<>());
        replacementCache.put(event.getPlayer(), new HashMap<>());
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

    private List<R> populatePlayerRestrictions(Player player) {
        return registry.entries().stream()
                .filter(restriction -> restriction.condition.test(player) && restriction.target != null)
                .toList();
    }

    private List<R> getRestrictionsFor(Player player) {
        return restrictionsCache.computeIfAbsent(player, this::populatePlayerRestrictions);
    }

    private Stream<R> getRestrictionsFor(Player player, Predicate<T> isMatchingTarget, Predicate<R> filter) {
        return getRestrictionsFor(player).stream()
                .filter(restriction -> isMatchingTarget.test(restriction.target))
                .filter(filter);
    }

    private Stream<R> getReplacementsFor(Player player) {
        return getRestrictionsFor(player).stream()
                .filter(restriction -> restriction.replacement != null);
    }

    private Stream<R> getReplacementsFor(Player player, Predicate<T> isMatchingTarget, Predicate<R> filter) {
        return getRestrictionsFor(player, isMatchingTarget, filter)
                .filter(restriction -> restriction.replacement != null);
    }

    /**
     * Determine the ResourceLocation of the target
     */
    abstract protected ResourceLocation getTargetName(T target);

    /**
     * Create a Predicate using target to match with potential replacements registered to applicable restrictions.
     */
    abstract protected Predicate<T> createPredicateFor(T target);


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
     * Gets a count for all restrictions with replacements applicable to player.
     */
    public long countReplacementsFor(Player player) {
        return getReplacementsFor(player).count();
    }

    /**
     * Get replacement for target using restrictions applicable to player. Will return target if no replacements found.
     */
    @NotNull
    public T getReplacementFor(@NotNull Player player, @NotNull T target, @Nullable Predicate<R> filter) {
        Map<T, T> playerCache = replacementCache.computeIfAbsent(player, (_player) -> new HashMap<>());

        if (playerCache.containsKey(target)) {
            return playerCache.get(target);
        }

        var actualFilter = filter == null ? emptyFilter : filter;
        var replacement = getActualReplacement(player, target, actualFilter);

        playerCache.put(target, replacement);
        replacementCache.put(player, playerCache);

        return replacement;
    }

    @NotNull
    public T getReplacementFor(@NotNull Player player, @NotNull T target) {
        return getReplacementFor(player, target, null);
    }

    /**
     * @deprecated Use getReplacementFor
     */
    @Deprecated
    public T getReplacement(Player player, T target) {
        return getReplacementFor(player, target);
    }


    /**
     * Internal method exposed for subclasses to implement expressive methods (e.g. BlockSkills.isHarvestable)
     */
    protected boolean canPlayer(Player player, Predicate<T> isMatchingTarget, Predicate<R> filter, String fieldName, ResourceLocation resource) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on target {}}", fieldName, resource);
            return false;
        }

        boolean hasRestrictions = getRestrictionsFor(player, isMatchingTarget, filter)
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", resource, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }

    protected boolean canPlayer(Player player, Predicate<T> isMatchingTarget, String fieldName, ResourceLocation resource) {
        return canPlayer(player, isMatchingTarget, emptyFilter, fieldName, resource);
    }

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
