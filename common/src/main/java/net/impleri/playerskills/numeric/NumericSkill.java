package net.impleri.playerskills.numeric;

import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NumericSkill extends Skill<Double> {
    public NumericSkill(ResourceLocation name) {
        this(name, null);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value) {
        this(name, value, null);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value, @Nullable String description) {
        this(name, value, description, new ArrayList<>());
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value, @Nullable String description, List<Double> options) {
        super(name, NumericSkillType.name, value, description, options);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value, int changesAllowed) {
        this(name, value, null, changesAllowed);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value, @Nullable String description, int changesAllowed) {
        this(name, value, description, new ArrayList<>(), changesAllowed);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value, @Nullable String description, List<Double> options, int changesAllowed) {
        super(name, NumericSkillType.name, value, description, options, changesAllowed);
    }

    @Override
    public Skill<Double> copy() {
        return new NumericSkill(name, value, description, changesAllowed);
    }
}
