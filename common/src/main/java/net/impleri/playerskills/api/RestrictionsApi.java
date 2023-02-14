package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    protected List<R> getRestrictionsFor(Player player, Predicate<T> isMatchingTarget) {
        return registry.entries().stream()
                .filter(restriction -> restriction.condition.test(player) && isMatchingTarget.test(restriction.target)).toList();
    }

    protected List<R> getReplacementsFor(Player player, Predicate<T> isMatchingTarget) {
        return registry.entries().stream()
                .filter(restriction -> restriction.condition.test(player) && isMatchingTarget.test(restriction.target) && restriction.replacement != null).toList();
    }

    protected List<R> countReplacementsFor(Player player) {
        return registry.entries().stream()
                .filter(restriction -> restriction.condition.test(player) && restriction.target != null && restriction.replacement != null).toList();
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

    public boolean canPlayer(Player player, ResourceLocation resource, String fieldName) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on {}", fieldName, resource);
            return false;
        }

        boolean hasRestrictions = registry.find(resource).stream()
                .filter(restriction -> restriction.condition.test(player)) // reduce to those whose condition matches the player
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", resource, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }
}
