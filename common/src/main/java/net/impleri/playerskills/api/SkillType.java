package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper to `Skill`s providing logic for
 * 1. serialization to/from NBT
 * 2. Executing logic to determine if a skill value should be changed.
 */
abstract public class SkillType<T> {
    private static final String valueSeparator = ";";
    private static final String stringValueNone = "[NULL]";

    /**
     * Get all Types
     */
    public static List<SkillType<?>> all() {
        return SkillTypes.entries();
    }

    /**
     * Find a SkillType by string
     */
    public static <V> SkillType<V> find(String name) throws RegistryItemNotFound {
        return find(SkillResourceLocation.of(name));
    }

    /**
     * Find a SkillType by name
     */
    public static <V> SkillType<V> find(ResourceLocation location) throws RegistryItemNotFound {
        return SkillTypes.find(location);
    }

    /**
     * Find a SkillType for a specific Skill
     */
    public static <V> SkillType<V> forSkill(Skill<V> skill) throws RegistryItemNotFound {
        return find(skill.getType());
    }

    public static <V> SkillType<V> maybeForSkill(Skill<V> skill) {
        try {
            return find(skill.getType());
        } catch (RegistryItemNotFound e) {
            return null;
        }
    }

    public static <V> String serializeToString(Skill<V> skill) {
        PlayerSkills.LOGGER.debug("Serializing skill {} with type {}", skill.getName(), skill.getType());
        try {
            SkillType<V> type = find(skill.getType());
            String storage = type.serialize(skill);

            PlayerSkills.LOGGER.debug("Dehydrating skill {} for storage: {}", skill.getName(), storage);

            return storage;
        } catch (RegistryItemNotFound e) {
            PlayerSkills.LOGGER.warn("Attempted to serialize skill {} not found in registry", skill.getName());
        }

        return "";
    }

    @ApiStatus.Internal
    public static @Nullable <V> Skill<V> unserializeFromString(String rawSkill) {
        PlayerSkills.LOGGER.debug("Unpacking skill {} from storage", rawSkill);

        if (rawSkill == null || rawSkill.equals("")) {
            return null;
        }

        String[] elements = SkillType.splitRawSkill(rawSkill);
        String name = elements[0];
        String type = elements[1];
        String value = elements[2];
        int changesAllowed;

        try {
            changesAllowed = Integer.parseInt(elements[3]);
        } catch (NumberFormatException e) {
            PlayerSkills.LOGGER.error("Unable to parse changesAllowed ({}) back into an integer, data possibly corrupted", elements[3]);
            return null;
        }

        List<String> options = Arrays.stream(elements).skip(4).toList();

        try {
            SkillType<V> skillType = SkillTypes.find(SkillResourceLocation.of(type));
            PlayerSkills.LOGGER.debug("Hydrating {} skill named {}: {}", type, name, value);
            return skillType.unserialize(name, value, changesAllowed);
        } catch (RegistryItemNotFound e) {
            PlayerSkills.LOGGER.warn("No skill type {} in the registry to hydrate {}", type, name);
        }

        return null;
    }

    public static String[] splitRawSkill(String value) {
        String[] parts = value.split(valueSeparator);
        String skillName = parts[0];
        String skillType = parts[1];
        String skillValue = parts[2].equals(stringValueNone) ? null : parts[2];
        String skillChangesAllowed = parts[3];

        return new String[]{skillName, skillType, skillValue, skillChangesAllowed};
    }

    public ResourceLocation getName() {
        return SkillResourceLocation.of("skill");
    }

    @Nullable
    protected String getDescriptionFor(ResourceLocation name) {
        try {
            return net.impleri.playerskills.server.api.Skill.find(name).description;
        } catch (RegistryItemNotFound e) {
            PlayerSkills.LOGGER.debug("Could not get description for skill {}", name);
        }

        return null;
    }

    /**
     * Convert into string for NBT storage
     */
    public String serialize(Skill<T> skill) {
        String value = castToString(skill.getValue());
        String[] parts = {
                skill.getName().toString(),
                skill.getType().toString(),
                (value == null || value.equals("")) ? stringValueNone : value,
                String.valueOf(skill.getChangesAllowed()),
        };

        return String.join(valueSeparator, parts);
    }

    protected abstract String castToString(T value);

    /**
     * Convert from string in NBT storage
     */
    public Skill<T> unserialize(String skillName, String value, int changesAllowed) throws RegistryItemNotFound {
        ResourceLocation name = SkillResourceLocation.of(skillName);
        Skill<T> baseSkill = net.impleri.playerskills.server.api.Skill.find(name);
        @Nullable T castValue = castFromString(value);

        return baseSkill.copy(castValue, changesAllowed);
    }

    @Nullable
    public abstract T castFromString(String value);

    /**
     * Logic to determine if skill is activated for the expected value
     */
    public boolean hasValue(Skill<T> skill) {
        return hasValue(skill, null);
    }

    /**
     * Logic to determine if skill is activated for the expected value
     */
    public boolean hasValue(Skill<T> skill, @Nullable T expectedValue) {
        if (expectedValue == null) {
            return skill.getValue() != null;
        }

        return skill.getValue() == expectedValue;
    }

    /**
     * Logic to determine if a player has a skill at an expected level
     */
    public boolean can(Skill<T> skill, @Nullable T expectedValue) {
        return hasValue(skill, expectedValue);
    }

    @Nullable
    abstract public T getPrevValue(Skill<T> skill, @Nullable T min, @Nullable T max);

    @Nullable
    public T getPrevValue(Skill<T> skill) {
        return this.getPrevValue(skill, null, null);
    }

    @Nullable
    abstract public T getNextValue(Skill<T> skill, @Nullable T min, @Nullable T max);

    @Nullable
    public T getNextValue(Skill<T> skill) {
        return this.getNextValue(skill, null, null);
    }
}
