package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

public abstract class RestrictionsApi<T, R extends AbstractRestriction<T>> {
    protected final Registry<R> registry;

    private final Field[] allRestrictionFields;

    public RestrictionsApi(Registry<R> registry, Field[] fields) {
        this.registry = registry;
        this.allRestrictionFields = fields;
    }

    private Field getField(String name) {
        Optional<Field> found = Arrays.stream(allRestrictionFields)
                .filter(field -> field.getName().equals(name))
                .findFirst();

        return found.orElse(null);
    }

    protected boolean getFieldValueFor(R restriction, String fieldName) {
        Field field = getField(fieldName);

        // default to allow if field doesn't exist (this should never happen)
        if (field == null) {
            return true;
        }

        try {
            // return the boolean value of the field
            return field.getBoolean(restriction);
        } catch (IllegalAccessException | IllegalArgumentException | NullPointerException |
                 ExceptionInInitializerError ignored) {
        }

        // default to allow if we get some error when trying to access the field
        return true;
    }

    abstract protected ResourceLocation getTargetName(T target);

    abstract protected Predicate<T> createPredicateFor(T target);

    protected List<R> getRestrictionsFor(Player player) {
        return registry.entries().stream()
                .filter(restriction -> restriction.condition.test(player) && restriction.target != null)
                .toList();
    }

    protected List<R> getReplacementsFor(Player player) {
        return getRestrictionsFor(player).stream()
                .filter(restriction -> restriction.replacement != null)
                .toList();
    }

    protected List<R> getRestrictionsFor(Player player, Predicate<T> isMatchingTarget) {
        return getRestrictionsFor(player).stream()
                .filter(restriction -> isMatchingTarget.test(restriction.target))
                .toList();
    }

    protected List<R> getReplacementsFor(Player player, Predicate<T> isMatchingTarget) {
        return getRestrictionsFor(player, isMatchingTarget).stream()
                .filter(restriction -> restriction.replacement != null)
                .toList();
    }

    public long countReplacementsFor(Player player) {
        return getReplacementsFor(player).size();
    }

    @NotNull
    protected T getReplacementInternal(Player player, T target) {
        T replacement = target;
        boolean hasReplacement = true;

        // Recurse through replacements until we don't have one so that we can allow for cascading replacements
        while (hasReplacement) {
            var nextReplacement = getReplacementsFor(player, createPredicateFor(replacement))
                    .stream()
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

    private final Map<Player, Map<T, T>> replacementCache = new HashMap<>();

    @NotNull
    public T getReplacement(Player player, T target) {
        Map<T, T> playerCache = replacementCache.getOrDefault(player, new HashMap<>());

        if (playerCache.containsKey(target)) {
            return playerCache.get(target);
        }

        var replacement = getReplacementInternal(player, target);

        playerCache.put(target, replacement);
        replacementCache.put(player, playerCache);

        return replacement;
    }

    public void clearPlayerCache(Player player) {
        replacementCache.put(player, new HashMap<>());
    }

    protected boolean canPlayer(Player player, Predicate<T> isMatchingTarget, String fieldName, ResourceLocation resource) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on target {}}", fieldName, resource);
            return false;
        }

        boolean hasRestrictions = getRestrictionsFor(player, isMatchingTarget).stream()
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", resource, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }

    public boolean canPlayer(Player player, T target, String fieldName) {
        var targetName = getTargetName(target);
        Predicate<T> predicate = createPredicateFor(target);
        T actualTarget = getReplacement(player, target);

        PlayerSkills.LOGGER.debug("Checking if {} ({}) is {}.", targetName, getTargetName(actualTarget), fieldName);

        return canPlayer(player, predicate, fieldName, targetName);
    }

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
