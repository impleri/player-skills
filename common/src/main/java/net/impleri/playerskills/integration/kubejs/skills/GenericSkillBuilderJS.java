package net.impleri.playerskills.integration.kubejs.skills;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.Registries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class GenericSkillBuilderJS<T> extends BuilderBase<Skill<T>> {
    public @Nullable T initialValue;
    public @Nullable String description;

    public GenericSkillBuilderJS(ResourceLocation name) {
        super(name);
    }

    @Override
    public RegistryObjectBuilderTypes<Skill<?>> getRegistryType() {
        return Registries.SKILLS;
    }

    @HideFromJS
    public void syncWith(Skill<T> skill) {
        initialValue = skill.getValue();
        description = skill.getDescription();
    }

    public GenericSkillBuilderJS<T> initialValue(T value) {
        initialValue = value;

        return this;
    }

    public GenericSkillBuilderJS<T> description(String value) {
        description = value;

        return this;
    }

    public GenericSkillBuilderJS<T> clearValue() {
        initialValue = null;

        return this;
    }
}
