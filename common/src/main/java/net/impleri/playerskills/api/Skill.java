package net.impleri.playerskills.api;

import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.registry.Skills;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Base generic skill. These are meant to be nothing more than
 * containers for data. All logic should be handled by the SkillType.
 */
public class Skill<T> {
    public static final int UNLIMITED_CHANGES = -1;

    /**
     * Get all registered Skills
     */
    public static List<Skill<?>> all() {
        return Skills.entries();
    }

    /**
     * Find a Skill by string
     */
    public static <V> Skill<V> find(String name) throws RegistryItemNotFound {
        return find(SkillResourceLocation.of(name));
    }

    /**
     * Find a Skill by name
     */
    public static <V> Skill<V> find(ResourceLocation location) throws RegistryItemNotFound {
        return Skills.find(location);
    }

    @ApiStatus.Internal
    private static <V> String dumpSkill(Skill<V> skill) {
        return "" + skill.getName().toString() + "=" + Objects.requireNonNullElse(skill.getValue(), "null");
    }

    public static void logSkills(List<Skill<?>> skills, String description) {
        var skillList = skills.stream()
                .map(Skill::dumpSkill)
                .collect(Collectors.joining(", "));
        PlayerSkillsCore.LOGGER.debug("{}: {}", description, skillList);
    }

    protected ResourceLocation name;
    protected ResourceLocation type;
    protected T value;
    protected List<T> options;
    protected int changesAllowed;
    protected String description;

    public Skill(ResourceLocation name, ResourceLocation type) {
        this(name, type, null);
    }

    public Skill(ResourceLocation name, ResourceLocation type, T value) {
        this(name, type, value, null);
    }

    public Skill(ResourceLocation name, ResourceLocation type, T value, String description) {
        this(name, type, value, description, new ArrayList<>());
    }

    public Skill(ResourceLocation name, ResourceLocation type, T value, String description, List<T> options) {
        this(name, type, value, description, options, UNLIMITED_CHANGES);
    }

    public Skill(ResourceLocation name, ResourceLocation type, T value, String description, List<T> options, int changesAllowed) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.description = description;
        this.options = options;
        this.changesAllowed = changesAllowed;
    }

    public Skill<T> copy() {
        return new Skill<T>(name, type, value, description, options, changesAllowed);
    }

    public ResourceLocation getName() {
        return name;
    }

    public ResourceLocation getType() {
        return type;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NotNull
    public List<T> getOptions() {
        return options;
    }

    public int getChangesAllowed() {
        return changesAllowed;
    }

    public boolean areChangesAllowed() {
        return changesAllowed != 0;
    }

    public void setName(ResourceLocation name) {
        this.name = name;
    }

    public void setType(ResourceLocation type) {
        this.type = type;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public void setOptions(@NotNull List<T> options) {
        this.options = options;
    }


    public void consumeChange() {
        if (changesAllowed > 0) {
            changesAllowed--;
        }
    }

    public boolean isAllowedValue(T nextVal) {
        return options.size() == 0 || options.contains(nextVal);
    }

    public boolean isSameAs(Skill<?> that) {
        return that.name == this.name;
    }

    private <V> boolean canEquals(Skill<V> that) {
        return that.getClass().isInstance(this);
    }


    @Override
    public boolean equals(Object that) {
        if (that instanceof Skill<?> skill) {
            return skill.canEquals(this) && skill.name == this.name && skill.type == this.type;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * type.hashCode();
    }
}
