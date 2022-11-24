package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.registry.SkillTypes;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to `Skill`s providing logic for
 * 1. serialization to/from NBT
 * 2. Executing logic to determine if a skill value should be changed.
 */
abstract public class SkillType<T> {
    private static final String valueSeparator = ";";
    private static final String optionsSeparator = "!";
    private static final String optionsValueEmpty = "[EMPTY]";
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

    public static String[] splitRawSkill(String value) {
        String[] parts = value.split(valueSeparator);
        String skillName = parts[0];
        String skillType = parts[1];
        String skillValue = parts[2].equals(stringValueNone) ? null : parts[2];
        String skillChangesAllowed = parts[3];
        String[] skillOptions = (parts[4].equals(optionsValueEmpty)) ? new String[]{} : parts[4].split(optionsSeparator);

        String[] mainData = {skillName, skillType, skillValue, skillChangesAllowed};

        return ArrayUtils.addAll(mainData, skillOptions);
    }

    public ResourceLocation getName() {
        return SkillResourceLocation.of("skill");
    }

    @Nullable
    protected String getDescriptionFor(ResourceLocation name) {
        try {
            return Skill.find(name).description;
        } catch (RegistryItemNotFound e) {
            PlayerSkillsCore.LOGGER.warn("Could not get description for sill {}", name);
        }

        return null;
    }

    /**
     * Convert into string for NBT storage
     */
    public String serialize(Skill<T> skill) {
        return serialize(skill, "", new ArrayList<>());
    }

    /**
     * Helper serializer to ensure expected format when deserializing
     */
    public String serialize(Skill<T> skill, String value, List<String> options) {
        String serialOptions = String.join(optionsSeparator, options);
        String[] parts = {
                skill.getName().toString(),
                skill.getType().toString(),
                (value == null || value.equals("")) ? stringValueNone : value,
                String.valueOf(skill.getChangesAllowed()),
                (serialOptions.equals("")) ? optionsValueEmpty : serialOptions,
        };

        return String.join(valueSeparator, parts);
    }

    /**
     * Convert from string in NBT storage
     */
    public Skill<T> unserialize(String name, String value, int changesAllowed, List<String> options) {
        return new Skill<T>(SkillResourceLocation.of(name), this.getName());
    }

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
