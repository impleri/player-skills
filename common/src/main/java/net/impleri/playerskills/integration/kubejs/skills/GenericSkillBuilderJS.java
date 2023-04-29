package net.impleri.playerskills.integration.kubejs.skills;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.TeamMode;
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
    public TeamMode teamMode = TeamMode.off();
    public String notifyKey;
    public boolean notify = false;


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

    public GenericSkillBuilderJS<T> notifyOnChange() {
        return notifyOnChange(null);
    }

    public GenericSkillBuilderJS<T> notifyOnChange(String key) {
        this.notify = true;

        if (key != null && !key.isEmpty()) {
            this.notifyKey = key;
        }

        return this;
    }

    public GenericSkillBuilderJS<T> clearNotification() {
        this.notify = false;
        this.notifyKey = null;

        return this;
    }

    public GenericSkillBuilderJS<T> clearValue() {
        initialValue = null;

        return this;
    }

    public GenericSkillBuilderJS<T> sharedWithTeam() {
        teamMode = TeamMode.shared();

        return this;
    }

    public GenericSkillBuilderJS<T> teamLimitedTo(Double amount) {
        teamMode = TeamMode.limited(amount);

        return this;
    }

    public GenericSkillBuilderJS<T> percentageOfTeam(Double percentage) {
        teamMode = TeamMode.proportional(percentage);

        return this;
    }
}
