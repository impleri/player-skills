package net.impleri.playerskills.numeric;

import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class NumericSkill extends Skill<Double> {
    public NumericSkill(ResourceLocation name) {
        this(name, null);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value) {
        this(name, value, null);
    }

    public NumericSkill(ResourceLocation name, @Nullable Double value, @Nullable String description) {
        super(name, NumericSkillType.name, value, description);
    }

    @Override
    public Skill<Double> copy() {
        return new NumericSkill(name, value, description);
    }
}
