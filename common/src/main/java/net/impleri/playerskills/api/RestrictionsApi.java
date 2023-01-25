package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public abstract class RestrictionsApi<T extends AbstractRestriction<?>> {
    private final Registry<T> registry;

    private final Field[] allRestrictionFields;

    public RestrictionsApi(Registry<T> registry, Field[] fields) {
        this.registry = registry;
        this.allRestrictionFields = fields;
    }

    private Field getField(String name) {
        Optional<Field> found = Arrays.stream(allRestrictionFields)
                .filter(field -> field.getName().equals(name))
                .findFirst();

        return found.orElse(null);
    }

    private boolean getFieldValueFor(T restriction, String fieldName) {
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

    public boolean canPlayer(Player player, ResourceLocation item, String fieldName) {
        if (player == null) {
            PlayerSkills.LOGGER.warn("Attempted to determine if null player can {} on {}", fieldName, item);
            return false;
        }

        boolean hasRestrictions = registry.find(item).stream()
                .filter(restriction -> restriction.condition.test(player)) // reduce to those whose condition matches the player
                .map(restriction -> getFieldValueFor(restriction, fieldName)) // get field value
                .anyMatch(value -> !value); // do we have any restrictions that deny the action

        PlayerSkills.LOGGER.debug("Does {} for {} have {} restrictions? {}", item, player.getName().getString(), fieldName, hasRestrictions);

        return !hasRestrictions;
    }
}
