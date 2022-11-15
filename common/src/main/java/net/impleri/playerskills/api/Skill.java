package net.impleri.playerskills.api;

import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.registry.Skills;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base generic skill. These are meant to be nothing more than
 * containers for data. All logic should be handled by the SkillType.
 */
public class Skill<T> {
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

    protected ResourceLocation name;
    protected ResourceLocation type;
    protected T value;
    protected String description;

    public Skill(ResourceLocation name, ResourceLocation type) {
        this(name, type, null);
    }

    public Skill(ResourceLocation name, ResourceLocation type, T value) {
        this(name, type, value, null);
    }

    public Skill(ResourceLocation name, ResourceLocation type, T value, String description) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public Skill<T> copy() {
        return new Skill<T>(name, type, value, description);
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

    public void setName(ResourceLocation name) {
        this.name = name;
    }

    public void setType(ResourceLocation type) {
        this.type = type;
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    public void getDescription(@Nullable String description) {
        this.description = description;
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
