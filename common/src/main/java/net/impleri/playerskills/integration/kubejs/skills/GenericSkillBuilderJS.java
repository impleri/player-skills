package net.impleri.playerskills.integration.kubejs.skills;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.Registries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GenericSkillBuilderJS<T> extends BuilderBase<Skill<T>> {
    public @Nullable T initialValue;
    public @Nullable String description;
    public List<T> options = new ArrayList<>();
    public int changesAllowed = Skill.UNLIMITED_CHANGES;


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

    public GenericSkillBuilderJS<T> options(T[] options) {
        this.options = Arrays.stream(options).toList();

        return this;
    }

    public GenericSkillBuilderJS<T> limitChanges(Double changesAllowed) {
        this.changesAllowed = changesAllowed.intValue();

        return this;
    }

    public GenericSkillBuilderJS<T> unlimitedChanges(Double changesAllowed) {
        this.changesAllowed = Skill.UNLIMITED_CHANGES;

        return this;
    }

    public GenericSkillBuilderJS<T> clearValue() {
        initialValue = null;

        return this;
    }
}
